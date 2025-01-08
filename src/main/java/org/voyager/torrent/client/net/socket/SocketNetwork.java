package org.voyager.torrent.client.net.socket;

import io.reactivex.rxjava3.core.Single;
import org.voyager.torrent.client.net.messages.Msg;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.Optional;

public class SocketNetwork implements Network {

	private Socket socket;
	private InputStream in;
	private OutputStream out;

	public SocketNetwork(Socket socket) throws IOException {
		this.socket = socket;
		this.in = socket.getInputStream();
		this.out = socket.getOutputStream();
	}

	@Override
	public boolean isReadable() {
		return socket != null && !socket.isClosed() && socket.isConnected();
	}

	@Override
	public boolean isWritable() {
		return socket != null && !socket.isClosed() && socket.isConnected();
	}

	@Override
	public boolean isOpen() {
		return socket != null && !socket.isClosed();
	}

	@Override
	public void nextWrite() {

	}

	@Override
	public void nextRead() {

	}

	@Override
	public Single<NetworkResult> queueWriter(Msg msg) {
		return null;
	}

	@Override
	public Optional<NetworkResult> queueReader() {
		return Optional.empty();
	}

	@Override
	public Single<NetworkResult> readHandshake() {
		return null;
	}


	private void closeConnection() throws IOException {
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
	}
}