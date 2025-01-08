package org.voyager.torrent.client.builders.strategies;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.strategy.ClientStrategy;
import org.voyager.torrent.client.strategy.ManagerPeerStrategy;
import org.voyager.torrent.client.strategy.PeerStrategy;
import org.voyager.torrent.client.strategy.ProcessMsgStrategy;
import org.voyager.torrent.client.strategy.basic.BasicManagerPeerStrategy;

public class ManagerPeerStrategyBuilder {

	private PeerStrategyBuilder peerStrategyBuilder;

	public ManagerPeerStrategy build(ClientTorrent clientTorrent){
		if(peerStrategyBuilder == null)peerStrategyBuilder = new PeerStrategyBuilder();

		PeerStrategy peerStrategy = peerStrategyBuilder.build(clientTorrent);

		return new BasicManagerPeerStrategy()
				.setPeerStrategy( peerStrategy );
	}
}
