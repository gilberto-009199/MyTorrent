package org.voyager.torrent.client.connect;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.Torrent;

public class BasicManagerAnnounce implements ManagerAnnounce{

    private Torrent torrent;
    private ClientTorrent client;
    private ManagerPeer managerPeer;

    private int timeReAnnounceInSecond = 60;
	private int timeVerifyNewsPeersInSecond = 60;

    public BasicManagerAnnounce(ClientTorrent client){
        this.client = client;
        this.torrent = client.getTorrent();
        this.managerPeer = client.getManagerPeer();
    }

    @Override
    public void run() {
        while(true){       
            
            sleep(timeReAnnounceInSecond);
            // logic

            sleep(timeVerifyNewsPeersInSecond);
            // logic

        }
    }

    private void sleep(long ms){ try{Thread.sleep(ms);}catch (Exception e) {}  }

    @Override
    public Torrent getTorrent() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTorrent'");
    }

    @Override
    public ManagerAnnounce withManagerPeer(ManagerPeer managerPeer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withManagerPeer'");
    }

    @Override
    public ManagerAnnounce withTimeAnnounce(int timeReAnnounceInSec) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withTimeAnnounce'");
    }

    @Override
    public ManagerAnnounce withTimeVerifyNewsPeersInSecond(int timeVerifyNewsPeersInSecond) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withTimeVerifyNewsPeersInSecond'");
    }
    
    
}
