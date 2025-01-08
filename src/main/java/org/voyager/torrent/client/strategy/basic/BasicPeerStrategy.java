package org.voyager.torrent.client.strategy.basic;

import org.voyager.torrent.client.net.messages.Msg;
import org.voyager.torrent.client.net.messages.MsgHandShake;
import org.voyager.torrent.client.net.socket.NetworkResult;
import org.voyager.torrent.client.peers.Peer;
import org.voyager.torrent.client.strategy.PeerStrategy;
import org.voyager.torrent.client.strategy.ProcessMsgStrategy;

import java.io.IOException;
import java.util.Optional;

public class BasicPeerStrategy implements PeerStrategy {

	private ProcessMsgStrategy processMsgStrategy;

	@Override
	public void hookConnected(Peer peer) {	}

	@Override
	public void hookDisconnected(Peer peer) {	}

	@Override
	public void hookRead(Peer peer) throws IOException {
		if(!peer.network().isReadable())return;


		if(!peer.statePeer().handshake()){
			readMsgHandShake(peer);
			return;
		}

		peer.network().nextRead();

		Optional<NetworkResult> optMsg = peer.network().queueReader();

		boolean notPresentThen = !optMsg.isPresent();
		if(notPresentThen)return;

		optMsg.ifPresent(result ->{

			if(result.success()) processMsgStrategy.hookReceive(peer, result.msg());

		});

	}

	@Override
	public void hookWrite(Peer peer) {

		if(!peer.network().isWritable())return;

		if(!peer.statePeer().handshake()){
			writeMsgHandShake(peer);
		}

		peer.network().nextWrite();

		// process queue
		// logic
		//  verify keep alive and send keep alive 2s
		//for(Msg msg: peer.statePeer().queueWriter()){
		//	processMsgStrategy.hookSend(peer, msg);
		//}

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
		// AQUI GIL
		peer.network()
		.readHandshake()
		.doOnSuccess(result -> {


			if(!result.success())return;
			if(!(result.msg() instanceof MsgHandShake))return;

			MsgHandShake msg = (MsgHandShake) result.msg();

			peer.infoRemote().setPeerId(msg.getPeerId());
			peer.infoRemote().setClientType(msg.getClientType());

			boolean infoHashValid = MsgHandShake.checkHandShake(msg, peer.infoLocal().infoHash());

			peer.statePeer().setHandshake(infoHashValid);

			if(!infoHashValid)return;

			processMsgStrategy.hookReceive(peer, msg);

		}).doOnError(throwable -> {

		}).subscribe();

	}

	public PeerStrategy setProcessMsgStrategy(ProcessMsgStrategy processMsgStrategy) {
		this.processMsgStrategy = processMsgStrategy;
		return this;
	}
}
