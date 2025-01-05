package org.voyager.torrent.client.strategy;

import org.voyager.torrent.client.managers.BasicManagerAnnounce;
import org.voyager.torrent.client.managers.ManagerAnnounce;
import org.voyager.torrent.client.peers.InfoPeer;

import java.util.List;

public interface ManagerAnnounceStrategy extends Strategy{
	void hookNewListInfoPeer(ManagerAnnounce managerAnnounce, List<InfoPeer> listInfoPeerRemote);

	void hookAnnounce(ManagerAnnounce managerAnnounce);
}
