package org.voyager.torrent.client.builders.strategies;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.strategy.ProcessMsgStrategy;
import org.voyager.torrent.client.strategy.basic.BasicProcessMsgStrategy;

public class ProcessMsgStrategyBuilder {

	public ProcessMsgStrategy build(ClientTorrent clientTorrent){
		return new BasicProcessMsgStrategy();
	}
}
