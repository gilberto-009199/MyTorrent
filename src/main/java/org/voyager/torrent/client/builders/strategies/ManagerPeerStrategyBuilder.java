package org.voyager.torrent.client.builders.strategies;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.strategy.ClientStrategy;
import org.voyager.torrent.client.strategy.ManagerPeerStrategy;
import org.voyager.torrent.client.strategy.basic.BasicManagerPeerStrategy;

public class ManagerPeerStrategyBuilder {
	public ManagerPeerStrategy build(ClientTorrent clientTorrent){
		return new BasicManagerPeerStrategy();
	}
}
