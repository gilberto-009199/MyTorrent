[![Testar no Browser](https://raw.githubusercontent.com/gilberto-009199/JAgendaWeb/master/gitpod.svg)](https://gitpod.io#https://github.com/gilberto-009199/MyTorrent)
## My Torrent

  Torrent client implemented in Java, project that references the implementation of the biTorrent protocol from the article in [Blog](https://app.gitbook.com/@gilberto-tec/s/blog/java/torrent-client).
  May God help me finish it by Christmas lol!!!

   ```java
   ClientTorrent mytorrent = new ClientTorrent("debian_bookworm12.torrent");
   mytorrent.start(); // non blocking main-threat
   ```

   Tarefas/Tasks:
   + .torrent OK!
   + magnet:link
   + announce servers/trackers OK!
   + connect others peers OK!
   + Implement Life Cycle Peer:
      + send MsgHandshake     OK!
      + send MsgBitfield      OK
      + send MsgRequest       ~
      + MsgPiece              ~
      + MsgCancel 
      + MsgNotInterested/MsgInterested OK
      + MsgChoke/MsgUnChoke  ~
      + Otimize and Mitigation Cases
   + Mounted File
     + Create file OK!
     + Verify File for Sha1 OK!
     + Mounted file for MsgPieces ~
   + adding interface for config client
   + adding listener event client
   + return CompletableFuture in Start

## Referencias

   [WikiBitTorrent](https://wiki.theory.org/Main_Page)
	
	

