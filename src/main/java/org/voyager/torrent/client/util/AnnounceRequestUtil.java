package org.voyager.torrent.client.util;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.voyager.torrent.client.connect.Peer;
import org.voyager.torrent.client.connect.PeerNonBlock;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.util.BinaryUtil;
import org.voyager.torrent.util.HttpUtil;
import org.voyager.torrent.util.ReaderBencode;

public class AnnounceRequestUtil {

    public static Optional<List<PeerNonBlock>> requestAnnounce(Torrent torrent){
        return requestAnnounce(torrent.getAnnounceURL(), torrent.genAnnounceParameters(), 0, 3);
    }

    public static Optional<List<PeerNonBlock>> requestAnnounce(String announce, Map<String, String> parameters){
        return requestAnnounce(announce, parameters, 0, 3);
    }

    public static Optional<List<PeerNonBlock>> requestAnnounce(String announce, Map<String, String> parameters, int retry, int limit){

        List<PeerNonBlock> listPeer = new ArrayList<PeerNonBlock>();

        try{
            URL url_announce = new URL(announce+"?"+HttpUtil.getParamsString(parameters));
            
            // get data for connect pars
            HttpURLConnection con = (HttpURLConnection) url_announce.openConnection();
            con.setRequestMethod("GET");

            con.connect();

            // read response
            StringBuffer res = BinaryUtil.inputStreamReaderToStringBuffer( new InputStreamReader(con.getInputStream()) );

            Map<ByteBuffer, Object> map = ReaderBencode.bencodeToMap(res);
            
            List<Map<ByteBuffer, Object>> peersList = (List<Map<ByteBuffer, Object>>) map.get(BinaryUtil.stringToByteBuffer("peers"));
            
            for (Map<ByteBuffer, Object> rawPeer : peersList) {
                
                int peerPort = ((Integer) rawPeer.get(BinaryUtil.stringToByteBuffer("port"))).intValue();
                
                String ip = null;
                try {
                    ip = new String(((ByteBuffer) rawPeer.get(BinaryUtil.stringToByteBuffer("ip"))).array(),
                            "ASCII");
                } catch (UnsupportedEncodingException e) {
                    System.out.println("Unable to parse encoding");
                    continue;
                }
                
                listPeer.add( new PeerNonBlock().withHost(ip).withPort(peerPort) ); 
            }
        }catch(Exception e){
            System.out.println("Error In Announce:");
            e.printStackTrace();
            // @todo colocar retry ???
            if(retry < limit)return requestAnnounce( announce, parameters , ++retry, limit);

            throw new RuntimeException("ERROR retry announce");
        }

        return Optional.of(listPeer);
    }
}
