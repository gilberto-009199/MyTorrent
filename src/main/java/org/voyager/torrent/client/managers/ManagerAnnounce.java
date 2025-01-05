package org.voyager.torrent.client.managers;

import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.Torrent;

public interface ManagerAnnounce extends Manager{

    // Getters
    int timeVerifyNewsPeersInSecond();
    ManagerAnnounce setTimeVerifyNewsPeersInSecond(int timeVerifyNewsPeersInSecond);

    int timeReAnnounceInSecond();
    ManagerAnnounce setTimeReAnnounceInSecond(int timeReAnnounceInSecond);

    ClientTorrent client();
    ManagerAnnounce setClient(ClientTorrent client);

}
