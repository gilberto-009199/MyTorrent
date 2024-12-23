package org.voyager.torrent.client;

import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.managers.BasicManagerAnnounce;
import org.voyager.torrent.client.managers.BasicManagerFile;
import org.voyager.torrent.client.managers.BasicManagerPeer;
import org.voyager.torrent.client.managers.ManagerAnnounce;
import org.voyager.torrent.client.managers.ManagerFile;
import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.files.Torrent;

public class ClientTorrent{ 

	private boolean verbouse;
	private Torrent torrent;

	private int maxUploaderPeerSecond = -1;
	private int maxDownloaderPeerSecond = -1;

	private int timeReAnnounceInSec = 60;
	private int timeVerifyNewsPeersInSec = 60;

	// odeio essa solução mas o quasar e o loom são muito intruzivos
	// fora que o executors e o scheduler são insufientes
	private Semaphore semaphoreExecutor;
	
	private ManagerFile managerFile;
	private Thread thrManagerFile;

	private ManagerAnnounce managerAnnounce;
	private Thread thrManagerAnnounce;

	private ManagerPeer managerPeer;
	private Thread thrManagerPeer;

	// @todo add config for parameters or Builder
	public ClientTorrent(String torrentFile){ torrent = Torrent.of(torrentFile);}
	public ClientTorrent(String torrentFile, boolean verbouse){ 
		this.verbouse = verbouse; 
		torrent = Torrent.of(torrentFile);
	}
	
	// @todo add mode simple, server, consumer, seeding
	public void start() { start(1); }
	public void start(int totalThreads) {
        // Stop any existing setup
        if (semaphoreExecutor != null) stop();

        semaphoreExecutor = new Semaphore(totalThreads, totalThreads > 1);

        // Initialize managers with shared semaphore
        managerAnnounce = new BasicManagerAnnounce(this);
        managerPeer		= new BasicManagerPeer(this);
        managerFile		= new BasicManagerFile(this);

        // Configure dependencies between managers
        managerAnnounce.withSemaphoreExecutor(semaphoreExecutor)
					   .withManagerPeer(managerPeer)
                       .withManagerFile(managerFile)
                       .withTimeReAnnounceInSecond(timeReAnnounceInSec)
                       .withTimeVerifyNewsPeersInSecond(timeVerifyNewsPeersInSec);

        managerPeer.withSemaphoreExecutor(semaphoreExecutor)
				   .withManagerAnnounce(managerAnnounce)
                   .withManagerFile(managerFile)
                   .withMaxUploaderPeerSecond(maxUploaderPeerSecond)
                   .withMaxDownloaderPeerSecond(maxDownloaderPeerSecond);

        managerFile.withSemaphoreExecutor(semaphoreExecutor)
				   .withManagerPeer(managerPeer)
                   .withManagerAnnounce(managerAnnounce);

        // Start the threads
        resume();
    }

    // Resume or start the threads
    public void resume() {
        // Ensure threads are not already running
        if (thrManagerAnnounce == null || !thrManagerAnnounce.isAlive()) {
            thrManagerAnnounce = new Thread(managerAnnounce, "ManagerAnnounceThread");
            thrManagerAnnounce.start();
        }

		if (thrManagerFile == null || !thrManagerFile.isAlive()) {
			thrManagerFile = new Thread(managerFile, "ManagerFileThread");
			thrManagerFile.start();
		}

        if (thrManagerPeer == null || !thrManagerPeer.isAlive()) {
            thrManagerPeer = new Thread(managerPeer, "ManagerPeerThread");
            thrManagerPeer.start();
        }


    }

    // Stop all threads and reset the semaphore
    public void stop() {
        if (semaphoreExecutor == null) return;

        // Interrupt and join threads safely
        if (thrManagerAnnounce != null && thrManagerAnnounce.isAlive()) {
            thrManagerAnnounce.interrupt();
            try {
                thrManagerAnnounce.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (thrManagerPeer != null && thrManagerPeer.isAlive()) {
            thrManagerPeer.interrupt();
            try {
                thrManagerPeer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

		if (thrManagerFile != null && thrManagerFile.isAlive()) {
			thrManagerFile.interrupt();
			try {
				thrManagerFile.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

        semaphoreExecutor = null;
    }

    public Torrent getTorrent() { return this.torrent;  }
    public ManagerPeer getManagerPeer() { return this.managerPeer;  }
	public ManagerAnnounce getManagerAnnounce() { return this.managerAnnounce; }
	public ManagerFile getManagerFile() { return this.managerFile; }


	/*public void start() {

		
		
		/*
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
	private void sleep(long ms){ try{Thread.sleep(ms);}catch (Exception e) {}  }*/
	/*private void initPeers(int count) {
				
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
	}*/
/*
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
			
			torrent.info_hash.array();
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
*/

}
