package org.voyager.torrent.client.enums;

import java.nio.charset.StandardCharsets;

public enum ClientTorrentType {
	// https://wiki.theory.org/BitTorrentSpecification#peer_id
	atorrent_for_android("7T","aTorrent for Android", new byte[0]),
	anyevent__bittorrent("AB","AnyEvent::BitTorrent", new byte[0]),
	ares("AG","Ares", new byte[0]),
	aresDOT("A~","Ares", new byte[0]),
	arctic("AR","Arctic", new byte[0]),
	avicora("AV","Avicora", new byte[0]),
	artemis("AT","Artemis", new byte[0]),
	bitpump("AX","BitPump", new byte[0]),
	azureus("AZ","Azureus", new byte[0]),
	bitbuddy("BB","BitBuddy", new byte[0]),
	bitcomet("BC","BitComet", new byte[0]),
	baretorrent("BE","Baretorrent", new byte[0]),
	bitflu("BF","Bitflu", new byte[0]),
	btg_uses_rasterbar_libtorrent("BG","BTG (uses Rasterbar libtorrent)", new byte[0]),
	bitcometlite_uses_6_digit_version_number("BL","BitCometLite (uses 6 digit version number)", new byte[0]),
	bitblinder("BL","BitBlinder", new byte[0]),
	bittorrent_pro_azureus_spyware("BP","BitTorrent Pro (Azureus + spyware)", new byte[0]),
	bitrocket("BR","BitRocket", new byte[0]),
	btslave("BS","BTSlave", new byte[0]),
	mainline_bittorrent_7("BT","mainline BitTorrent (versions >= 7.9)", new byte[0]),
	bbtor("BT","BBtor", new byte[0]),
	bt("Bt","Bt", new byte[0]),
	bitwombat("BW","BitWombat", new byte[0]),
	_bittorrent_x("BX","~Bittorrent X", new byte[0]),
	enhanced_ctorrent("CD","Enhanced CTorrent", new byte[0]),
	ctorrent("CT","CTorrent", new byte[0]),
	delugetorrent("DE","DelugeTorrent", new byte[0]),
	propagate_data_client("DP","Propagate Data Client", new byte[0]),
	ebit("EB","EBit", new byte[0]),
	electric_sheep("ES","electric sheep", new byte[0]),
	filecroc("FC","FileCroc", new byte[0]),
	free_download_manager_5("FD","Free Download Manager (versions >= 5.1.12)", new byte[0]),
	foxtorrent("FT","FoxTorrent", new byte[0]),
	freebox_bittorrent("FX","Freebox BitTorrent", new byte[0]),
	gstorrent("GS","GSTorrent", new byte[0]),
	hekate("HK","Hekate", new byte[0]),
	halite("HL","Halite", new byte[0]),
	hmule_uses_rasterbar_libtorrent("HM","hMule (uses Rasterbar libtorrent)", new byte[0]),
	hydranode("HN","Hydranode", new byte[0]),
	ilivid("IL","iLivid", new byte[0]),
	justseed_it_client("JS","Justseed.it client", new byte[0]),
	javatorrent("JT","JavaTorrent", new byte[0]),
	kget("KG","KGet", new byte[0]),
	ktorrent("KT","KTorrent", new byte[0]),
	leechcraft("LC","LeechCraft", new byte[0]),
	lh_abc("LH","LH-ABC", new byte[0]),
	lphant("LP","Lphant", new byte[0]),
	libtorrent("LT","libtorrent", new byte[0]),
	libtorrentDOT("lt","libTorrent", new byte[0]),
	limewire("LW","LimeWire", new byte[0]),
	meerkat("MK","Meerkat", new byte[0]),
	monotorrent("MO","MonoTorrent", new byte[0]),
	moopolice("MP","MooPolice", new byte[0]),
	miro("MR","Miro", new byte[0]),
	moonlighttorrent("MT","MoonlightTorrent", new byte[0]),
	net__bittorrent("NB","Net::BitTorrent", new byte[0]),
	net_transport("NX","Net Transport", new byte[0]),
	oneswarm("OS","OneSwarm", new byte[0]),
	omegatorrent("OT","OmegaTorrent", new byte[0]),
	protocol__bittorrent("PB","Protocol::BitTorrent", new byte[0]),
	pando("PD","Pando", new byte[0]),
	picotorrent("PI","PicoTorrent", new byte[0]),
	phptracker("PT","PHPTracker", new byte[0]),
	qbittorrent("qB","qBittorrent", new byte[0]),
	qqdownload("QD","QQDownload", new byte[0]),
	qt_4_torrent_example("QT","Qt 4 Torrent example", new byte[0]),
	retriever("RT","Retriever", new byte[0]),
	reztorrent("RZ","RezTorrent", new byte[0]),
	shareaza_alpha_beta("S~","Shareaza alpha/beta", new byte[0]),
	_swiftbit("SB","~Swiftbit", new byte[0]),
	thunder_aka("SD","Thunder (aka XùnLéi)", new byte[0]),
	somud("SM","SoMud", new byte[0]),
	bitspirit("SP","BitSpirit", new byte[0]),
	swarmscope("SS","SwarmScope", new byte[0]),
	symtorrent("ST","SymTorrent", new byte[0]),
	sharktorrent("st","sharktorrent", new byte[0]),
	shareaza("SZ","Shareaza", new byte[0]),
	torch("TB","Torch", new byte[0]),
	terasaur_seed_bank("TE","terasaur Seed Bank", new byte[0]),
	tribler6("TL","Tribler (versions >= 6.1.0)", new byte[0]),
	torrentdotnet("TN","TorrentDotNET", new byte[0]),
	transmission("TR","Transmission", new byte[0]),
	torrentstorm("TS","Torrentstorm", new byte[0]),
	tuotu("TT","TuoTu", new byte[0]),
	uleecher("UL","uLeecher!", new byte[0]),
	µtorrent_for_mac("UM","µTorrent for Mac", new byte[0]),
	µtorrent("UT","µTorrent", new byte[0]),
	vagaa("VG","Vagaa", new byte[0]),
	webtorrent_desktop("WD","WebTorrent Desktop", new byte[0]),
	bitlet("WT","BitLet", new byte[0]),
	webtorrent("WW","WebTorrent", new byte[0]),
	firetorrent("WY","FireTorrent", new byte[0]),
	xfplay("XF","Xfplay", new byte[0]),
	xunlei("XL","Xunlei", new byte[0]),
	xswifter("XS","XSwifter", new byte[0]),
	xantorrent("XT","XanTorrent", new byte[0]),
	xtorrent("XX","Xtorrent", new byte[0]),
	ziptorrent("ZT","ZipTorrent", new byte[0]),

