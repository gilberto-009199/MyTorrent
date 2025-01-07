package org.voyager.torrent.client.builders;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.managers.BasicManagerFile;
import org.voyager.torrent.client.managers.ManagerFile;
import org.voyager.torrent.client.managers.ManagerPeer;

public class ManagerFileBuilder {

	private Torrent torrent;

	// @todo add strategy's

	public ManagerFile build(ClientTorrent clientTorrent){ return build(this.torrent, clientTorrent); }
	public ManagerFile build(Torrent torrent, ClientTorrent clientTorrent){

		ManagerFile managerFile = new BasicManagerFile(clientTorrent);

		return managerFile;
	}

	private ManagerFileBuilder withTorrent(Torrent torrent) {
		this.torrent = torrent;
		return this;
	}
}
