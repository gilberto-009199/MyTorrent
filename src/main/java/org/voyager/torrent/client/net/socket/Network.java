package org.voyager.torrent.client.net.socket;

import org.voyager.torrent.client.net.messages.Msg;
import org.voyager.torrent.client.net.messages.MsgHandShake;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Optional;

public interface Network{

	// @todo adicionar mais abstrações no futuro
	boolean isRedable();
	boolean isWritable();
	boolean isOpen();

	NetworkResult write(Msg msg);
	NetworkResult write(ByteBuffer buffer);
	NetworkResult write(byte[] buffer);

	NetworkResult read(int bytes);
	NetworkResult read(byte[] buffer);
	NetworkResult read(MsgHandShake msg);
	NetworkResult readMsg();
	NetworkResult read(ByteBuffer buffer);
	NetworkResult readFull(ByteBuffer contentBuffer);
}
