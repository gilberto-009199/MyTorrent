package org.voyager.torrent;

import java.io.File;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.util.ReaderBencode;

import GivenTools.BencodingException;

public class Main {

	public static void main(String[] args) {
		ClientTorrent mytorrent = new ClientTorrent();
		mytorrent.addTorentFile("./netinst.torrent");
		try {
			mytorrent.start();
		} catch (BencodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
