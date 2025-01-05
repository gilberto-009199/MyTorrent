package org.voyager.torrent.client.managers;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.net.messages.MsgCancel;
import org.voyager.torrent.client.net.socket.SocketChannelNetwork;
import org.voyager.torrent.client.peers.BasicPeer;
import org.voyager.torrent.client.net.exceptions.HandShakeInvalidException;
import org.voyager.torrent.client.net.exceptions.NoReaderBufferException;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.net.messages.MsgPiece;
import org.voyager.torrent.client.net.messages.MsgRequest;
import org.voyager.torrent.client.peers.InfoPeer;
import org.voyager.torrent.client.peers.Peer;
import org.voyager.torrent.client.strategy.ManagerAnnounceStrategy;
import org.voyager.torrent.client.strategy.ManagerFileStrategy;
import org.voyager.torrent.client.strategy.ManagerPeerStrategy;
import org.voyager.torrent.client.strategy.Strategy;
import org.voyager.torrent.client.strategy.basic.BasicManagerPeerStrategy;
import org.voyager.torrent.client.strategy.basic.BasicPeerStrategy;

public class BasicManagerPeer implements ManagerPeer{

    private ClientTorrent client;

    private ManagerPeerStrategy strategy;
    private Thread threadCurrent;

    // IO non-blocker
    private Selector selector;

    private final Map<SocketChannel, Peer> mapChannelAndPeer;

    public BasicManagerPeer(){
        this.mapChannelAndPeer      = new ConcurrentHashMap<>();
    }

    public BasicManagerPeer(ClientTorrent client){
        this();
        this.client                 = client;
    }


    @Override
    public void run() {
        
        initSelector();

        while(!isInterrupted()) {
            try {

                client.state().semaphoreExecutor().acquire();

                System.out.println("++++++++ ManagerPeer +++++++");

                process();


            } catch (InterruptedException e) {  Thread.currentThread().interrupt();  } 
              finally {
                client.state().semaphoreExecutor().release();
            }

            System.out.println("-------- ManagerPeer -------");
            sleep(128);
        }

    }

    private void process(){
        processQueueNewsPeer();
        processLifeCycle();
        processReceiveMsgPiece();
        processSendMsgRequest();
        processReceiveMsgRequest();
        // @todo process send keep-alive
    }

    private void processLifeCycle() {
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
                SocketChannel channel = (SocketChannel) key.channel();
                mapChannelAndPeer.remove(channel);
                closeChannel(channel);
                key.cancel();

            }
        }
    }

    // Hook Selector handler for handling completed connections
    public void handlerConnect(SelectionKey key) {

        SocketChannel channel = (SocketChannel) key.channel();
        Peer peer = mapChannelAndPeer.get(channel);

        System.out.println("handlerConnect: "+ peer);

        try {
    
            // Finalizar a conexão
            if (channel.finishConnect()) {

                peer.statePeer().setConnected(true);
                peer.statePeer().setHandshake(false);

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
        Peer peer = mapChannelAndPeer.get(channel);

        boolean isReadable = channel.isConnected() && channel.isOpen();
        boolean notIsReadableThen = !isReadable;
        if(notIsReadableThen)return;

        System.out.println("handlerRead: "+ peer);

        try {

            if(peer.statePeer().connected()) peer.read();

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
        Peer peer = mapChannelAndPeer.get(channel);

        boolean isWritable = channel.isConnected() && channel.isOpen();
        boolean notIsWritableThen = !isWritable;
        if(notIsWritableThen)return;

        System.out.println("handlerWrite: "+ peer);

        try {
            

            if( peer.statePeer().connected() ) peer.write();

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
/*
        List<MsgRequest> listMsgRequest = managerFile.msgRequest();

        //  peers for sort metrics
        List<BasicPeer> listPeer = mapChannelAndPeer.values().stream()
                                                                .sorted(Collections.reverseOrder())
                                                                .collect(Collectors.toList());


        List<MsgRequest> listMsgRequestInPeer = new ArrayList<>();

        for (BasicPeer peerNonBlock : listPeer) {
            if(!peerNonBlock.isConnected) continue;
            if(!peerNonBlock.hasHandshake) continue;

            // @todo colocar choked verify
            if(peerNonBlock.getPiecesMap() == null) continue;
            if(peerNonBlock.hasChoked()) continue;


            for(int i = 0; i < listMsgRequest.size(); i++){
                MsgRequest request = listMsgRequest.get(i);

                if(listMsgRequestInPeer.contains(request))continue;

                byte[] map = peerNonBlock.getPiecesMap().getMap();

                if(map[request.getPosition()] != 0){

                    peerNonBlock.queueNewMsgIfNotExist(request);
                    listMsgRequestInPeer.add(request);
                }

            }
        }
*/
    }

    // Process Pieces Recieve from peers
    private void processReceiveMsgPiece(){
       /* while(!queueRecieveMsgPiece.isEmpty()){
            MsgPiece msg = queueRecieveMsgPiece.poll();
            managerFile.queueMsg(msg);
        }*/
    }

    private void processReceiveMsgRequest(){
        // queueRecieveMsgRequest
        // verify piece exist in managerFile
        // see exist add queue for return picei
    }

    // Process Queue
    private void processQueueNewsPeer(){
        while(!queueNewsPeer.isEmpty()){

            Peer peer = queueNewsPeer.poll();

            try{

                SocketChannel channel = SocketChannel.open();

                channel.configureBlocking(false);

                InetSocketAddress address = new InetSocketAddress(peer.infoLocal().host(), peer.infoLocal().port());

                channel.connect(address);
                
                // 30s for connect and keep-alive
                channel.socket().setSoTimeout(30000);
                channel.register(selector, SelectionKey.OP_CONNECT);

                peer.setStrategy( new BasicPeerStrategy())
                    .setNetwork(  new SocketChannelNetwork(channel) )
                    .setManagerPeer(this);

                mapChannelAndPeer.put(channel, peer);
    
                System.out.println("Peer registry: " + peer);
    
            } catch (IOException e) {
                System.err.println("Erro ao registrar peer: " + peer);
                e.printStackTrace();
            }
        }
    }

    // Util selector
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

    public List<Peer> listPeer() { return mapChannelAndPeer.values().stream().collect(Collectors.toList()); }

    @Override
    public ClientTorrent client() { return this.client; }
    @Override
    public ManagerPeer setClient(ClientTorrent client) {
        this.client                 = client;
        return this;
    }

    @Override
    public ManagerPeerStrategy strategy() { return this.strategy;  }
    @Override
    public ManagerPeer setStrategy(Strategy strategy) {
        this.strategy = (ManagerPeerStrategy) strategy;
        return this;
    }

    @Override
    public Thread thread() {  return this.threadCurrent; }
    @Override
    public ManagerPeer setThread(Thread thread) {
        this.threadCurrent = thread;
        return this;
    }
}
