package org.voyager.torrent.client.peers;


import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.metrics.PeerMetrics;
import org.voyager.torrent.client.net.limits.PeerLimit;

public class StatePeer {

	// Data States
	private boolean handshake	= false;
	private boolean connected	= false;
	private boolean interest	= false;
	private boolean choked		= true;

	private PiecesMap piecesMap;

	// Data Limit and metrics
	private PeerMetrics metrics;
	private PeerLimit limits;

	public boolean handshake() { return handshake; }
	public void setHandshake(boolean handshake) { this.handshake = handshake; }

	public boolean connected() { return connected; }
	public void setConnected(boolean connected) { this.connected = connected; }

	public boolean choked() {	return choked; }
	public void setChoked(boolean choked) { this.choked = choked; }

	public boolean interest() {	return interest; }
	public void setInterest(boolean interest) { this.interest = interest; }

	public PiecesMap piecesMap() { return piecesMap;	}
	public void setPiecesMap(PiecesMap piecesMap) { this.piecesMap = piecesMap; }

	public PeerMetrics metrics() { return metrics; }
	public void setMetrics(PeerMetrics metrics) { this.metrics = metrics; }

	public PeerLimit limits() { return limits; }
	public void setLimits(PeerLimit limits) { this.limits = limits; }
}
