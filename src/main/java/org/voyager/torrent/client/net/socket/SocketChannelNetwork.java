package org.voyager.torrent.client.net.socket;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import org.voyager.torrent.client.net.messages.*;

import java.io.EOFException;
import java.io.IOException;
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.NonWritableChannelException;
import java.nio.channels.SocketChannel;
import java.util.AbstractMap.SimpleEntry;
import java.util.Map.Entry;

import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class SocketChannelNetwork implements Network {

	private SocketChannel socketChannel;

	// Writter
	private Queue<Entry<SingleEmitter<NetworkResult>, Msg>> queueMsgWriter = new ConcurrentLinkedQueue<>();
	private Queue<Msg> queueMsgReader = new ConcurrentLinkedQueue<>();

	private Entry<SingleEmitter<NetworkResult>, Msg> currentMsg;
	private ByteBuffer currentBuffer;

	public SocketChannelNetwork(SocketChannel socketChannel) {
		this.socketChannel = socketChannel;
	}

	public boolean isOpen() {
		return socketChannel != null && socketChannel.isOpen();
	}
	@Override
	public boolean isReadable() { return isOpen() && socketChannel.isConnected(); }
	@Override
	public boolean isWritable() { return isOpen() && socketChannel.isConnected() && !socketChannel.isBlocking();}
	@Override


	public  void nextWrite() {
		boolean noEmptyBufferAndNotRemaining = currentBuffer != null && !currentBuffer.hasRemaining();
		boolean noEmptyQueue = !queueMsgWriter.isEmpty();

		// Logic alter Msg in Writer buffer
		if(!noEmptyBufferAndNotRemaining && noEmptyQueue){
			currentMsg = queueMsgWriter.element();
			currentBuffer = ByteBuffer.wrap( currentMsg.getValue().toPacket() );
		}

		if(currentBuffer == null)return;

		SingleEmitter<NetworkResult> emitter = currentMsg.getKey();

		try{

			int bytesRead = socketChannel.write(currentBuffer);

			if(bytesRead < 0){
				queueMsgWriter.remove(currentMsg);
				currentMsg		= null;
				currentBuffer 	= null;
				emitter.tryOnError(new NonWritableChannelException());
			}

			if(!currentBuffer.hasRemaining()) {
				emitter.onSuccess(new NetworkResult(true));
				currentMsg		= null;
				currentBuffer 	= null;
			}

		}catch (IOException ex){
			queueMsgWriter.remove(currentMsg);
			currentMsg		= null;
			currentBuffer 	= null;
			if(emitter.isDisposed())emitter.tryOnError( ex );
		}



	}

	public void nextRead() {
		if (!isReadable()) return;

		// <4 bytes> for length, <1 byte> for ID message
		try {

			ByteBuffer metadataBuffer = ByteBuffer.allocate(4);

			int bytesRead = socketChannel.read(metadataBuffer);

			boolean closeConnected = bytesRead == -1;
			boolean emptyBuffer = bytesRead == 0;

			if (closeConnected) closeConnection();
			if (closeConnected || emptyBuffer) return;

			((Buffer) metadataBuffer).flip();

			int length = metadataBuffer.getInt();

			boolean isKeepAlive = length == 0;

			if (isKeepAlive) return;


			ByteBuffer contentBuffer = ByteBuffer.allocate(length);

			readFull(contentBuffer)
			.doOnSuccess(content -> {

				((Buffer) contentBuffer).flip();

				byte id = content.get();

				queueMsg(id, contentBuffer);

			})
			.doOnError(error -> {
				System.err.println("Erro: " + error.getMessage());
			})
			.subscribe();

		} catch (Exception ex) {	}

	}

	@Override
	public  Single<NetworkResult> queueWriter(Msg msg) {
		return Single.create(emitter -> {
			queueMsgWriter.add(new SimpleEntry<>(emitter, msg));
		});
	}

	@Override
	public Optional<NetworkResult> queueReader() {

		if (queueMsgReader.isEmpty()) return Optional.empty();

		NetworkResult result = new NetworkResult(queueMsgReader.poll()).setSuccess(true);

		return Optional.of(result);

	}



	private void queueMsg(byte id,
						  ByteBuffer content){
		System.out.println("Receive: [len: " + content.capacity() + ", id: " + id + "]");

		switch (id) {
			case MsgHave.ID:			queueMsgReader.add( new MsgHave());						break;
			case MsgPort.ID: 			queueMsgReader.add( new MsgPort(content.array()));		break;
			case MsgChoke.ID:			queueMsgReader.add( new MsgChoke());					break;
			case MsgCancel.ID: 			queueMsgReader.add( new MsgCancel());					break;
			case MsgUnChoke.ID:			queueMsgReader.add( new MsgUnChoke());					break;
			case MsgInterested.ID: 		queueMsgReader.add( new MsgInterested());				break;
			case MsgNotInterested.ID: 	queueMsgReader.add( new MsgNotInterested());			break;
			case MsgPiece.ID: 			queueMsgReader.add( new MsgPiece(content.array()));		break;
			case MsgRequest.ID: 		queueMsgReader.add( new MsgRequest(content.array()));	break;
			case MsgBitfield.ID: 		queueMsgReader.add( new MsgBitfield(content.array()));	break;

			default:
				System.out.printf("### Error message state: %d%n ###", (int) id);
				System.out.write(content.array(), 0, Math.min(content.capacity(), 100));
				System.out.println("\n### Final unknown message. ###");
		}

	}

	@Override
	public Single<NetworkResult> readHandshake() {
		return Single.create(event -> {
			if (!isReadable()) event.onSuccess( new NetworkResult(false) );

			try {

				ByteBuffer contentBuffer = ByteBuffer.allocate(68);

				int bytesRead = socketChannel.read(contentBuffer);

				boolean closeConnected = bytesRead == -1;
				boolean emptyBuffer = bytesRead == 0;

				if (closeConnected) closeConnection();
				if (closeConnected || emptyBuffer) {
					event.onSuccess( new NetworkResult(false) );
					return;
				}

				((Buffer) contentBuffer).flip();

				int length = contentBuffer.getInt();

				boolean isKeepAlive = length == 0;

				if (isKeepAlive) {
					event.onSuccess( new NetworkResult(false) );
					return;
				}

				Msg msg = new MsgHandShake(contentBuffer.array());

				event.onSuccess(
						new NetworkResult(
								true,
								msg
						)
				);

			} catch (Exception ex) {  event.tryOnError(ex);	}

		});

	}

	public Single<ByteBuffer> readFull(ByteBuffer contentBuffer) {
		return Single.create(emitter -> {
			try {
				while (contentBuffer.hasRemaining()) {
					int bytesRead = socketChannel.read(contentBuffer);

					if (bytesRead == -1) {
						emitter.tryOnError(new EOFException("SocketChannel was closed before reading the full buffer."));
						return;
					}

					if (bytesRead == 0) {
						Thread.yield();
					}
				}

				// A leitura foi completada com sucesso.
				emitter.onSuccess(contentBuffer);

			} catch (IOException e) {
				// Notifica erro ao emitter.
				emitter.tryOnError(e);
			}
		});
	}

	private void closeConnection() {
		try{
			if (socketChannel != null && socketChannel.isOpen()) {
				socketChannel.close();
			}
		}catch (Exception e){  }
	}
}
