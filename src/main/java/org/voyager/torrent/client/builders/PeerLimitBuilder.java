package org.voyager.torrent.client.builders;

import org.voyager.torrent.client.net.limits.BandWidthLimit;
import org.voyager.torrent.client.net.limits.MsgLimit;
import org.voyager.torrent.client.net.limits.PeerLimit;

public class PeerLimitBuilder {

	private MsgLimit msgLimit;
	private BandWidthLimit bandWidthLimit;

	// Build logic
	public PeerLimit build() {

		if(msgLimit == null)msgLimit = new MsgLimit(20);

		if(bandWidthLimit == null)bandWidthLimit = new BandWidthLimit(17 * 1024);

		return new PeerLimit(msgLimit, bandWidthLimit);

	}

	public PeerLimitBuilder withMsgLimit(MsgLimit msgLimit){
		this.msgLimit = msgLimit;
		return this;
	}

	public PeerLimitBuilder withBandWidthLimit(BandWidthLimit bandWidthLimit){
		this.bandWidthLimit = bandWidthLimit;
		return this;
	}

}
