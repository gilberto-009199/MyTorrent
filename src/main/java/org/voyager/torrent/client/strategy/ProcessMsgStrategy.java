package org.voyager.torrent.client.strategy;

import org.voyager.torrent.client.net.messages.*;
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

	void hookSend(Peer peer, Msg msg);
	void hookSend(Peer peer, MsgHandShake msg);
	void hookSend(Peer peer, MsgChoke msg);
	void hookSend(Peer peer, MsgUnChoke msg);
	void hookSend(Peer peer, MsgRequest msg);
	void hookSend(Peer peer, MsgBitfield msg);
	void hookSend(Peer peer, MsgCancel msg);
	void hookSend(Peer peer, MsgNotInterested msg);
	void hookSend(Peer peer, MsgInterested msg);
	void hookSend(Peer peer, MsgPort msg);
	void hookSend(Peer peer, MsgHave msg);

}
