package org.voyager.torrent.client.network.messages;

/* @doc:
        https://wiki.theory.org/BitTorrentSpecification#Messages
	    <len=0001><id=1>
        The unchoke message is fixed-length and has no payload.
*/
public class MsgUnChoke implements Msg{

	public static final int ID = 1;

	public int length(){
		// <4 Byte Length><1 Bytes ID>
		return 4 + 1;
	}

	@Override
	public byte[] toPacket() {
		return new byte[]{
				// <len=0001>
				0, 0, 0, 1,
				// <id=0>
				ID
		};
	}

	@Override
	public int getID(){ return ID; }

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (! (obj instanceof MsgChoke))return false;

		return true;
	}

	@Override
	public String toString(){
		return "MsgUnChoke[]";
	}
}
