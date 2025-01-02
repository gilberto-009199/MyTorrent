package org.voyager.torrent.client.net.messages;

public interface Msg{
	void of(byte[] packet);
	byte[] toPacket();
	int length();
	int getID();
}
