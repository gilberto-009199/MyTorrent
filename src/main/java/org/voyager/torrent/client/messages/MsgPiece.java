package org.voyager.torrent.client.messages;


import java.util.Arrays;

/* @doc:
        https://wiki.theory.org/BitTorrentSpecification#Messages
	    <len=0009+X><id=7><index><begin><block>
        The piece message is variable length, where X is the length of the block.
        The payload contains the following information:
            + index: integer specifying the zero-based piece index
            + begin: integer specifying the zero-based byte offset within the piece
            + block: block of data, which is a subset of the piece specified by index.
*/
public class MsgPiece implements Msg{

    public static final int ID = 7;

    private int end;
    private int begin;
    private int position;
    private byte[] block;

    //	<len=0009+X><id=7><index|position><begin><block>
    public MsgPiece(int position, int begin, byte[] block){
        this.position = position;
        this.begin = begin;
        this.end = begin + block.length;
        this.block = block;
    }

    public MsgPiece(byte[] packet){
        int index = 0;
        if(packet[index++] != ID){
            throw new RuntimeException("Packet Not MsgPiece");
        };
        
        this.position = (
            packet[index++] << 24   +
            packet[index++] << 16   +
            packet[index++] << 8    +
            packet[index++]
        );
        this.begin = (
            packet[index++] << 24   +
            packet[index++] << 16   +
            packet[index++] << 8    +
            packet[index++]
        );
        
        this.block = new byte[packet.length - index];
        this.end = begin + block.length;
        System.arraycopy(packet, index, block, 0, block.length);
        
    }
    public int length(){
        // <4 Byte Length><1 Bytes ID><4 Bytes position><4 Bytes begin><X Bytes block>
        return 4 + 1 + 4 + 4 + block.length;
    }

    //	<len=0009+X><id=7><index><begin><block>
    public byte[] toPacket(){
        int length = 9 + block.length;
        byte[] metadata = new byte[]{
        // <len=0009+X>
            (byte)(length >> 24),
            (byte)(length >> 16),
            (byte)(length >> 8),
            (byte)(length),
        // <id=7> (1 byte)
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
            (byte) begin
        };
        
        byte[] packet = new byte[metadata.length + block.length];
    
        System.arraycopy(metadata, 0, packet, 0, metadata.length);
        
        // <block>
        System.arraycopy(block, 0, packet, metadata.length, block.length);
        
        return packet;
    }


    public MsgPiece withPosition(int position){
        this.position = position;
        return this;
    }
    public MsgPiece withBegin(int begin){
        this.begin = begin;
        return this;
    }
    public MsgPiece withBlock(byte[] block){
        this.block = block;
        this.end = begin + block.length;
        return this;
    }

    @Override
    public int getID(){ return ID; }
    public static int getId() { return ID;  }
    public int getPosition() { return position; }
    public void setPosition(int position) {  this.position = position; }
    public int getBegin() {   return begin; }
    public void setBegin(int begin) {  this.begin = begin; }
    public int getEnd() {   return this.end; }
    public void setEnd(int end) {  this.end = end; }
    public byte[] getBlock() {  return block; }
    public void setBlock(byte[] block) { this.block = block; }

    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (! (obj instanceof MsgPiece))return false;

        MsgPiece msg = (MsgPiece) obj;

        return position == msg.position &&
               begin == msg.begin &&
               end == msg.end &&
               Arrays.equals(block, msg.block);
    }

    @Override
    public String toString(){
        //	<len=0009+X><id=7><index><begin><block>
        return "MsgPiece[position: "+ position +", begin: "+ begin+", end: "+ end +", block: [length: "+ (begin - end) +"]]";
    }
}
