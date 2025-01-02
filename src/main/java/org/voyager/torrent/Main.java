package org.voyager.torrent;

import org.voyager.torrent.client.ClientTorrent;

import org.voyager.torrent.client.builders.*;

public class Main {

	public static void main(String[] args) {

		// Example Simple File Torrent Builder
		ClientTorrent client = ClientTorrentBuilder.of("debian_bookworm12.torrent").build();
		client.start();

		/* Comment Doc clientTorrent

		// Example Simple Magnet Link
		ClientTorrent client2 = ClientTorrentBuilder.of("magnet:?xt=urn:btih:ad42ce8109f54c99613ce38f9b4d87e70f24a165&dn=magnet1.gif&tr=http%3A%2F%2Fbittorrent-test-tracker.codecrafters.io%2Fannounce\n").build();
		client2.start();

		// Example Simple File Torrent Config BandWith 16kb peer second
		ClientTorrent client3 = ClientTorrentBuilder.of("debian_bookworm12.torrent")
				.withManagerPeerBuilder(new ManagerPeerBuilder()
						.withPeerLimitBuilder( new PeerLimitBuilder()
								.withBandWidthLimit( new BandWidthLimit(
										16 * 1024 // 16 kb/s
									)
								)
						)
				).build();

		client3.start();

		// Example Simple File Torrent Config Max MsgPiece(Piece File) send peer Second
		ClientTorrent client4 = ClientTorrentBuilder
				.of("debian_bookworm12.torrent")
				.withManagerPeerBuilder(new ManagerPeerBuilder()
						.withPeerLimitBuilder(
								new PeerLimitBuilder()
										.withMsgLimit(
												new MsgLimit(
														20,// max countLimit,
														 5,	// max countChokeLimit,
														 5,	// max countUnChokeLimit,
														 5,	// max countBitfieldLimit,
														 5,	// max countCancelLimit,
														 5,	// max countHaveLimit,
														 5,	// max countInterestedlLimit,
														 5,	// max countNotInterestedLimit,
														 1,	// max countPieceLimit,
														 5	// max countRequestLimit
												)
										)
						)

				).build();

		client4.start();

		// Example Simple File Torrent Config
		// + 15s Re Announce in Trackers
		// + 8s add new Peers in ManagerPeers
		ClientTorrent client5 = ClientTorrentBuilder.of("debian_bookworm12.torrent")
				.withManagerAnnounceBuilder(
						new ManagerAnnounceBuilder()
								.withTimeReAnnounceInSecond(15)
								.withTimeVerifyNewsPeersInSecond(8)
				).build();

		client5.start();


		 */
		// @todo add example config durant exec
	}

}
