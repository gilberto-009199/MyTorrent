package org.voyager.torrent.client.peers;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;


import org.voyager.torrent.client.exceptions.HandShakeInvalidException;
import org.voyager.torrent.client.exceptions.NoReaderBufferException;
import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.messages.MsgPiece;
import org.voyager.torrent.client.messages.MsgRequest;
import org.voyager.torrent.util.BinaryUtil;

public class PeerNonBlock implements Comparable<PeerNonBlock>{

	// code msg 
	public static final byte 	ID_MsgChoke			= 0;
	public static final byte 	ID_MsgUnchoke			= 1;
	public static final byte 	ID_MsgInterested		= 2;
	public static final byte 	ID_MsgNotInterested	= 3;
	public static final byte 	ID_MsgHave				= 4;
	public static final byte 	ID_MsgBitfield			= 5;
	public static final byte 	ID_MsgRequest			= 6;
	public static final byte 	ID_MsgPiece			= 7;
	public static final byte 	ID_MsgCancel			= 8;
	public static final byte[]	Protocol			= BinaryUtil.stringToByteBuffer("BitTorrent protocol").array();

	// Info 
	private String host;
	private int port;
	private byte[] peerId;
	private ManagerPeer client;
	private byte[] infoHash;

	

	//	Data 
	private boolean choked = true;
	// Data Connection
	public boolean isConnected = false; // CONNECT SOCKET
	public boolean hasHandshake = false; // Connect Socket and shake Hands 
	private SocketChannel socketChannel = null;
	
	//	Data Pieces
	private PiecesMap piecesMap;

	// Data for rank peers
	private int countMsgPieces = 0;
	private int countMsgRequest = 0;
	private int countMsgInterest = 0;
	private int countMsgNotInterest = 0;
	private int countMsgChoke = 0;
	private int countMsgUnChoke = 0;

	private boolean verbouse;

	public PeerNonBlock() {}
	public PeerNonBlock(String host, int port, byte[] peerId, ManagerPeer client) {
		this.host = host;
		this.port = port;
		this.peerId = peerId;
		this.client = client;
		this.infoHash = client.getTorrent().getInfoHash();
		this.piecesMap = new PiecesMap(client.getTorrent());
	}

