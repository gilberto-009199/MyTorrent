package org.voyager.torrent.client.net.messages;

import org.voyager.torrent.client.enums.ClientTorrentType;
import org.voyager.torrent.client.net.exceptions.HandShakeInvalidException;
import org.voyager.torrent.util.BinaryUtil;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/*
	@doc:
			https://wiki.theory.org/BitTorrentSpecification#Handshake
			<pstrlen><pstr><reserved><info_hash><peer_id>
			handshake: <pstrlen><pstr><reserved><info_hash><peer_id>
				+ pstrlen: string length of <pstr>, as a single raw byte
				+ pstr: string identifier of the protocol
				+ reserved: eight (8) reserved bytes. All current implementations use all zeroes.
				 	Each bit in these bytes can be used to change the behavior of the protocol.
				 	An email from Bram suggests that trailing bits should be used first, so that leading bits may
				  be used to change the meaning of trailing bits.
				+ info_hash: 20-byte SHA1 hash of the info key in the metainfo file.
					This is the same info_hash that is transmitted in tracker requests.
				+ peer_id: 20-byte string used as a unique ID for the client.
					This is usually the same peer_id that is transmitted in tracker requests
				(but not always e.g. an anonymity option in Azureus).
			In version 1.0 of the BitTorrent protocol, pstrlen = 19, and pstr = "BitTorrent protocol".
*/
public class MsgHandShake implements Msg{

	public static final byte[] PROTOCOL	= BinaryUtil.stringToByteBuffer("BitTorrent protocol").array();
	public static final int ID 			= 66;

	private ClientTorrentType clientType;
	private byte[] extension;
	private byte[] protocol;
	private byte[] infoHash;
	private byte[] peerId;

	public MsgHandShake() {}

	// <Identifilter Protocol><Protocol><Extensions Protocol><info_hash><Peer ID>
	public MsgHandShake(byte[] infoHash, byte[] peerId) {
		this.protocol  = PROTOCOL;
		this.extension = new byte[8];
		this.infoHash = infoHash;
		this.peerId = peerId;
		this.clientType = ClientTorrentType.fromPeerId(this.peerId);
	}

	// <Identifilter Protocol><Protocol><Extensions Protocol><info_hash><Peer ID>
	public MsgHandShake(byte[] packet) {
		of(packet);
	}

	public boolean checkHandShake(byte[] packet){ return checkHandShake(packet, this.infoHash);	}
	public boolean checkHandShake(MsgHandShake msg){ return checkHandShake(msg, this.infoHash); }
	public boolean checkHandShake(byte[] packet, byte[] infoHash){ return checkHandShake(new MsgHandShake(packet), infoHash); }
	public static boolean checkHandShake(MsgHandShake msg, byte[] infoHash){
		// verify handshake msg.infoHash == infoHash
		return Arrays.equals(msg.infoHash, infoHash);
	}

	// @todo create metodos for verify extensions

	public int length(){
		// <1 Byte Identifilter Protocol>< X bytes Protocol><8 Bytes Extensions Protocol><X Bytes info_hash><X Bytes Peer ID>
		return 1 + PROTOCOL.length + 8 + infoHash.length + peerId.length;
	}


	// <Identifilter Protocol><Protocol><Extensions Protocol><info_hash><Peer ID>
	@Override
	public byte[] toPacket() {
		int index = 0;
		byte[] handshake = new byte[68];

		// <Identifilter Protocol> 0x13 19	DCN-MEAS	DCN Measurement Subsystems Wikipedia: https://en.wikipedia.org/wiki/List_of_IP_protocol_numbers
		handshake[index] = 0x13;
		index++;

		//<Protocol>
		System.arraycopy(PROTOCOL, 0, handshake, index, PROTOCOL.length);
		index += PROTOCOL.length;

		// <Extensions Protocol> 00000000 - no extension
		System.arraycopy(new byte[8], 0, handshake, index, 8);
		index += 8;

		// <info_hash>
		System.arraycopy(infoHash, 0, handshake, index, infoHash.length);
		index += infoHash.length;

		// <Peer ID>
		System.arraycopy(peerId, 0, handshake, index, peerId.length);

		return handshake;
	}

	// <Identifilter Protocol><Protocol><Extensions Protocol><info_hash><Peer ID>
	@Override
	public void of(byte[] packet) {
		if(packet.length != 68 ) throw new HandShakeInvalidException("Diferente de 68 handshake");

		int index = 0;

		// pstrlen (desconsiderado diretamente, já que sabemos o tamanho fixo)
		index++;

		// <protocol> 20 bytes
		this.protocol = new byte[19];
		System.arraycopy(packet, index, this.protocol, 0, 19);
		index += 19;

		// Verificar se o protocolo é válido

		if (!Arrays.equals(this.protocol, PROTOCOL)){
			try {
				System.out.write(packet);
			} catch (IOException e) {}
			throw new RuntimeException(" Error Invalid protocol identifier: "+ new String(packet, StandardCharsets.US_ASCII));
		}

		// <extension> 8 bytes
		this.extension = new byte[8];
		System.arraycopy(packet, index, this.extension, 0, 8);
		index += 8;

		// <info_hash> 20 bytes
		this.infoHash = new byte[20];
		System.arraycopy(packet, index, this.infoHash, 0, 20);
		index += 20;

		// <peer_id> 20 bytes
		this.peerId = new byte[20];
		System.arraycopy(packet, index, this.peerId, 0, 20);

		this.clientType = ClientTorrentType.fromPeerId(this.peerId);
	}

	public ClientTorrentType getClientType() {	return clientType;	}
	public void setClientType(ClientTorrentType clientType) {	this.clientType = clientType; }

	public byte[] getExtension() {	return extension;}
	public void setExtension(byte[] extension) { this.extension = extension; }

	public byte[] getProtocol() { return protocol; }
	public void setProtocol(byte[] protocol) { this.protocol = protocol; }

	public byte[] getInfoHash() { return infoHash; }
	public void setInfoHash(byte[] infoHash) { this.infoHash = infoHash; }

	public byte[] getPeerId() { return peerId; }
	public void setPeerId(byte[] peerId) { this.peerId = peerId; }

	@Override
	public int getID(){ return ID; }


	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (obj == null) return false;
		if (! (obj instanceof MsgHandShake))return false;

		MsgHandShake msg = (MsgHandShake) obj;

		return Arrays.equals(infoHash, msg.infoHash) &&
			   Arrays.equals(extension, msg.extension) &&
			   Arrays.equals(protocol, msg.protocol) &&
			   Arrays.equals(peerId, msg.peerId);
	}

	public String toString(){
		return "MsgHandShake[protocol: "+ new String(protocol, StandardCharsets.UTF_8) +", clientType: "+ clientType +", peerId: "+ new String(peerId, StandardCharsets.UTF_8) +", extension: "+ Arrays.toString(extension) +", infoHash: "+ Arrays.toString(infoHash) +"]";
	}
}
