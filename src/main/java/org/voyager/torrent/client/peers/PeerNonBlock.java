package org.voyager.torrent.client.peers;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.ConnectException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.voyager.torrent.client.enums.ClientTorrentType;
import org.voyager.torrent.client.exceptions.HandShakeInvalidException;
import org.voyager.torrent.client.exceptions.NoReaderBufferException;
import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.limits.PeerLimit;
import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.messages.*;
import org.voyager.torrent.client.metrics.PeerMetrics;
import org.voyager.torrent.client.network.Network;

public class PeerNonBlock implements Comparable<PeerNonBlock>{

	// Config
	private ClientTorrentType clientType;
	private boolean verbouse = true;
	// @todo colocar remote<Variavel> e local<Variavel>
	private byte[] peerIdRemote;
	private ManagerPeer manager;
	private byte[] infoHash;
	private byte[] peerId;
	private String host;
	private int port;

	// Data States
	public boolean hasHandshake = false;
	public boolean isConnected = false;
	private boolean choked = true;
	private PiecesMap piecesMap;

	// Data Limit and metrics
	private PeerLimit limits;
	private PeerMetrics metrics;

	private Network network;

	// Data Queue Msg for Writer
	private final Queue<Msg> queueMsg;

	public PeerNonBlock() {
		this.metrics = new PeerMetrics();
		this.queueMsg = new ConcurrentLinkedQueue<Msg>();
		this.limits	= new PeerLimit(20, 16 * 1024);
	}

	public PeerNonBlock(String host, int port, byte[] peerId, ManagerPeer client) {
		this();
		this.host = host;
		this.port = port;
		this.peerId = peerId;
		this.manager = client;
		this.infoHash = client.getTorrent().getInfoHash();
		this.piecesMap = new PiecesMap(client.getTorrent());
	}

	// Queue Msg Writer
	public void queueNewMsgIfNotExist(Msg msg) { if(!queueMsg.contains(msg))queueNewMsg(msg); }
	public void queueNewMsg(Msg msg) {
		queueMsg.add(msg);
	}
	public void processQueueNewMsg() {
		for(Msg msg: queueMsg){
			try{

				boolean sendThenRemoveQueue = writeMsg(msg);
				if(sendThenRemoveQueue)queueMsg.remove(msg);

			}catch (Exception e){
				System.err.println(this + ":" + msg + ": Erro ao processar mensagem: " + e.getMessage());
				e.printStackTrace();
			}
		}
	}

	public void readMsg(byte id, ByteBuffer contentMessage){

		metrics.msgMetrics.countMsg++;

		switch (id) {
			case MsgHave.ID:			processMsgHave();						break;
			case MsgPort.ID: 			processMsgPort();						break;
			case MsgChoke.ID:			processMsgChoke();						break;
			case MsgCancel.ID: 			processMsgCancel();						break;
			case MsgUnChoke.ID:			processMsgUnChoke();					break;
			case MsgInterested.ID: 		processMsgInterest();					break;
			case MsgNotInterested.ID: 	processMsgNotInterest();				break;
			case MsgPiece.ID: 			processMsgPiece(contentMessage);		break;
			case MsgRequest.ID: 		processMsgRequest(contentMessage);		break;
			case MsgBitfield.ID: 		processMsgBitfield(contentMessage);		break;

			default:
				System.out.printf("### Error message state: %d%n ###", (int) id);
				System.out.write(contentMessage.array(), 0, Math.min(contentMessage.capacity(), 100));
				System.out.println("\n### Final unknown message. ###");
		}
	}

	// MsgHandShake
	private void processMsgHandShake(ByteBuffer contentMessage) {
		metrics.msgMetrics.countMsgHandShake++;
		metrics.bandWidthMetrics.addDownloaderBytes(contentMessage.capacity());
		if(verbouse)System.out.println("  Receive MsgHandShake "+ this);
		if(verbouse)System.out.println("\t  Os parametros da comunicação.");

		// handshake etc etc
		// @todo add strategy initial connected
		PiecesMap currentMap = this.manager.getManagerFile().getMap();
		queueNewMsg(new MsgBitfield(currentMap));
	}

	// MsgChoke
	private void processMsgChoke() {
		metrics.msgMetrics.countMsgChoke++;
		metrics.bandWidthMetrics.addDownloaderBytes(1);

		if(verbouse)System.out.println("  Receive MsgChoke "+ this);
		if(verbouse)System.out.println("\t  Informa ao peer que ele foi bloqueado. Isso significa que o peer não poderá solicitar peças do arquivo.");
		if(verbouse)System.out.println("\t Peer Paralizado");

		setChoked(true);
		// @todo add strategy chocked
		//queueNewMsg(new MsgUnChoke());
	}

