package org.voyager.torrent.client.network;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.ByteBuffer;

public class SocketNetwork implements Network{

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
		return socket != null && socket.isConnected() && !socket.isInputShutdown();
	}

	@Override
	public boolean isWritable() {
		return socket != null && socket.isConnected() && !socket.isOutputShutdown();
	}

	@Override
	public boolean isOpen() {
		return socket != null && !socket.isClosed();
	}

	@Override
	public int write(ByteBuffer[] buffers) throws IOException {
		if (!isWritable()) {
			throw new IOException("Socket is not writable or not connected.");
		}
		int totalBytesWritten = 0;
		for (ByteBuffer buffer : buffers) {
			while (buffer.hasRemaining()) {
				byte[] data = new byte[buffer.remaining()];
				buffer.get(data);
				out.write(data);
				totalBytesWritten += data.length;
			}
		}
		out.flush();
		return totalBytesWritten;
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
	public int read(ByteBuffer[] buffers) throws IOException {
		if (!isRedable()) {
			throw new IOException("Socket is not readable or not connected.");
		}
		int totalBytesRead = 0;
		for (ByteBuffer buffer : buffers) {
			byte[] data = new byte[buffer.remaining()];
			int bytesRead = in.read(data);
			if (bytesRead == -1) {
				closeConnection();
				return -1;
			}
			buffer.put(data, 0, bytesRead);
			totalBytesRead += bytesRead;
		}
		return totalBytesRead;
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
		byte[] buffer = new byte[bytes];
		int bytesRead = in.read(buffer);
		if (bytesRead == -1) {
			closeConnection();
		}
		return bytesRead;
	}

	private void closeConnection() throws IOException {
		if (socket != null && !socket.isClosed()) {
			socket.close();
		}
	}
}
