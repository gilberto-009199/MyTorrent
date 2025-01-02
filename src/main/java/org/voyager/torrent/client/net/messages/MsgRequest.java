package org.voyager.torrent.client.network.messages;


/* @doc:
        https://wiki.theory.org/BitTorrentSpecification#Messages
	    <len=0013><id=6><index><begin><length>
        The request message is fixed length, and is used to request a block.
        The payload contains the following information:
         + index: integer specifying the zero-based piece index
         + begin: integer specifying the zero-based byte offset within the piece
         + length: integer specifying the requested length.
*/
public class MsgRequest implements Msg{

    public static final int ID = 6;
    
    private int position;
    private int begin;
    private int length;
    
    //	<len=0013><id=6><index><begin><length>
    public MsgRequest(){}
    public MsgRequest(int position, int begin, int length){
        this.position = position;
        this.begin = begin;
        this.length = length;
    }

    public MsgRequest(byte[] packet){
        int index = 0;
        if(packet[index++] != ID){
            throw new RuntimeException("Packet Not MsgRequest");
        };
        
        //	<len=0013><id=6><index><begin><length>
        this.position = (
                ((packet[index++] & 0xFF) << 24)   |
                ((packet[index++] & 0xFF) << 16)   |
                ((packet[index++] & 0xFF) << 8 )   |
                ( packet[index++] & 0xFF)
        );

        this.begin = (
                ((packet[index++] & 0xFF) << 24)   |
                ((packet[index++] & 0xFF) << 16)   |
                ((packet[index++] & 0xFF) << 8 )   |
                ( packet[index++] & 0xFF)
        );

        this.length = (
                ((packet[index++] & 0xFF) << 24)   |
                ((packet[index++] & 0xFF) << 16)   |
                ((packet[index++] & 0xFF) << 8 )   |
                ( packet[index] & 0xFF)
        );
        
    }

	public int length(){
        // <4 Byte Length><1 Bytes ID><4 Bytes position><4 Bytes begin><4 Bytes length>
        return 4 + 1 + 4 + 4 + 4;
    }

    //	<len=0013><id=6><index><begin><length>
    public byte[] toPacket(){
        // int 32 bytes
        // 1 byte => 8 bit 0|1
        // 32 - 24 = 8 
        // 32 - 16 = 16
        // 32 - 8  = 24 
        return new byte[]{
        // <len=0013>
            0, 0, 0, 13,
        // <id=6> (1 byte)
            ID,
        // <index|position in piece> (4 bytes)
            (byte) (position >> 24),
            (byte) (position >> 16),
            (byte) (position >> 8),
            (byte) position,
        // <begin> (4 bytes)
            (byte) (begin >> 24),
            (byte) (begin >> 16),
            (byte) (begin >> 8),
            (byte) begin,
        // <length> (4 bytes)
            (byte) (length >> 24),
            (byte) (length >> 16),
            (byte) (length >> 8),
            (byte) length
        };
    }

    public MsgRequest withPosition(int position){
        this.position = position;
        return this;
    }
    public MsgRequest withBegin(int begin){
        this.begin = begin;
        return this;
    }
    public MsgRequest withLength(int length){
        this.length = length;
        return this;
    }
    public static int getId() { return ID;   }
    public int getPosition() { return position; }
    public void setPosition(int position) {  this.position = position; }
    public int getBegin() {   return begin;  }
    public void setBegin(int begin) {  this.begin = begin; }
    public int getLength() {  return length; }
    public void setLength(int length) { this.length = length; }

    @Override
    public int getID(){ return ID; }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (! (obj instanceof MsgRequest))return false;

        MsgRequest msg = (MsgRequest) obj;

        return position == msg.position &&
               begin == msg.begin &&
               length == msg.length;
    }

    public String toString(){
        //	<len=0013><id=6><index><begin><length>
        return "MsgRequest[position: "+ position +", begin: "+ begin+", length: "+ length +"]";
    }
}
