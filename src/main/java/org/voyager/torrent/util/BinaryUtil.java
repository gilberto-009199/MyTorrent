package org.voyager.torrent.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.util.Map;
import java.util.Random;

public class BinaryUtil {
	
	public static byte[] genBinaryArray(int sizeArray) {
		Random rand = new Random(System.currentTimeMillis());
		byte[] vector = new byte[sizeArray];
	
		for (int i = 0; i < sizeArray; ++i) {
			vector[i] = (byte) ('A' + rand.nextInt(26));
		}
		
		return vector;
	}
	
	public static StringBuffer inputStreamReaderToStringBuffer(InputStreamReader reader) throws IOException {
		
		BufferedReader in = new BufferedReader( reader );
		String inputLine;
		StringBuffer content = new StringBuffer();
		while ((inputLine = in.readLine()) != null) {
		    content.append(inputLine);
		}
		in.close();
		
		return content;
	}
	
	public static ByteBuffer stringToByteBuffer(String text) {
		return ByteBuffer.wrap(	text.getBytes() );
	};

	
}
