package org.voyager.torrent.client.connect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import org.voyager.torrent.util.BinaryUtil;

public class Peer {}/*implements Runnable{

	// Info 
	private String host;
	private int port;
	private byte[] peerId;
	private ManagerPeer client;
	private byte[] infoHash;

	// code msg
	public static final byte 	MsgChoke			= 0;
	public static final byte 	MsgUnchoke			= 1;
	public static final byte 	MsgInterested		= 2;
	public static final byte 	MsgNotInterested	= 3;
	public static final byte 	MsgHave				= 4;
	public static final byte 	MsgBitfield			= 5;
	public static final byte 	MsgRequest			= 6;
	public static final byte 	MsgPiece			= 7;
	public static final byte 	MsgCancel			= 8;
	public static final byte[]	Protocol			= BinaryUtil.stringToByteBuffer("BitTorrent protocol").array();
	
	// in & out
	private Socket socket = null;
	private InputStream in;
	private OutputStream out;

	//	state 
	//	status chocked 
	//		if  choked then no send data
	//		if !choked then send data
	private boolean choked = true;

	//	status Pieces
	private PiecesMap piecesMap;
	private boolean verbouse;

	public boolean isConnected = false; // CONNECT SOCKET
	public boolean hasHandshake = false; // Connect Socket and shake Hands 
	
	public Peer() {}
	public Peer(String host, int port, byte[] peerId, ManagerPeer client) {
		this.host = host;
		this.port = port;
		this.peerId = peerId;
		this.client = client;
		this.infoHash = client.getTorrent().getInfoHash();
		this.piecesMap = new PiecesMap(client.getTorrent());
	}

	public void run() {
		
		// open connect verify 
		// @todo try reconnect
		if(!this.isConnected)this.isConnected = connect();
		
		// Handshake  verify 
		// @todo send shakeHands
		if(this.isConnected && !this.hasHandshake)this.hasHandshake = shakeHands();
		
		// verify
		if(!this.hasHandshake || !this.isConnected)this.client.connectError(this);
		else {
			// Cicly Life
			while (this.socket != null && !this.socket.isClosed()) {
				//read length buffer 
				DataInputStream dataInput = new DataInputStream(this.in);

				try {
					

					// size messagem
					// @todo colocar um limite por seguranca
					int length = dataInput.readInt();
					// 0 = keep alive (mantenha vivo)
					if(length == 0)continue;
					else if(length > 0) {
						
						byte[] buff = new byte[length];
						dataInput.read(buff, 0, length);
						
						byte state = buff[0];
						
						switch(state) {
							case MsgChoke:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgChoke");
								if(verbouse)System.out.println("\t MsgChoke:");
								if(verbouse)System.out.println("\t  Informa ao peer que ele foi bloqueado. Isso significa que o peer não poderá solicitar peças do arquivo.");
								if(verbouse)System.out.println("\t Peer Paralizado");

								setChoked(true);

								break;
							case MsgUnchoke:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgUnchoke");
								if(verbouse)System.out.println("\t MsgUnchoke:");
								if(verbouse)System.out.println("\t  Informa ao peer que ele foi desbloqueado. Isso significa que o peer poderá solicitar peças do arquivo.");
								if(verbouse)System.out.println("\t Peer DesParalizado");
								
								setChoked(false);

								break;
							case MsgInterested:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgInterested");
								if(verbouse)System.out.println("\t MsgInterested:");
								if(verbouse)System.out.println("\t  Indica que o peer está interessado em receber peças do arquivo. Geralmente enviado quando o peer detecta que outro possui peças que ele não tem.");
								if(verbouse)System.out.println("\t Peer esta interresado");

								this.client.addInterestPeer(this);
								
								break;
							case MsgNotInterested:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgNotInterested");
								if(verbouse)System.out.println("\t MsgNotInterested:");
								if(verbouse)System.out.println("\t  Indica que o peer não está interessado em receber peças do arquivo. Geralmente enviado quando o peer já possui todas as peças que o outro peer oferece.");
								if(verbouse)System.out.println("\t Peer não esta interresado");

								this.client.removeInterestPeer(this);
								
								break;
							case MsgHave:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgHave");
								if(verbouse)System.out.println("\t MsgHave:");
								if(verbouse)System.out.println("\t  Notifica ao peer que uma nova peça do arquivo foi baixada e está disponível. Essa mensagem ajuda a manter os peers atualizados sobre as peças disponíveis.");
								if(verbouse)System.out.println("\t Tem uma nova peça:" + Arrays.toString(buff));

								// atualizar mapa pices

								break;
							case MsgBitfield:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgBitfield");
								if(verbouse)System.out.println("\t MsgBitfield:");
								if(verbouse)System.out.println("\t  Envia um mapa de bits que representa todas as peças que o peer possui. É usado logo no início da conexão para informar o estado atual do peer.");
								if(verbouse)System.out.println("\t Mapa Pices:"+ new PiecesMap(buff, piecesMap.getSizePiece()));

								piecesMap.setMap(buff);
								
								break;
							case MsgRequest:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgRequest");
								if(verbouse)System.out.println("\t MsgRequest:");
								if(verbouse)System.out.println("\t  Solicita uma peça específica do arquivo. Contém informações como o índice da peça e o deslocamento dentro dela.");
								
								processMsgRequest(buff);

								break;
							case MsgPiece:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgPiece");
								if(verbouse)System.out.println("\t MsgPiece:");
								if(verbouse)System.out.println("\t  Transfere um pedaço de uma peça solicitada para o peer que fez o pedido. Contém os dados reais do arquivo.");

								processMsgPiece(buff);

								break;
							case MsgCancel:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgCancel");
								if(verbouse)System.out.println("\t MsgCancel:");
								if(verbouse)System.out.println("\t  Cancela um pedido anterior de uma peça. Pode ser usado se o pedido não for mais necessário ou se houver um problema na conexão.");
								if(verbouse)System.out.println("\t Cancelando solicitação para a peça: "+ Arrays.toString(buff));

								// @todo Remover da fila de solicitações de peças

								break;
							default:
								if(verbouse){
									System.out.printf("ERROR: MSG no mapper %d :\n\n", (int) state);
									System.out.write(buff, 0, (buff.length > 100 ? 100: buff.length));
									System.out.println("\n\n FINAL MSG no mapper");
								}
						}
						//handshake[index] = 0x13;
						//System.out.println("result: "+ Arrays.toString(buff));
						//is.readFully(response);		
						//this.socket.setSoTimeout(130000);
						
					}
					
					
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return;
				}

				sleep(30);
				
			}
			
		}
	}

	public void processMsgRequest(byte[] packet){
		MsgRequest msg  = new MsgRequest(packet);
		if(verbouse)System.out.println("Request: "+ msg);
		
		client.addQueue(this, msg);
	}

	public void processMsgPiece(byte[] packet){
		MsgPiece msg  = new MsgPiece(packet);
		if(verbouse)System.out.println("Piece: "+ msg);

		client.addQueue(this, msg);

	}

	//	<len=0013><id=6><index><begin><length>
	public void sendMsgRequest(long position, long begin, long length){
		//		@Link:	https://www.bittorrent.org/beps/bep_0003.html
		//				https://wiki.theory.org/BitTorrentSpecification#request:_.3Clen.3D0013.3E.3Cid.3D6.3E.3Cindex.3E.3Cbegin.3E.3Clength.3E
		//		@Desc: As mensagens 'request' contêm um índice, begin e length. Os dois últimos são deslocamentos de bytes.
		//			 Length é geralmente uma potência de dois, a menos que seja truncado pelo fim do arquivo.
		//		@Atenção: requisição maxima e 2^14 (16 kiB), do contrario ele fecha a conexão

		byte[] msgRequest = new byte[13];
		int index = 0;
 
		 // <id=6> (1 byte)
		 msgRequest[index++] = MsgRequest; // ID para "request".
 
		 // <index|position in piece> (4 bytes)
		 msgRequest[index++] = (byte) (position >> 24);
		 msgRequest[index++] = (byte) (position >> 16);
		 msgRequest[index++] = (byte) (position >> 8);
		 msgRequest[index++] = (byte) position;
 
		 // <begin> (4 bytes)
		 msgRequest[index++] = (byte) (begin >> 24);
		 msgRequest[index++] = (byte) (begin >> 16);
		 msgRequest[index++] = (byte) (begin >> 8);
		 msgRequest[index++] = (byte) begin;
 
		 // <length> (4 bytes)
		 msgRequest[index++] = (byte) (length >> 24);
		 msgRequest[index++] = (byte) (length >> 16);
		 msgRequest[index++] = (byte) (length >> 8);
		 msgRequest[index] = (byte) length;

		try{
			// <len=0013> (4 bytes)
			out.write(new byte[]{
				0,
				0,
				0,
				13 // length
			});
			out.write(msgRequest);
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
		};
	}

	public void sendMsgRequest(	MsgRequest request){
		try{
			out.write(request.toPacket());
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
		};
	}

	//	<len=0009+X><id=7><index><begin><block>
	public void sendMsgPiece(long position, long begin, byte[] block){
		//		@Link:	https://wiki.theory.org/BitTorrentSpecification#piece:_.3Clen.3D0009.2BX.3E.3Cid.3D7.3E.3Cindex.3E.3Cbegin.3E.3Cblock.3E
		//		@Desc: A mensagem da peça tem comprimento variável, onde X é o comprimento do bloco. A carga contém as seguintes informações:
		//			+ index: inteiro especificando o índice de peças baseado em zero
		//			+ begin: inteiro especificando o deslocamento de byte baseado em zero dentro da peça
		//			+ block: bloco de dados, que é um subconjunto da peça especificada pelo índice.
		//		@Atenção: requisição maxima e 2^14 (16 kiB), do contrario ele fecha a conexão
		System.out.println("sendMsgPiece[position: "+ position +", begin: "+ begin +", block_length: "+ block.length +"]");

		byte[] msgPiece = new byte[9];
		int index = 0;
 
		 // <id=9> (1 byte)
		 msgPiece[index++] = MsgPiece; // ID para "piece".
 
		 // <index|position in piece> (4 bytes)
		 msgPiece[index++] = (byte) (position >> 24);
		 msgPiece[index++] = (byte) (position >> 16);
		 msgPiece[index++] = (byte) (position >> 8);
		 msgPiece[index++] = (byte) position;
 
		 // <begin> (4 bytes)
		 msgPiece[index++] = (byte) (begin >> 24);
		 msgPiece[index++] = (byte) (begin >> 16);
		 msgPiece[index++] = (byte) (begin >> 8);
		 msgPiece[index++] = (byte) begin;
 

		try{
			// <len=009+BLOCK_SIZE> (4 bytes)
			int length = msgPiece.length + block.length;
			out.write(new byte[]{
				(byte)(length >> 24),
				(byte)(length >> 16),
				(byte)(length >> 8),
				(byte)(length)
			});
			out.write(msgPiece);
			// <block>
			out.write(block);
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
		};
	}

	public void sendMsgPiece(MsgPiece request){
		try{
			out.write(request.toPacket());
			out.flush();
		}catch(Exception e){
			e.printStackTrace();
		};
	}
	// handschake utils func's 
	private boolean shakeHands() {
		
		boolean hasHandshake = false;
		
		try {
			
			DataOutputStream os = new DataOutputStream(this.out);
			DataInputStream is = new DataInputStream(this.in);

			if (is == null || os == null) {
				return false;
			}

			// send handshake 
			os.write(this.genHandshake());
			os.flush();

			// response for peer
			byte[] response = new byte[68];
			
			
			this.socket.setSoTimeout(10000);
			is.readFully(response);		
			this.socket.setSoTimeout(130000);
			
			//System.out.println(Arrays.toString(response));
			if(checkHandshake(response)) {
				hasHandshake = true;
				if(verbouse)System.out.println("Connect & ShakeedHands:\n\t"+ this);
			// no equals then stop thread for peer or wait
			}else hasHandshake = false;
			
		} catch (Exception e) {
			hasHandshake = false;
			this.client.shakeHandsError(this);
		}
		
		return hasHandshake;
	}

	// connect to host:port 
	public boolean connect() {
		
		boolean isConnected = false;
		
		try{
			this.socket = new Socket(this.host, this.port);
			// channel in and out 
			this.in = this.socket.getInputStream();
			this.out = this.socket.getOutputStream();
			isConnected = true;
		}catch (IOException e) {
			isConnected = false;
			this.client.connectError(this);
		}
		
		
		return isConnected;
	}
	
	public byte[] genHandshake() {	return Peer.genHandshake(peerId, infoHash); }

	// <Identifilter Protocol><Protocol><Extensions Protocol><info_hash><Peer ID>
	 
	public static byte[] genHandshake(byte[] peerId, byte[] infohash) {
		int index = 0;
		byte[] handshake = new byte[68];

		// <Identifilter Protocol> 0x13 19	DCN-MEAS	DCN Measurement Subsystems Wikipedia: https://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
		handshake[index] = 0x13;
		index++;
		
		//<Protocol>
		System.arraycopy(Protocol, 0, handshake, index, Protocol.length);
		index += Protocol.length;

		// <Extensions Protocol> 00000000 - no extension
		System.arraycopy(new byte[8], 0, handshake, index, 8);
		index += 8;
		
		//<info_hash>
		System.arraycopy(infohash, 0, handshake, index, infohash.length);
		index += infohash.length;

		//<Peer ID>
		System.arraycopy(peerId, 0, handshake, index, peerId.length);
		
		return handshake;
	}
	
	public boolean checkHandshake(byte[] response) { return checkHandshake(this.infoHash, response); }
	
	public static boolean checkHandshake(byte[] infoHash, byte[] response) {

		// Exibindo o protocolo (os primeiros 20 bytes da resposta)
		int offset = 0;
		byte[] protocol = Arrays.copyOfRange(response, offset, Protocol.length + 1);
		offset += protocol.length;
		System.out.println("\t Protocolo: " + new String(protocol));
	
		// Exibindo as extensões (8 bytes depois do protocolo)
		byte[] extensions = Arrays.copyOfRange(response, offset, offset + 8);
		offset += extensions.length;
		System.out.println("\t Extensões: " + Arrays.toString(extensions));
	
		// Exibindo o info_hash remoto (20 bytes após o campo de extensões)
		byte[] peerHash = Arrays.copyOfRange(response, offset, offset + 20);
		offset += peerHash.length;
		System.out.println("\t Info_hash remoto: " + Arrays.toString(peerHash));
		// <info_hash local> == <info_hash remoto>
		if(!Arrays.equals(peerHash, infoHash))
		{
			return false;
		}

		// Exibindo o Peer ID (que vem após o info_hash) 20 bytes
		byte[] peerId = Arrays.copyOfRange(response, offset, offset + 20);
		offset += peerId.length;
		System.out.println("\t Peer ID: " + new String(peerId));

		return true;
	}

	public PiecesMap getPiecesMap(){ return this.piecesMap; }
	public void setPiecesMap(PiecesMap piecesMap){ this.piecesMap = piecesMap; }
	public Peer withPiecesMap(PiecesMap piecesMap){
		this.piecesMap = piecesMap;
		return this;
	}

	public boolean hasChoked() { return this.choked;}
	private void setChoked(boolean choked) {
		this.choked = choked;
	}
	
	public ManagerPeer getManagerPeer() { return client; }
	public void setManagerPeer(ManagerPeer client) { this.client = client;	}
	public Peer withManagerPeer(ManagerPeer client){
		this.client = client;
		return this;
	}

	public String getHost() { return host; }
	public void setHost(String host) { this.host = host;}
	public Peer withHost(String host){
		this.host = host;
		return this;
	}

	public int getPort() { return port; }
	public void setPort(int port) { this.port = port; }
	public Peer withPort(int port){
		this.port = port;
		return this;
	}

	public byte[] getPeerId() { return peerId; }
	public void setPeerId(byte[] peerId) { this.peerId = peerId; }
	public Peer withPeerId(byte[] peerId){
		this.peerId = peerId;
		return this;
	}

	public Socket getSocket() {	return socket; }
	public void setSocket(Socket socket) {
		this.socket = socket;
	}

	public InputStream getIn() { return in;	}
	public void setIn(InputStream in) {
		this.in = in;
	}

	public OutputStream getOut() { return out;}

	public void setOut(OutputStream out) {
		this.out = out;
	}

	public void setVerbouse(boolean verbouse){
		this.verbouse = verbouse;
	}
	public boolean getVerbouse(){ return this.verbouse; }

	public String toString() {
		return ("host: "+this.host+"\t port: "+this.port+"\t connect: "+this.isConnected+"\t hasHandshake: "+ this.hasHandshake + "\t Map: "+ this.piecesMap);
	}

	private void sleep(long ms){ try{Thread.sleep(ms);}catch (Exception e) {}  }
}*/