	public void writeMsg(MsgPiece msg) throws IOException { writeMsg(socketChannel, msg); }
	public void writeMsg(SocketChannel channel, MsgPiece msg) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(msg.toPacket());
		channel.write(buffer);
		//buffer.flip();
		buffer.clear();
	}

	public void writeMsg(MsgRequest msg) throws IOException { this.writeMsg(this.socketChannel, msg);	}
	public void writeMsg(SocketChannel channel, MsgRequest msg) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(msg.toPacket());
		channel.write(buffer);
		//buffer.flip();
		buffer.clear();
	}

	public void writeShake() throws IOException { this.writeShake(this.socketChannel);	}
	public void writeShake(SocketChannel channel) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(genHandshake());
		channel.write(buffer);
		//buffer.flip();
		buffer.clear();
	}

	public void readShake(SocketChannel channel) throws IOException{
		ByteBuffer buffer = ByteBuffer.allocate(68);

        int bytesRead = channel.read(buffer);
        if (bytesRead == -1) {
            throw new ConnectException("fechar coneção");
        }else if(bytesRead == 0){
			// @todo remover isso, quando encotnrar a jornada correta
			System.out.println("n ha dados para ler");
			throw new NoReaderBufferException("nao ha dados para ler");
		}

        buffer.flip();

		if (buffer.remaining() != 68 ) {
            throw new HandShakeInvalidException("Inferior a 68 handshake");
        }

		this.hasHandshake = checkHandshake(buffer.array());
    	
		if(!this.hasHandshake)throw new HandShakeInvalidException("Handshake Invalid");
	}

	public void readMsg(SocketChannel channel) throws IOException {

        ByteBuffer lengthBuffer = ByteBuffer.allocate(4);
		
        int bytesRead = channel.read(lengthBuffer);
        if (bytesRead == -1) {
            throw new ConnectException("fechar coneção");
        }else if(bytesRead < 4){
			// @todo remover isso, quando encotnrar a jornada correta
			System.out.println("sem length");
			throw new NoReaderBufferException("Sem Length no packet");
		}

		lengthBuffer.flip();
        int length = lengthBuffer.getInt();

        if (length == 0) {
            System.out.println("menssagem de Keep a Live");
            return;
        }

		ByteBuffer messageBuffer = ByteBuffer.allocate(length);
		
		bytesRead = channel.read(messageBuffer);

		if (bytesRead == -1) {
			System.out.println("Connection closed by peer while reading message.");
			channel.close();
			return;
		}

		// Garante que a mensagem foi lida completamente
		while (messageBuffer.hasRemaining()) {
			bytesRead = channel.read(messageBuffer);
			if (bytesRead == -1) {
				System.out.println("Connection closed by peer while reading message.");
				channel.close();
				return;
			}
		}

		messageBuffer.flip();

		byte[] buff = new byte[messageBuffer.remaining()];
		messageBuffer.get(buff);

		byte state = buff[0];

		switch (state) {
			case ID_MsgChoke:
				System.out.println("Received MsgChoke: Peer is now choked.");
				countMsgChoke++;
				
				setChoked(true);
				
				break;
			case ID_MsgUnchoke:
				System.out.println("Received MsgUnchoke: Peer is now unchoked.");
				countMsgUnChoke++;

				setChoked(false);
				
				break;
			case ID_MsgInterested:
				System.out.println("Received MsgInterested: Peer is interested.");
				countMsgInterest++;
				break;
			case ID_MsgNotInterested:
				System.out.println("Received MsgNotInterested: Peer is not interested.");
				countMsgNotInterest++;
				break;
			case ID_MsgHave:
				System.out.println("Received MsgHave: Peer has new piece.");

				break;
			case ID_MsgBitfield:
				System.out.println("Received MsgBitfield: Peer sent bitfield.");
				
				this.piecesMap = new PiecesMap(buff, buff.length);

				break;
			case ID_MsgRequest:
				System.out.println("Received MsgRequest: Peer requested a piece.");
				countMsgRequest++;
				
				processMsgRequest(buff);

				break;
			case ID_MsgPiece:
				System.out.println("Received MsgPiece: Received a piece of data.");
				countMsgPieces++;

				processMsgPiece(buff);

				break;
			case ID_MsgCancel:
				System.out.println("Received MsgCancel: Peer cancelled a request.");

				break;
			default:
				System.out.printf("Unknown message state: %d%n", (int) state);
				System.out.write(buff, 0, Math.min(buff.length, 100));
				System.out.println("\nFinal unknown message.");
		}

	}

	public void processMsgRequest(byte[] buff){
		MsgRequest msg = new MsgRequest(buff);
		this.client.queueNewMsg(this, msg);
	}

	public void processMsgPiece(byte[] buff){
		MsgPiece msg = new MsgPiece(buff);
		this.client.queueNewMsg(this, msg);
	}

	public byte[] genHandshake() {	return PeerNonBlock.genHandshake(peerId, infoHash); }
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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (! (obj instanceof PeerNonBlock))return false;

        PeerNonBlock peer = (PeerNonBlock) obj;
        return port == peer.port && host.equals(peer.host);
    }

    @Override
    public int hashCode() {
        int result = host.hashCode();
        result = 31 * result + port;
        return result;
    }

	public PiecesMap getPiecesMap(){ return this.piecesMap; }
	public void setPiecesMap(PiecesMap piecesMap){ this.piecesMap = piecesMap; }
	public PeerNonBlock withPiecesMap(PiecesMap piecesMap){
		this.piecesMap = piecesMap;
		return this;
	}

	public boolean hasChoked() { return this.choked;}
	private void setChoked(boolean choked) { this.choked = choked; }
	
	public ManagerPeer getManagerPeer() { return client; }
	public void setManagerPeer(ManagerPeer client) { this.client = client;	}
	public PeerNonBlock withManagerPeer(ManagerPeer client){
		this.client = client;
		return this;
	}

	public String getHost() { return host; }
	public void setHost(String host) { this.host = host;}
	public PeerNonBlock withHost(String host){
		this.host = host;
		return this;
	}

	public int getPort() { return port; }
	public void setPort(int port) { this.port = port; }
	public PeerNonBlock withPort(int port){
		this.port = port;
		return this;
	}

	public byte[] getPeerId() { return peerId; }
	public void setPeerId(byte[] peerId) { this.peerId = peerId; }
	public PeerNonBlock withPeerId(byte[] peerId){
		this.peerId = peerId;
		return this;
	}
	
	public byte[] getInfoHash() { return infoHash; }
	public void setInfoHash(byte[] infoHash) { this.infoHash = infoHash; }
	public PeerNonBlock withInfoHash(byte[] infoHash){
		this.infoHash = infoHash;
		return this;
	}

	public SocketChannel getSocketChannel() { return socketChannel;	}
	public void setSocketChannel(SocketChannel socketChannel) { this.socketChannel = socketChannel;}

	public boolean hasHandshake() { return this.hasHandshake; }
	public void setHandshake(boolean hasHandshake) { this.hasHandshake = hasHandshake; }

	public boolean isConnected() { return this.isConnected; }
	public void setConneted(boolean isConnected) { this.isConnected = isConnected; }

	public String toString() {
		return ("host: "+this.host+"\t port: "+this.port+"\t connect: "+this.isConnected+"\t hasHandshake: "+ this.hasHandshake + "\t Map: "+ this.piecesMap);
	}

	@Override
	public int compareTo(PeerNonBlock peer) {

		if( !this.isConnected && peer.isConnected )return -1;

		if( this.isConnected  && !peer.isConnected )return 1;

		if( this.choked && !peer.choked )return -1;

		if( !this.choked && peer.choked )return 1;

		if( this.piecesMap == null && peer.piecesMap != null )return -1;

		if( this.piecesMap != null && peer.piecesMap == null )return 1;
		
		
		int sum1 =	(-1 * this.countMsgChoke) 		+ 
					( 1 * this.countMsgUnChoke) 	+ 
					( 3 * this.countMsgInterest) 	+ 
					(-3 * this.countMsgNotInterest) + 
					( 2 * this.countMsgPieces) 		+
					( 1 * this.countMsgRequest);

		int sum2 =	(-1 * peer.countMsgChoke) 		+ 
					( 1 * peer.countMsgUnChoke) 	+ 
					( 3 * peer.countMsgInterest) 	+ 
					(-3 * peer.countMsgNotInterest) + 
					( 2 * peer.countMsgPieces) 		+
					( 1 * peer.countMsgRequest);

		return Integer.compare(sum1, sum2);
	}

}
