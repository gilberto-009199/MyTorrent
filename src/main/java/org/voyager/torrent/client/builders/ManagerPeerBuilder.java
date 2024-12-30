package org.voyager.torrent.client.builders;

import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.managers.BasicManagerPeer;
import org.voyager.torrent.client.managers.ManagerPeer;

public class ManagerPeerBuilder {

	private Torrent torrent;
	private PeerLimitBuilder peerLimitBuilder;
	// @todo config keep alive time
	// @todo add strategy's
	public ManagerPeer build(){ return build(this.torrent); }
	public ManagerPeer build(Torrent torrent){

		if(peerLimitBuilder == null)peerLimitBuilder = new PeerLimitBuilder();

		ManagerPeer managerPeer = new BasicManagerPeer();

		return managerPeer.withTorrent(torrent);
	}

	public ManagerPeerBuilder withPeerLimitBuilder(PeerLimitBuilder peerLimitBuilder){
		this.peerLimitBuilder = peerLimitBuilder;
		return this;
	}

	private ManagerPeerBuilder withTorrent(Torrent torrent) {
		this.torrent = torrent;
		return this;
	}

}
