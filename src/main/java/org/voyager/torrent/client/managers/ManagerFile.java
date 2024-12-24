package org.voyager.torrent.client.managers;

import org.voyager.torrent.client.files.PiecesMap;
import org.voyager.torrent.client.messages.MsgPiece;
import org.voyager.torrent.client.messages.MsgRequest;

import java.util.List;
import java.util.concurrent.Semaphore;

public interface ManagerFile extends Runnable{
    ManagerFile withManagerPeer(ManagerPeer managerPeer);
    ManagerFile withManagerAnnounce(ManagerAnnounce managerAnnounce);
    ManagerFile withSemaphoreExecutor(Semaphore semaphoreExecutor);
    List<MsgRequest> calcMsgRequest();
    void queueMsg(MsgPiece msg);
    PiecesMap getMap();
    
}
