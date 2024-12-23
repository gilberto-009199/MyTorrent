package org.voyager.torrent.client.files;

import GivenTools.TorrentInfo;

import java.util.Arrays;

public class PiecesMap {

    private byte[] map;
    private int sizePiece;
    public final int sizeBlock = 16 * 1024;

    public PiecesMap(Torrent torrent){ 
        this.map            = new byte[(torrent.getListPieceHashes().size() / 8) + 1];
        this.sizePiece      = torrent.getPieceLength();
    }

    public PiecesMap(TorrentInfo torrent){ 
        // calc (total hashes / 8), for pieces => 
        //  vect bytes => 
        //      1 byte =>
        //          8 bits  =>
        //              0|1 bit
        // norma +1 byte 000000 end
        this.map        = new byte[(torrent.piece_hashes.length / 8) + 1];
        this.sizePiece  = torrent.piece_length;
    }

    public PiecesMap(int countPieces, int pieceLength){ 
        this.map = new byte[countPieces];
        this.sizePiece = pieceLength;
    }

    public PiecesMap(byte[] map, int pieceLength){  
        this.map = map;
        this.sizePiece = pieceLength;
    }

    // Data func
    public long totalPieces(){ 
        long total = 0l;
        for (byte b : map) {
            // @todo perceba que 1 byte tem 8 bits e isso pode gerar um mapa mais relevante no futuro
            if(b != 0)total++;
        }
        return total;
    }
    
    public boolean complete(){
        return totalPieces() == map.length;
    }

    public int totalBlockInPiece(){
        return (int) Math.ceil((double) sizePiece / sizeBlock);
    }
    
    public PiecesMap diff(PiecesMap piecesMap){
        PiecesMap diff = new PiecesMap(this.map.length, this.sizePiece);
        byte[] mapDiff = diff.getMap();
        // diff this for pieceMpa 
        // x ? y
        // 0 ? 1 = 1  ~0b0 & 0b1
        // 1 ? 0 = 0  ~0b1 & 0b0
        // 0 ? 0 = 0  ~0b0 & 0b0
        // 1 ? 1 = 0  ~0b1 & 0b1
        
        for(int i = 0; i < map.length; i++) {
            mapDiff[i] = (byte) ( ~(map[i]) & (piecesMap.map[i]) );
        }

        return diff;
    }
    // @todo imnplementar change segmento
    public void  setMap(byte[] map){  this.map = map; }
    public byte[] getMap(){  return this.map; }

    public void setSizePiece(int sizePiece){this.sizePiece = sizePiece;}
    public int getSizePiece(){ return this.sizePiece; }
    


    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (! (obj instanceof PiecesMap))return false;

        PiecesMap piecesMap = (PiecesMap) obj;
        return map.length == piecesMap.map.length &&
                sizePiece == piecesMap.sizePiece &&
                Arrays.equals(map, piecesMap.map);
    }

    public String toString(){
        double progress = ((double) totalPieces() / map.length) * 100;
        return String.format("Progress: %.2f%% (%d/%d peças)", progress, totalPieces(), map.length);
    }
/* 
    public void addPieceBlock(MsgPiece msg) {
        if(mapReciveMsgPiece == null)this.mapReciveMsgPiece = new HashMap<>();

        if(mapReciveMsgPiece.get(msg.getPosition()) == null)mapReciveMsgPiece.put(msg.getPosition(), new ArrayList<>(totalBlockInPiece()));

        List<MsgPiece> listPieces = mapReciveMsgPiece.get(msg.getPosition());

        listPieces.add(msg);
    }

    public void reCalcMap(){
        
        // alter map bytes
        // talves cheksum in pieces
        int totalBlockInPiece = totalBlockInPiece();
        byte[] newMap = map.clone();
        // clear
        for (int index = 0; index < newMap.length; index++) { newMap[index] = 0;  }

        for (Entry<Integer, List<MsgPiece>> postionAndListMsgPiece: mapReciveMsgPiece.entrySet()) {
            boolean complete = isPieceComplete(
                postionAndListMsgPiece.getValue(),
                totalBlockInPiece
            );
            newMap[postionAndListMsgPiece.getKey()] = (byte) (complete ? 1 : 0 );
        }
        map = newMap;
    }

    private boolean isPieceComplete(List<MsgPiece> listMsgPiece, int totalBlockInPiece) {

        listMsgPiece.sort(Comparator.comparingInt(MsgPiece::getBegin));

        int expectedBegin = 0;
        for (MsgPiece msgPiece : listMsgPiece) {
            if (msgPiece.getBegin() != expectedBegin) {
                return false; // Falha na continuidade
            }
            expectedBegin += msgPiece.getBlock().length;
        }

        return listMsgPiece.size() == totalBlockInPiece;
    }
*/
}
