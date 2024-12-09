package org.voyager.torrent.client.connect;

import org.voyager.torrent.client.files.Torrent;


/* meu circulo de vida de gerenciador*/
public interface ManagerPeer extends Runnable {
	
	public ManagerPeer withManagerAnnounce( ManagerAnnounce managerAnnounce);
	public ManagerPeer withMaxUploaderPeerSecond( int maxUploaderPeerSecond);
	public ManagerPeer withMaxDownloaderPeerSecond( int maxDownloaderPeerSecond);
	
	public Torrent getTorrent();
	public void addQueue(Peer peer, MsgRequest msg);
	public void addQueue(Peer peer, MsgPiece msg);
	public boolean connectError(Peer peer);
	public boolean shakeHandsError(Peer peer);
	public boolean downloaded(Peer peer);
	public boolean uploaded(Peer peer);
    public void addInterestPeer(Peer peer);
    public void removeInterestPeer(Peer peer);
}
