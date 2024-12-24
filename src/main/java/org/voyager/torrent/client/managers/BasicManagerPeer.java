package org.voyager.torrent.client.managers;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.messages.MsgBitfield;
import org.voyager.torrent.client.network.ChannelNetwork;
import org.voyager.torrent.client.peers.Peer;
import org.voyager.torrent.client.peers.PeerNonBlock;
import org.voyager.torrent.client.exceptions.HandShakeInvalidException;
import org.voyager.torrent.client.exceptions.NoReaderBufferException;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.messages.MsgPiece;
import org.voyager.torrent.client.messages.MsgRequest;

public class BasicManagerPeer implements ManagerPeer{

    private Torrent torrent;
    private ClientTorrent client;
    private ManagerFile managerFile;
    private Semaphore semaphoreExecutor;
    private ManagerAnnounce managerAnnounce;

    // IO non-blocker
    private Selector selector;
    private Queue<PeerNonBlock> queueNewsPeer;

    // Recieve Msg
    private Queue<MsgPiece> queueRecieveMsgPiece;
    private Queue<MsgRequest> queueRecieveMsgRequest;

    private Map<SocketChannel, PeerNonBlock> mapChannelAndPeer;

    public BasicManagerPeer(ClientTorrent client){
        this.client                 = client;
        this.torrent                = client.getTorrent();
        this.managerAnnounce        = client.getManagerAnnounce();
        this.managerFile            = client.getManagerFile();
        this.mapChannelAndPeer      = new ConcurrentHashMap<>();
        this.queueNewsPeer          = new ConcurrentLinkedQueue<>();
        this.queueRecieveMsgPiece   = new ConcurrentLinkedQueue<>();
        this.queueRecieveMsgRequest = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        
        initSelector();

        while(!isInterrupted()) {
            try {

                semaphoreExecutor.acquire();
                System.out.println("++++++++ ManagerPeer +++++++");

                process();


            } catch (InterruptedException e) {  Thread.currentThread().interrupt();  } 
              finally {
                semaphoreExecutor.release();
            }

            System.out.println("-------- ManagerPeer -------");
            // sleep calc pela menor latencia dentro dos pares
            sleep(128);
        }

    }

    private void process(){
        processQueueNewsPeer();

        select();
        
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        Iterator<SelectionKey> iterator = selectedKeys.iterator();

        while (iterator.hasNext()) {
            
            SelectionKey key = iterator.next();
            iterator.remove();
            
            try {
                if (key.isConnectable()) {

                    handlerConnect(key);

                } else if (key.isReadable()) {

                    handlerRead(key);

                } else if (key.isWritable()) {

                    handlerWrite(key);

                }

            } catch (Exception e) {
                System.err.println("Erro ao processar chave: " + e.getMessage());
                e.printStackTrace();
                closeChannel((SocketChannel) key.channel());
                key.cancel();
            }
        }

        processReceiveMsgPiece();
        processSendMsgRequest();
        processReceiveMsgRequest();
        // @todo process send keep-alive
    }

