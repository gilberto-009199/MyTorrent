package org.voyager.torrent.client.connect;

import java.util.Arrays;

import GivenTools.TorrentInfo;

public class PiecesMap {
    
    private byte[] map;

    public PiecesMap(TorrentInfo torrent){ 
        // calc (total hashes / 8), for pieces => 
        //  vect bytes => 
        //      1 byte =>
        //          8 bits  =>
        //              0|1 bit
        // norma +1 byte 000000 end
        this.map = new byte[(torrent.piece_hashes.length / 8) + 1];
    }
    public PiecesMap(int countPieces){ this.map = new byte[countPieces];}
    public PiecesMap(byte[] map){  this.map = map; }

    // Data
    public long totalPieces(){ 
        long total = 0l;
        for (byte b : map) {
            // @todo perceba que 1 byte tem 8 bits e isso pode gerar um mapa mais relevante no futuro
            if(b != 0)total++;
        }    
        return total;
    }
    
    // @todo imnplemnntar change segmento
    public void  setMap(byte[] map){  this.map = map; }
    public byte[] getMap(){  return this.map; }

    
    public String toString(){
        double progress = ((double) totalPieces() / map.length) * 100;
        return String.format("Progress: %.2f%% (%d/%d peças)", progress, totalPieces(), map.length);
    }
}
