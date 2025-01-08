package org.voyager.torrent.client.builders.strategies;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.strategy.ClientStrategy;
import org.voyager.torrent.client.strategy.ManagerAnnounceStrategy;
import org.voyager.torrent.client.strategy.ManagerFileStrategy;
import org.voyager.torrent.client.strategy.ManagerPeerStrategy;

// @todo add configs and properties
public class ClientStrategyBuilder {

	private ManagerAnnounceStrategyBuilder managerAnnounceStrategyBuilder;
	private ManagerFileStrategyBuilder managerFileStrategyBuilder;
	private ManagerPeerStrategyBuilder managerPeerStrategyBuilder;

	public ClientStrategy build(ClientTorrent clientTorrent){

		if(managerAnnounceStrategyBuilder == null)managerAnnounceStrategyBuilder = new ManagerAnnounceStrategyBuilder();
		if(managerFileStrategyBuilder == null)managerFileStrategyBuilder = new ManagerFileStrategyBuilder();
		if(managerPeerStrategyBuilder == null)managerPeerStrategyBuilder = new ManagerPeerStrategyBuilder();

		return new ClientStrategy() {
			@Override
			public ManagerPeerStrategy peerStrategy() {
				return managerPeerStrategyBuilder.build(clientTorrent);
			}

			@Override
			public ManagerFileStrategy fileStrategy() {
				return managerFileStrategyBuilder.build(clientTorrent);
			}

			@Override
			public ManagerAnnounceStrategy announceStrategy() {
				return managerAnnounceStrategyBuilder.build(clientTorrent);
			}
		};

	}
}
