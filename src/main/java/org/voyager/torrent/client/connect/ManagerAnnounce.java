package org.voyager.torrent.client.connect;

import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.files.Torrent;

public interface ManagerAnnounce extends Runnable{
    public Torrent getTorrent();
    
    public ManagerAnnounce withManagerFile(ManagerFile managerFile);
    public ManagerAnnounce withManagerPeer(ManagerPeer managerPeer);
    public ManagerAnnounce withSemaphoreExecutor(Semaphore semaphoreExecutor);
    public ManagerAnnounce withTimeReAnnounceInSecond(int timeReAnnounceInSecond);
    public ManagerAnnounce withTimeVerifyNewsPeersInSecond(int timeVerifyNewsPeersInSecond);

}
