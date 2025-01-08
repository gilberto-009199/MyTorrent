package org.voyager.torrent.client.net.socket;

import io.reactivex.rxjava3.core.Single;
import org.voyager.torrent.client.net.messages.Msg;

import java.util.Optional;

public interface Network{

	// @todo adicionar mais abstrações no futuro
	boolean isReadable();
	boolean isWritable();
	boolean isOpen();

	void nextWrite();
	void nextRead();

	Single<NetworkResult> queueWriter(Msg msg);
	Optional<NetworkResult> queueReader();

	Single<NetworkResult> readHandshake();
}
