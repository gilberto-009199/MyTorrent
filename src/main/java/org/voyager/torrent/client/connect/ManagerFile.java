package org.voyager.torrent.client.connect;

import java.util.List;
import java.util.concurrent.Semaphore;

public interface ManagerFile extends Runnable{
    public ManagerFile withManagerPeer(ManagerPeer managerPeer);
    public ManagerFile withManagerAnnounce(ManagerAnnounce managerAnnounce);
    public ManagerFile withSemaphoreExecutor(Semaphore semaphoreExecutor);
    public List<MsgRequest> calcMsgRequest();
    public void queueMsg(MsgPiece msg);
    
}
