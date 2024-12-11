package org.voyager.torrent.client.connect;

import java.nio.channels.SocketChannel;
import java.util.Comparator;

import org.voyager.torrent.util.BinaryUtil;

public class PeerNonBlock implements Comparator<PeerNonBlock>{
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
	private SocketChannel socket = null;
	
	//	state 
	//	status chocked 
	//		if  choked then no send data
	//		if !choked then send data
	private boolean choked = true;
	
	// rank peer
	private int countMsgPieces = 0;
	private int countMsgRequest = 0;
	private int countMsgInterest = 0;
	private int countMsgNotInterest = 0;
	private int countMsgChoke = 0;
	private int countMsgUnChoke = 0;

	//	status Pieces
	private PiecesMap piecesMap;
	private boolean verbouse;

	public boolean isConnected = false; // CONNECT SOCKET
	public boolean hasHandshake = false; // Connect Socket and shake Hands 
	
	public PeerNonBlock() {}
	public PeerNonBlock(String host, int port, byte[] peerId, ManagerPeer client) {
		this.host = host;
		this.port = port;
		this.peerId = peerId;
		this.client = client;
		this.infoHash = client.getTorrent().getInfoHash();
		this.piecesMap = new PiecesMap(client.getTorrent());
	}

	public int compare(PeerNonBlock peer1, PeerNonBlock peer2) {
		int sum1 =	(-1 * peer1.countMsgChoke) 			+ 
					( 1 * peer1.countMsgUnChoke) 		+ 
					( 3 * peer1.countMsgInterest) 		+ 
					(-3 * peer1.countMsgNotInterest) 	+ 
					( 2 * peer1.countMsgPieces) 		+
					( 1 * peer1.countMsgRequest);

		int sum2 =	(-1 * peer2.countMsgChoke) 			+ 
					( 1 * peer2.countMsgUnChoke) 		+ 
					( 3 * peer2.countMsgInterest) 		+ 
					(-3 * peer2.countMsgNotInterest) 	+ 
					( 2 * peer2.countMsgPieces) 		+
					( 1 * peer2.countMsgRequest);

		return Integer.compare(sum1, sum2);
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

	public String toString() {
		return ("host: "+this.host+"\t port: "+this.port+"\t connect: "+this.isConnected+"\t hasHandshake: "+ this.hasHandshake + "\t Map: "+ this.piecesMap);
	}

}
