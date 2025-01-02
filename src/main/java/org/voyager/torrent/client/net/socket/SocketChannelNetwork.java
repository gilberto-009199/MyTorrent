package org.voyager.torrent.client.net.socket;

import org.voyager.torrent.client.net.exceptions.NoReaderBufferException;
import org.voyager.torrent.client.net.messages.*;

import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Optional;

public class SocketChannelNetwork implements Network {

	private SocketChannel socketChannel;

	public SocketChannelNetwork(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	@Override
	public boolean isRedable() {
		return socketChannel != null && socketChannel.isConnected() && socketChannel.isOpen();
	}

	@Override
	public boolean isWritable() {
		return socketChannel != null && socketChannel.isConnected() && socketChannel.isOpen() && !socketChannel.isBlocking();
	}

	@Override
	public boolean isOpen() {
		return socketChannel != null && socketChannel.isOpen();
	}
git status
	@Override
	public NetworkResult write(ByteBuffer buffers){
		if (!isWritable()) return Optional.of(0);

		try{

			int byteRead =  socketChannel.write(buffers);

			return Optional.of(byteRead);

		}catch (Exception e){ e.printStackTrace(); }

		return Optional.of(0);
	}

	public NetworkResult write(Msg msg){
		if (!isWritable()) return Optional.of(0);

		try{

			ByteBuffer byteBuffer = ByteBuffer.wrap(msg.toPacket());

			int byteRead =  socketChannel.write(byteBuffer);

			return Optional.of(byteRead);

		}catch (Exception e){ e.printStackTrace(); }

		return Optional.of(0);
	}

	@Override
	public NetworkResult write(byte[] buffer){

		if (!isWritable()) return Optional.of(0);

		try{

			ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

			int byteRead =  socketChannel.write(byteBuffer);

			return Optional.of(byteRead);

		}catch (Exception e){ e.printStackTrace(); }

		return Optional.of(0);
	}

	@Override
	public Optional<Integer> read(ByteBuffer buffers){
		if (!isRedable()) return Optional.of(0);

		try {

			int bytesRead = (int) socketChannel.read(buffers);

			if (bytesRead == -1) closeConnection();

			return Optional.of(bytesRead);

		} catch (Exception e){ e.printStackTrace(); }

		return Optional.of(0);
	}

	@Override
	public NetworkResult read(byte[] buffer){

		if (!isRedable())return Optional.of(0);

		try {
			ByteBuffer byteBuffer = ByteBuffer.wrap(buffer);

			int bytesRead = socketChannel.read(byteBuffer);

			if (bytesRead == -1) closeConnection();

			return Optional.of(bytesRead);

		} catch (Exception e){}

		return Optional.of(0);
	}

	public NetworkResult readMsg(){
		if (!isRedable()) return null;

		// <4 bytes> for length, <1 byte> for ID message
		ByteBuffer metadataBuffer = ByteBuffer.allocate(4);

		int bytesRead = read(metadataBuffer).get();

		boolean closeConnected		= bytesRead == -1;
		boolean emptyBuffer			= bytesRead ==  0;

		if(closeConnected) closeConnection();
		if(closeConnected || emptyBuffer){
			return Optional.empty();
		}

		((Buffer) metadataBuffer).flip();

		int length = metadataBuffer.getInt();
		boolean isKeepAlive = length == 0;
		if(isKeepAlive){
			return Optional.empty();
		}

		ByteBuffer contentBuffer = ByteBuffer.allocate(length);

		readFull(contentBuffer);

		((Buffer) contentBuffer).flip();

		byte id = contentBuffer.get();

		System.out.println("Receive: [len: "+ length +", id: "+ id +"] from "+ this);

		return readMsg(id, contentBuffer);
	}

	private NetworkResult readMsg(byte id, ByteBuffer content){

		switch (id) {
			case MsgHave.ID:			return Optional.of( new MsgHave() );
			case MsgPort.ID: 			return Optional.of( new MsgPort(content.array()));
			case MsgChoke.ID:			return Optional.of( new MsgChoke());
			case MsgCancel.ID: 			return Optional.of( new MsgCancel());
			case MsgUnChoke.ID:			return Optional.of( new MsgUnChoke());
			case MsgInterested.ID: 		return Optional.of( new MsgInterested());
			case MsgNotInterested.ID: 	return Optional.of( new MsgNotInterested());
			case MsgPiece.ID: 			return Optional.of( new MsgPiece(content.array()));
			case MsgRequest.ID: 		return Optional.of( new MsgRequest(content.array()));
			case MsgBitfield.ID: 		return Optional.of( new MsgBitfield(content.array()) );

			default:
				System.out.printf("### Error message state: %d%n ###", (int) id);
				System.out.write(content.array(), 0, Math.min(content.capacity(), 100));
				System.out.println("\n### Final unknown message. ###");
		}

		return Optional.empty();
	}

	@Override
	public NetworkResult read(MsgHandShake msg){

		try {


			if (!isRedable()) return Optional.of(0);

			ByteBuffer byteBuffer = ByteBuffer.allocate(68);
			int bytesRead = socketChannel.read(byteBuffer);

			boolean closeConnected		= bytesRead == -1;
			boolean emptyBuffer			= bytesRead ==  0;
			boolean sizeBufferIncorrect	= bytesRead != 68;

			if(closeConnected) closeConnection();

			if(closeConnected || emptyBuffer || sizeBufferIncorrect) return Optional.of(0);

			((Buffer) byteBuffer).flip();
			msg.of(byteBuffer.array());

		}catch (Exception e){ return Optional.of(0); }

		return Optional.of(1);
	}

	@Override
	public NetworkResult read(int bytes){
		if (!isRedable()) {
			return Optional.of(0);
		}

		try {

			ByteBuffer byteBuffer = ByteBuffer.allocate(bytes);
			int bytesRead = socketChannel.read(byteBuffer);
			if (bytesRead == -1) {
				closeConnection();
			} else if (bytesRead == 0) {
				return 0; // Nada para ler no momento
			}
			((Buffer) byteBuffer).flip();

			return Optional.of(bytesRead);

		}catch (Exception e){}

		return Optional.of(0);
	}

	@Override
	public NetworkResult readFull(ByteBuffer buffer){
		if (!isRedable()) {
			throw new IOException("SocketChannel is not readable or not connected.");
		}
		int totalBytesRead = 0;
		while (buffer.hasRemaining()) {
			int bytesRead = socketChannel.read(buffer);
			if (bytesRead == -1) {
				closeConnection();
				System.out.println("Connection closed by peer while reading message.");
				return -1;
			}
			totalBytesRead += bytesRead;
		}
		return totalBytesRead;
	}


	private void closeConnection() {
		try{
			if (socketChannel != null && socketChannel.isOpen()) {
				socketChannel.close();
			}
		}catch (Exception e){  }
	}
}
