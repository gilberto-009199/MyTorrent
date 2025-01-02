package org.voyager.torrent.client.network.messages;

public interface Msg{
	byte[] toPacket();
	int length();
	int getID();
}
