package org.voyager.torrent.client.builders.strategies;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.managers.BasicManagerFile;
import org.voyager.torrent.client.strategy.ClientStrategy;
import org.voyager.torrent.client.strategy.ManagerFileStrategy;
import org.voyager.torrent.client.strategy.basic.BasicManagerFileStrategy;

public class ManagerFileStrategyBuilder {
	public ManagerFileStrategy build(ClientTorrent clientTorrent){
		return new BasicManagerFileStrategy();
	}
}
