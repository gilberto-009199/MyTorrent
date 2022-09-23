package org.voyager.torrent.client.connect;

import GivenTools.TorrentInfo;


/* meu circulo de vida de gerenciador*/
public interface ManagerPeer {
	
	public TorrentInfo getTorrent();
	public boolean connectError(Peer peer);
	public boolean shakeHandsError(Peer peer);
	public boolean downloaded(Peer peer);
	public boolean uploaded(Peer peer);
}
