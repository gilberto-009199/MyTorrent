package org.voyager.torrent.client.messages;


/* @doc:
        https://wiki.theory.org/BitTorrentSpecification#Messages
	    <len=0003><id=9><listen-port>
        The port message is sent by newer versions of the Mainline that implements a DHT tracker.
        The listen port is the port this peer's DHT node is listening on.
        This peer should be inserted in the local routing table (if DHT tracker is supported).
*/
public class MsgPort implements Msg{

	public static final int ID = 9;

	private int port;

	// <len=0003><id=9><listen-port>
	public MsgPort(){}
	public MsgPort(int port){}
	public MsgPort(byte[] packet){
		int index = 0;
		if(packet[index++] != ID){
			throw new RuntimeException("Packet not MsgPort");
		}

		this.port = (int)(
					packet[index++] << 24 +
					packet[index++] << 16 +
					packet[index++] << 8  +
					packet[index]
		);
	}

	// <len=0003><id=9><listen-port>
	@Override
	public byte[] toPacket() {
		return new byte[]{
			// <len=0003>
				0,0,0,3,
			// <id=9>
				ID,
			// <listen-port>
				(byte)(port >> 24),
				(byte)(port >> 16),
				(byte)(port >> 8),
				(byte)(port)
		};
	}

	@Override
	public String toString(){
		return "MsgPort[port: "+ port +"]";
	}
}
