package org.voyager.torrent.client.net.socket;

import org.voyager.torrent.client.net.messages.Msg;

public class NetworkResult {

	private boolean success;
	private byte[] buffer;
	private Msg msg;

	public NetworkResult(boolean success){ this.success = success;	}
	public NetworkResult(byte[] buffer){ this.buffer = buffer;	}
	public NetworkResult(Msg msg){ this.msg = msg;	}
	public NetworkResult(boolean success, byte[] buffer){
		this.success = success;
		this.buffer = buffer;
	}
	public NetworkResult(boolean success, Msg msg){
		this.success = success;
		this.msg = msg;
	}
	public NetworkResult(boolean success, byte[] buffer, Msg msg){
		this.success = success;
		this.buffer = buffer;
		this.msg = msg;
	}

	public boolean success() { return success;	}
	public NetworkResult setSuccess(boolean success) {
		this.success = success;
		return this;
	}

	public byte[] buffer() { return buffer;	}
	public NetworkResult setBuffer(byte[] buffer) {
		this.buffer = buffer;
		return this;
	}

	public Msg msg() {	return msg;	}
	public NetworkResult setMsg(Msg msg) {
		this.msg = msg;
		return this;
	}
}
