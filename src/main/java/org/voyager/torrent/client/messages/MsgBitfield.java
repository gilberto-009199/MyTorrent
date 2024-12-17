package org.voyager.torrent.client.messages;

import org.voyager.torrent.client.files.PiecesMap;

/* @doc:
 		https://wiki.theory.org/BitTorrentSpecification#Messages
	    <len=0001+X><id=5><bitfield>
		The bitfield message may only be sent immediately after the handshaking
		sequence is completed, and before any other messages are sent. It is optional,
		and need not be sent if a client has no pieces.
*/
public class MsgBitfield implements Msg{

	public static final int ID = 5;

	private PiecesMap map;

	public MsgBitfield(PiecesMap map){	this.map = map;	}
	public MsgBitfield(byte[] packet){

		int index = 0;
		if(packet[index++] != ID){
			throw new RuntimeException("Packet Not MsgBitfield");
		};

		byte[] bitfield = new byte[packet.length - index];
		System.arraycopy(packet, index, bitfield, 0, bitfield.length);
		this.map = new PiecesMap(bitfield, bitfield.length);
	}

	@Override
	public byte[] toPacket() {
		int length = 1 + map.getMap().length;
		byte[] metadata = new byte[]{
				// <len=0001+X>
				(byte)(length >> 24),
				(byte)(length >> 16),
				(byte)(length >> 8),
				(byte)(length),
				// <id=5>
				ID
		};

		byte[] packet = new byte[metadata.length + map.getMap().length];
		System.arraycopy(metadata, 0, packet, 0, metadata.length);

		// <bitfield>
		System.arraycopy(map.getMap(), 0, packet, metadata.length, map.getMap().length);

		//	<len=0001+X><id=5><bitfield>
		return packet;
	}

	@Override
	public String toString(){
		return "MsgBitfield[map: "+ map +"]";
	}
}
