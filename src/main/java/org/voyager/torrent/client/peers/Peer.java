package org.voyager.torrent.client.peers;

import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.net.socket.Network;
import org.voyager.torrent.client.strategy.PeerStrategy;

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
	Peer setNetwork(Network network);

	ManagerPeer managerPeer();
	Peer setManagerPeer(ManagerPeer managerPeer);

	PeerStrategy strategy();
	Peer setStrategy(PeerStrategy strategy);

	// Hooks
	void connected() throws IOException;
	void disconnected() throws IOException;
	void write() throws IOException;
	void read() throws IOException;

}
