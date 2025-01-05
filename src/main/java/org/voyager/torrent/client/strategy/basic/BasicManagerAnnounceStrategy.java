package org.voyager.torrent.client.strategy.basic;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.managers.ManagerAnnounce;
import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.peers.InfoPeer;
import org.voyager.torrent.client.peers.Peer;
import org.voyager.torrent.client.strategy.ManagerAnnounceStrategy;
import org.voyager.torrent.client.strategy.ManagerPeerStrategy;

import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Map;
import java.util.Queue;

public class BasicManagerAnnounceStrategy implements ManagerAnnounceStrategy {

	@Override
	public void hookNewListInfoPeer(ManagerAnnounce managerAnnounce,
									List<InfoPeer> listInfoPeerRemote) {

		managerAnnounce
				.client()
				.state()
				.setListInfoPeerRemote(listInfoPeerRemote);

	}

	@Override
	public void hookAnnounce(ManagerAnnounce managerAnnounce) {
		ClientTorrent client = managerAnnounce.client();
		ManagerPeer managerPeer = client.managerPeer();

		managerPeer.strategy().processNewsInfoPeer(managerPeer);
	}
}