	// MsgUnChoke
	private void processMsgUnChoke() {
		metrics.msgMetrics.countMsgUnChoke++;
		metrics.bandWidthMetrics.addDownloaderBytes(1);

		if(verbouse)System.out.println("  Receive MsgUnchoke "+ this);
		if(verbouse)System.out.println("\t  Informa ao peer que ele foi desbloqueado. Isso significa que o peer poderá solicitar peças do arquivo.");
		if(verbouse)System.out.println("\t Peer DesParalizado");

		setChoked(false);
		// @todo add strategy chocked
		//queueNewMsg(new MsgUnChoke());
	}

	// MsgPort
	private void processMsgPort() {
		metrics.msgMetrics.countMsgPort++;
		metrics.bandWidthMetrics.addDownloaderBytes(1);

		if(verbouse)System.out.println("  Receive MsgPort"+ this);
		if(verbouse)System.out.println("\t DHT, tabela distribuida de pares");

	}

	// MsgHave
	private void processMsgHave() {
		metrics.msgMetrics.countMsgHave++;
		metrics.bandWidthMetrics.addDownloaderBytes(1);

		if(verbouse)System.out.println("  Receive MsgHave "+ this);
		if(verbouse)System.out.println("\t  Notifica ao peer que uma nova peça do arquivo foi baixada e está disponível. Essa mensagem ajuda a manter os peers atualizados sobre as peças disponíveis.");

	}

	// MsgNotInterest
	private void processMsgNotInterest() {
		metrics.msgMetrics.countMsgNotInterest++;
		metrics.bandWidthMetrics.addDownloaderBytes(1);

		if(verbouse)System.out.println("  Receive MsgNotInterested "+ this);
		if(verbouse)System.out.println("\t  Indica que o peer não está interessado em receber peças do arquivo. Geralmente enviado quando o peer já possui todas as peças que o outro peer oferece.");
		if(verbouse)System.out.println("\t Peer não esta interresado");

	}

	// MsgInterest
	private void processMsgInterest() {
		metrics.msgMetrics.countMsgInterest++;
		metrics.bandWidthMetrics.addDownloaderBytes(1);

		if(verbouse)System.out.println("  Receive MsgInterested "+ this);
		if(verbouse)System.out.println("\t  Indica que o peer está interessado em receber peças do arquivo. Geralmente enviado quando o peer detecta que outro possui peças que ele não tem.");
		if(verbouse)System.out.println("\t Peer esta interresado");


	}

	// MsgCancel
	private void processMsgCancel() {
		metrics.msgMetrics.countMsgCancel++;
		metrics.bandWidthMetrics.addDownloaderBytes(1);

		if(verbouse)System.out.println("  Receive MsgCancel "+ this);
		if(verbouse)System.out.println("\t  Cancela um pedido anterior de uma peça. Pode ser usado se o pedido não for mais necessário ou se houver um problema na conexão.");


	}

	// MsgBitfield
	private void processMsgBitfield(ByteBuffer contentMessage) { processMsgBitfield(contentMessage.array());}
	private void processMsgBitfield(byte[] buff) {
		metrics.msgMetrics.countMsgBitfield++;
		metrics.bandWidthMetrics.addDownloaderBytes(buff.length);

		if(verbouse)System.out.println("  Receive MsgBitfield "+ this);
		if(verbouse)System.out.println("\t  Envia um mapa de bits que representa todas as peças que o peer possui. É usado logo no início da conexão para informar o estado atual do peer.");

		this.piecesMap = new PiecesMap(buff, buff.length);
	}

	// MsgRequest
	public void processMsgRequest(ByteBuffer contentMessage){	processMsgRequest(contentMessage.array());}
	public void processMsgRequest(byte[] buff){
		metrics.msgMetrics.countMsgRequest++;
		metrics.bandWidthMetrics.addDownloaderBytes(buff.length);

		if(verbouse)System.out.println("  Receive MsgRequest "+ this);
		if(verbouse)System.out.println("\t  Solicita uma peça específica do arquivo. Contém informações como o índice da peça e o deslocamento dentro dela.");

		MsgRequest msg = new MsgRequest(buff);
		this.manager.queueNewMsg(this, msg);
	}

