package org.voyager.torrent;

import org.voyager.torrent.client.ClientTorrent;

import GivenTools.BencodingException;

public class Main {

	public static void main(String[] args) {
		ClientTorrent mytorrent = new ClientTorrent(true);
		mytorrent.addTorentFile("./netinst.torrent");
		try {
			mytorrent.start();
		} catch (BencodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
