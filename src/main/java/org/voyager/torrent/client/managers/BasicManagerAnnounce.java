package org.voyager.torrent.client.managers;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.peers.BasicPeer;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.peers.InfoPeer;
import org.voyager.torrent.client.peers.Peer;
import org.voyager.torrent.client.strategy.ManagerAnnounceStrategy;
import org.voyager.torrent.client.strategy.ManagerFileStrategy;
import org.voyager.torrent.client.strategy.Strategy;
import org.voyager.torrent.client.util.AnnounceRequestUtil;

public class BasicManagerAnnounce implements ManagerAnnounce{

    private ClientTorrent client;

    private ManagerAnnounceStrategy strategy;
    private Thread threadCurrent;

    private List<InfoPeer> listInfoPeerRemote = new ArrayList<>();

    private long timeReAnnounceInSecond = 32;
	private long timeVerifyNewsPeersInSecond = 32;

    private Long lastAnnounceTime;
    private Long lastVerifyPeersTime;

    public BasicManagerAnnounce(ClientTorrent client){ this.client = client; }

    @Override
    public void run() {

        initAnnounce();

        while(!isInterrupted()){
            try{
                client.state().semaphoreExecutor().acquire();
                System.out.println("++++++ ManagerAnounce ++++");

                long currentTime = System.currentTimeMillis();

                processAnnounce(currentTime);
                processAddingNewsPeers(currentTime);

            } catch (InterruptedException e) {  Thread.currentThread().interrupt();  } 
              finally {
                System.out.println("------ ManagerAnounce ----");
                client.state().semaphoreExecutor().release();
            }

            long timeLoop = Math.min(timeReAnnounceInSecond, timeVerifyNewsPeersInSecond) * 1000;
            sleep(timeLoop);
        }
    }

    private void initAnnounce() {
        this.lastAnnounceTime       = System.currentTimeMillis() - (timeReAnnounceInSecond       * 1000);
        this.lastVerifyPeersTime    = System.currentTimeMillis() - (timeVerifyNewsPeersInSecond  * 1000);
    }

    public void processAnnounce(long currentTime){

        if(!timeReAnnounceInSecondThen(currentTime))return;

        List<InfoPeer> listInfoPeer = AnnounceRequestUtil
                    .requestAnnounce(client.state().torrent())
                    .orElse(new ArrayList<>(0));

        strategy.hookNewListInfoPeer(this, listInfoPeer);
    }

    public void processAddingNewsPeers(long currentTime){
        if(!timeVerifyNewsPeersInSecondThen(currentTime))return;

        strategy.hookAnnounce(this);
    }

    public boolean timeReAnnounceInSecondThen(long currentTime){
        if((currentTime - lastAnnounceTime) >= timeReAnnounceInSecond * 1000) {
            lastAnnounceTime = currentTime;
            return true;
        }
        return false;
    }

    public boolean timeVerifyNewsPeersInSecondThen(long currentTime){
        if((currentTime - lastVerifyPeersTime) >= timeVerifyNewsPeersInSecond * 1000) {
            lastVerifyPeersTime = currentTime;
            return true;
        }
        return false;
    }
    private void sleep(long ms){ try{Thread.sleep(ms);}catch (Exception e) {}  }
    private boolean isInterrupted(){ return Thread.currentThread().isInterrupted(); }

    public long timeReAnnounceInSecond() { return timeReAnnounceInSecond; }
    public BasicManagerAnnounce setTimeReAnnounceInSecond(long timeReAnnounceInSecond) {
        this.timeReAnnounceInSecond = timeReAnnounceInSecond;
        return this;
    }

    public long timeVerifyNewsPeersInSecond() { return timeVerifyNewsPeersInSecond;  }
    public BasicManagerAnnounce setTimeVerifyNewsPeersInSecond(long timeVerifyNewsPeersInSecond) {
        this.timeVerifyNewsPeersInSecond = timeVerifyNewsPeersInSecond;
        return this;
    }

    @Override
    public ClientTorrent client() { return this.client; }
    @Override
    public ManagerAnnounce setClient(ClientTorrent client) {
        this.client                 = client;
        return this;
    }

    @Override
    public ManagerAnnounceStrategy strategy() { return this.strategy;  }
    @Override
    public ManagerAnnounce setStrategy(Strategy strategy) {
        this.strategy = (ManagerAnnounceStrategy) strategy;
        return this;
    }

    @Override
    public Thread thread() {  return this.threadCurrent; }
    @Override
    public ManagerAnnounce setThread(Thread thread) {
        this.threadCurrent = thread;
        return this;
    }
}
