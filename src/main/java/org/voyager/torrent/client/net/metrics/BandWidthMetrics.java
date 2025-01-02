package org.voyager.torrent.client.net.metrics;

import java.util.concurrent.atomic.AtomicLong;

public class BandWidthMetrics implements Comparable<BandWidthMetrics>{

	public long countBytesUploaded;
	public long countBytesDownloaded;
	public long maxBytesPerSecond;

	// controller Max BytesPerSecond
	public AtomicLong currentBytes;
	public long lastResetTime;

	public BandWidthMetrics() {
		this.countBytesUploaded = 0;
		this.countBytesDownloaded = 0;
		this.maxBytesPerSecond = 0;
		this.currentBytes = new AtomicLong(0);
		this.lastResetTime = System.currentTimeMillis();
	}

	public void addUploaderBytes(long bytes) {
		countBytesUploaded += bytes;
		addBytes(bytes);
	}

	public void addDownloaderBytes(long bytes) {
		countBytesDownloaded += bytes;
		addBytes(bytes);
	}

	// Controle de envio/recebimento
	public void addBytes(long bytes) {
		long now = System.currentTimeMillis();

		synchronized (this) {
			if (now - lastResetTime >= 1000) {
				currentBytes.set(0);
				lastResetTime = now;
			}
		}

		if (currentBytes.get() + bytes > maxBytesPerSecond) {
			maxBytesPerSecond = currentBytes.get() + bytes;
		}

		currentBytes.addAndGet(bytes);
	}

	@Override
	public int compareTo(BandWidthMetrics bandWidthMetrics) { return BandWidthMetrics.compare(this, bandWidthMetrics); }
	public static int compare(BandWidthMetrics bw1, BandWidthMetrics bw2) {
		long sum1	=	(1 * bw1.countBytesUploaded) +
						(2 * bw1.countBytesDownloaded);

		long sum2	=	(1 * bw2.countBytesUploaded) +
						(2 * bw2.countBytesDownloaded);

		return Long.compare(sum1, sum2);
	}

	@Override
	public String toString() {
		return "BandWidthMetrics[" +
				"totalBytesUploaded: " + countBytesUploaded +
				", totalBytesDownloaded: " + countBytesDownloaded +
				", maxBytesPerSecond: " + maxBytesPerSecond +
				", currentBytes: " + currentBytes +
				']';
	}
}