	// MsgPiece
	public void processMsgPiece(ByteBuffer contentMessage){	processMsgPiece(contentMessage.array()); }
	public void processMsgPiece(byte[] buff){
		metrics.msgMetrics.countMsgPieces++;
		metrics.bandWidthMetrics.addDownloaderBytes(buff.length);

		if(verbouse)System.out.println("  Receive MsgPiece "+ this);
		if(verbouse)System.out.println("\t  Transfere um pedaço de uma peça solicitada para o peer que fez o pedido. Contém os dados reais do arquivo.");

		MsgPiece msg = new MsgPiece(buff);
		this.manager.queueNewMsg(this, msg);
	}




	// Writers
	// write MsgHandShake
	public void writeShake() throws IOException {
		this.writeMsg(new MsgHandShake(this.infoHash, this.peerId));
	}
	// write generic
	public boolean writeMsg(Msg msg) throws IOException {

		if(!network.isWritable())return false;
		if(!limits.tryConsume(msg))return false;

		try {
			byte[] packet = msg.toPacket();

			// @todo mapear caso de packet.length > network rate byte por second
			if(network.isWritable()){

				System.out.println("SEND: ["+ msg + "] to "+ this);

				network.write(msg);

			} else return false;

		}catch (Exception e){
			//isConnected = false;
			//hasHandshake = false;
			e.printStackTrace();
			return false;
		}
		return true;
	}

	// Readers
	// Reader HandShake
	public void readShake() throws IOException{

		if(!network.isRedable())return;

		ByteBuffer buffer = ByteBuffer.allocate(68);

        int bytesRead = network.read(buffer);
		if (bytesRead == -1) {
			throw new ConnectException("fechar coneção");
		}

		if(bytesRead == 0){
			System.out.println("Keep-Alive");
			//throw new NoReaderBufferException("nao ha dados para ler");
			return;
		}

		if(bytesRead < 68){
			// @todo remover isso, quando encontrar a jornada correta
			System.out.println("No HandShake");
			throw new NoReaderBufferException("No HandShake");
		}

		((Buffer) buffer).flip();

		MsgHandShake msg = new MsgHandShake(buffer.array());

		this.peerIdRemote = msg.getPeerId();
		this.clientType = msg.getClientType();

		this.hasHandshake = MsgHandShake.checkHandShake(msg, this.infoHash);
    	
		if(!this.hasHandshake)throw new HandShakeInvalidException("HandShake Invalid");
		else processMsgHandShake(buffer);
	}
	// reader generic
	public void readMsg() throws IOException {

		if(!network.isRedable())return;
		if(!hasHandshake){
			readShake();
			return;
		}

		// <4 bytes> for length, <1 byte> for ID message
        ByteBuffer metadataBuffer = ByteBuffer.allocate(4);

        int bytesRead = network.read(metadataBuffer);

        if (bytesRead == -1) {
            throw new ConnectException("fechar coneção");
        }else if(bytesRead < 4){
			// @todo remover isso, quando encontrar a jornada correta
			System.out.println("No packet");
			throw new NoReaderBufferException("No packet");
		}

		((Buffer) metadataBuffer).flip();

        int length = metadataBuffer.getInt();
        if (length == 0) {
            System.out.println("Keep a Live");
            return;
		}

		ByteBuffer contentBuffer = ByteBuffer.allocate(length);

		network.readFull(contentBuffer);

		((Buffer) contentBuffer).flip();

		byte id = contentBuffer.get();

		System.out.println("Receive: [len: "+ length +", id: "+ id +"] from "+ this);

		readMsg(id, contentBuffer);
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

	public ManagerPeer getManagerPeer() { return manager; }
	public void setManagerPeer(ManagerPeer client) { this.manager = client;	}
	public PeerNonBlock withManagerPeer(ManagerPeer client){
		this.manager = client;
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

	public Network getNetwork() { return network; }
	public void setNetwork(Network network) { this.network = network; }
	public PeerNonBlock withNetwork(Network network) {
		this.network = network;
		return this;
	}

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

		return this.metrics.compareTo(peer.metrics);
	}

	public String toString() {
		return "PeerNonBlock[host: "+ this.host +
				", port: "+ this.port +
				", connect: "+ this.isConnected +
				", hasHandshake: "+ this.hasHandshake +
				", clientType: "+ this.clientType +
				", Map: "+ this.piecesMap+"]";
	}

}