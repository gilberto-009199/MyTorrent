package org.voyager.torrent.client.limits;

import org.voyager.torrent.client.messages.Msg;
import org.voyager.torrent.client.peers.Peer;

public class PeerLimit {
	public MsgLimit msgLimit;
	public BandWidthLimit bandWidthLimit;

	public PeerLimit(int maxMsgPeerSecond, int maxBandWidthPeerSecoud){
		this.msgLimit = new MsgLimit(maxMsgPeerSecond);
		this.bandWidthLimit = new BandWidthLimit(maxBandWidthPeerSecoud);
	}

	public synchronized boolean tryConsume(Msg msg) {
		return msgLimit.tryConsume(msg) &&  bandWidthLimit.tryConsume(msg);
	}
}
