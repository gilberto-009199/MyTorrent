package org.voyager.torrent.client.peers;

import java.io.IOException;
import java.net.ConnectException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Arrays;


import org.voyager.torrent.client.enums.ClientTorrentType;
import org.voyager.torrent.client.exceptions.HandShakeInvalidException;
import org.voyager.torrent.client.exceptions.NoReaderBufferException;
import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.messages.*;
import org.voyager.torrent.util.BinaryUtil;

public class PeerNonBlock implements Comparable<PeerNonBlock>{

	// Info
	private ClientTorrentType clientType;
	private byte[] peerIdRemote;
	private ManagerPeer client;
	private byte[] infoHash;
	private byte[] peerId;
	private String host;
	private int port;

	// Data
	private boolean choked = true;
	// Data Connection
	public boolean isConnected = false; // CONNECT SOCKET
	public boolean hasHandshake = false; // Connect Socket and shake Hands 
	private SocketChannel socketChannel = null;

	//	Data Pieces parts
	private PiecesMap piecesMap;

	// Data metrics peer
	private int countMsgPieces		= 0;
	private int countMsgRequest		= 0;
	private int countMsgInterest	= 0;
	private int countMsgNotInterest	= 0;
	private int countMsgChoke		= 0;
	private int countMsgUnChoke		= 0;

	private boolean verbouse = true;

	public PeerNonBlock() {}
	public PeerNonBlock(String host, int port, byte[] peerId, ManagerPeer client) {
		this.host = host;
		this.port = port;
		this.peerId = peerId;
		this.client = client;
		this.infoHash = client.getTorrent().getInfoHash();
		this.piecesMap = new PiecesMap(client.getTorrent());
	}

	// MsgPiece
	public void writeMsg(MsgPiece msg) throws IOException { writeMsg(socketChannel, msg); }
	public void writeMsg(SocketChannel channel, MsgPiece msg) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(msg.toPacket());
		channel.write(buffer);
		//buffer.flip();
		buffer.clear();
	}

	// MsgRequest
	public void writeMsg(MsgRequest msg) throws IOException { this.writeMsg(this.socketChannel, msg);	}
	public void writeMsg(SocketChannel channel, MsgRequest msg) throws IOException {
		ByteBuffer buffer = ByteBuffer.wrap(msg.toPacket());
		channel.write(buffer);
		//buffer.flip();
		buffer.clear();
	}

	// MsgHandShake
	public void writeShake() throws IOException { this.writeShake(this.socketChannel);	}
	public void writeShake(SocketChannel channel) throws IOException {
		MsgHandShake msg = new MsgHandShake(this.infoHash, this.peerId);
		ByteBuffer buffer = ByteBuffer.wrap(msg.toPacket());
		channel.write(buffer);
		// peerIdRemote
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

		MsgHandShake msg = new MsgHandShake(buffer.array());
		this.peerIdRemote = msg.getPeerId();
		this.clientType = msg.getClientType();

		this.hasHandshake = MsgHandShake.checkHandShake(msg, this.infoHash);
    	
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
			case MsgChoke.ID:
				if(verbouse)System.out.println("  Receive MsgChoke "+ this);
				if(verbouse)System.out.println("\t  Informa ao peer que ele foi bloqueado. Isso significa que o peer não poderá solicitar peças do arquivo.");
				if(verbouse)System.out.println("\t Peer Paralizado");

				countMsgChoke++;
				setChoked(true);
				break;
			case MsgUnChoke.ID:
				if(verbouse)System.out.println("  Receive MsgUnchoke "+ this);
				if(verbouse)System.out.println("\t  Informa ao peer que ele foi desbloqueado. Isso significa que o peer poderá solicitar peças do arquivo.");
				if(verbouse)System.out.println("\t Peer DesParalizado");
				countMsgUnChoke++;
				setChoked(false);
				break;
			case MsgInterested.ID:
				if(verbouse)System.out.println("  Receive MsgInterested "+ this);
				if(verbouse)System.out.println("\t  Indica que o peer está interessado em receber peças do arquivo. Geralmente enviado quando o peer detecta que outro possui peças que ele não tem.");
				if(verbouse)System.out.println("\t Peer esta interresado");
				countMsgInterest++;
				break;
			case MsgNotInterested.ID:
				if(verbouse)System.out.println("  Receive MsgNotInterested "+ this);
				if(verbouse)System.out.println("\t  Indica que o peer não está interessado em receber peças do arquivo. Geralmente enviado quando o peer já possui todas as peças que o outro peer oferece.");
				if(verbouse)System.out.println("\t Peer não esta interresado");
				countMsgNotInterest++;
				break;
			case MsgHave.ID:
				if(verbouse)System.out.println("  Receive MsgHave "+ this);
				if(verbouse)System.out.println("\t  Notifica ao peer que uma nova peça do arquivo foi baixada e está disponível. Essa mensagem ajuda a manter os peers atualizados sobre as peças disponíveis.");
				break;
			case MsgBitfield.ID:
				if(verbouse)System.out.println("  Receive MsgBitfield "+ this);
				if(verbouse)System.out.println("\t  Envia um mapa de bits que representa todas as peças que o peer possui. É usado logo no início da conexão para informar o estado atual do peer.");
				if(verbouse)System.out.println("\t Mapa Pices:"+ new PiecesMap(buff, piecesMap.getSizePiece()));

				this.piecesMap = new PiecesMap(buff, buff.length);
				break;
			case MsgRequest.ID:
				if(verbouse)System.out.println("  Receive MsgRequest "+ this);
				if(verbouse)System.out.println("\t  Solicita uma peça específica do arquivo. Contém informações como o índice da peça e o deslocamento dentro dela.");

				countMsgRequest++;
				processMsgRequest(buff);
				break;
			case MsgPiece.ID:
				if(verbouse)System.out.println("  Receive MsgPiece "+ this);
				if(verbouse)System.out.println("\t  Transfere um pedaço de uma peça solicitada para o peer que fez o pedido. Contém os dados reais do arquivo.");

				countMsgPieces++;
				processMsgPiece(buff);
				break;
			case MsgCancel.ID:
				if(verbouse)System.out.println("  Receive MsgCancel "+ this);
				if(verbouse)System.out.println("\t  Cancela um pedido anterior de uma peça. Pode ser usado se o pedido não for mais necessário ou se houver um problema na conexão.");

				break;
			case MsgPort.ID:
				if(verbouse)System.out.println("  Receive MsgPort"+ this);
				if(verbouse)System.out.println("\t DHT, tabela distribuida de pares");

				break;
			default:
				System.out.printf("### Error message state: %d%n ###", (int) state);
				System.out.write(buff, 0, Math.min(buff.length, 100));
				System.out.println("\n### Final unknown message. ###");
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

	public String toString() {
		return "PeerNonBlock[host: "+this.host+", port: "+this.port+", connect: "+this.isConnected+", hasHandshake: "+ this.hasHandshake + ", clientType: "+ this.clientType +", Map: "+ this.piecesMap+"]";
	}
}
