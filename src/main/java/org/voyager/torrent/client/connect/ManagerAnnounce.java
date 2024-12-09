package org.voyager.torrent.client.connect;

import org.voyager.torrent.client.files.Torrent;

public interface ManagerAnnounce extends Runnable{
    public Torrent getTorrent();

    public ManagerAnnounce withManagerPeer(ManagerPeer managerPeer);
    public ManagerAnnounce withTimeAnnounce(int timeReAnnounceInSec);
    public ManagerAnnounce withTimeVerifyNewsPeersInSecond(int timeVerifyNewsPeersInSecond);

}
