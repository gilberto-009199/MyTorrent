package org.voyager.torrent.client.connect;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.exceptions.HandShakeInvalidException;
import org.voyager.torrent.client.exceptions.NoReaderBufferException;
import org.voyager.torrent.client.files.Torrent;

public class BasicManagerPeer implements ManagerPeer{

    private Torrent torrent;
    private ClientTorrent client;
    private ManagerFile managerFile;
    private Semaphore semaphoreExecutor;
    private ManagerAnnounce managerAnnounce;

    private int maxUploaderPeerSecond = -1;
	private int maxDownloaderPeerSecond = -1;

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

                process();

                System.out.println("++++++++ ManagerPeer +++++++");

            } catch (InterruptedException e) {  Thread.currentThread().interrupt();  } 
              finally {
                semaphoreExecutor.release();
            }
            
            sleep(50);
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
                key.cancel();
                closeChannel((SocketChannel) key.channel());
            }

            
        }

        System.out.println("+++++++++++++++");

        processRecieveMsgPiece();
        processSendMsgRequest();
        processRecieveMsgRequest();
    }

    // Hook Selector handler for handling completed connections
    public void handlerConnect(SelectionKey key) {
        // Este método é chamado quando um canal cliente completa a conexão com um servidor.
        // - Verifica se a conexão foi estabelecida com sucesso.
        // - Pode enviar uma mensagem inicial (como um handshake) para estabelecer a comunicação.
        // - Atualiza o estado do Peer associado ao canal.

        SocketChannel channel = (SocketChannel) key.channel();
        PeerNonBlock peer = mapChannelAndPeer.get(channel);
        
        System.out.println("handlerConnect: \n\t"+ peer);

        try {
    
            // Finalizar a conexão
            if (channel.finishConnect()) {

                System.out.println("Conexão estabelecida para o peer: \n\t" + peer);

                // Registrar interesse em leitura
                peer.setConneted(true);
                peer.setHandshake(false);

                //key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);

                //key.interestOps(SelectionKey.OP_READ);
                key.interestOps(SelectionKey.OP_WRITE);

            }
        } catch (IOException e) {
            System.err.println("Erro ao estabelecer conexão ou enviar handshake");
            e.printStackTrace();
        }
    }

    // Hook Selector handler for reading from channels
    public void handlerRead(SelectionKey key) throws IOException{
        // Este método é chamado quando há dados disponíveis para leitura no canal.
        // - Lê os dados do canal no buffer associado.
        // - Passa o controle para o Peer associado para processar a mensagem recebida.
        // - Pode incluir lógica para lidar com fragmentação ou mensagens parciais.
        
        
        SocketChannel channel = (SocketChannel) key.channel();
        PeerNonBlock peer = mapChannelAndPeer.get(channel);
        
        System.out.println("handlerRead: \n\t"+ peer);

        try {
            
            if(peer.isConnected() && !peer.hasHandshake()){

                peer.readShake(channel);
                
                key.interestOps(SelectionKey.OP_READ);

            } else {

                peer.readMsg(channel);

            }

            

        } catch (NoReaderBufferException e) {
            System.err.println("Erro durante leitura do peer: " + peer);
            e.printStackTrace();

        } catch (HandShakeInvalidException | ConnectException e) {
            System.err.println("Erro durante leitura do peer: " + peer);
            e.printStackTrace();

            channel.close();
            key.cancel();
        } catch (IOException e) {
            System.err.println("Erro durante leitura do peer: " + peer);
            e.printStackTrace();

            channel.close();
            key.cancel();
        }
       
    }

    // Hook Selector handler for writing to channels
    public void handlerWrite(SelectionKey key) {
        // Este método é chamado quando o canal está pronto para escrita.
        // - Recupera dados que precisam ser enviados para o canal (geralmente de uma fila de mensagens do Peer).
        // - Escreve os dados no canal usando buffers.
        // - Pode alternar o interesse do canal para evitar ciclos contínuos de escrita.

        SocketChannel channel = (SocketChannel) key.channel();
        PeerNonBlock peer = mapChannelAndPeer.get(channel);
        
        System.out.println("handlerWrite: \n\t"+ peer);

        try {
            
            if(peer.isConnected() && !peer.hasHandshake()){
            
                peer.writeShake(channel);

                key.interestOps(SelectionKey.OP_READ);

            } else {

                //peer.writeMsg(channel);

            }

        } catch (IOException e) {
            System.err.println("Erro durante leitura do peer: " + peer);
            e.printStackTrace();
            key.cancel();

            try { channel.close(); }
            catch (IOException ex) {
                System.err.println("Erro ao fechar canal: " + ex.getMessage());
            }
        }

        //ByteBuffer buffer = ByteBuffer.wrap(peer.genHandshake());
        //buffer.order(ByteOrder.LITTLE_ENDIAN); // Configurar como little-endian

        //buffer.flip();
        
        //channel.write(buffer);


        // @Alterar o modo para leitura
    }

    // Process send Request Pieces in peer
    private void processSendMsgRequest(){
        // @todo  add time entry request 500ms

        List<MsgRequest> listMsgRequest = managerFile.calcMsgRequest();
        // priorite peers for sort 
        // process send Request for peers contained Piece and retrict max bytes request for peer
        List<PeerNonBlock> listPeer = mapChannelAndPeer.values().stream()
                                                                .sorted(Collections.reverseOrder())
                                                                .toList();

        Map<Peer, List<MsgRequest>> mapPeerAndMsgRequest = new HashMap<>();
        
        for (PeerNonBlock peerNonBlock : listPeer) {
            // verify conected
            // verify max pieces for peer
            // registry mapPeerAndMsgRequest
            // listMsgRequest.remove()
        }

        for (Entry<Peer, List<MsgRequest>> entry : mapPeerAndMsgRequest.entrySet()) {
            // send request for peer
        }
    }
    
    // Process Pieces Recieve from peers
    private void processRecieveMsgPiece(){
        while(!queueRecieveMsgPiece.isEmpty()){
            MsgPiece msg = queueRecieveMsgPiece.poll();
            managerFile.queueMsg(msg);
        }
    }

    private void processRecieveMsgRequest(){
        // queueRecieveMsgRequest
        // verify piece exist in managerFile
        // see exist add queue for return picei

    }

    
    private void closeChannel(SocketChannel channel) {
        try {
            channel.close();
        } catch (IOException e) {
            System.err.println("Erro ao fechar canal: " + e.getMessage());
        }
    }

    // Process Queue
    private void processQueueNewsPeer(){
        while(!queueNewsPeer.isEmpty()){

            PeerNonBlock peer = queueNewsPeer.poll();

            try{

                SocketChannel channel = SocketChannel.open();
                channel.configureBlocking(false);
                channel.connect(new InetSocketAddress(peer.getHost(), peer.getPort()));
                
                // 30s de inatividade
                channel.socket().setSoTimeout(30000);

                channel.register(selector, SelectionKey.OP_CONNECT);
    
                mapChannelAndPeer.put(channel, peer);
    
                System.out.println("Peer registrado: " + peer);
    
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
    public void queueNewMsg(PeerNonBlock peer, MsgRequest msg) {
        queueRecieveMsgRequest.add(msg);
    }

    @Override
    public void queueNewMsg(PeerNonBlock peer, MsgPiece msg) {
        queueRecieveMsgPiece.add(msg);
    }

    // Util selector
    // @todo Mitigar
    private void select(){ try{ selector.select(); }catch(Exception e){ e.printStackTrace(); } }

    // @todo Mitigar
    private void initSelector(){ 
        if(selector != null && selector.isOpen())return;
        try{ selector = Selector.open(); }catch(Exception e){e.printStackTrace();}
    }

    private void sleep(long ms){ try{Thread.sleep(ms);}catch (Exception e) {}  }
    private boolean isInterrupted(){ return Thread.currentThread().isInterrupted(); }

    @Override
    public ManagerPeer withManagerAnnounce(ManagerAnnounce managerAnnounce) {
        this.managerAnnounce = managerAnnounce;
        return this;
    }

    @Override
    public ManagerPeer withMaxUploaderPeerSecond(int maxUploaderPeerSecond) {
        this.maxUploaderPeerSecond = maxUploaderPeerSecond;
        return this;
    }

    @Override
    public ManagerPeer withMaxDownloaderPeerSecond(int maxDownloaderPeerSecond) {
        this.maxDownloaderPeerSecond = maxDownloaderPeerSecond;
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
