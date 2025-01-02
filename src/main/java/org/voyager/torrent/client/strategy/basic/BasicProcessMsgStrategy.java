package org.voyager.torrent.client.strategy.basic;

import org.voyager.torrent.client.net.limits.BandWidthLimit;
import org.voyager.torrent.client.net.messages.*;
import org.voyager.torrent.client.net.metrics.BandWidthMetrics;
import org.voyager.torrent.client.net.metrics.MsgMetrics;
import org.voyager.torrent.client.net.socket.Network;
import org.voyager.torrent.client.peers.Peer;
import org.voyager.torrent.client.strategy.ProcessMsgStrategy;

public class BasicProcessMsgStrategy implements ProcessMsgStrategy {

	@Override
	public void hookReceive(Peer peer, Msg msg) {

		if(peer.network().isRedable())return;

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
	public void hookReceive(Peer peer, MsgHandShake msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgHandShake++;
	}

	@Override
	public void hookReceive(Peer peer, MsgChoke msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgChoke++;
	}

	@Override
	public void hookReceive(Peer peer, MsgUnChoke msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgUnChoke++;

	}

	@Override
	public void hookReceive(Peer peer, MsgRequest msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgRequest++;
	}

	@Override
	public void hookReceive(Peer peer, MsgBitfield msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgBitfield++;
	}

	@Override
	public void hookReceive(Peer peer, MsgCancel msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgCancel++;
	}


	@Override
	public void hookReceive(Peer peer, MsgInterested msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgInterest++;
	}
	@Override
	public void hookReceive(Peer peer, MsgNotInterested msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgNotInterest++;
	}


	@Override
	public void hookReceive(Peer peer, MsgPort msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgPort++;
	}

	@Override
	public void hookReceive(Peer peer, MsgHave msg) {
		MsgMetrics metric = peer.statePeer().metrics().msgMetrics;
		metric.countMsgHave++;

	}

	@Override
	public void hookSend(Peer peer, Msg msg) {

		if(peer.network().isWritable())return;

		BandWidthLimit limit = peer.statePeer().limits().bandWidthLimit;
		BandWidthMetrics metric = peer.statePeer().metrics().bandWidthMetrics;

		if(!limit.tryConsume(msg))return;

		metric.addUploaderBytes(msg.length());

		if(msg instanceof  MsgPort)hookSend(peer, (MsgPort) msg);
		else if(msg instanceof  MsgHave)hookSend(peer, (MsgHave) msg);
		else if(msg instanceof  MsgChoke)hookSend(peer, (MsgChoke) msg);
		else if(msg instanceof  MsgCancel)hookSend(peer, (MsgCancel) msg);
		else if(msg instanceof  MsgUnChoke)hookSend(peer, (MsgUnChoke) msg);
		else if(msg instanceof  MsgRequest)hookSend(peer, (MsgRequest) msg);
		else if(msg instanceof  MsgBitfield)hookSend(peer, (MsgBitfield) msg);
		else if(msg instanceof  MsgHandShake)hookSend(peer, (MsgHandShake) msg);
		else if(msg instanceof  MsgInterested)hookSend(peer, (MsgInterested) msg);
		else if(msg instanceof  MsgNotInterested)hookSend(peer, (MsgNotInterested) msg);
		else peer.network().write(msg).orElseThrow(null);

	}

	@Override
	public void hookSend(Peer peer, MsgHandShake msg) {
		Network net = peer.network();

	}

	@Override
	public void hookSend(Peer peer, MsgChoke msg) {	}

	@Override
	public void hookSend(Peer peer, MsgUnChoke msg) {	}

	@Override
	public void hookSend(Peer peer, MsgRequest msg) {	}

	@Override
	public void hookSend(Peer peer, MsgBitfield msg) {	}

	@Override
	public void hookSend(Peer peer, MsgCancel msg) {	}

	@Override
	public void hookSend(Peer peer, MsgNotInterested msg) {	}

	@Override
	public void hookSend(Peer peer, MsgInterested msg) {	}

	@Override
	public void hookSend(Peer peer, MsgPort msg) {	}

	@Override
	public void hookSend(Peer peer, MsgHave msg) {	}
}
