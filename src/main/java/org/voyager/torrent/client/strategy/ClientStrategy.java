package org.voyager.torrent.client.strategy;

public interface ClientStrategy extends Strategy{
	ManagerPeerStrategy peerStrategy();
	ManagerFileStrategy fileStrategy();
	ManagerAnnounceStrategy announceStrategy();
}
