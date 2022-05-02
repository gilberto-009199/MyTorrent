package org.voyager.torrent.client;

import java.io.File;

public class ClientTorrent { 
	
	public static String separator = System.getProperty("file.separator");
	public static String dirUser = new File(System.getProperty("user.home")).getAbsolutePath()+separator;
	public static String dirRuntime = "."+separator;
	
	
	public ClientTorrent Build() {
		return null;
   }
}
