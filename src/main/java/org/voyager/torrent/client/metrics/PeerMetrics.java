package org.voyager.torrent.client.metrics;

import org.voyager.torrent.client.net.metrics.BandWidthMetrics;
import org.voyager.torrent.client.net.metrics.MsgMetrics;

public class PeerMetrics implements Comparable<PeerMetrics>{

	public BandWidthMetrics bandWidthMetrics;
	public MsgMetrics msgMetrics;

	public PeerMetrics(){
		this.bandWidthMetrics = new BandWidthMetrics();
		this.msgMetrics = new MsgMetrics();
	}

	@Override
	public String toString() {
		return "PeerMetrics[bandWidthMetrics: ["+ bandWidthMetrics +"], msgMetrics: ["+ msgMetrics +"]]";
	}

	@Override
	public int compareTo(PeerMetrics metric) { return PeerMetrics.compare(this, metric); }
	public static int compare(PeerMetrics m1, PeerMetrics m2) {
		return Integer.compare(
						m1.msgMetrics.compareTo(m2.msgMetrics),
						m1.bandWidthMetrics.compareTo(m2.bandWidthMetrics)
		);
	}
}
