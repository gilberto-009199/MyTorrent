package org.voyager.torrent;

import org.voyager.torrent.client.ClientTorrent;

import GivenTools.BencodingException;

public class Main {

	public static void main(String[] args) {
		//ClientTorrent mytorrent = new ClientTorrent("debian_jesse8.torrent", true);
		ClientTorrent mytorrent = new ClientTorrent("debian_bookworm12.torrent");
		mytorrent.start();
		
	}

}
