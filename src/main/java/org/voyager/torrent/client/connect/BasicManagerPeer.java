package org.voyager.torrent.client.connect;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.Torrent;

public class BasicManagerPeer implements ManagerPeer{

    private Torrent torrent;
    private ClientTorrent client;
    private ManagerAnnounce managerPeer;

    private int maxUploaderPeerSecond = -1;
	private int maxDownloaderPeerSecond = -1;

    public BasicManagerPeer(ClientTorrent client){
        this.client = client;
        this.torrent = client.getTorrent();
        this.managerPeer = client.getManagerAnnounce();
    }

    @Override
    public void run() {

        while(true){
            sleep(50);

            // logic
            
        }

    }

    private void sleep(long ms){ try{Thread.sleep(ms);}catch (Exception e) {}  }

    @Override
    public ManagerPeer withManagerAnnounce(ManagerAnnounce managerAnnounce) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withManagerAnnounce'");
    }

    @Override
    public ManagerPeer withMaxUploaderPeerSecond(int maxUploaderPeerSecond) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withMaxUploaderPeerSecond'");
    }

    @Override
    public ManagerPeer withMaxDownloaderPeerSecond(int maxDownloaderPeerSecond) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'withMaxDownloaderPeerSecond'");
    }

    @Override
    public Torrent getTorrent() {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getTorrent'");
    }

    @Override
    public void addQueue(Peer peer, MsgRequest msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addQueue'");
    }

    @Override
    public void addQueue(Peer peer, MsgPiece msg) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addQueue'");
    }

    @Override
    public boolean connectError(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'connectError'");
    }

    @Override
    public boolean shakeHandsError(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'shakeHandsError'");
    }

    @Override
    public boolean downloaded(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'downloaded'");
    }

    @Override
    public boolean uploaded(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'uploaded'");
    }

    @Override
    public void addInterestPeer(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'addInterestPeer'");
    }

    @Override
    public void removeInterestPeer(Peer peer) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'removeInterestPeer'");
    }

    

}