    // Hook Selector handler for handling completed connections
    public void handlerConnect(SelectionKey key) {

        SocketChannel channel = (SocketChannel) key.channel();
        PeerNonBlock peer = mapChannelAndPeer.get(channel);

        System.out.println("handlerConnect: "+ peer);

        try {
    
            // Finalizar a conexão
            if (channel.finishConnect()) {

                peer.setConneted(true);
                peer.setHandshake(false);

                System.out.println("\t Conexão estabelecida para o peer: " + peer);

                // Registrar interesse em escrita para enviar handshake
                key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);

            }

        } catch (IOException e) {
            System.err.println("Erro na conexão ou handshake: \t"+ e.getMessage());
            closeChannel(channel);
            mapChannelAndPeer.remove(channel);
            key.cancel();

        }
    }

    // Hook Selector handler for reading from channels
    public void handlerRead(SelectionKey key) throws IOException{
        SocketChannel channel = (SocketChannel) key.channel();
        PeerNonBlock peer = mapChannelAndPeer.get(channel);

        boolean isReadable = channel.isConnected() && channel.isOpen();
        boolean notIsReadableThen = !isReadable;
        if(notIsReadableThen)return;

        System.out.println("handlerRead: "+ peer);

        try {

            if(peer.isConnected()){
                peer.readMsg();
            }

            key.interestOps(SelectionKey.OP_WRITE);

        } catch (NoReaderBufferException e) {
            System.err.println("Erro durante leitura do peer: " + peer);
            e.printStackTrace();
        } catch (ConnectException e){
          // retry connection
            closeChannel(channel);
            mapChannelAndPeer.remove(channel);
            key.cancel();
        } catch(HandShakeInvalidException | IOException e) {
            System.err.println("Erro durante leitura do peer: " + peer);

            e.printStackTrace();

            closeChannel(channel);
            mapChannelAndPeer.remove(channel);
            key.cancel();
        }
       
    }

    // Hook Selector handler for writing to channels
    public void handlerWrite(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        PeerNonBlock peer = mapChannelAndPeer.get(channel);

        boolean isWritable = channel.isConnected() && channel.isOpen();
        boolean notIsWritableThen = !isWritable;
        if(notIsWritableThen)return;

        System.out.println("handlerWrite: "+ peer);

        try {
            
            if(peer.isConnected() && !peer.hasHandshake()){
                peer.writeShake();
            }
            if(peer.hasHandshake() && !peer.hasChoked()){
                peer.processQueueNewMsg();
            }

            key.interestOps(SelectionKey.OP_READ);

        } catch (IOException e) {
            System.err.println("Erro durante leitura do peer: " + peer);

            e.printStackTrace();

            closeChannel(channel);
            mapChannelAndPeer.remove(channel);
            key.cancel();
        }
    }

    // Process send Request Pieces in peer
    private void processSendMsgRequest(){
        List<MsgRequest> listMsgRequest = managerFile.calcMsgRequest();

        //  peers for sort metrics
        List<PeerNonBlock> listPeer = mapChannelAndPeer.values().stream()
                                                                .sorted(Collections.reverseOrder())
                                                                .collect(Collectors.toList());

        Map<PeerNonBlock, List<MsgRequest>> mapPeerAndMsgRequest = new HashMap<>();

        for (PeerNonBlock peerNonBlock : listPeer) {
            if(!peerNonBlock.isConnected) continue;
            if(!peerNonBlock.hasHandshake) continue;

            // @todo colocar choked verify
            if(peerNonBlock.getPiecesMap() == null) continue;
            if(peerNonBlock.hasChoked()) continue;

            mapPeerAndMsgRequest.put(peerNonBlock, new ArrayList<MsgRequest>());
            List<MsgRequest> listMsgRequestForPeer = mapPeerAndMsgRequest.get(peerNonBlock);

            for(int i = 0; i < listMsgRequest.size(); i++){
                MsgRequest request = listMsgRequest.get(i);
                byte[] map = peerNonBlock.getPiecesMap().getMap();

                if(map.length >= request.getPosition() && map[request.getPosition()] != 0){
                    listMsgRequestForPeer.add(request);
                }

            }
            listMsgRequest.removeAll(listMsgRequestForPeer);
        }

        // add queue peer
        for (Entry<PeerNonBlock, List<MsgRequest>> entry : mapPeerAndMsgRequest.entrySet()) {
            PeerNonBlock peerNonBlock = entry.getKey();
            List<MsgRequest> listMsgRequestForPeer = entry.getValue();

            for(MsgRequest msg : listMsgRequestForPeer){
               peerNonBlock.queueNewMsgIfNotExist(msg);
            }
        }
    }

    // Process Pieces Recieve from peers
    private void processReceiveMsgPiece(){
        while(!queueRecieveMsgPiece.isEmpty()){
            MsgPiece msg = queueRecieveMsgPiece.poll();
            managerFile.queueMsg(msg);
        }
    }

    private void processReceiveMsgRequest(){
        // queueRecieveMsgRequest
        // verify piece exist in managerFile
        // see exist add queue for return picei
    }


    // Process Queue
    private void processQueueNewsPeer(){
        while(!queueNewsPeer.isEmpty()){

            PeerNonBlock peer = queueNewsPeer.poll();

            try{

                SocketChannel channel = SocketChannel.open();
                channel.configureBlocking(false);
                channel.connect(new InetSocketAddress(peer.getHost(), peer.getPort()));
                
                // 30s for connect and keep-alive
                channel.socket().setSoTimeout(30000);
                channel.register(selector, SelectionKey.OP_CONNECT);

                peer.withNetwork( new ChannelNetwork(channel) )
                    .withManagerPeer(this);

                mapChannelAndPeer.put(channel, peer);
    
                System.out.println("Peer registry: " + peer);
    
            } catch (IOException e) {
                System.err.println("Erro ao registrar peer: " + peer);
                e.printStackTrace();
            }
        }
    }

    // Hooks Queue's
    //  Queue New Peers from ManagerAnnounce
    public synchronized void queueNewsPeer(PeerNonBlock peer) { queueNewsPeer.add(peer); }
    public synchronized void queueNewsPeerIfNotPresent(PeerNonBlock peer) { 
        boolean IfPresent = mapChannelAndPeer.containsValue(peer);
        if(!IfPresent)queueNewsPeer(peer);
    }

    //  Queue New Msg from Peers
    @Override
    public void queueNewMsg(PeerNonBlock peer, MsgRequest msg) { queueRecieveMsgRequest.add(msg); }
    @Override
    public void queueNewMsg(PeerNonBlock peer, MsgPiece msg) { queueRecieveMsgPiece.add(msg); }

    // Util selector
    // @todo Mitigar
    private void select(){ try{ selector.select(); }catch(Exception e){ e.printStackTrace(); } }

    // @todo Mitigar
    private void initSelector(){ 
        if(selector != null && selector.isOpen())return;
        try{ selector = Selector.open(); }catch(Exception e){e.printStackTrace();}
    }

    private void closeChannel(SocketChannel channel) {
        try { channel.close(); }
        catch (IOException e) {
            System.err.println("Erro ao fechar canal: " + e.getMessage());
        }
    }

    private void sleep(long ms){ try{Thread.sleep(ms);}catch (Exception e) {}  }
    private boolean isInterrupted(){ return Thread.currentThread().isInterrupted(); }

    @Override
    public ManagerPeer withManagerAnnounce(ManagerAnnounce managerAnnounce) {
        this.managerAnnounce = managerAnnounce;
        return this;
    }


    @Override
    public Torrent getTorrent() { return this.torrent; }

    @Override
    public boolean connectError(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connectError'");
    }

    @Override
    public boolean shakeHandsError(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shakeHandsError'");
    }

    @Override
    public boolean downloaded(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'downloaded'");
    }

    @Override
    public boolean uploaded(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploaded'");
    }

    @Override
    public void addInterestPeer(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addInterestPeer'");
    }

    @Override
    public void removeInterestPeer(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeInterestPeer'");
    }

    @Override
    public ManagerFile getManagerFile() { return this.managerFile;  }
    @Override
    public ManagerPeer withManagerFile(ManagerFile managerFile) {
        this.managerFile = managerFile;
        return this;
    }

    @Override
    public ManagerPeer withSemaphoreExecutor(Semaphore semaphoreExecutor) {
        this.semaphoreExecutor = semaphoreExecutor;
        return this;
    }

}
