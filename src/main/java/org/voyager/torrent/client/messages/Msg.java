package org.voyager.torrent.client.messages;

public interface Msg{
	byte[] toPacket();
	int length();
	int getID();
}
