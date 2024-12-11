package org.voyager.torrent.client.connect;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.ClientTorrent;
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

            } catch (InterruptedException e) {  Thread.currentThread().interrupt();  } 
              finally {
                semaphoreExecutor.release();
            }
            
            sleep(30);
        }

    }

    private void process(){
        processQueueNewsPeer();

        select();
        
        for (SelectionKey key : selector.selectedKeys()) {
            if (key.isConnectable()) {
                handlerConnect(key);
            } else if (key.isReadable()) {
                handlerRead(key);
            } else if (key.isWritable()) {
                handlerWrite(key);
            }
        }

        selector.selectedKeys().clear();

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
        System.out.println("handlerConnect");
    }

    // Hook Selector handler for reading from channels
    public void handlerRead(SelectionKey key) {
        // Este método é chamado quando há dados disponíveis para leitura no canal.
        // - Lê os dados do canal no buffer associado.
        // - Passa o controle para o Peer associado para processar a mensagem recebida.
        // - Pode incluir lógica para lidar com fragmentação ou mensagens parciais.
        System.out.println("handlerRead");

        // send queue Pieces in Manager Files
    }

    // Hook Selector handler for writing to channels
    public void handlerWrite(SelectionKey key) {
        // Este método é chamado quando o canal está pronto para escrita.
        // - Recupera dados que precisam ser enviados para o canal (geralmente de uma fila de mensagens do Peer).
        // - Escreve os dados no canal usando buffers.
        // - Pode alternar o interesse do canal para evitar ciclos contínuos de escrita.
        System.out.println("handlerWrite");

        // @Alterar o modo para leitura
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

    // Process send Request Pieces in peer
    private void processSendMsgRequest(){
        List<MsgRequest> listMsgRequest = managerFile.calcMsgRequest();
        // priorite peers for sort 
        // process send peers contained Piece in
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
    

    // Hooks Queue's
    //  Queue New Peers from ManagerAnnounce
    public synchronized void queueNewsPeer(PeerNonBlock peer) { queueNewsPeer.add(peer); }
    public synchronized void queueNewsPeerIfNotPresent(PeerNonBlock peer) { 
        boolean IfPresent = mapChannelAndPeer.containsValue(peer);
        if(!IfPresent)queueNewsPeer(peer);
    }

    //  Queue New Msg from Peers
    @Override
    public void queueNewMsg(Peer peer, MsgRequest msg) {
        queueRecieveMsgRequest.add(msg);
    }

    @Override
    public void queueNewMsg(Peer peer, MsgPiece msg) {
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
