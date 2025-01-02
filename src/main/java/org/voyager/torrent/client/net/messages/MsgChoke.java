package org.voyager.torrent.client.net.messages;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

/* @doc:
        https://wiki.theory.org/BitTorrentSpecification#Messages
	    <len=0001><id=0>
        The choke message is fixed-length and has no payload.
*/
public class MsgChoke implements Msg{

	public static final int ID = 0;

	@Override
	public byte[] toPacket() {
		return new byte[]{
				// <len=0001>
				0, 0, 0, 1,
				// <id=0>
				ID
		};
	}

	public int length(){
		// <4 bytes LEN> + <1 byte ID>
		return 4 + 1;
	}

	@Override
	public void of(byte[] packet) {
		throw new NotImplementedException();
	}

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (! (obj instanceof MsgChoke))return false;

		return true;
	}

	@Override
	public int getID(){ return ID; }

	@Override
	public String toString(){
		return "MsgChoke[]";
	}
}
