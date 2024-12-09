package org.voyager.torrent.client;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.BindException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.voyager.torrent.client.connect.ManagerPeer;
import org.voyager.torrent.client.connect.MsgPiece;
import org.voyager.torrent.client.connect.MsgRequest;
import org.voyager.torrent.client.connect.Peer;
import org.voyager.torrent.client.connect.PiecesMap;
import org.voyager.torrent.util.BinaryUtil;
import org.voyager.torrent.util.HttpUtil;
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
	private PiecesMap piecesMap;
	
	// List Peers Connected
	public List<Peer> listPeers;
	public List<Integer> listInterestPeer;
	
	// Queue's Pieces Recieve data
	public Queue<Entry<Peer, MsgPiece>> queuePeerRecievePieces = new ConcurrentLinkedQueue<>();

	// Queue's Pieces Request data
	public Queue<Entry<Peer, MsgRequest>> queuePeerRequestPieces = new ConcurrentLinkedQueue<>();

	public int uploaded;
	public int downloaded;

	public ClientTorrent(boolean verbouse){ this.verbouse = verbouse; }

	public void start() throws BencodingException {
		
		listPeers = new ArrayList<Peer>(); 
		
		// Read my file torrent and announce_url
		processFileTorrent();

		initPeers(15);

		while( !piecesMap.complete() ){

			List<Map.Entry<Peer,PiecesMap>> listPeerAndMap = new ArrayList<Map.Entry<Peer,PiecesMap>>();
			for(Peer peer : listPeers){
				if(peer.hasChoked())continue;

				PiecesMap diff = piecesMap.diff( peer.getPiecesMap() );
				// verify exist pieces
				if(diff.totalPieces() > 0){
					listPeerAndMap.add(new SimpleEntry<Peer, PiecesMap>(peer, diff));
				}
			}

			// @todo no futuro criar um logica para dividir as requisições entre os pares
			Queue<Map.Entry<Peer, List<MsgRequest>>> queueRequest = new LinkedList<Map.Entry<Peer, List<MsgRequest>>>();
			int maxPieceForPeer = 3;
			for(Map.Entry<Peer,PiecesMap> peerAndMap : listPeerAndMap) {
				if(verbouse)System.out.println("\t Request Peer & Map:");
				if(verbouse)System.out.println("\t 	Peer: "+ peerAndMap.getKey());
				if(verbouse)System.out.println("\t 	Map: "+ peerAndMap.getValue());

				Peer peer = peerAndMap.getKey();
				PiecesMap map = peerAndMap.getValue();
				int sizePiece = map.getSizePiece();
				int totalBlock = map.totalBlockInPiece();
				byte[] mapBinary = map.getMap();

				List<MsgRequest> listPiece = new ArrayList<>();
				for(int index = 0; index < mapBinary.length; index++){
					// verify != 0 piece
					if(mapBinary[index] != 0){
						//  divide in block size 16kb piece
						//  send request all blocks for piece
						// for block size in piece:
						for (int 
							beginBlock = 0;
							 	beginBlock < totalBlock 
								&&
							 	listPiece.size() < maxPieceForPeer;
							beginBlock++) {

							int begin = beginBlock * 16384;
							int length = Math.min(16384, sizePiece - begin); // caso o piece ja tenha finalizado o tamanho de block

							// Adiciona o bloco à lista de requisições.
							listPiece.add(new MsgRequest(index, begin, length));

						}
					}
				}
				queueRequest.add(new SimpleEntry<>(peer, listPiece));
			}

			for (Entry<Peer,List<MsgRequest>> peerAndPiece : queueRequest) {
				Peer peer = peerAndPiece.getKey();
				List<MsgRequest> pieces = peerAndPiece.getValue();
				for (MsgRequest request : pieces) {
					peer.sendMsgRequest( request );
				}
			}

			//   mounted pices in queuePeerRecievePieces
			//		hashes verify
			//		pices mounted
			//		updated new pieces map
			//			priority:
			//		 		0 peers interested
			//		 		1 peers
			//		 		2 peers not interested
			System.out.println("Queue Piece: "+ queuePeerRecievePieces);
			for (Entry<Peer, MsgPiece> peerAndPiece : queuePeerRecievePieces) {
				piecesMap.addPieceBlock(peerAndPiece.getValue());
				queuePeerRecievePieces.remove(peerAndPiece);
			}
			


			//   send pices	   in queuePeerRequestPieces
			//		priority:
			//			0 peers interested
			//			1 peers
			//			2 peers not interested

			sleep(3000);
			piecesMap.reCalcMap();
			System.out.println("MyMap:"+ piecesMap);
		}
	}
	private void sleep(long ms){ try{Thread.sleep(ms);}catch (Exception e) {}  }
	private void initPeers(int count) {
				
		if(verbouse)System.out.println("Total de Pares: \n"+ listPeers.stream().map((peer) -> peer.toString()+"\n").toList());
		
		for(Peer peer : listPeers.subList(0, count)) {
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

	private void processFileTorrent(){
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
			
			torrent.info_hash.array()
			this.piecesMap = new PiecesMap(torrent);

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
			
		} catch (BencodingException e) {
			System.err.println("Error ao ler o bencode");
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Manager  Peers Interest and Not Interest
	public List<Integer> getListInterestPeers(){ return this.listInterestPeer;}
	public List<Peer> getListInterestPeersWithPeers(){ 
		return this.listInterestPeer.stream().map(listPeers::get).toList();
	}
	public void addInterestPeer(Peer peer) {	
		this.listInterestPeer.add(  this.listPeers.indexOf(peer) );
	}
	public void removeInterestPeer(Peer peer) {
		this.listInterestPeer.remove(  this.listPeers.indexOf(peer) );
	}
	
	public void addQueue(Peer peer ,MsgPiece msg) {	
		this.queuePeerRecievePieces.add(new SimpleEntry<Peer, MsgPiece>(peer, msg));
	}
	
	public void addQueue(Peer peer, MsgRequest msg) {	
		this.queuePeerRequestPieces.add(new SimpleEntry<Peer, MsgRequest>(peer, msg));
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
