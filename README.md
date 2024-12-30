[![Testar no Browser](https://raw.githubusercontent.com/gilberto-009199/JAgendaWeb/master/gitpod.svg)](https://gitpod.io#https://github.com/gilberto-009199/MyTorrent)
## My Torrent

  Torrent client implemented in Java, project that references the implementation of the biTorrent protocol from the article in [Blog](https://app.gitbook.com/@gilberto-tec/s/blog/java/torrent-client).
  May God help me finish it by Christmas lol!!!

   ```java
    ClientTorrent client = ClientTorrentBuilder.of("debian_bookworm12.torrent").build();
    client.start(); // non blocking main-threat
   ```

   Tarefas/Tasks:
   + .torrent OK!
   + magnet:link
   + announce servers/trackers OK!
   + connect others peers OK!
   + Implement Life Cycle Peer:
      + [MsgHandshake](./src/main/java/org/voyager/torrent/client/messages/MsgHandShake.java)     OK!
      + [MsgBitfield](./src/main/java/org/voyager/torrent/client/messages/MsgBitfield.java)      OK
      + [MsgRequest](./src/main/java/org/voyager/torrent/client/messages/MsgRequest.java)       ~
      + [MsgPiece](./src/main/java/org/voyager/torrent/client/messages/MsgPiece.java)              ~
      + [MsgCancel](./src/main/java/org/voyager/torrent/client/messages/MsgCancel.java) OK
      + [MsgInterested](./src/main/java/org/voyager/torrent/client/messages/MsgInterested.java) OK
      + [MsgNotInterested](./src/main/java/org/voyager/torrent/client/messages/MsgNotInterested.java) OK
      + [MsgChoke](./src/main/java/org/voyager/torrent/client/messages/MsgChoke.java)  ~
      + [MsgUnChoke](./src/main/java/org/voyager/torrent/client/messages/MsgUnChoke.java) ~
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
	
	

