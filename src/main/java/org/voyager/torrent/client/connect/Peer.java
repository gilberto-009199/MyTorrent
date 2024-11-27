package org.voyager.torrent.client.connect;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Arrays;

import org.voyager.torrent.util.BinaryUtil;

public class Peer implements Runnable{

	// Info 
	private String host;
	private int port;
	private byte[] peer_id;
	private ManagerPeer client;
	private byte[] infoHash;

	// Codigo de menssagem
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
	
	// state
	private Socket socket = null;
	private InputStream in;
	private OutputStream out;
	// map updated in MsgBitfield for state peer
	private byte[] piecesInRemote;
	private boolean verbouse;
	public boolean isConnected = false; // CONNECT SOCKET
	public boolean hasHandshake = false; // Connect Socket and shake Hands 
	
	
	public Peer(String host, int port, byte[] peer_id, ManagerPeer client) {
		this.host = host;
		this.port = port;
		this.peer_id = peer_id;
		this.client = client;
		this.infoHash = client.getTorrent().info_hash.array();
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
								if(verbouse)System.out.println(Arrays.toString(buff));
								// comportamento de receber
								
								break;
							case MsgUnchoke:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgUnchoke");
								if(verbouse)System.out.println("\t MsgUnchoke:");
								if(verbouse)System.out.println("\t  Informa ao peer que ele foi desbloqueado. Isso significa que o peer poderá solicitar peças do arquivo.");
								if(verbouse)System.out.println(Arrays.toString(buff));
								// comportamento de receber
								
								break;
							case MsgInterested:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgInterested");
								if(verbouse)System.out.println("\t MsgInterested:");
								if(verbouse)System.out.println("\t  Indica que o peer está interessado em receber peças do arquivo. Geralmente enviado quando o peer detecta que outro possui peças que ele não tem.");
								if(verbouse)System.out.println(Arrays.toString(buff));
								// comportamento de receber
								
								break;
							case MsgNotInterested:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgNotInterested");
								if(verbouse)System.out.println("\t MsgNotInterested:");
								if(verbouse)System.out.println("\t  Indica que o peer não está interessado em receber peças do arquivo. Geralmente enviado quando o peer já possui todas as peças que o outro peer oferece.");
								if(verbouse)System.out.println(Arrays.toString(buff));
								// comportamento de receber
								
								break;
							case MsgHave:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgHave");
								if(verbouse)System.out.println("\t MsgHave:");
								if(verbouse)System.out.println("\t  Notifica ao peer que uma nova peça do arquivo foi baixada e está disponível. Essa mensagem ajuda a manter os peers atualizados sobre as peças disponíveis.");
								if(verbouse)System.out.println(Arrays.toString(buff));
								// comportamento de receber
								
								break;
							case MsgBitfield:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgBitfield");
								if(verbouse)System.out.println("\t MsgBitfield:");
								if(verbouse)System.out.println("\t  Envia um mapa de bits que representa todas as peças que o peer possui. É usado logo no início da conexão para informar o estado atual do peer.");
								if(verbouse)System.out.println(Arrays.toString(buff));
								// comportamento de receber
								// -1 iqual a ter 0 igual a não ter uma peça
								
								break;
							case MsgRequest:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgRequest");
								if(verbouse)System.out.println("\t MsgRequest:");
								if(verbouse)System.out.println("\t  Solicita uma peça específica do arquivo. Contém informações como o índice da peça e o deslocamento dentro dela.");
								if(verbouse)System.out.println(Arrays.toString(buff));
								// comportamento de receber
								
								break;
							case MsgPiece:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgPiece");
								if(verbouse)System.out.println("\t MsgPiece:");
								if(verbouse)System.out.println("\t  Transfere um pedaço de uma peça solicitada para o peer que fez o pedido. Contém os dados reais do arquivo.");
								if(verbouse)System.out.println(Arrays.toString(buff));
								// comportamento de receber
								
								break;
							case MsgCancel:
								if(verbouse)System.out.println(this+"\n:\tRecive MsgCancel");
								if(verbouse)System.out.println("\t MsgCancel:");
								if(verbouse)System.out.println("\t  Cancela um pedido anterior de uma peça. Pode ser usado se o pedido não for mais necessário ou se houver um problema na conexão.");
								if(verbouse)System.out.println(Arrays.toString(buff));
								// comportamento de receber
								
								break;
							default:
								if(verbouse){
									System.out.println("DEFAULT result: ");
									System.out.println(Arrays.toString(buff));
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

				
				
			}
			
		}
	}
	
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
	
	public byte[] genHandshake() {	return Peer.genHandshake(peer_id, infoHash); }

	/*
	 * 
	 * Handshake:  
	 * <Identifilter Protocol><Protocol><Extensions Protocol><info_hash><Peer ID>
	 * 
	 */
	public static byte[] genHandshake(byte[] peer_id, byte[] infohash) {
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
		System.arraycopy(peer_id, 0, handshake, index, peer_id.length);
		
		return handshake;
	}
	
	public boolean checkHandshake(byte[] response) { return checkHandshake(this.infoHash, response); }
	
	public static boolean checkHandshake(byte[] infoHash, byte[] response) {
		//<info_hash> for response
		byte[] peerHash = new byte[20];
		System.arraycopy(response, 28, peerHash, 0, 20);
		// <info_hash local> == <info_hash remoto>
		if(!Arrays.equals(peerHash, infoHash))
		{
			return false;
		}
			
		return true;
	}

	
	
	public ManagerPeer getTorrent_info() { return client; }

	public void setTorrent_info(ManagerPeer client) {
		this.client = client;
	}

	public String getHost() { return host; }

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() { return port; }

	public void setPort(int port) {
		this.port = port;
	}

	public byte[] getPeer_id() { return peer_id; }

	public void setPeer_id(byte[] peer_id) {
		this.peer_id = peer_id;
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
	
	public void setPiecesInRemote(byte[] piecesInRemote){
		this.piecesInRemote = piecesInRemote;
	}

	public byte[] getPiecesInRemote(){return this.piecesInRemote;}

	public void setVerbouse(boolean verbouse){
		this.verbouse = verbouse;
	}
	public boolean getVerbouse(){ return this.verbouse; }

	public String toString() {
		return ("host: "+this.host+"\t port: "+this.port+"\t connect: "+this.isConnected+"\t hasHandshake: "+ this.hasHandshake );
	}
}
