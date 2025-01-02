package org.voyager.torrent.client.strategy.basic;

import org.voyager.torrent.client.net.messages.Msg;
import org.voyager.torrent.client.net.messages.MsgHandShake;
import org.voyager.torrent.client.peers.Peer;
import org.voyager.torrent.client.strategy.PeerStrategy;
import org.voyager.torrent.client.strategy.ProcessMsgStrategy;

import java.io.IOException;
import java.util.Optional;
import java.util.function.Supplier;

public class BasicPeerStrategy implements PeerStrategy {

	private ProcessMsgStrategy processMsgStrategy;

	@Override
	public void hookConnected(Peer peer) {	}

	@Override
	public void hookDisconnected(Peer peer) {	}

	@Override
	public void hookRead(Peer peer) throws IOException {
		if(!peer.network().isRedable())return;
		if(!peer.statePeer().handshake()){
			readMsgHandShake(peer);
			return;
		}

		Optional<Msg> optMsg = peer.network().readMsg();

		boolean notPresentThen = !optMsg.isPresent();
		if(notPresentThen)return;

		Msg msg = optMsg.get();

		processMsgStrategy.hookReceive(peer, msg);
	}

	@Override
	public void hookWrite(Peer peer) {
		if(!peer.network().isWritable())return;
		if(!peer.statePeer().handshake()){
			writeMsgHandShake(peer);
			return;
		}

		// process queue
		for(Msg msg: peer.statePeer().queueWriter()){
			processMsgStrategy.hookSend(peer, msg);
		}
	}

	// Writer MsgHandShake
	private void writeMsgHandShake(Peer peer) {

		MsgHandShake msg = new MsgHandShake(
				peer.infoLocal().infoHash(),
				peer.infoLocal().peerId()
		);

		processMsgStrategy.hookSend(peer, msg);
	}

	// Read MsgHandShake
	private void readMsgHandShake(Peer peer) throws IOException {

		MsgHandShake msg = new MsgHandShake();

		boolean read = peer.network()
				.read(msg)
				.map( r -> r > 1)
				.orElse(false);

		if(!read)return;

		peer.infoRemote().setPeerId(msg.getPeerId());
		peer.infoRemote().setClientType(msg.getClientType());

		boolean infoHashValid = MsgHandShake.checkHandShake(msg, peer.infoLocal().infoHash());

		peer.statePeer().setHandshake(infoHashValid);

		if(!infoHashValid)return;

		processMsgStrategy.hookReceive(peer, msg);
	}

}
