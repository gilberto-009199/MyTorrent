package org.voyager.torrent.client.managers;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.strategy.Strategy;

public interface Manager extends Runnable{

	ClientTorrent client();
	Manager setClient(ClientTorrent client);

	Strategy strategy();
	Manager setStrategy(Strategy managerAnnounceThread);

	Thread  thread();
	Manager setThread(Thread thread);

}
