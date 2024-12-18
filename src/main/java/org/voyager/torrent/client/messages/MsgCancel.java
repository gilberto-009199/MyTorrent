package org.voyager.torrent.client.messages;

import org.voyager.torrent.client.files.PiecesMap;

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
                packet[index++] << 24   +
                packet[index++] << 16   +
                packet[index++] << 8    +
                packet[index++]
        );
        // <begin> (4 bytes)
        this.begin = (
                packet[index++] << 24   +
                packet[index++] << 16   +
                packet[index++] << 8    +
                packet[index++]
        );

        // <length> (4 bytes)
        this.length = (
                packet[index++] << 24   +
                packet[index++] << 16   +
                packet[index++] << 8    +
                packet[index]
        );

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

    @Override
    public String toString(){
        return "MsgCancel[position: "+ position +", begin: "+ begin +", length: "+ length +"]";
    }
}