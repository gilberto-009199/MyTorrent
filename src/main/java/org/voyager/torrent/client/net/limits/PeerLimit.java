package org.voyager.torrent.client.net.limits;

import org.voyager.torrent.client.net.messages.Msg;

public class PeerLimit {
	public MsgLimit msgLimit;
	public BandWidthLimit bandWidthLimit;

	public PeerLimit(int maxMsgPeerSecond,
					 int maxBandWidthPeerSecond){
		this(new MsgLimit(maxMsgPeerSecond), new BandWidthLimit(maxBandWidthPeerSecond));
	}

	public PeerLimit(MsgLimit msgLimit,
					 BandWidthLimit bandWidthLimit){
		this.msgLimit = msgLimit;
		this.bandWidthLimit = bandWidthLimit;
	}

	public synchronized boolean tryConsume(Msg msg) {
		return msgLimit.tryConsume(msg) &&  bandWidthLimit.tryConsume(msg);
	}
}
