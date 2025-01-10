package org.voyager.torrent.client.strategy.basic;

import io.reactivex.rxjava3.core.Single;
import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.net.limits.BandWidthLimit;
import org.voyager.torrent.client.net.messages.*;
import org.voyager.torrent.client.net.metrics.BandWidthMetrics;
import org.voyager.torrent.client.net.metrics.MsgMetrics;
import org.voyager.torrent.client.net.socket.Network;
import org.voyager.torrent.client.net.socket.NetworkResult;
import org.voyager.torrent.client.peers.Peer;
import org.voyager.torrent.client.strategy.ProcessMsgStrategy;

public class BasicProcessMsgStrategy implements ProcessMsgStrategy {

	@Override
	public void hookReceive(Peer peer,
							Msg msg) {

		BandWidthMetrics bandMetric = peer.statePeer().metrics().bandWidthMetrics;
		MsgMetrics msgMetric = peer.statePeer().metrics().msgMetrics;

		bandMetric.addDownloaderBytes(msg.length());
		msgMetric.countMsg++;

		if(msg instanceof  MsgPort)hookReceive(peer, (MsgPort) msg);
		else if(msg instanceof  MsgHave)hookReceive(peer, (MsgHave) msg);
		else if(msg instanceof  MsgChoke)hookReceive(peer, (MsgChoke) msg);
		else if(msg instanceof  MsgCancel)hookReceive(peer, (MsgCancel) msg);
		else if(msg instanceof  MsgUnChoke)hookReceive(peer, (MsgUnChoke) msg);
		else if(msg instanceof  MsgRequest)hookReceive(peer, (MsgRequest) msg);
		else if(msg instanceof  MsgBitfield)hookReceive(peer, (MsgBitfield) msg);
		else if(msg instanceof  MsgHandShake)hookReceive(peer, (MsgHandShake) msg);
		else if(msg instanceof  MsgInterested)hookReceive(peer, (MsgInterested) msg);
		else if(msg instanceof  MsgNotInterested)hookReceive(peer, (MsgNotInterested) msg);

	}

	@Override
	public void hookReceive(Peer peer,
							MsgHandShake msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgHandShake++;

		boolean handShakeValid = MsgHandShake.checkHandShake( msg, peer.infoLocal().infoHash());
		if(handShakeValid){
			peer.statePeer().setConnected(true);
			peer.statePeer().setHandshake(true);
			peer.infoRemote()
					.setPeerId(msg.getPeerId())
					.setInfoHash(msg.getInfoHash())
					.setClientType(msg.getClientType());
		}
	}

	@Override
	public void hookReceive(Peer peer,
							MsgChoke msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgChoke++;

		peer.statePeer().setChoked(true);
	}

	@Override
	public void hookReceive(Peer peer,
							MsgUnChoke msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgUnChoke++;

		peer.statePeer().setChoked(false);
	}

	@Override
	public void hookReceive(Peer peer,
							MsgRequest msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgRequest++;
	}

	@Override
	public void hookReceive(Peer peer,
							MsgBitfield msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgBitfield++;

		peer.statePeer().setPiecesMap(msg.map());

		// send bitfield
		PiecesMap map = peer.managerPeer().client().managerFile().getMap();
		Msg msgSend = new MsgBitfield(map);

		hookSend(peer, msgSend);

	}

	@Override
	public void hookReceive(Peer peer,
							MsgCancel msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgCancel++;
		// cancel msg request
	}

	@Override
	public void hookReceive(Peer peer,
							MsgInterested msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgInterest++;

		peer.statePeer().setInterest(true);
	}

	@Override
	public void hookReceive(Peer peer,
							MsgNotInterested msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgNotInterest++;

		peer.statePeer().setInterest(false);
	}


	@Override
	public void hookReceive(Peer peer,
							MsgPort msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgPort++;
	}

	@Override
	public void hookReceive(Peer peer,
							MsgHave msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgHave++;
	}

	@Override
	public Single<NetworkResult> hookSend(Peer peer,
										  Msg msg) {

		BandWidthLimit limit = peer.statePeer().limits().bandWidthLimit;
		BandWidthMetrics metric = peer.statePeer().metrics().bandWidthMetrics;

		if(peer.network().isWritable() || !limit.tryConsume(msg)){
			return Single.create(emitter ->{
				emitter.onSuccess(new NetworkResult(false));
			});
		}

		metric.addUploaderBytes(msg.length());

		if(msg instanceof  MsgPort)return hookSend(peer, (MsgPort) msg);
		else if(msg instanceof  MsgHave)return hookSend(peer, (MsgHave) msg);
		else if(msg instanceof  MsgChoke)return hookSend(peer, (MsgChoke) msg);
		else if(msg instanceof  MsgCancel)return hookSend(peer, (MsgCancel) msg);
		else if(msg instanceof  MsgUnChoke)return hookSend(peer, (MsgUnChoke) msg);
		else if(msg instanceof  MsgRequest)return hookSend(peer, (MsgRequest) msg);
		else if(msg instanceof  MsgBitfield)return hookSend(peer, (MsgBitfield) msg);
		else if(msg instanceof  MsgHandShake)return hookSend(peer, (MsgHandShake) msg);
		else if(msg instanceof  MsgInterested)return hookSend(peer, (MsgInterested) msg);
		else if(msg instanceof  MsgNotInterested)return hookSend(peer, (MsgNotInterested) msg);
		else return peer.network().queueWriter(msg);

	}

	@Override
	public Single<NetworkResult> hookSend(Peer peer,
						 MsgHandShake msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgHandShake++;

		return peer.network().queueWriter(msg);
	}

	@Override
	public Single<NetworkResult> hookSend(Peer peer,
						 MsgChoke msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgChoke++;

		return peer.network().queueWriter(msg);
	}

	@Override
	public Single<NetworkResult> hookSend(Peer peer,
						 MsgUnChoke msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgUnChoke++;

		return peer.network().queueWriter(msg);
	}

	@Override
	public Single<NetworkResult> hookSend(Peer peer,
						 MsgRequest msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgRequest++;

		return peer.network().queueWriter(msg);
	}

	@Override
	public Single<NetworkResult> hookSend(Peer peer,
						 MsgBitfield msg) {

		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgBitfield++;

		return peer.network().queueWriter(msg);
	}

	@Override
	public Single<NetworkResult> hookSend(Peer peer,
						 MsgCancel msg) {

		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgCancel++;

		return peer.network().queueWriter(msg);
	}

	@Override
	public Single<NetworkResult> hookSend(Peer peer,
						 MsgNotInterested msg) {

		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgNotInterest++;

		return peer.network().queueWriter(msg);
	}

	@Override
	public Single<NetworkResult> hookSend(Peer peer,
						 MsgInterested msg) {

		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgInterest++;

		return peer.network().queueWriter(msg);
	}

	@Override
	public Single<NetworkResult> hookSend(Peer peer,
						 MsgPort msg) {

		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgPort++;

		return peer.network().queueWriter(msg);
	}

	@Override
	public Single<NetworkResult> hookSend(Peer peer,
						 MsgHave msg) {

		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgHave++;

		return peer.network().queueWriter(msg);
	}
}
