package org.voyager.torrent.client.enums;

import java.nio.charset.StandardCharsets;

public enum ClientTorrentType {
	// https://wiki.theory.org/BitTorrentSpecification#peer_id
	atorrent_for_android("7T","aTorrent for Android"),
	anyevent__bittorrent("AB","AnyEvent::BitTorrent"),
	ares("AG","Ares"),
	aresDOT("A~","Ares"),
	arctic("AR","Arctic"),
	avicora("AV","Avicora"),
	artemis("AT","Artemis"),
	bitpump("AX","BitPump"),
	azureus("AZ","Azureus"),
	bitbuddy("BB","BitBuddy"),
	bitcomet("BC","BitComet"),
	baretorrent("BE","Baretorrent"),
	bitflu("BF","Bitflu"),
	btg_uses_rasterbar_libtorrent("BG","BTG (uses Rasterbar libtorrent)"),
	bitcometlite_uses_6_digit_version_number("BL","BitCometLite (uses 6 digit version number)"),
	bitblinder("BL","BitBlinder"),
	bittorrent_pro_azureus_spyware("BP","BitTorrent Pro (Azureus + spyware)"),
	bitrocket("BR","BitRocket"),
	btslave("BS","BTSlave"),
	mainline_bittorrent_7("BT","mainline BitTorrent (versions >= 7.9)"),
	bbtor("BT","BBtor"),
	bt("Bt","Bt"),
	bitwombat("BW","BitWombat"),
	_bittorrent_x("BX","~Bittorrent X"),
	enhanced_ctorrent("CD","Enhanced CTorrent"),
	ctorrent("CT","CTorrent"),
	delugetorrent("DE","DelugeTorrent"),
	propagate_data_client("DP","Propagate Data Client"),
	ebit("EB","EBit"),
	electric_sheep("ES","electric sheep"),
	filecroc("FC","FileCroc"),
	free_download_manager_5("FD","Free Download Manager (versions >= 5.1.12)"),
	foxtorrent("FT","FoxTorrent"),
	freebox_bittorrent("FX","Freebox BitTorrent"),
	gstorrent("GS","GSTorrent"),
	hekate("HK","Hekate"),
	halite("HL","Halite"),
	hmule_uses_rasterbar_libtorrent("HM","hMule (uses Rasterbar libtorrent)"),
	hydranode("HN","Hydranode"),
	ilivid("IL","iLivid"),
	justseed_it_client("JS","Justseed.it client"),
	javatorrent("JT","JavaTorrent"),
	kget("KG","KGet"),
	ktorrent("KT","KTorrent"),
	leechcraft("LC","LeechCraft"),
	lh_abc("LH","LH-ABC"),
	lphant("LP","Lphant"),
	libtorrent("LT","libtorrent"),
	libtorrentDOT("lt","libTorrent"),
	limewire("LW","LimeWire"),
	meerkat("MK","Meerkat"),
	monotorrent("MO","MonoTorrent"),
	moopolice("MP","MooPolice"),
	miro("MR","Miro"),
	moonlighttorrent("MT","MoonlightTorrent"),
	net__bittorrent("NB","Net::BitTorrent"),
	net_transport("NX","Net Transport"),
	oneswarm("OS","OneSwarm"),
	omegatorrent("OT","OmegaTorrent"),
	protocol__bittorrent("PB","Protocol::BitTorrent"),
	pando("PD","Pando"),
	picotorrent("PI","PicoTorrent"),
	phptracker("PT","PHPTracker"),
	qbittorrent("qB","qBittorrent"),
	qqdownload("QD","QQDownload"),
	qt_4_torrent_example("QT","Qt 4 Torrent example"),
	retriever("RT","Retriever"),
	reztorrent("RZ","RezTorrent"),
	shareaza_alpha_beta("S~","Shareaza alpha/beta"),
	_swiftbit("SB","~Swiftbit"),
	thunder_aka("SD","Thunder (aka XùnLéi)"),
	somud("SM","SoMud"),
	bitspirit("SP","BitSpirit"),
	swarmscope("SS","SwarmScope"),
	symtorrent("ST","SymTorrent"),
	sharktorrent("st","sharktorrent"),
	shareaza("SZ","Shareaza"),
	torch("TB","Torch"),
	terasaur_seed_bank("TE","terasaur Seed Bank"),
	tribler6("TL","Tribler (versions >= 6.1.0)"),
	torrentdotnet("TN","TorrentDotNET"),
	transmission("TR","Transmission"),
	torrentstorm("TS","Torrentstorm"),
	tuotu("TT","TuoTu"),
	uleecher("UL","uLeecher!"),
	µtorrent_for_mac("UM","µTorrent for Mac"),
	µtorrent("UT","µTorrent"),
	vagaa("VG","Vagaa"),
	webtorrent_desktop("WD","WebTorrent Desktop"),
	bitlet("WT","BitLet"),
	webtorrent("WW","WebTorrent"),
	firetorrent("WY","FireTorrent"),
	xfplay("XF","Xfplay"),
	xunlei("XL","Xunlei"),
	xswifter("XS","XSwifter"),
	xantorrent("XT","XanTorrent"),
	xtorrent("XX","Xtorrent"),
	ziptorrent("ZT","ZipTorrent"),

	allPeers("AP","AllPeers"),
	g3Torrent("G3", "G3 Torrent"),

	abc("A","ABC"),
	osprey_permaseed("O","Osprey Permaseed"),
	btqueue("Q","BTQueue"),
	tribler5("R","Tribler (versions < 6.1.0)"),
	shadow_s_client("S","Shadow's client"),
	bittornado("T","BitTornado"),
	upnp_nat_bit_torrent("U","UPnP NAT Bit Torrent"),

	// Clients which have been seen in the wild and need to be identified:
	malwareBD("BD", "??malware??"),
	malwareNP("NP", "??malware??"),
	malwareWF("wF", "??malware??"),
	malwareHK("hk", "??chinese_malware??"),
	noIdentification(null, "no_identification_client");

	String clientId;
	String clientName;

	ClientTorrentType(String clientId, String clientName){
		this.clientId = clientId;
		this.clientName = clientName;
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
				return client;
			}
		}

		for (ClientTorrentType client : values()) {
			if (client.clientId != null &&
				client.clientId.length() == 1 &&
				client.clientId.equals(prefix.substring(0, 1))) {
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
