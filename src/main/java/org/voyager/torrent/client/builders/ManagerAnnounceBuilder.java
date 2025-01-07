package org.voyager.torrent.client.builders;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.managers.BasicManagerAnnounce;
import org.voyager.torrent.client.managers.ManagerAnnounce;
import org.voyager.torrent.client.managers.ManagerFile;
import org.voyager.torrent.client.managers.ManagerPeer;

public class ManagerAnnounceBuilder {

	private Torrent torrent;
	private int timeReAnnounceInSecond;
	private int timeVerifyNewsPeersInSecond;

	// @todo add strategy's

	public ManagerAnnounce build(ClientTorrent clientTorrent){ return build(this.torrent, clientTorrent); }
	public ManagerAnnounce build(Torrent torrent, ClientTorrent clientTorrent){

		if(timeReAnnounceInSecond < 0)timeReAnnounceInSecond = 32;
		if(timeVerifyNewsPeersInSecond < 0)timeVerifyNewsPeersInSecond = 32;

		return new BasicManagerAnnounce(clientTorrent)
				.setTimeReAnnounceInSecond(timeReAnnounceInSecond)
				.setTimeVerifyNewsPeersInSecond(timeVerifyNewsPeersInSecond);

	}

	public ManagerAnnounceBuilder withTimeReAnnounceInSecond(int timeReAnnounceInSecond){
		this.timeReAnnounceInSecond = timeReAnnounceInSecond;
		return this;
	}

	public ManagerAnnounceBuilder withTimeVerifyNewsPeersInSecond(int timeVerifyNewsPeersInSecond){
		this.timeVerifyNewsPeersInSecond = timeVerifyNewsPeersInSecond;
		return this;
	}

	private ManagerAnnounceBuilder withTorrent(Torrent torrent) {
		this.torrent = torrent;
		return this;
	}

}
