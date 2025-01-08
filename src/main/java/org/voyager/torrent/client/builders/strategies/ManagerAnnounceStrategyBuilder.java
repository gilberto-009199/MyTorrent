package org.voyager.torrent.client.builders.strategies;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.strategy.ClientStrategy;
import org.voyager.torrent.client.strategy.ManagerAnnounceStrategy;
import org.voyager.torrent.client.strategy.basic.BasicManagerAnnounceStrategy;

public class ManagerAnnounceStrategyBuilder {

	public ManagerAnnounceStrategy build(ClientTorrent clientTorrent){
		return new BasicManagerAnnounceStrategy();
	}

}
