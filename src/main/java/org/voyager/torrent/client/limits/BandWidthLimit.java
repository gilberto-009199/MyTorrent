package org.voyager.torrent.client.limits;

import org.voyager.torrent.client.messages.Msg;

import java.util.concurrent.atomic.AtomicLong;

public class BandWidthLimit {

	public long maxBytesPerSecond;
	public AtomicLong currentBytes;
	public volatile long lastResetTime;

	public BandWidthLimit(long maxBytesPerSecond) {
		this.maxBytesPerSecond = maxBytesPerSecond;
		this.currentBytes = new AtomicLong(0);
		this.lastResetTime = System.currentTimeMillis();
	}

	public synchronized boolean tryConsume(Msg msg) { return  tryConsume(msg.length()); }
	public synchronized boolean tryConsume(long bytes) {
		long now = System.currentTimeMillis();

		if (now - lastResetTime >= 1000) {
			currentBytes.set(0);
			lastResetTime = now;
		}

		if (currentBytes.get() + bytes > maxBytesPerSecond) return false;

		currentBytes.addAndGet(bytes);

		return true;
	}

	public synchronized void reset() {
		currentBytes.set(0);
		lastResetTime = System.currentTimeMillis();
	}

	@Override
	public String toString() {
		return "BandWidthRate[" +
				"maxBytesPerSecond: " + maxBytesPerSecond +
				", currentBytes: " + currentBytes +
				", lastResetTime: " + lastResetTime +
				']';
	}
}
