package org.voyager.torrent.client.network.messages;

/* @doc:
        https://wiki.theory.org/BitTorrentSpecification#Messages
	    <len=0013><id=8><index><begin><length>
        The cancel message is fixed length, and is used to cancel block requests.
        The payload is identical to that of the "request" message. It is typically
        used during "End Game" (see the Algorithms section below).
*/
public class MsgCancel implements Msg{
    public static final int ID = 8;

    private int position;
    private int begin;
    private int length;

    //	<len=0013><id=8><index><begin><length>
    public MsgCancel(){}
    public MsgCancel(int position, int begin, int length){
        this.position = position;
        this.begin = begin;
        this.length = length;
    }

    //	<len=0013><id=8><index><begin><length>
    public MsgCancel(byte[] packet){
        int index = 0;
        if(packet[index++] != ID){
            throw new RuntimeException("Packet Not MsgCancel");
        };
        // <index|position in piece> (4 bytes)
        this.position = (
                ((packet[index++] & 0xFF) << 24)   |
                ((packet[index++] & 0xFF) << 16)   |
                ((packet[index++] & 0xFF) << 8 )   |
                ( packet[index++] & 0xFF)
        );
        // <begin> (4 bytes)
        this.begin = (
                ((packet[index++] & 0xFF) << 24)   |
                ((packet[index++] & 0xFF) << 16)   |
                ((packet[index++] & 0xFF) << 8 )   |
                ( packet[index++] & 0xFF)
        );

        // <length> (4 bytes)
        this.length = (
                ((packet[index++] & 0xFF) << 24)   |
                ((packet[index++] & 0xFF) << 16)   |
                ((packet[index++] & 0xFF) << 8 )   |
                ( packet[index] & 0xFF)
        );

    }

    public int length(){
        // <4 bytes LEN> + <1 byte ID> + <4 bytes INDEX> + <4 bytes BEGIN> + <4 bytes LENGTH>
        return 4 + 1 + 4 + 4 + 4;
    }

    //	<len=0013><id=8><index><begin><length>
    public byte[] toPacket(){
        return new byte[]{
                // <len=0013>
                0, 0, 0, 13,
                // <id=8> (1 byte)
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

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (! (obj instanceof MsgCancel))return false;

        MsgCancel msg = (MsgCancel) obj;

        return position == msg.position &&
               begin == msg.begin &&
               length == msg.length;
    }

    @Override
    public int getID(){ return ID; }

    public int getPosition() {        return position;    }
    public void setPosition(int position) {        this.position = position;    }

    public int getBegin() {   return begin;    }
    public void setBegin(int begin) {       this.begin = begin;    }

    public int getLength() {       return length;    }
    public void setLength(int length) {        this.length = length;    }

    @Override
    public String toString(){
        return "MsgCancel[position: "+ position +", begin: "+ begin +", length: "+ length +"]";
    }
}
