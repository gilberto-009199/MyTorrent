package org.voyager.torrent.client.connect;

import java.util.Arrays;

import GivenTools.TorrentInfo;

public class PiecesMap {
    
    private byte[] map;
    private int sizePiece;

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

    public long totalBlockInPiece(){
        return (int) Math.ceil((double) sizePiece / 16384);
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

    public String toString(){
        double progress = ((double) totalPieces() / map.length) * 100;
        return String.format("Progress: %.2f%% (%d/%d peças)", progress, totalPieces(), map.length);
    }
}
