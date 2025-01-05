package org.voyager.torrent.client.managers;

import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.net.messages.MsgPiece;
import org.voyager.torrent.client.net.messages.MsgRequest;
import org.voyager.torrent.client.strategy.ManagerAnnounceStrategy;
import org.voyager.torrent.client.strategy.ManagerFileStrategy;
import org.voyager.torrent.client.strategy.Strategy;
import org.voyager.torrent.client.strategy.basic.BasicManagerPeerStrategy;
import org.voyager.torrent.client.util.FileTorrentUtil;
import org.voyager.torrent.util.FileUtil;

public class BasicManagerFile implements ManagerFile{

    private Torrent torrent;
    private ClientTorrent client;

    private Queue<MsgPiece> queueRecieveMsgPiece;
    private RandomAccessFile randomAccessFile;
    private final Lock lockMap;
    private PiecesMap map;

    private ManagerFileStrategy strategy;
    private Thread threadCurrent;

    // mapped packets MsgPiece
    private Map<Integer, List<MsgPiece>> mapReceiveMsgPiece;
    private List<MsgRequest> listMsgRequest;

    public BasicManagerFile(){
        this.listMsgRequest         = new ArrayList<>();
        this.lockMap                = new ReentrantLock();
        this.queueRecieveMsgPiece   = new ConcurrentLinkedQueue<>();
    }
    public BasicManagerFile(ClientTorrent client){
        this();
        this.client                 = client;
        this.torrent                = client.state().torrent();
        this.map                    = new PiecesMap(this.torrent);
        this.mapReceiveMsgPiece      = new HashMap<>(this.map.getMap().length);
    }

    @Override
    public void run() {

        initFileSystem();

        while(!isInterrupted()){
            try {
                client.state().semaphoreExecutor().acquire();
                System.out.println("++++++++ ManagerFile +++++++");

                process();

            } catch (InterruptedException e) {Thread.currentThread().interrupt(); }
            finally {
                client.state().semaphoreExecutor().release();
            }
            System.out.println("-------- ManagerFile -------");
            sleep(1000);
        }
    }


    private void process(){
        // process MsgPices recieve in My Queue 
        processReceiveMsgPiece();
        calcMsgRequest();
        // verify blocks hashes
        // recalcMap and MsgRequest
    }

    private void processReceiveMsgPiece() {
        lockMap.lock();
        try {

            while(!queueRecieveMsgPiece.isEmpty()){

                MsgPiece piece = queueRecieveMsgPiece.poll();

                int offset = piece.getPosition() * map.getSizePiece();
                int begin = piece.getBegin();

                // save file in random file
                randomAccessFile.seek(offset + begin);
                randomAccessFile.write(piece.getBlock());

                mapReciveMsgPiece.putIfAbsent(piece.getPosition(), new ArrayList<>());
                mapReciveMsgPiece.get(piece.getPosition()).add(piece);

                System.out.println("Piece: "+ piece);
                System.out.println(" offset: "+ offset);
                System.out.println(" begin: "+ begin);
                System.out.println(" begin+offset: "+ begin+offset);
                System.out.println("\n\tCONTENT:\n\n");
                System.out.write(piece.getBlock());
                System.out.println("\n\tFINAL CONTENT\n\n");

            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            lockMap.unlock();
        }
    }

    public List<MsgRequest> calcMsgRequest(){

        lockMap.lock();

        listMsgRequest = new ArrayList<>();

        try {
            
            //map.reCalcMap();

            byte[] binaryMap = map.getMap();
            for(int index = 0; index < binaryMap.length; index++){
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


    private void initFileSystem() {

        String home = System.getProperty("user.home");

        // Constrói o caminho do diretório de download usando Paths
        String dirName = Base64.getEncoder().encodeToString(torrent.getInfoHash());
        Path dirDownload = Paths.get(home, dirName);

        // Exibe o caminho final para validação (opcional)
        System.out.println("Dir: " + dirDownload);

        FileUtil.createDirectoryIfNotExist(dirDownload.toString());

        // create file and sabe
        Path file = Paths.get(dirDownload.toString(), torrent.getFileName());
        randomAccessFile = FileUtil.createFileEmptyIfNotExist( torrent.getFileLength(), file.toFile());

        verifyFileSystem();
    }

    private void verifyFileSystem(){

        try{
            lockMap.lock();

            List<byte[]> listPieceHashes = torrent.getListPieceHashes();
            byte[] binaryMap = map.getMap();

            int pieceLength = binaryMap.length;
            long fileLength = torrent.getFileLength();
            int sizePiece = (int) Math.ceil((double) fileLength / pieceLength);

            for(int i = 0; i < binaryMap.length; i++) {
                byte[] sha = listPieceHashes.get(i);
                boolean valid = FileTorrentUtil.verify(i, sha, randomAccessFile, pieceLength, fileLength);
                binaryMap[i] = (byte) (valid ? 1 : 0);
            }
            // corrija isso
            map = new PiecesMap(binaryMap, sizePiece);

        }finally {
            lockMap.unlock();
        }

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

    public PiecesMap getMap() { return map;  }

    @Override
    public List<MsgRequest> msgRequest() { return this.listMsgRequest; }



    @Override
    public ManagerFile withTorrent(Torrent torrent) {
        this.torrent = torrent;
        return this;
    }

    public void setMap(PiecesMap map) { this.map = map; }

    public Torrent getTorrent() { return torrent; }
    public void setTorrent(Torrent torrent) { this.torrent = torrent; }


    @Override
    public ClientTorrent client() { return this.client; }
    @Override
    public ManagerFile setClient(ClientTorrent client) {
        this.client                 = client;
        this.map                    = new PiecesMap(client.torrent());
        //this.mapReciveMsgPiece      = new HashMap<>(this.map.getMap().length);
        return this;
    }

    @Override
    public ManagerFileStrategy strategy() { return this.strategy;  }
    @Override
    public ManagerFile setStrategy(Strategy strategy) {
        this.strategy = (ManagerFileStrategy) strategy;
        return this;
    }

    @Override
    public Thread thread() {  return this.threadCurrent; }
    @Override
    public ManagerFile setThread(Thread thread) {
        this.threadCurrent = thread;
        return this;
    }
}
