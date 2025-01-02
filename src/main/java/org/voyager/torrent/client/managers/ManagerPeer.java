package org.voyager.torrent.client.managers;

import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.peers.BasicPeer;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.net.messages.MsgPiece;
import org.voyager.torrent.client.net.messages.MsgRequest;


public interface ManagerPeer extends Runnable {

	// Actions
	void queueNewMsg(BasicPeer peer, MsgRequest msg);
	void queueNewMsg(BasicPeer peer, MsgPiece msg);
	void queueNewsPeer(BasicPeer peer);
	void queueNewsPeerIfNotPresent(BasicPeer peer);

	// Hooks
	boolean connectError(Peer peer);
	boolean shakeHandsError(Peer peer);
	boolean downloaded(Peer peer);
	boolean uploaded(Peer peer);
	void addInterestPeer(Peer peer);
	void removeInterestPeer(Peer peer);

	// Getters
	Torrent getTorrent();
	ManagerFile getManagerFile();

	// Withs
	ManagerPeer withTorrent(Torrent torrent);
	ManagerPeer withManagerFile(ManagerFile managerFile);
	ManagerPeer withClientTorrent(ClientTorrent clientTorrent);
	ManagerPeer withSemaphoreExecutor(Semaphore semaphoreExecutor);
	ManagerPeer withManagerAnnounce( ManagerAnnounce managerAnnounce);

}
