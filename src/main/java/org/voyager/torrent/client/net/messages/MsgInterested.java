package org.voyager.torrent.client.network.messages;

/* @doc:
        https://wiki.theory.org/BitTorrentSpecification#Messages
	    <len=0001><id=2>
        The interested message is fixed-length and has no payload.
*/
public class MsgInterested implements Msg{

	public static final int ID = 2;

	// <len=0001><id=2>
	public MsgInterested(){}
	//	<len=0001><id=2>
	public MsgInterested(byte[] packet){
		int index = 0;
		if(packet[index++] != ID){
			throw new RuntimeException("Packet Not MsgInterested");
		};
	}

	public int length(){
		// <4 Byte Length><1 Bytes ID>
		return 4 + 1;
	}
	//	<len=0001><id=2>
	public byte[] toPacket(){
		return new byte[]{
				// <len=0001>
				0, 0, 0, 1,
				// <id=4> (1 byte)
				ID
		};
	}

	@Override
	public int getID(){ return ID; }

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (! (obj instanceof MsgInterested))return false;

		return true;
	}

	@Override
	public String toString(){
		return "MsgInterest[]";
	}

}
