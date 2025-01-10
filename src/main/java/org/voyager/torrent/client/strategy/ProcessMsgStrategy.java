package org.voyager.torrent.client.strategy;

import io.reactivex.rxjava3.core.Single;
import org.voyager.torrent.client.net.messages.*;
import org.voyager.torrent.client.net.socket.NetworkResult;
import org.voyager.torrent.client.peers.Peer;

public interface ProcessMsgStrategy extends Strategy{

	void hookReceive(Peer peer, Msg msg);
	void hookReceive(Peer peer, MsgHandShake msg);
	void hookReceive(Peer peer, MsgChoke msg);
	void hookReceive(Peer peer, MsgUnChoke msg);
	void hookReceive(Peer peer, MsgRequest msg);
	void hookReceive(Peer peer, MsgBitfield msg);
	void hookReceive(Peer peer, MsgCancel msg);
	void hookReceive(Peer peer, MsgNotInterested msg);
	void hookReceive(Peer peer, MsgInterested msg);
	void hookReceive(Peer peer, MsgPort msg);
	void hookReceive(Peer peer, MsgHave msg);

	Single<NetworkResult> hookSend(Peer peer, Msg msg);
	Single<NetworkResult> hookSend(Peer peer, MsgHandShake msg);
	Single<NetworkResult> hookSend(Peer peer, MsgChoke msg);
	Single<NetworkResult> hookSend(Peer peer, MsgUnChoke msg);
	Single<NetworkResult> hookSend(Peer peer, MsgRequest msg);
	Single<NetworkResult> hookSend(Peer peer, MsgBitfield msg);
	Single<NetworkResult> hookSend(Peer peer, MsgCancel msg);
	Single<NetworkResult> hookSend(Peer peer, MsgNotInterested msg);
	Single<NetworkResult> hookSend(Peer peer, MsgInterested msg);
	Single<NetworkResult> hookSend(Peer peer, MsgPort msg);
	Single<NetworkResult> hookSend(Peer peer, MsgHave msg);

}
