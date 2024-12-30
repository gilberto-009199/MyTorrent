package org.voyager.torrent.client.managers;

import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.Torrent;

public interface ManagerAnnounce extends Runnable{

    // Getters
    Torrent getTorrent();

    // Withs
    ManagerAnnounce withTorrent(Torrent torrent);
    ManagerAnnounce withManagerFile(ManagerFile managerFile);
    ManagerAnnounce withManagerPeer(ManagerPeer managerPeer);
    ManagerAnnounce withClientTorrent(ClientTorrent clientTorrent);
    ManagerAnnounce withSemaphoreExecutor(Semaphore semaphoreExecutor);
    ManagerAnnounce withTimeReAnnounceInSecond(int timeReAnnounceInSecond);
    ManagerAnnounce withTimeVerifyNewsPeersInSecond(int timeVerifyNewsPeersInSecond);

}
