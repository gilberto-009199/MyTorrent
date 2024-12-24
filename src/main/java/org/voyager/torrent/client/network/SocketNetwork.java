package org.voyager.torrent.client.network;

import org.voyager.torrent.client.messages.Msg;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

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
	public boolean isRedable() {
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
	public int write(Msg msg) throws IOException {
		if (!isWritable()) {
			throw new IOException("Socket is not writable or not connected.");
		}
		byte[] data = msg.toPacket();
		out.write(data);
		out.flush();
		return data.length;
	}

	@Override
	public int write(ByteBuffer buffer) throws IOException {
		if (!isWritable()) {
			throw new IOException("Socket is not writable or not connected.");
		}
		int length = buffer.remaining();
		byte[] data = new byte[length];
		buffer.get(data);
		out.write(data);
		out.flush();
		return length;
	}

	@Override
	public int write(byte[] buffer) throws IOException {
		if (!isWritable()) {
			throw new IOException("Socket is not writable or not connected.");
		}
		out.write(buffer);
		out.flush();
		return buffer.length;
	}

	@Override
	public int read(ByteBuffer buffer) throws IOException {
		if (!isRedable()) {
			throw new IOException("Socket is not readable or not connected.");
		}
		byte[] data = new byte[buffer.remaining()];
		int bytesRead = in.read(data);
		if (bytesRead == -1) {
			closeConnection();
			return -1;
		}
		buffer.put(data, 0, bytesRead);
		return bytesRead;
	}

	@Override
	public int read(byte[] buffer) throws IOException {
		if (!isRedable()) {
			throw new IOException("Socket is not readable or not connected.");
		}
		int bytesRead = in.read(buffer);
		if (bytesRead == -1) {
			closeConnection();
		}
		return bytesRead;
	}

	@Override
	public int read(int bytes) throws IOException {
		if (!isRedable()) {
			throw new IOException("Socket is not readable or not connected.");
		}
		byte[] data = new byte[bytes];
		int bytesRead = in.read(data);
		if (bytesRead == -1) {
			closeConnection();
			return -1;
		}
		return bytesRead;
	}

	@Override
	public int readFull(ByteBuffer contentBuffer) throws IOException {
		if (!isRedable()) {
			throw new IOException("Socket is not readable or not connected.");
		}
		int totalBytesRead = 0;
		while (contentBuffer.hasRemaining()) {
			byte[] chunk = new byte[contentBuffer.remaining()];
			int bytesRead = in.read(chunk);
			if (bytesRead == -1) {
				closeConnection();
				return -1;
			}
			contentBuffer.put(chunk, 0, bytesRead);
			totalBytesRead += bytesRead;
		}
		return totalBytesRead;
	}

	private void closeConnection() throws IOException {
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
	}
}