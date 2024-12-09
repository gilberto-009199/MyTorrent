package org.voyager.torrent.client.connect;

import GivenTools.TorrentInfo;


/* meu circulo de vida de gerenciador*/
public interface ManagerPeer {
	
	public TorrentInfo getTorrent();
	public void addQueue(Peer peer, MsgRequest msg);
	public void addQueue(Peer peer, MsgPiece msg);
	public boolean connectError(Peer peer);
	public boolean shakeHandsError(Peer peer);
	public boolean downloaded(Peer peer);
	public boolean uploaded(Peer peer);
    public void addInterestPeer(Peer peer);
    public void removeInterestPeer(Peer peer);
}
