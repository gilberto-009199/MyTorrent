package org.voyager.torrent.client;

import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.peers.InfoPeer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Semaphore;

public class StateClientTorrent {

	private ClientTorrent client;

	private Torrent torrent;
	private Semaphore semaphoreExecutor;
	private List<InfoPeer> listInfoPeerRemote;

	public StateClientTorrent(ClientTorrent client){ this.client = client; }

	public ClientTorrent client() { return client;	}
	public Torrent torrent() { return torrent;	}
	public Semaphore semaphoreExecutor(){ return this.semaphoreExecutor; }
	public List<InfoPeer> listInfoPeerRemote() { return listInfoPeerRemote;	}

	public StateClientTorrent setListInfoPeerRemote(List<InfoPeer> listInfoPeer){

		if(listInfoPeerRemote == null)listInfoPeerRemote = new ArrayList<>();

		for(InfoPeer info : listInfoPeer){

			boolean notInListInfoPeerRemoteThen = !listInfoPeerRemote.contains(info);

			if(notInListInfoPeerRemoteThen) listInfoPeerRemote.add(info);
		}

		return this;
	}

	public StateClientTorrent setSemaphoreExecutor(Semaphore semaphoreExecutor) {
		this.semaphoreExecutor = semaphoreExecutor;
		return this;
	}

	public StateClientTorrent setTorrent(Torrent torrent) {
		this.torrent = torrent;
		return this;
	}
}
