package org.voyager.torrent;

import org.voyager.torrent.client.ClientTorrent;

import GivenTools.BencodingException;

public class Main {

	public static void main(String[] args) {
		ClientTorrent mytorrent = new ClientTorrent("netinst.torrent", true);

		mytorrent.start();
		
	}

}
