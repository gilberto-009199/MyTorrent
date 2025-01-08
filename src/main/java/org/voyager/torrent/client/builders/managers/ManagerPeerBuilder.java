package org.voyager.torrent.client.builders.managers;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.builders.PeerLimitBuilder;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.managers.BasicManagerPeer;
import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.net.limits.PeerLimit;

public class ManagerPeerBuilder {

	private Torrent torrent;
	private PeerLimitBuilder peerLimitBuilder;
	// @todo config keep alive time
	// @todo add strategy's
	public ManagerPeer build(ClientTorrent clientTorrent){ return build(this.torrent, clientTorrent); }
	public ManagerPeer build(Torrent torrent, ClientTorrent clientTorrent){

		if(peerLimitBuilder == null)peerLimitBuilder = new PeerLimitBuilder();

		PeerLimit limit = peerLimitBuilder.build();

		return new BasicManagerPeer(clientTorrent)
				.setLimit(limit);
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
