package org.voyager.torrent.client.network;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
public class ChannelNetwork implements Network {

	private SocketChannel socketChannel;

	public ChannelNetwork(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	@Override
	public boolean isRedable() {
		return socketChannel != null && socketChannel.isConnected();
	}

	@Override
	public boolean isWritable() {
		return socketChannel != null && socketChannel.isConnected();
	}

	@Override
	public boolean isOpen() {
		return socketChannel != null && socketChannel.isOpen();
	}

	@Override
	public int write(ByteBuffer[] buffers) throws IOException {
		if (!isWritable()) {
			throw new IOException("SocketChannel is not writable or not connected.");
		}
		return (int) socketChannel.write(buffers);
	}

	@Override
	public int write(byte[] buffer) throws IOException {
		if (!isWritable()) {
			throw new IOException("SocketChannel is not writable or not connected.");
		}
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		return socketChannel.write(byteBuffer);
	}

	@Override
	public int read(ByteBuffer[] buffers) throws IOException {
		if (!isRedable()) {
			throw new IOException("SocketChannel is not readable or not connected.");
		}
		int bytesRead = (int) socketChannel.read(buffers);
		if (bytesRead == -1) {
			closeConnection();
		}
		return bytesRead;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		if (!isRedable()) {
			throw new IOException("SocketChannel is not readable or not connected.");
		}
		ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);
		int bytesRead = socketChannel.read(byteBuffer);
		if (bytesRead == -1) {
			closeConnection();
		}
		return bytesRead;
	}

	@Override
	public int read(int bytes) throws IOException {
		if (!isRedable()) {
			throw new IOException("SocketChannel is not readable or not connected.");
		}
		ByteBuffer byteBuffer = ByteBuffer.allocate(bytes);
		int bytesRead = socketChannel.read(byteBuffer);
		if (bytesRead == -1) {
			closeConnection();
		} else if (bytesRead == 0) {
			return 0; // Nada para ler no momento
		}
		byteBuffer.flip();
		return bytesRead;
	}

	private void closeConnection() throws IOException {
		if (socketChannel != null && socketChannel.isOpen()) {
			socketChannel.close();
		}
	}
}
