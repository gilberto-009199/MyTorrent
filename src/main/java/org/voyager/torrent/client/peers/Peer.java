package org.voyager.torrent.client.peers;

import org.voyager.torrent.client.net.socket.Network;

import java.io.IOException;

public interface Peer extends Comparable<Peer>{

	// Info
	InfoPeer infoLocal();
	Peer setInfoLocal(InfoPeer info);
	InfoPeer infoRemote();
	Peer setInfoRemote(InfoPeer info);

	// Data
	StatePeer statePeer();
	Peer setStatePeer(StatePeer data);
	Network network();

	// Hooks
	void connected() throws IOException;
	void disconnected() throws IOException;
	void write() throws IOException;
	void read() throws IOException;

}
