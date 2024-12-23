package org.voyager.torrent.client.messages;

/* @doc:
        https://wiki.theory.org/BitTorrentSpecification#Messages
	    <len=0001><id=3>
        The not interested message is fixed-length and has no payload.
*/
public class MsgNotInterested implements Msg{

	public static final int ID = 3;

	// <len=0001><id=3>
	public MsgNotInterested(){}
	//	<len=0001><id=3>
	public MsgNotInterested(byte[] packet){
		int index = 0;
		if(packet[index++] != ID){
			throw new RuntimeException("Packet Not MsgNotInterested");
		};
	}
	public int length(){
		// <4 Byte Length><1 Bytes ID>
		return 4 + 1;
	}
	//	<len=0001><id=3>
	public byte[] toPacket(){
		return new byte[]{
				// <len=0001>
				0, 0, 0, 1,
				// <id=3> (1 byte)
				ID
		};
	}

	@Override
	public int getID(){ return ID; }

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (! (obj instanceof MsgNotInterested))return false;

		return true;
	}

	@Override
	public String toString(){
		return "MsgNotInterest[]";
	}
}
