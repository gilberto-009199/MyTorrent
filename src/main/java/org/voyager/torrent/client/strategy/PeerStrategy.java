package org.voyager.torrent.client.strategy;

import org.voyager.torrent.client.peers.Peer;

import java.io.IOException;

public interface PeerStrategy extends Strategy{
	void hookConnected(Peer peer);
	void hookDisconnected(Peer peer);
	void hookRead(Peer peer) throws IOException;
	void hookWrite(Peer peer);
}
