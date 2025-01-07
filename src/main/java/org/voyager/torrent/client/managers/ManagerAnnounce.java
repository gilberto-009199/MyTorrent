package org.voyager.torrent.client.managers;

import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.strategy.ManagerAnnounceStrategy;
import org.voyager.torrent.client.strategy.ManagerPeerStrategy;

public interface ManagerAnnounce extends Manager{

    // Getters
    long timeVerifyNewsPeersInSecond();
    ManagerAnnounce setTimeVerifyNewsPeersInSecond(long timeVerifyNewsPeersInSecond);

    long timeReAnnounceInSecond();
    ManagerAnnounce setTimeReAnnounceInSecond(long timeReAnnounceInSecond);

    ManagerAnnounceStrategy strategy();

    ClientTorrent client();
    ManagerAnnounce setClient(ClientTorrent client);

}
