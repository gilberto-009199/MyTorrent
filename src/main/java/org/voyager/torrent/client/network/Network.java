package org.voyager.torrent.client.network;

import java.io.IOException;
import java.nio.ByteBuffer;

public interface Network{

	// @todo adicionar mais abstrações no futuro

	public boolean isRedable();
	public boolean isWritable();
	public boolean isOpen();

	int write(ByteBuffer[] buffer)throws IOException;
	int write(byte[] buffer)throws IOException ;

	int read(ByteBuffer[] buffer)throws IOException ;
	int read(byte[] buffer)throws IOException ;
	int read(int bytes)throws IOException ;

}
