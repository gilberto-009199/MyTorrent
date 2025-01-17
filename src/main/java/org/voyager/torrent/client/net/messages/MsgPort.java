package org.voyager.torrent.client.net.messages;


import sun.reflect.generics.reflectiveObjects.NotImplementedException;

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
	public void of(byte[] packet) {
		throw new NotImplementedException();
	}

	@Override
	public int getID(){ return ID; }

	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (! (obj instanceof MsgPort))return false;

		MsgPort msg = (MsgPort) obj;

		return port == msg.port;
	}

	@Override
	public String toString(){
		return "MsgPort[port: "+ port +"]";
	}
}