	allPeers("AP","AllPeers", new byte[0]),
	g3Torrent("G3", "G3 Torrent", new byte[0]),

	abc("A","ABC", new byte[0]),
	osprey_permaseed("O","Osprey Permaseed", new byte[0]),
	btqueue("Q","BTQueue", new byte[0]),
	tribler5("R","Tribler (versions < 6.1.0)", new byte[0]),
	shadow_s_client("S","Shadow's client", new byte[0]),
	bittornado("T","BitTornado", new byte[0]),
	upnp_nat_bit_torrent("U","UPnP NAT Bit Torrent", new byte[0]),

	// Clients which have been seen in the wild and need to be identified:
	malwareBD("BD", "??malware??", new byte[0]),
	malwareNP("NP", "??malware??", new byte[0]),
	malwareWF("wF", "??malware??", new byte[0]),
	malwareHK("hk", "??chinese_malware??", new byte[0]),
	noIdentification(null, "no_identification_client", new byte[0]);

	String clientId;
	String clientName;
	byte[] rawPeerId;

	ClientTorrentType(String clientId,
					  String clientName,
					  byte[] rawPeerId){
		this.clientId = clientId;
		this.clientName = clientName;
		this.rawPeerId = rawPeerId;
	}

	/**
	 * Identifica o tipo de cliente BitTorrent com base no peerId.
	 *
	 * @param peerId Array de bytes contendo o peerId.
	 * @return O tipo de cliente correspondente ou {@code noIdentification} se não for encontrado.
	 */
	public static ClientTorrentType fromPeerId(byte[] peerId) {
		if (peerId == null || peerId.length < 2) {
			return noIdentification;
		}

		// Extrai os dois primeiros caracteres do peerId (ignora o "-" inicial)
		String prefix = new String(peerId, 1, 2, StandardCharsets.UTF_8);

		// Procura pelo cliente correspondente
		for (ClientTorrentType client : values()) {
			if (client.clientId != null && client.clientId.equals(prefix)) {
				client.rawPeerId = peerId;
				return client;
			}
		}

		for (ClientTorrentType client : values()) {
			if (client.clientId != null &&
				client.clientId.length() == 1 &&
				client.clientId.equals(prefix.substring(0, 1))) {
				client.rawPeerId = peerId;
				return client;
			}
		}


		return noIdentification; // Cliente desconhecido
	}

	/**
	 * Representação do tipo de cliente como string.
	 *
	 * @return Nome do cliente ou descrição genérica.
	 */
	@Override
	public String toString() {
		return clientName;
	}
}
