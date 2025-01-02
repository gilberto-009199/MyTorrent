package org.voyager.torrent.client.peers;

import org.voyager.torrent.client.enums.ClientTorrentType;
import org.voyager.torrent.client.managers.ManagerPeer;

public class InfoPeer {

	private ClientTorrentType clientType;
	private boolean verbose = true;
	private ManagerPeer manager;
	private byte[] infoHash;
	private byte[] peerId;
	private String host;
	private int port;

	public ClientTorrentType clientType() {	return clientType;	}
	public InfoPeer setClientType(ClientTorrentType clientType) {
		this.clientType = clientType;
		return this;
	}

	public boolean verbouse() {	return verbose;}
	public InfoPeer setVerbose(boolean verbose) {
		this.verbose = verbose;
		return this;
	}

	public ManagerPeer manager() {	return manager;	}
	public InfoPeer setManager(ManagerPeer manager) {
		this.manager = manager;
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
}
