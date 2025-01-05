package org.voyager.torrent.client.peers;

import org.voyager.torrent.client.enums.ClientTorrentType;
import org.voyager.torrent.client.managers.ManagerPeer;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class InfoPeer {

	private ClientTorrentType clientType;
	private byte[] infoHash;
	private byte[] peerId;
	private String host;
	private int port;

	public ClientTorrentType clientType() {	return clientType;	}
	public InfoPeer setClientType(ClientTorrentType clientType) {
		this.clientType = clientType;
		return this;
	}

	public byte[] infoHash() {	return infoHash; }
	public InfoPeer setInfoHash(byte[] infoHash) {
		this.infoHash = infoHash;
		return this;
	}

	public byte[] peerId() { return peerId;	}
	public InfoPeer setPeerId(byte[] peerId) {
		this.peerId = peerId;
		return this;
	}

	public String host() { return host;	}
	public InfoPeer setHost(String host) {
		this.host = host;
		return this;
	}

	public int port() {	return port; }
	public InfoPeer setPort(int port) {
		this.port = port;
		return this;
	}

	public String toString() {
		return "InfoPeer[" +
				"infoHash: "+ Arrays.toString(infoHash) +
				", peerId: "+ new String(peerId, StandardCharsets.UTF_8) +
				", host: "+   host		+
				", port:" +   port 		+
				", ClientType: [" + clientType +"]"+
				"]";
	}
}
