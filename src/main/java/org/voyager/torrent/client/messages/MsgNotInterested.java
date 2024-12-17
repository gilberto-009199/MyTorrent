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
	public String toString(){
		return "MsgNotInterest[]";
	}
}
