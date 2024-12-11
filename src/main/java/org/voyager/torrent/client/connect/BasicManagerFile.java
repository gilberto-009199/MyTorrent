package org.voyager.torrent.client.connect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.Torrent;

public class BasicManagerFile implements ManagerFile{

    private PiecesMap map;
    private final Lock lockMap;
    private Torrent torrent;
    private ClientTorrent client;
    private ManagerPeer managerPeer;
    private Semaphore semaphoreExecutor;
    private ManagerAnnounce managerAnnounce;

    private Queue<MsgPiece> queueRecieveMsgPiece;

    // mapped packets MsgPiece
    private Map<Integer, List<MsgPiece>> mapReciveMsgPiece;

    public BasicManagerFile(ClientTorrent client){
        this.client                 = client;
        this.torrent                = client.getTorrent();
        this.lockMap                = new ReentrantLock();
        this.managerPeer            = client.getManagerPeer();
        this.queueRecieveMsgPiece   = new ConcurrentLinkedQueue<>();
        this.map                    = new PiecesMap(client.getTorrent());
        this.mapReciveMsgPiece      = new HashMap<>(this.map.getMap().length);
    }

    @Override
    public void run() {
        while(!isInterrupted()){
            try {
                semaphoreExecutor.acquire();

                process();

            } catch (InterruptedException e) {  Thread.currentThread().interrupt();  } 
              finally {
                semaphoreExecutor.release();
            }
            sleep(1000);
        }
    }
    
    private void process(){
        // process MsgPices recieve in My Queue 
        processRecieveMsgPiece();
        // verify blocks hashes

    }

    private void processRecieveMsgPiece() {
        lockMap.lock();
        try {
            
            //  interar block in MsgPiece
            //      save file    
            //      clear MsgPiece block 
            //      manteined begin, end , position in MsgPiece

            // recalc Map

        } catch (Exception e) {}
         finally{
            lockMap.unlock();
        }
    }

    public List<MsgRequest> calcMsgRequest(){
        
        List<MsgRequest> listMsgRequest = new ArrayList<>();
        
        lockMap.lock();
        try {
            
            //map.reCalcMap();

            byte[] mapBinary = map.getMap();
            for(int index = 0; index < mapBinary.length; index++){
                listMsgRequest.addAll(genListMsgRequestFromPiece(index));
            }

        } catch (Exception e) {}
         finally{
            lockMap.unlock();
        }

        return listMsgRequest;
    }

    private List<MsgRequest> genListMsgRequestFromPiece(int piece) {

        List<MsgRequest> listMsgRequests = new ArrayList<>();

        if(mapReciveMsgPiece == null)this.mapReciveMsgPiece = new HashMap<>();
        
        mapReciveMsgPiece.putIfAbsent(piece, new ArrayList<>());

        List<MsgPiece> listBlockPiece = mapReciveMsgPiece.get(piece);

        int pieceSize = map.getSizePiece(); // Supondo que 'map' contém informações sobre o tamanho das peças
        int blockSize = map.sizeBlock;
        int totalBlocks = map.totalBlockInPiece();

        boolean[] receivedBlocks = new boolean[totalBlocks];
        for (MsgPiece block : listBlockPiece) {
            int beginIndex = block.getBegin() / blockSize;
            receivedBlocks[beginIndex] = true;
        }

        for (int i = 0; i < totalBlocks; i++) {
            if (!receivedBlocks[i]) {
                int begin = i * blockSize;
                int length = Math.min(blockSize, pieceSize - begin); // Último bloco pode ser menor que blockSize
                listMsgRequests.add(new MsgRequest(piece, begin, length));
            }
        }

        return listMsgRequests;
    }

    public void queueMsg(MsgPiece msg){
        lockMap.lock();

        try { queueRecieveMsgPiece.add(msg); }
        catch (Exception e) {}
        finally{
            lockMap.unlock();
        }
    }

    private void sleep(long ms){ try{Thread.sleep(ms);}catch (Exception e) {}  }
    private boolean isInterrupted(){ return Thread.currentThread().isInterrupted(); }

    @Override
    public ManagerFile withManagerPeer(ManagerPeer managerPeer) {
        this.managerPeer = managerPeer;
        return this;
    }

    @Override
    public ManagerFile withManagerAnnounce(ManagerAnnounce managerAnnounce) {
        this.managerAnnounce = managerAnnounce;
        return this;
    }

    @Override
    public ManagerFile withSemaphoreExecutor(Semaphore semaphoreExecutor) {
        this.semaphoreExecutor = semaphoreExecutor;
        return this;
    }
}
