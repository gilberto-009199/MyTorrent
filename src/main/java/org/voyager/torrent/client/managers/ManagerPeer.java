package org.voyager.torrent.client.managers;

import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.messages.Msg;
import org.voyager.torrent.client.peers.Peer;
import org.voyager.torrent.client.peers.PeerNonBlock;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.messages.MsgPiece;
import org.voyager.torrent.client.messages.MsgRequest;


/* meu circulo de vida de gerenciador*/
public interface ManagerPeer extends Runnable {

	public ManagerFile getManagerFile();
	
	public ManagerPeer withManagerFile(ManagerFile managerFile);
	public ManagerPeer withSemaphoreExecutor(Semaphore semaphoreExecutor);
	public ManagerPeer withManagerAnnounce( ManagerAnnounce managerAnnounce);
	
	public Torrent getTorrent();
	public void queueNewMsg(PeerNonBlock peer, MsgRequest msg);
	public void queueNewMsg(PeerNonBlock peer, MsgPiece msg);

	public boolean connectError(Peer peer);
	public boolean shakeHandsError(Peer peer);
	public boolean downloaded(Peer peer);
	public boolean uploaded(Peer peer);

	public void queueNewsPeer(PeerNonBlock peer);
    public void queueNewsPeerIfNotPresent(PeerNonBlock peer);

    public void addInterestPeer(Peer peer);
    public void removeInterestPeer(Peer peer);
    
}
