package org.voyager.torrent.client.files;

import java.io.File;
import java.security.KeyStore.Entry;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.voyager.torrent.util.BinaryUtil;
import org.voyager.torrent.util.HttpUtil;
import org.voyager.torrent.util.ReaderBencode;

import GivenTools.TorrentInfo;

public class Torrent {

    public static String separator = System.getProperty("file.separator");
	public static String dirUser = new File(System.getProperty("user.home")).getAbsolutePath()+separator;
	public static String dirRuntime = "."+separator;

    // Data
    
    private int fileLength;
    private byte[] peerId;
    private int pieceLength;
    private String fileName;
    private byte[] infoHash;
    private String announceURL;
    private TorrentInfo torrentInfo;
    private List<byte[]> listPieceHashes;

    public Map<String, String> genAnnounceParameters(){

        if(this.peerId == null)this.peerId =  BinaryUtil.genBinaryArray(20);
        
        int port        = -1,
            uploaded    = 0,
            downloaded  = 0;

        return genAnnounceParameters( HttpUtil.toHexString(this.peerId), port, uploaded, downloaded);
    }

    public Map<String, String> genAnnounceParameters(String peerId){
        int port        = -1,
            uploaded    = 0,
            downloaded  = 0;
        
        return genAnnounceParameters(peerId, port, uploaded, downloaded);
    }

    public Map<String, String> genAnnounceParameters(String peerId, int port){
        int uploaded    = 0,
            downloaded  = 0;
        
        return genAnnounceParameters(peerId, port, uploaded, downloaded);
    }

    public Map<String, String> genAnnounceParameters(String peerId, int port, int uploaded){
        int downloaded  = 0;
        return genAnnounceParameters(peerId, port, uploaded, downloaded);
    }

    public Map<String, String> genAnnounceParameters(String peerId, int port, int uploaded, int downloaded){
        Map<String, String> parameters = new HashMap<String, String>();

        parameters.put("info_hash",     HttpUtil.toHexString(infoHash));
        parameters.put("peer_id",       peerId); // identify par
        parameters.put("port",          String.valueOf(port)); // port connect
        parameters.put("uploaded",      String.valueOf(uploaded));
        parameters.put("downloaded",    String.valueOf(downloaded)); 
        parameters.put("left",          String.valueOf(fileLength));

        return parameters;
    }
   
    public int getFileLength() {   return fileLength;  }
    public void setFileLength(int fileLength) {  this.fileLength = fileLength;  }
    public Torrent withFileLength(int fileLength) { 
        this.fileLength = fileLength;
        return this;
    }

    public int getPieceLength() {  return pieceLength;  }
    public void setPieceLength(int pieceLength) { this.pieceLength = pieceLength;  }
    public Torrent withPieceLength(int pieceLength) { 
        this.pieceLength = pieceLength;
        return this;
    }

    public String getFileName() {  return fileName;  }
    public void setFileName(String fileName) { this.fileName = fileName; }
    public Torrent withFileName(String fileName) { 
        this.fileName = fileName;
        return this;
    }

    public byte[] getInfoHash() { return this.infoHash;  }
    public void setInfoHash(byte[] infoHash) { this.infoHash = infoHash; }
    public Torrent withInfoHash(byte[] infoHash) { 
        this.infoHash = infoHash;
        return this;
    }

    public byte[] getPeerId(){ return this.peerId; }
    public void setPeerId(byte[] peerId){ this.peerId = peerId; }
    public Torrent withPeerId(byte[] peerId){
        this.peerId = peerId;
        return this;
    }

    public String getAnnounceURL() { return announceURL; }
    public void setAnnounceURL(String announceURL) { this.announceURL = announceURL; }
    public Torrent withAnnounceURL(String announceURL) { 
        this.announceURL = announceURL;
        return this;
    }

    public List<byte[]> getListPieceHashes() { return listPieceHashes; }
    public void setListPieceHashes(List<byte[]> listPieceHashes) { this.listPieceHashes = listPieceHashes; }
    public Torrent withListPieceHashes(List<byte[]> listPieceHashes) { 
        this.listPieceHashes = listPieceHashes;
        return this;
    }

	public static TorrentInfo parceTorrentFile(String arquivo){ return parceTorrentFile(new File(arquivo)); }
	public static TorrentInfo parceTorrentFile(File arquivo){
		return ReaderBencode.parseTorrentFile(arquivo);
	}
    
    // @todo create method magnetc link
    public static Torrent of(String path){
        Torrent instance = new Torrent();

        if(path == null || path.isEmpty())throw new RuntimeException("File not exist");

        TorrentInfo info = parceTorrentFile(path);

        if(info == null)throw new RuntimeException("File In format Incorrect");
        
        // @todo remove in future
        instance.torrentInfo = info;

        instance.withAnnounceURL(info.announce_url)
                .withFileLength(info.file_length)
                .withFileName(info.file_name)
                .withInfoHash(info.info_hash.array())
                .withListPieceHashes(Arrays.stream(info.piece_hashes).map(buffer -> buffer.array()).collect(Collectors.toList()))
                .withPieceLength(info.piece_length);

        return instance;
    }
}
