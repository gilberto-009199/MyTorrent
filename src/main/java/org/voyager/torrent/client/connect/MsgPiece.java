package org.voyager.torrent.client.connect;

import java.nio.ByteBuffer;

public class MsgPiece {

    public static final int ID = 7;
    
    private int begin;
    private int position;
    private byte[] block;

    
    //	<len=0009+X><id=7><index><begin><block>
    public MsgPiece(int position, int begin, byte[] block){
        this.position = position;
        this.begin = begin;
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
        System.arraycopy(packet, index, block, 0, block.length);
        
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
        
        return packet;  // Retorna o pacote completo
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
        return this;
    }
    public static int getId() { return ID;  }
    public int getPosition() { return position; }
    public void setPosition(int position) {  this.position = position; }
    public int getBegin() {   return begin; }
    public void setBegin(int begin) {  this.begin = begin; }
    public byte[] getBlock() {  return block; }
    public void setBlock(byte[] block) { this.block = block; }

    public String toString(){
        //	<len=0009+X><id=7><index><begin><block>
        return "MsgPiece[position: "+ position +", begin: "+ begin+", block: [length: "+ block.length +"]]";
    }
}
