package org.voyager.torrent.client.files;

import java.security.KeyStore.Entry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.voyager.torrent.util.BinaryUtil;
import org.voyager.torrent.util.HttpUtil;

public class torrent {

    // Data
    int file_length;
    int piece_length;
    String file_name;
    byte[] info_hash;
    String announce_url;
    List<byte[]> piece_hashes;

    public Map<String, String> genAnnounceParameters(){
        String peerId =  HttpUtil.toHexString(BinaryUtil.genBinaryArray(20));
        int port        = -1,
            uploaded    = 0,
            downloaded  = 0;

        return genAnnounceParameters(peerId, port, uploaded, downloaded);
    }

    public Map<String, String> genAnnounceParameters(String peerId){
        int port        = -1,
            uploaded    = 0,
            downloaded  = 0;
        
        return genAnnounceParameters(peerId, port, uploaded, downloaded);
    }

    public Map<String, String> genAnnounceParameters(String peerId, int port){
        int uploaded    = 0,
            downloaded  = 0;
        
        return genAnnounceParameters(peerId, port, uploaded, downloaded);
    }

    public Map<String, String> genAnnounceParameters(String peerId, int port, int uploaded){
        int downloaded  = 0;
        return genAnnounceParameters(peerId, port, uploaded, downloaded);
    }

    public Map<String, String> genAnnounceParameters(String peerId, int port, int uploaded, int downloaded){
        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put("info_hash",     HttpUtil.toHexString(info_hash));
        parameters.put("peer_id",       peerId); // identify par


        parameters.put("port",          String.valueOf(port)); // port connect
        parameters.put("uploaded",      String.valueOf(uploaded));
        parameters.put("downloaded",    String.valueOf(downloaded)); 
        parameters.put("left",          String.valueOf(file_length));

        return parameters;
    }

    public static torrent of(String path){
        torrent instance = new torrent();

        return instance;
    }
}
