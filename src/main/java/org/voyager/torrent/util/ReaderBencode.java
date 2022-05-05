package org.voyager.torrent.util;


import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Map;

import GivenTools.Bencoder2;
import GivenTools.BencodingException;
import GivenTools.TorrentInfo;

import static java.lang.System.out;

public class ReaderBencode {
	
	public static Map<ByteBuffer,Object> bencodeToMap(StringBuffer content) throws BencodingException{
		return bencodeToMap(content.toString().getBytes());
	}
	public static Map<ByteBuffer,Object> bencodeToMap(String content) throws BencodingException{
		return bencodeToMap(content.getBytes());
	}
	public static Map<ByteBuffer,Object> bencodeToMap(byte[] bencoded_bytes) throws BencodingException{
		return (Map<ByteBuffer,Object>)Bencoder2.decode(bencoded_bytes);
	}
	
	/* read file .torrent*/
	public static TorrentInfo parseTorrentFile(File torrentFile) {
		
		try {
			DataInputStream dataInputStream = new DataInputStream(new FileInputStream(torrentFile));
			long fSize = torrentFile.length();

			if (fSize > Integer.MAX_VALUE || fSize < Integer.MIN_VALUE) {
				dataInputStream.close();
				throw new IllegalArgumentException(fSize + " is too large a torrent filesize for this program to handle");
			}

			byte[] torrentData = new byte[(int)fSize];
			dataInputStream.readFully(torrentData);
			TorrentInfo torrentInfo = new TorrentInfo(torrentData);

			dataInputStream.close();
			
			out.println("Successfully parsed torrent file.");
			
			return torrentInfo;
		} catch (FileNotFoundException e) {
			out.println("Error: File not found");
			return null;
		} catch (IOException e) {
			out.println("errrrr");
			return null;
		} catch (IllegalArgumentException e) {
			out.println("Error: Illegal argument");
			return null;
		} catch (BencodingException e) {
			e.printStackTrace();
			out.println("Error: Invalid torrent file specified");
			return null;
		}
	}
}
