package org.voyager.torrent.client.managers;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.peers.BasicPeer;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.util.AnnounceRequestUtil;

public class BasicManagerAnnounce implements ManagerAnnounce{

    private Torrent torrent;
    private ClientTorrent client;
    private ManagerPeer managerPeer;
    private ManagerFile managerFile;
    private Semaphore semaphoreExecutor;
    
    private int timeReAnnounceInSecond = 32;
	private int timeVerifyNewsPeersInSecond = 32;

    private Long lastAnnounceTime;
    private Long lastVerifyPeersTime;

    public BasicManagerAnnounce(Torrent torrent){
        this.torrent = torrent;
    }
    public BasicManagerAnnounce(ClientTorrent client){
        this(client.getTorrent());
        this.client = client;
        this.managerPeer = client.getManagerPeer();
        this.managerFile = client.getManagerFile();
    }

    @Override
    public void run() {

        this.lastAnnounceTime       = System.currentTimeMillis() - timeReAnnounceInSecond       * 1000;
        this.lastVerifyPeersTime    = System.currentTimeMillis() - timeVerifyNewsPeersInSecond  * 1000;

        int timeLoop = Math.min(timeReAnnounceInSecond, timeVerifyNewsPeersInSecond) * 1000;

        while(!isInterrupted()){
            try{
                semaphoreExecutor.acquire();
                System.out.println("++++++++ ManagerAnounce ++++");

                process();

            } catch (InterruptedException e) {  Thread.currentThread().interrupt();  } 
              finally {
                semaphoreExecutor.release();
            }
            System.out.println("------ ManagerAnounce ----");
            sleep(timeLoop);
        }
    }

    
    public void process(){

        long currentTime = System.currentTimeMillis();

        Optional<List<BasicPeer>> optListPeers = null;

        // if timeReAnnounceInSecond
        if((currentTime - lastAnnounceTime) >= timeReAnnounceInSecond * 1000) {
            optListPeers = AnnounceRequestUtil.requestAnnounce(this.torrent);

            lastAnnounceTime = currentTime;
        }
    
        // if timeVerifyNewsPeersInSecond
        if((currentTime - lastVerifyPeersTime) >= timeVerifyNewsPeersInSecond * 1000) {
            if(optListPeers != null && optListPeers.isPresent()) {
                for (BasicPeer peer : optListPeers.get().subList(0, Math.min(20, optListPeers.get().size()))) {

                    peer.withPeerId(torrent.getPeerId())
                        .withInfoHash(torrent.getInfoHash());

                    managerPeer.queueNewsPeerIfNotPresent(peer);
                }
            }
            lastVerifyPeersTime = currentTime;
        }

    }

    private void sleep(long ms){ try{Thread.sleep(ms);}catch (Exception e) {}  }
    private boolean isInterrupted(){ return Thread.currentThread().isInterrupted(); }

    @Override
    public Torrent getTorrent() { return this.torrent; }

    @Override
    public ManagerAnnounce withTorrent(Torrent torrent) {
        this.torrent = torrent;
        return this;
    }

    @Override
    public ManagerAnnounce withManagerPeer(ManagerPeer managerPeer) {
        this.managerPeer = managerPeer;
        return this;
    }

    @Override
    public ManagerAnnounce withClientTorrent(ClientTorrent clientTorrent) {
        this.client = clientTorrent;
        return this;
    }

    @Override
    public ManagerAnnounce withTimeReAnnounceInSecond(int timeReAnnounceInSecond) {
        this.timeReAnnounceInSecond = timeReAnnounceInSecond;
        return this;
    }

    @Override
    public ManagerAnnounce withTimeVerifyNewsPeersInSecond(int timeVerifyNewsPeersInSecond) {
        this.timeVerifyNewsPeersInSecond = timeVerifyNewsPeersInSecond;
        return this;
    }

    @Override
    public ManagerAnnounce withManagerFile(ManagerFile managerFile) {
        this.managerFile = managerFile;
        return this;
    }

    @Override
    public ManagerAnnounce withSemaphoreExecutor(Semaphore semaphoreExecutor){
        this.semaphoreExecutor = semaphoreExecutor;
        return this;
    }
}
