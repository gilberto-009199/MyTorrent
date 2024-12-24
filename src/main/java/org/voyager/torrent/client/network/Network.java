package org.voyager.torrent.client.network;

import org.voyager.torrent.client.messages.Msg;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Network{

	// @todo adicionar mais abstrações no futuro

	boolean isRedable();
	boolean isWritable();
	boolean isOpen();

	int write(Msg msg)throws IOException;
	int write(ByteBuffer buffer)throws IOException;
	int write(byte[] buffer)throws IOException ;

	int read(ByteBuffer buffer)throws IOException ;
	int read(byte[] buffer)throws IOException ;
	int read(int bytes)throws IOException ;
	int readFull(ByteBuffer contentBuffer) throws IOException;
}
