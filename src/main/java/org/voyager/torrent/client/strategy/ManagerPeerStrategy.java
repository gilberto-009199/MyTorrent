package org.voyager.torrent.client.strategy;

import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.peers.Peer;

public interface ManagerPeerStrategy {
	void processConnected(ManagerPeer managerPeer, Peer peer);
	void processDisconnected(ManagerPeer managerPeer, Peer peer);
}
