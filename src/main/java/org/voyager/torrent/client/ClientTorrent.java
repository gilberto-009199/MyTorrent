package org.voyager.torrent.client;

import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.managers.BasicManagerAnnounce;
import org.voyager.torrent.client.managers.BasicManagerFile;
import org.voyager.torrent.client.managers.BasicManagerPeer;
import org.voyager.torrent.client.managers.ManagerAnnounce;
import org.voyager.torrent.client.managers.ManagerFile;
import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.strategy.ClientStrategy;

public class ClientTorrent{ 

	private StateClientTorrent state;

	private ManagerAnnounce managerAnnounce;
	private ManagerFile managerFile;
	private ManagerPeer managerPeer;

	private ClientStrategy strategy;

	// @todo add config for parameters or Builder
	public ClientTorrent(String torrentFile){ this(Torrent.of(torrentFile)); }
	public ClientTorrent(Torrent torrent){
		this.state = new StateClientTorrent(this);
		this.state.setTorrent( torrent );
	}

	// @todo add mode simple, server, consumer, seeding
	public void start() { start(1); }
	public void start(int totalThreads) {
        // Stop any existing setup
		boolean semaphoreNullThen = state.semaphoreExecutor() != null;

		if(semaphoreNullThen) stop();

		state.setSemaphoreExecutor(new Semaphore(totalThreads, totalThreads > 1));

        // Initialize managers with shared semaphore
		if(managerAnnounce == null)managerAnnounce 	= new BasicManagerAnnounce(this);
		if(managerPeer == null)managerPeer 			= new BasicManagerPeer(this);
		if(managerFile == null)managerFile 			= new BasicManagerFile(this);

        // Configure dependencies between managers
		managerFile.setClient(this)
				.setStrategy(strategy.fileStrategy());
		
		managerPeer.setClient(this)
				.setStrategy(strategy.peerStrategy());

        managerAnnounce.setClient(this)
				.setStrategy(strategy.announceStrategy());

        // Start the threads
        resume();
    }

    // Resume or start the threads
    public void resume() {
		if (managerFile.thread() == null || !managerFile.thread().isAlive()) {
			managerFile.setThread( new Thread(managerFile, "ManagerFileThread") );
			managerFile.thread().start();
		}

		if (managerPeer.thread() == null || !managerPeer.thread().isAlive()) {
			managerPeer.setThread( new Thread(managerPeer, "ManagerPeerThread") );
			managerPeer.thread().start();
		}

		if (managerAnnounce.thread() == null || !managerAnnounce.thread().isAlive()) {
			managerAnnounce.setThread( new Thread(managerAnnounce, "ManagerAnnounceThread") );
			managerAnnounce.thread().start();
		}

    }

    // Stop all threads and reset the semaphore
    public void stop() {

        if (state.semaphoreExecutor() == null) return;

        // Interrupt and join threads safely
        if (managerAnnounce.thread() != null && managerAnnounce.thread().isAlive()) {
			managerAnnounce.thread().interrupt();
            try {
				managerAnnounce.thread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (managerPeer.thread() != null && managerPeer.thread().isAlive()) {
			managerPeer.thread().interrupt();
            try {
				managerPeer.thread().join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

		if (managerFile.thread() != null && managerFile.thread().isAlive()) {
			managerFile.thread().interrupt();
			try {
				managerFile.thread().join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

		state.setSemaphoreExecutor(null);
    }

	public StateClientTorrent state() { return state; }

	public ManagerFile managerFile() { return managerFile;	}
	public ClientTorrent setManagerFile(ManagerFile managerFile) {
		this.managerFile = managerFile;
		return this;
	}

	public ManagerAnnounce managerAnnounce() { return managerAnnounce;	}
	public ClientTorrent setManagerAnnounce(ManagerAnnounce managerAnnounce) {
		this.managerAnnounce = managerAnnounce;
		return this;
	}

	public ManagerPeer managerPeer() { return managerPeer; }
	public ClientTorrent setManagerPeer(ManagerPeer managerPeer) {
		this.managerPeer = managerPeer;
		return this;
	}
}
