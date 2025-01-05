package org.voyager.torrent.client.strategy;

import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.peers.InfoPeer;
import org.voyager.torrent.client.peers.Peer;

import java.util.List;

public interface ManagerPeerStrategy extends Strategy{

	PeerStrategy peerStrategy();

	void processConnected(ManagerPeer managerPeer, Peer peer);
	void processDisconnected(ManagerPeer managerPeer, Peer peer);

	void hookNewsInfoPeer(ManagerPeer managerPeer);
}
