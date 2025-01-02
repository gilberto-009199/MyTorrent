package org.voyager.torrent.client.network.limits;

import org.voyager.torrent.client.network.messages.Msg;
import org.voyager.torrent.client.peers.Peer;

public class PeerLimit {
	public MsgLimit msgLimit;
	public BandWidthLimit bandWidthLimit;

	public PeerLimit(int maxMsgPeerSecond, int maxBandWidthPeerSecond){
		this(new MsgLimit(maxMsgPeerSecond), new BandWidthLimit(maxBandWidthPeerSecond));
	}
	public PeerLimit(MsgLimit msgLimit, BandWidthLimit bandWidthLimit){
		this.msgLimit = msgLimit;
		this.bandWidthLimit = bandWidthLimit;
	}

	public synchronized boolean tryConsume(Msg msg) {
		return msgLimit.tryConsume(msg) &&  bandWidthLimit.tryConsume(msg);
	}
}
