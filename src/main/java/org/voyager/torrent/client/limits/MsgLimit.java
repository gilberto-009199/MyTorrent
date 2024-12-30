package org.voyager.torrent.client.limits;

import org.voyager.torrent.client.messages.*;

import java.util.concurrent.atomic.AtomicLong;

public class MsgLimit {

	private final CountMsgLimit countLimit;
	private final CountMsgLimit countChokeLimit;
	private final CountMsgLimit countUnChokeLimit;
	private final CountMsgLimit countBitfieldLimit;
	private final CountMsgLimit countCancelLimit;
	private final CountMsgLimit countHaveLimit;
	private final CountMsgLimit countInterestedlLimit;
	private final CountMsgLimit countNotInterestedLimit;
	private final CountMsgLimit countPieceLimit;
	private final CountMsgLimit countRequestLimit;

	public MsgLimit(int maxMsgPeerSecond){
		this.countLimit					= new CountMsgLimit(			 maxMsgPeerSecond);
		this.countChokeLimit			= new CountMsgLimit(Math.min(4,  maxMsgPeerSecond));
		this.countUnChokeLimit			= new CountMsgLimit(Math.min(4,  maxMsgPeerSecond));
		this.countBitfieldLimit			= new CountMsgLimit(Math.min(2,  maxMsgPeerSecond));
		this.countCancelLimit			= new CountMsgLimit(Math.min(10, maxMsgPeerSecond));
		this.countHaveLimit				= new CountMsgLimit(Math.min(4,  maxMsgPeerSecond));
		this.countInterestedlLimit		= new CountMsgLimit(Math.min(2,  maxMsgPeerSecond));
		this.countNotInterestedLimit	= new CountMsgLimit(Math.min(2,  maxMsgPeerSecond));
		this.countPieceLimit			= new CountMsgLimit(Math.min(4,  maxMsgPeerSecond));
		this.countRequestLimit			= new CountMsgLimit(Math.min(5,  maxMsgPeerSecond));
	}

	public MsgLimit(int countLimit,
					int countChokeLimit,
					int countUnChokeLimit,
					int countBitfieldLimit,
					int countCancelLimit,
					int countHaveLimit,
					int countInterestedlLimit,
					int countNotInterestedLimit,
					int countPieceLimit,
					int countRequestLimit){
		this.countLimit					= new CountMsgLimit(countLimit);
		this.countChokeLimit			= new CountMsgLimit(Math.min(countChokeLimit,  			countLimit));
		this.countUnChokeLimit			= new CountMsgLimit(Math.min(countUnChokeLimit,  		countLimit));
		this.countBitfieldLimit			= new CountMsgLimit(Math.min(countBitfieldLimit,  		countLimit));
		this.countCancelLimit			= new CountMsgLimit(Math.min(countCancelLimit, 			countLimit));
		this.countHaveLimit				= new CountMsgLimit(Math.min(countHaveLimit,  			countLimit));
		this.countInterestedlLimit		= new CountMsgLimit(Math.min(countInterestedlLimit, 	countLimit));
		this.countNotInterestedLimit	= new CountMsgLimit(Math.min(countNotInterestedLimit,	countLimit));
		this.countPieceLimit			= new CountMsgLimit(Math.min(countPieceLimit,  			countLimit));
		this.countRequestLimit			= new CountMsgLimit(Math.min(countRequestLimit,  		countLimit));
	}

	public synchronized boolean tryConsume(Msg msg) {
		if(msg == null) return false;

		switch(msg.getID()){
			case MsgHave.ID: 			return tryConsumeMsgHave();
			case MsgPiece.ID: 			return tryConsumeMsgPiece();
			case MsgChoke.ID: 			return tryConsumeMsgChoke();
			case MsgCancel.ID: 			return tryConsumeMsgCancel();
			case MsgRequest.ID: 		return tryConsumeMsgRequest();
			case MsgUnChoke.ID: 		return tryConsumeMsgUnChoke();
			case MsgBitfield.ID: 		return tryConsumeMsgBitfield();
			case MsgInterested.ID: 		return tryConsumeMsgInterested();
			case MsgNotInterested.ID: 	return tryConsumeMsgNotInterested();

			default: 					return tryConsumeMsg();
		}
	}

	public synchronized void reset(Msg msg) {
		if(msg == null) return;

		switch(msg.getID()){
			case MsgHave.ID: 			resetMsgHave(); 			break;
			case MsgChoke.ID:			resetMsgChoke(); 			break;
			case MsgPiece.ID: 			resetMsgPiece(); 			break;
			case MsgCancel.ID: 			resetMsgCancel();			break;
			case MsgUnChoke.ID: 		resetMsgUnChoke(); 			break;
			case MsgRequest.ID: 		resetMsgRequest(); 			break;
			case MsgBitfield.ID: 		resetMsgBitfield(); 		break;
			case MsgInterested.ID: 		resetMsgInterested(); 		break;
			case MsgNotInterested.ID: 	resetMsgNotInterested(); 	break;

			default: 					resetMsg();
		}
	}

