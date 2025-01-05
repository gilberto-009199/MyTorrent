package org.voyager.torrent.client.strategy.basic;

import org.voyager.torrent.client.StateClientTorrent;
import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.peers.BasicPeer;
import org.voyager.torrent.client.peers.InfoPeer;
import org.voyager.torrent.client.peers.Peer;
import org.voyager.torrent.client.strategy.ManagerPeerStrategy;
import org.voyager.torrent.client.strategy.PeerStrategy;

import java.util.List;
import java.util.Queue;

public class BasicManagerPeerStrategy implements ManagerPeerStrategy {

	private PeerStrategy peerStrategy = new BasicPeerStrategy();

	@Override
	public PeerStrategy peerStrategy() { return peerStrategy; }

	@Override
	public void processConnected(ManagerPeer managerPeer,
								 Peer peer) {

	}

	@Override
	public void processDisconnected(ManagerPeer managerPeer,
									Peer peer) {

	}

	// process<???> for decision
	// hooks<???> for update state

	@Override
	public void hookNewsInfoPeer(ManagerPeer managerPeer) {

		StateClientTorrent state = managerPeer.client().state();

		List<Peer> listPeer = managerPeer.listPeer();
		List<InfoPeer> listInfoPeer = state.listInfoPeerRemote();

		Queue<Peer> queueNewsPeer = state.queueNewsPeer();

		for(InfoPeer info : listInfoPeer){
			boolean InPeerCurrent = listPeer.stream().anyMatch(peer -> peer.infoRemote().equals(info));

			if(InPeerCurrent)continue;

			queueNewsPeer.add(
					new BasicPeer()
					.setStrategy(peerStrategy())
					.setInfoRemote(info)
			);

		}
	}
}
