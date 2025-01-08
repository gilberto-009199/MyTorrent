package org.voyager.torrent.client.builders.strategies;

import org.voyager.torrent.client.ClientTorrent;

import org.voyager.torrent.client.strategy.PeerStrategy;

import org.voyager.torrent.client.strategy.ProcessMsgStrategy;
import org.voyager.torrent.client.strategy.basic.BasicManagerPeerStrategy;
import org.voyager.torrent.client.strategy.basic.BasicPeerStrategy;

// @todo add configs
public class PeerStrategyBuilder {

	private ProcessMsgStrategyBuilder processMsgStrategyBuilder;

	public PeerStrategy build(ClientTorrent clientTorrent){

		if(processMsgStrategyBuilder == null)processMsgStrategyBuilder = new ProcessMsgStrategyBuilder();

		ProcessMsgStrategy processMsgStrategy = processMsgStrategyBuilder.build(clientTorrent);

		return new BasicPeerStrategy()
				.setProcessMsgStrategy( processMsgStrategy );

	}
}
