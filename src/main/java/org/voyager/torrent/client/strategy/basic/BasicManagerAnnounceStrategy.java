package org.voyager.torrent.client.strategy.basic;

import org.voyager.torrent.client.managers.ManagerAnnounce;
import org.voyager.torrent.client.peers.InfoPeer;
import org.voyager.torrent.client.strategy.ManagerAnnounceStrategy;

import java.util.List;

public class BasicManagerAnnounceStrategy implements ManagerAnnounceStrategy {

	@Override
	public void hookNewListInfoPeer(ManagerAnnounce managerAnnounce,
									List<InfoPeer> listInfoPeerRemote) {

		managerAnnounce.client().state().add(listInfoPeerRemote);

	}
}
