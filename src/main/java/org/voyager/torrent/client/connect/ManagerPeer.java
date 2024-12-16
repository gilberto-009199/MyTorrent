package org.voyager.torrent.client.connect;

import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.files.Torrent;


/* meu circulo de vida de gerenciador*/
public interface ManagerPeer extends Runnable {
	
	public ManagerPeer withManagerFile(ManagerFile managerFile);
	public ManagerPeer withSemaphoreExecutor(Semaphore semaphoreExecutor);
	public ManagerPeer withManagerAnnounce( ManagerAnnounce managerAnnounce);
	public ManagerPeer withMaxUploaderPeerSecond( int maxUploaderPeerSecond);
	public ManagerPeer withMaxDownloaderPeerSecond( int maxDownloaderPeerSecond);
	
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
