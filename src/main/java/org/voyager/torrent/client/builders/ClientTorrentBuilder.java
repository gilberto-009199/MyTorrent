package org.voyager.torrent.client.builders;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.builders.managers.ManagerAnnounceBuilder;
import org.voyager.torrent.client.builders.managers.ManagerFileBuilder;
import org.voyager.torrent.client.builders.managers.ManagerPeerBuilder;
import org.voyager.torrent.client.builders.strategies.ClientStrategyBuilder;
import org.voyager.torrent.client.files.MagnetLink;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.managers.ManagerAnnounce;
import org.voyager.torrent.client.managers.ManagerFile;
import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.strategy.ClientStrategy;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.io.File;

public class ClientTorrentBuilder {

	private Torrent torrent;
	private ManagerAnnounceBuilder managerAnnounceBuilder;
	private ManagerFileBuilder managerFileBuilder;
	private ManagerPeerBuilder managerPeerBuilder;
	private ClientStrategyBuilder clientStrategyBuilder;

	// Build logic
	public ClientTorrent build(){ return build(this.torrent); }
	public ClientTorrent build(Torrent torrent){

		if(torrent == null)throw new RuntimeException("Not define Torrent");

		if(managerPeerBuilder == null)managerPeerBuilder 			= new ManagerPeerBuilder();
		if(managerFileBuilder == null)managerFileBuilder 			= new ManagerFileBuilder();
		if(managerAnnounceBuilder == null)managerAnnounceBuilder 	= new ManagerAnnounceBuilder();
		if(clientStrategyBuilder == null)clientStrategyBuilder 		= new ClientStrategyBuilder();

		ClientTorrent client = new ClientTorrent(torrent);

		ManagerFile		managerFile		= managerFileBuilder.build(torrent, client);
		ManagerPeer		managerPeer		= managerPeerBuilder.build(torrent, client);
		ManagerAnnounce managerAnnounce = managerAnnounceBuilder.build(torrent, client);
		ClientStrategy 	strategy 		= clientStrategyBuilder.build(client);

		return client.setManagerFile(managerFile)
					.setManagerPeer(managerPeer)
					.setManagerAnnounce(managerAnnounce)
					.setStrategy(strategy);
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
