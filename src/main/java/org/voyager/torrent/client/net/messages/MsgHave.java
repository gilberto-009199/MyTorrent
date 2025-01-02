package org.voyager.torrent.client.network.messages;


/* @doc:
        https://wiki.theory.org/BitTorrentSpecification#Messages
	    <len=0005><id=4><piece index>
        The have message is fixed length. The payload is the
        zero-based index of a piece that has just been successfully
        downloaded and verified via the hash.
*/
public class MsgHave implements Msg{

	public static final int ID = 4;

	private int position;

	//	<len=0005><id=4><piece index>
	public MsgHave(){}
	public MsgHave(int position){
		this.position = position;
	}

	//	<len=0005><id=4><piece index>
	public MsgHave(byte[] packet){
		int index = 0;
		if(packet[index++] != ID){
			throw new RuntimeException("Packet Not MsgHave");
		};
		// <piece index> (4 bytes)
		this.position = (
				((packet[index++] & 0xFF) << 24)   |
				((packet[index++] & 0xFF) << 16)   |
				((packet[index++] & 0xFF) << 8 )   |
				( packet[index] & 0xFF)
		);
	}

	public int length(){
		// <4 Byte Length><1 Bytes ID><4 Bytes position>
		return 4 + 1 + 4;
	}

	//	<len=0005><id=4><piece index>
	public byte[] toPacket(){
		return new byte[]{
				// <len=0005>
				0, 0, 0, 5,
				// <id=4> (1 byte)
				ID,
				// <piece index> (4 bytes)
				(byte) (position >> 24),
				(byte) (position >> 16),
				(byte) (position >> 8),
				(byte) position
		};
	}

	@Override
	public int getID(){ return ID; }

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (! (obj instanceof MsgHave))return false;

		MsgHave msg = (MsgHave) obj;

		return position == msg.position;
	}

	@Override
	public String toString(){
		return "MsgHave[position: "+ position +"]";
	}
}
