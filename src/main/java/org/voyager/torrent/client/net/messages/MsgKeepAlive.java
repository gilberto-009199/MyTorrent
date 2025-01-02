package org.voyager.torrent.client.net.messages;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class MsgKeepAlive implements Msg{

	@Override
	public byte[] toPacket() {
		return new byte[1];
	}

	@Override
	public int length() {
		return 1;
	}

	@Override
	public void of(byte[] packet) {
		throw new NotImplementedException();
	}

	@Override
	public int getID() {
		return 0;
	}
}
