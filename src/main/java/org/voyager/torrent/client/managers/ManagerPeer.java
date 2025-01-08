package org.voyager.torrent.client.managers;

import java.util.List;
import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.net.limits.PeerLimit;
import org.voyager.torrent.client.peers.BasicPeer;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.net.messages.MsgPiece;
import org.voyager.torrent.client.net.messages.MsgRequest;
import org.voyager.torrent.client.peers.InfoPeer;
import org.voyager.torrent.client.peers.Peer;
import org.voyager.torrent.client.strategy.ManagerPeerStrategy;
import org.voyager.torrent.client.strategy.Strategy;


public interface ManagerPeer extends Manager {

	List<Peer> listPeer();
	ClientTorrent client();
	ManagerPeerStrategy strategy();
	ManagerPeer setClient(ClientTorrent client);
	ManagerPeer setLimit(PeerLimit limit);
	PeerLimit limit();

}
