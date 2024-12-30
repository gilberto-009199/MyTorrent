package org.voyager.torrent.client;

import java.util.concurrent.Semaphore;

import org.voyager.torrent.client.builders.ManagerAnnounceBuilder;
import org.voyager.torrent.client.builders.ManagerFileBuilder;
import org.voyager.torrent.client.builders.ManagerPeerBuilder;
import org.voyager.torrent.client.managers.BasicManagerAnnounce;
import org.voyager.torrent.client.managers.BasicManagerFile;
import org.voyager.torrent.client.managers.BasicManagerPeer;
import org.voyager.torrent.client.managers.ManagerAnnounce;
import org.voyager.torrent.client.managers.ManagerFile;
import org.voyager.torrent.client.managers.ManagerPeer;
import org.voyager.torrent.client.files.Torrent;

public class ClientTorrent{ 

	private final Torrent torrent;

	// odeio essa solução mas o quasar e o loom são muito intruzivos
	// fora que o executors e o scheduler são insufientes
	private Semaphore semaphoreExecutor;

	private ManagerFile managerFile;
	private Thread thrManagerFile;

	private ManagerAnnounce managerAnnounce;
	private Thread thrManagerAnnounce;

	private ManagerPeer managerPeer;
	private Thread thrManagerPeer;

	// @todo add config for parameters or Builder
	public ClientTorrent(String torrentFile){ this(Torrent.of(torrentFile)); }
	public ClientTorrent(Torrent torrent){ this.torrent = torrent; }

	// @todo add mode simple, server, consumer, seeding
	public void start() { start(1); }
	public void start(int totalThreads) {
        // Stop any existing setup
        if (semaphoreExecutor != null) stop();

        semaphoreExecutor = new Semaphore(totalThreads, totalThreads > 1);

        // Initialize managers with shared semaphore
		if(managerAnnounce == null)managerAnnounce 	= new BasicManagerAnnounce(this);
		if(managerPeer == null)managerPeer 			= new BasicManagerPeer(this);
		if(managerFile == null)managerFile 			= new BasicManagerFile(this);

        // Configure dependencies between managers
        managerAnnounce.withClientTorrent(this)
						.withSemaphoreExecutor(semaphoreExecutor)
					   	.withManagerPeer(managerPeer)
                       	.withManagerFile(managerFile);

        managerPeer.withClientTorrent(this)
				   .withSemaphoreExecutor(semaphoreExecutor)
				   .withManagerAnnounce(managerAnnounce)
                   .withManagerFile(managerFile);

        managerFile.withClientTorrent(this)
				   .withSemaphoreExecutor(semaphoreExecutor)
				   .withManagerPeer(managerPeer)
                   .withManagerAnnounce(managerAnnounce);

        // Start the threads
        resume();
    }

    // Resume or start the threads
    public void resume() {
        if (thrManagerAnnounce == null || !thrManagerAnnounce.isAlive()) {
            thrManagerAnnounce = new Thread(managerAnnounce, "ManagerAnnounceThread");
            thrManagerAnnounce.start();
        }

		if (thrManagerFile == null || !thrManagerFile.isAlive()) {
			thrManagerFile = new Thread(managerFile, "ManagerFileThread");
			thrManagerFile.start();
		}

        if (thrManagerPeer == null || !thrManagerPeer.isAlive()) {
            thrManagerPeer = new Thread(managerPeer, "ManagerPeerThread");
            thrManagerPeer.start();
        }
    }

    // Stop all threads and reset the semaphore
    public void stop() {

        if (semaphoreExecutor == null) return;

        // Interrupt and join threads safely
        if (thrManagerAnnounce != null && thrManagerAnnounce.isAlive()) {
            thrManagerAnnounce.interrupt();
            try {
                thrManagerAnnounce.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        if (thrManagerPeer != null && thrManagerPeer.isAlive()) {
            thrManagerPeer.interrupt();
            try {
                thrManagerPeer.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

		if (thrManagerFile != null && thrManagerFile.isAlive()) {
			thrManagerFile.interrupt();
			try {
				thrManagerFile.join();
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
			}
		}

        semaphoreExecutor = null;
    }

    public Torrent getTorrent() { return this.torrent;  }
    public ManagerPeer getManagerPeer() { return this.managerPeer;  }
	public ManagerAnnounce getManagerAnnounce() { return this.managerAnnounce; }
	public ManagerFile getManagerFile() { return this.managerFile; }

	public ClientTorrent withManagerFile(ManagerFile managerFile) {
		this.managerFile = managerFile;
		return this;
	}
	public ClientTorrent withManagerPeer(ManagerPeer managerPeer) {
		this.managerPeer = managerPeer;
		return this;
	}
	public ClientTorrent withManagerAnnounce(ManagerAnnounce managerAnnounce) {
		this.managerAnnounce = managerAnnounce;
		return this;
	}
}
