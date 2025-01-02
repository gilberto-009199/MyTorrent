package org.voyager.torrent.client.managers;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.net.messages.MsgPiece;
import org.voyager.torrent.client.net.messages.MsgRequest;

import java.util.List;
import java.util.concurrent.Semaphore;

public interface ManagerFile extends Runnable{

    // Actions
    void queueMsg(MsgPiece msg);
    List<MsgRequest> calcMsgRequest();

    // Getters
    PiecesMap getMap();
    Torrent getTorrent();
    List<MsgRequest> msgRequest();

    // Withs
    ManagerFile withTorrent(Torrent torrent);
    ManagerFile withManagerPeer(ManagerPeer managerPeer);
    ManagerFile withSemaphoreExecutor(Semaphore semaphoreExecutor);
    ManagerFile withClientTorrent(ClientTorrent clientTorrent);
    ManagerFile withManagerAnnounce(ManagerAnnounce managerAnnounce);

}
