package org.voyager.torrent.client;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ServerSocket;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.voyager.torrent.client.model.Peer;
import org.voyager.torrent.util.BinaryUtil;
import org.voyager.torrent.util.HttpUtil;
import org.voyager.torrent.util.ReaderBencode;

import GivenTools.Bencoder2;
import GivenTools.BencodingException;
import GivenTools.TorrentInfo;

public class ClientTorrent { 
	
	public static String separator = System.getProperty("file.separator");
	public static String dirUser = new File(System.getProperty("user.home")).getAbsolutePath()+separator;
	public static String dirRuntime = "."+separator;
	 
	
	// torrent info e connets info
	public TorrentInfo torrent;
	public static int uploaded;
	public static int downloaded;
	
	public ClientTorrent Build() {
		return null;
	}
	
	public void start() throws BencodingException {
		try {
			
			// generate binarie 20 
			byte[] peerIdBinary = BinaryUtil.genBinaryArray(20);
			String peerId = HttpUtil.toHexString(peerIdBinary);
			// Identify torrent
			String info_hash = HttpUtil.toHexString(torrent.info_hash.array());
			
			
			Map<String, String> parameters = new HashMap<String,String>();
			parameters.put("info_hash",info_hash);
			parameters.put("peer_id", peerId); // identify par
			parameters.put("uploaded", "0");
			parameters.put("port", "-1"); // port connect
			parameters.put("downloaded", "0"); 
			parameters.put("left", torrent.file_length+"");
			System.out.println(torrent.announce_url);
			
			URL url_announce = new URL(torrent.announce_url+"?"+HttpUtil.getParamsString(parameters));
			
			// get data for connect pars
			HttpURLConnection con = (HttpURLConnection) url_announce.openConnection();
			con.setRequestMethod("GET");

			con.connect();

			// read response
			StringBuffer res =  BinaryUtil.inputStreamReaderToStringBuffer( new InputStreamReader(con.getInputStream()) );
			List<Peer> listPeers = new ArrayList<Peer>(); 
			System.out.println(res);
						
			Map<ByteBuffer,Object> map = ReaderBencode.bencodeToMap(res);
			
			int interval = (Integer) map.get( BinaryUtil.stringToByteBuffer("interval") );
			System.out.println( interval );
			
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
				//System.out.println("host: "+ip+"\t port: "+peerPort);
				
				listPeers.add( new Peer( ip, peerPort, peerIdBinary, this) ); 
			}
			
			for(Peer peer : listPeers.subList(0, 1)) {
				Thread thread = new Thread(peer);
				System.out.println(peer);
				thread.start();
			}
			
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public boolean addTorentFile(String arquivo){
		return addTorentFile(new File(arquivo));
	}
	public boolean addTorentFile(File arquivo){
		this.torrent = ReaderBencode.parseTorrentFile(arquivo);
		if(this.torrent == null)return false;
		else return true;
	}

}
