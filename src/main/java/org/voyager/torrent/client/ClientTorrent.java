package org.voyager.torrent.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.voyager.torrent.client.connect.ManagerPeer;
import org.voyager.torrent.client.connect.Peer;
import org.voyager.torrent.util.BinaryUtil;
import org.voyager.torrent.util.HttpUtil;
import org.voyager.torrent.util.PrintUtil;
import org.voyager.torrent.util.ReaderBencode;

import GivenTools.BencodingException;
import GivenTools.TorrentInfo;

public class ClientTorrent implements ManagerPeer{ 

	public static String separator = System.getProperty("file.separator");
	public static String dirUser = new File(System.getProperty("user.home")).getAbsolutePath()+separator;
	public static String dirRuntime = "."+separator;

	// torrent info e connets info
	public TorrentInfo torrent;

	private boolean verbouse;

	// state
	// Map Pieces Downloaded from file
	public Object mapPices;
	// List Peers Connected
	public List<Peer> listPeers;

	public int uploaded;
	public int downloaded;

	public ClientTorrent(boolean verbouse){ this.verbouse = verbouse; }

	public void start() throws BencodingException {
		
		List<Peer> listPeers = new ArrayList<Peer>(); 
		
		// Read my file torrent and announce_url
		try {
			
			// generate binarie 20 
			byte[] peerIdBinary = BinaryUtil.genBinaryArray(20);
			String peerId = HttpUtil.toHexString(peerIdBinary);
			// Identify torrent
			String info_hash = HttpUtil.toHexString(torrent.info_hash.array());
			
			
			Map<String, String> parameters = new HashMap<String,String>();
			parameters.put("info_hash",info_hash);
			parameters.put("peer_id", peerId); // identify par
			// Contruir o socket para enviar os Datagrams for peers 
			parameters.put("port", "-1"); // port connect
			parameters.put("uploaded", "0");
			parameters.put("downloaded", "0"); 
			parameters.put("left", torrent.file_length+"");
			System.out.println(" Announce URL:  "+ torrent.announce_url);
			
			URL url_announce = new URL(torrent.announce_url+"?"+HttpUtil.getParamsString(parameters));
			
			// get data for connect pars
			HttpURLConnection con = (HttpURLConnection) url_announce.openConnection();
			con.setRequestMethod("GET");

			con.connect();

			// read response
			StringBuffer res =  BinaryUtil.inputStreamReaderToStringBuffer( new InputStreamReader(con.getInputStream()) );
					
			Map<ByteBuffer,Object> map = ReaderBencode.bencodeToMap(res);
			
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
				
				listPeers.add( new Peer( ip, peerPort, peerIdBinary, this) ); 
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if(verbouse)System.out.println("Total de Pares: \n"+ listPeers.stream().map((peer) -> peer.toString()+"\n").toList());
		
		for(Peer peer : listPeers.subList(0, 4)) {
			peer.setVerbouse(verbouse);
			Thread thread = new Thread(peer);
			if(verbouse)System.out.println("Try Connect: \n\t"+ peer);
			try {
				thread.start();
			}catch (Exception e) {
				System.out.println(e.getMessage());
			}
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
	public TorrentInfo getTorrent() {
		return this.torrent;
	}

	public boolean connectError(Peer peer) {
		System.out.println("Erro na conexao: "+peer);
		
		return false;
	}
	public boolean downloaded(Peer peer) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean uploaded(Peer peer) {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean shakeHandsError(Peer peer) {
		System.out.println("Erro no hasdshake: "+peer);
		return false;
	}

}
