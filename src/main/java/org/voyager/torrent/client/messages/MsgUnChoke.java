package org.voyager.torrent.client.messages;

/* @doc:
        https://wiki.theory.org/BitTorrentSpecification#Messages
	    <len=0001><id=1>
        The unchoke message is fixed-length and has no payload.
*/
public class MsgUnChoke implements Msg{
	public static final int ID = 1;

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
	public String toString(){
		return "MsgUnChoke[]";
	}
}
