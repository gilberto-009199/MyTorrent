package org.voyager.torrent.client.peers;

import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.net.socket.Network;
import org.voyager.torrent.client.strategy.PeerStrategy;

import java.io.IOException;

// @todo no futuro criar uma unica e grande fila aonde os pares seriam chamados por um invocador
public class BasicPeer implements Peer{

	private ManagerPeer managerPeer;

	// Info
	private InfoPeer infoRemote;
	private InfoPeer infoLocal;

	// State Data
	private StatePeer state;

	// abstract network
	private Network network;

	private PeerStrategy strategy;

	// Hooks
	public void connected() throws IOException { strategy.hookConnected(this); }
	public void disconnected() throws IOException {	strategy.hookDisconnected(this);	}

	public void write() throws IOException {
		strategy.hookWrite(this);
	}

	public void read() throws IOException {
		strategy.hookRead(this);
	}

	// Setters And Getters
	@Override
	public InfoPeer infoLocal() { return this.infoLocal; }
	@Override
	public Peer setInfoLocal(InfoPeer info) {
		this.infoLocal = info;
		return this;
	}

	@Override
	public InfoPeer infoRemote() {	return this.infoRemote;	}
	@Override
	public Peer setInfoRemote(InfoPeer info) {
		this.infoRemote = info;
		return this;
	}

	@Override
	public StatePeer statePeer() { return this.state; }
	@Override
	public Peer setStatePeer(StatePeer data) {
		this.state = data;
		return this;
	}

	@Override
	public Network network() { return this.network;	}
	public Peer setNetwork(Network network){
		this.network = network;
		return this;
	}

	public ManagerPeer managerPeer() { return managerPeer;	}
	public BasicPeer setManagerPeer(ManagerPeer managerPeer) {
		this.managerPeer = managerPeer;
		return this;
	}

	public PeerStrategy strategy() { return strategy; }
	public BasicPeer setStrategy(PeerStrategy strategy) {
		this.strategy = strategy;
		return this;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (! (obj instanceof BasicPeer))return false;

		BasicPeer peer = (BasicPeer) obj;
		return infoLocal.port() == peer.infoLocal.port() && infoLocal.host().equals(peer.infoLocal.host());
	}

	@Override
	public int hashCode() {
		int result = infoLocal.host().hashCode();
		result = 31 * result + infoLocal.port();
		return result;
	}

	@Override
	public int compareTo(Peer peer) {
		if( !state.connected() && peer.statePeer().connected() )return -1;

		if( state.connected()  && !peer.statePeer().connected() )return 1;

		if( state.choked() && !peer.statePeer().choked() )return -1;

		if( !state.choked() && peer.statePeer().choked() )return 1;

		if( state.piecesMap() == null && peer.statePeer().piecesMap() != null )return -1;

		if( state.piecesMap() != null && peer.statePeer().piecesMap() == null )return 1;

		return state.metrics().compareTo(statePeer().metrics());
	}

	public String toString() {
		return "BasicPeer[" +
				"infoRemote: "+	infoRemote +
				", infoLocal: "+		infoLocal +
				", state: "+ 		state + "]";
	}


}