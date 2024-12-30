package org.voyager.torrent.client.builders;

import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.managers.BasicManagerFile;
import org.voyager.torrent.client.managers.ManagerFile;
import org.voyager.torrent.client.managers.ManagerPeer;

public class ManagerFileBuilder {

	private Torrent torrent;

	// @todo add strategy's

	public ManagerFile build(){ return build(this.torrent); }
	public ManagerFile build(Torrent torrent){

		ManagerFile managerFile = new BasicManagerFile();

		return managerFile.withTorrent(torrent);
	}

	private ManagerFileBuilder withTorrent(Torrent torrent) {
		this.torrent = torrent;
		return this;
	}
}