	// Limit Generic Msg
	public synchronized boolean tryConsumeMsg() { return countLimit.tryConsume(); }
	public synchronized void resetMsg() { countLimit.reset(); }

	// Limit MsgChoke
	public synchronized boolean tryConsume(MsgChoke msg) { return tryConsumeMsgChoke(); }
	public synchronized boolean tryConsumeMsgChoke() {	return countChokeLimit.tryConsume() && countLimit.tryConsume();  }
	public synchronized void reset(MsgChoke msg) { resetMsgChoke(); }
	public synchronized void resetMsgChoke() {	countChokeLimit.reset(); }

	// Limit MsgUnChoke
	public synchronized boolean tryConsume(MsgUnChoke msg) { return tryConsumeMsgUnChoke(); }
	public synchronized boolean tryConsumeMsgUnChoke() {	return countUnChokeLimit.tryConsume() && countLimit.tryConsume(); }
	public synchronized void reset(MsgUnChoke msg) { resetMsgUnChoke(); }
	public synchronized void resetMsgUnChoke() {	countUnChokeLimit.reset(); }

	// Limit MsgBitfield
	public synchronized boolean tryConsume(MsgBitfield msg) { return tryConsumeMsgUnChoke(); }
	public synchronized boolean tryConsumeMsgBitfield() {	return countBitfieldLimit.tryConsume() && countLimit.tryConsume(); }
	public synchronized void reset(MsgBitfield msg) { resetMsgBitfield(); }
	public synchronized void resetMsgBitfield() {	countBitfieldLimit.reset(); }

	// Limit MsgCancel
	public synchronized boolean tryConsume(MsgCancel msg) { return tryConsumeMsgCancel(); }
	public synchronized boolean tryConsumeMsgCancel() {	return countCancelLimit.tryConsume() && countLimit.tryConsume(); }
	public synchronized void reset(MsgCancel msg) { resetMsgCancel(); }
	public synchronized void resetMsgCancel() {	countCancelLimit.reset(); }

	// Limit MsgHave
	public synchronized boolean tryConsume(MsgHave msg) { return tryConsumeMsgHave(); }
	public synchronized boolean tryConsumeMsgHave() {	return countHaveLimit.tryConsume() && countLimit.tryConsume(); }
	public synchronized void reset(MsgHave msg) { resetMsgHave(); }
	public synchronized void resetMsgHave() {	countHaveLimit.reset(); }

	// Limit MsgInterest
	public synchronized boolean tryConsume(MsgInterested msg) { return tryConsumeMsgInterested(); }
	public synchronized boolean tryConsumeMsgInterested() {	return countInterestedlLimit.tryConsume() && countLimit.tryConsume(); }
	public synchronized void reset(MsgInterested msg) { resetMsgInterested(); }
	public synchronized void resetMsgInterested() {	countInterestedlLimit.reset(); }

	// Limit MsgNotInterested
	public synchronized boolean tryConsume(MsgNotInterested msg) { return tryConsumeMsgNotInterested(); }
	public synchronized boolean tryConsumeMsgNotInterested() {	return countNotInterestedLimit.tryConsume() && countLimit.tryConsume(); }
	public synchronized void reset(MsgNotInterested msg) { resetMsgNotInterested(); }
	public synchronized void resetMsgNotInterested() {	countNotInterestedLimit.reset(); }

	// Limit MsgPiece
	public synchronized boolean tryConsume(MsgPiece msg) { return tryConsumeMsgPiece(); }
	public synchronized boolean tryConsumeMsgPiece() {	return countPieceLimit.tryConsume() && countLimit.tryConsume(); }
	public synchronized void reset(MsgPiece msg) { resetMsgPiece(); }
	public synchronized void resetMsgPiece() {	countPieceLimit.reset(); }

	// Limit MsgRequest
	public synchronized boolean tryConsume(MsgRequest msg) { return tryConsumeMsgRequest(); }
	public synchronized boolean tryConsumeMsgRequest() {	return countRequestLimit.tryConsume() && countLimit.tryConsume(); }
	public synchronized void reset(MsgRequest msg) { resetMsgRequest(); }
	public synchronized void resetMsgRequest() {	countRequestLimit.reset(); }

}


class CountMsgLimit {

	private final long maxCountPerSecond;
	private final AtomicLong currentCount = new AtomicLong(0);
	private volatile long lastResetTime;

	public CountMsgLimit(long maxCountPerSecond) {
		this.maxCountPerSecond = maxCountPerSecond;
		this.lastResetTime = System.currentTimeMillis();
	}
	public synchronized boolean tryConsume() {
		long now = System.currentTimeMillis();
		if (now - lastResetTime >= 1000) {
			currentCount.set(0);
			lastResetTime = now;
		}

		if (currentCount.get() + 1 > maxCountPerSecond) {
			return false;
		}

		currentCount.incrementAndGet();
		return true;
	}
	public synchronized void reset() {
		currentCount.set(0);
		lastResetTime = System.currentTimeMillis();
	}
}