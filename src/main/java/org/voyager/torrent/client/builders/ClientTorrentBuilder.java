package org.voyager.torrent.client.builders;

import jdk.nashorn.internal.runtime.regexp.joni.constants.Arguments;
import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.MagnetLink;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.managers.ManagerAnnounce;
import org.voyager.torrent.client.managers.ManagerFile;
import org.voyager.torrent.client.managers.ManagerPeer;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;

public class ClientTorrentBuilder {

	private Torrent torrent;
	private ManagerAnnounceBuilder managerAnnounceBuilder;
	private ManagerFileBuilder managerFileBuilder;
	private ManagerPeerBuilder managerPeerBuilder;

	// Build logic
	public ClientTorrent build(){ return build(this.torrent); }
	public ClientTorrent build(Torrent torrent){

		if(torrent == null)throw new RuntimeException("Not define Torrent");

		if(managerPeerBuilder == null)managerPeerBuilder 			= new ManagerPeerBuilder();
		if(managerFileBuilder == null)managerFileBuilder 			= new ManagerFileBuilder();
		if(managerAnnounceBuilder == null)managerAnnounceBuilder 	= new ManagerAnnounceBuilder();

		ManagerFile		managerFile		= managerFileBuilder.build(torrent);
		ManagerPeer		managerPeer		= managerPeerBuilder.build(torrent);
		ManagerAnnounce managerAnnounce = managerAnnounceBuilder.build(torrent);

		return new ClientTorrent(torrent)
					.withManagerFile(managerFile)
					.withManagerPeer(managerPeer)
					.withManagerAnnounce(managerAnnounce);

	}

	public static ClientTorrentBuilder of(String fileOrMagnetLink){
		if(!fileOrMagnetLink.startsWith("magnet:?"))return ClientTorrentBuilder.of(new File(fileOrMagnetLink));
		else return ClientTorrentBuilder.of(new MagnetLink(fileOrMagnetLink));
	}
	public static ClientTorrentBuilder of(File file){
		return new ClientTorrentBuilder().withTorrent(Torrent.of(file));
	}

	public static ClientTorrentBuilder of(MagnetLink link){
		throw new NotImplementedException();
	}

	// With
	public ClientTorrentBuilder withManagerAnnounceBuilder(ManagerAnnounceBuilder managerAnnounceBuilder){
		this.managerAnnounceBuilder = managerAnnounceBuilder;
		return this;
	}

	public ClientTorrentBuilder withManagerFileBuilder(ManagerFileBuilder managerFileBuilder){
		this.managerFileBuilder = managerFileBuilder;
		return this;
	}

	public ClientTorrentBuilder withManagerPeerBuilder(ManagerPeerBuilder managerPeerBuilder){
		this.managerPeerBuilder = managerPeerBuilder;
		return this;
	}

	private ClientTorrentBuilder withTorrent(Torrent torrent) {
		this.torrent = torrent;
		return this;
	}
}
