package org.voyager.torrent.client.managers;

import org.voyager.torrent.client.ClientTorrent;
import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.files.Torrent;
import org.voyager.torrent.client.net.messages.MsgPiece;
import org.voyager.torrent.client.net.messages.MsgRequest;
import org.voyager.torrent.client.strategy.ManagerAnnounceStrategy;
import org.voyager.torrent.client.strategy.ManagerFileStrategy;

import java.util.List;
import java.util.concurrent.Semaphore;

public interface ManagerFile extends Manager{

    // Actions
    void queueMsg(MsgPiece msg);
    List<MsgRequest> calcMsgRequest();

    // Getters
    PiecesMap getMap();
    Torrent getTorrent();
    List<MsgRequest> msgRequest();

    ManagerFileStrategy strategy();

    ClientTorrent client();
    ManagerFile setClient(ClientTorrent client);


}
