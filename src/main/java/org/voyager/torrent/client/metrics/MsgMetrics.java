package org.voyager.torrent.client.metrics;

public class MsgMetrics implements Comparable<MsgMetrics>{

	// Data metrics Msg
	public int countMsg				= 0;
	public int countMsgBitfield		= 0;
	public int countMsgCancel		= 0;
	public int countMsgChoke		= 0;
	public int countMsgHandShake	= 0;
	public int countMsgHave			= 0;
	public int countMsgInterest		= 0;
	public int countMsgNotInterest	= 0;
	public int countMsgPieces		= 0;
	public int countMsgPort			= 0;
	public int countMsgRequest		= 0;
	public int countMsgUnChoke		= 0;

	@Override
	public String toString() {
		return "MsgMetrics[" +
				"countMsg: " + countMsg +
				", countMsgBitfield: " + countMsgBitfield +
				", countMsgCancel: " + countMsgCancel +
				", countMsgHandShake: " + countMsgHandShake +
				", countMsgHave: " + countMsgHave +
				", countMsgInterest: " + countMsgInterest +
				", countMsgNotInterest: " + countMsgNotInterest +
				", countMsgPieces: " + countMsgPieces +
				", countMsgPort: " + countMsgPort +
				", countMsgRequest: " + countMsgRequest +
				", countMsgUnChoke: " + countMsgUnChoke +
				']';
	}

	@Override
	public int compareTo(MsgMetrics metric) { return MsgMetrics.compare(this, metric); }
	public static int compare(MsgMetrics m1, MsgMetrics m2) {

		double sum1 =	(1 		* m1.countMsg ) +
						(2		* m1.countMsgBitfield ) +
						(1.5	* m1.countMsgCancel ) +
						(-1		* m1.countMsgChoke ) +
						(1.1	* m1.countMsgHandShake ) +
						(1.3	* m1.countMsgHave ) +
						(1.4	* m1.countMsgInterest ) +
						(-2		* m1.countMsgNotInterest ) +
						(3		* m1.countMsgPieces ) +
						(0		* m1.countMsgPort ) +
						(2		* m1.countMsgRequest ) +
						(3		* m1.countMsgUnChoke );

		double sum2 =	(1 		* m2.countMsg ) +
						(2		* m2.countMsgBitfield ) +
						(1.5	* m2.countMsgCancel ) +
						(-1		* m2.countMsgChoke ) +
						(1.1	* m2.countMsgHandShake ) +
						(1.3	* m2.countMsgHave ) +
						(1.4	* m2.countMsgInterest ) +
						(-2		* m2.countMsgNotInterest ) +
						(3		* m2.countMsgPieces ) +
						(0		* m2.countMsgPort ) +
						(2		* m2.countMsgRequest ) +
						(3		* m2.countMsgUnChoke );

		return Double.compare(sum1, sum2);
	}
}
