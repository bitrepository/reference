package org.bitrepository.integrityservice.utils;

/**
 * Util class to handle the presentation of time in a human readable form 
 */
public class TimeUtils {

	private static final int MS_PER_S = 1000;
	private static final int S_PER_M = 60;
	private static final int M_PER_H = 60;
	private static final int H_PER_D = 24;
	private static final int MS_PER_MINUTE = MS_PER_S * S_PER_M;
	private static final int MS_PER_HOUR = MS_PER_MINUTE * M_PER_H;
	private static final long MS_PER_DAY = MS_PER_HOUR * H_PER_D;
	
	/** Private constructor, util class.*/
	private TimeUtils() {}
	
	public String millisecondsToSeconds(long ms) {
		return "" + (ms / MS_PER_S) + "seconds";
	}
	
	public String millisecondsToMinutes(long ms) {
		return "" + (ms / ( MS_PER_S * S_PER_M )) + "minutes";
	}
	
	public String millisecondsToHours(long ms) {
		return "" + (ms /( MS_PER_S * S_PER_M * M_PER_H )) + "hours";
	}
	
	public String millisecondsToDays(long ms) {
		return "" + (ms /( MS_PER_S * S_PER_M * M_PER_H * H_PER_D)) + "days";
	}
	
	public String millisecondsToHuman(long ms) {
		StringBuilder sb = new StringBuilder();
		int days = 0, hours = 0, minutes = 0, seconds = 0;
		if(ms > 0) {
			days = (int) (ms / MS_PER_DAY);
			if(days > 0) {
				sb.append("" + days + "d");
			}
			ms = (ms % MS_PER_DAY);
			if(ms > 0) {
				hours = (int) (ms / MS_PER_HOUR);
				if(hours > 0) {
					sb.append(" " + hours + "h");
				}
				ms = (ms % MS_PER_HOUR);
				if( ms > 0) {
					minutes = (int) (ms / MS_PER_MINUTE);
					if(minutes > 0) {
						sb.append(" " + minutes + "m");
					}
					ms = (ms % MS_PER_MINUTE);
					if(ms > 0) {
						seconds = (int) (ms / MS_PER_S);
						if(seconds > 0) {
							sb.append(" " + seconds + "s");
						}
						ms = (ms % MS_PER_S);
						if(ms > 0) {
							sb.append(" " + ms + "ms");
						}
					}
				}
			}
		}
		return sb.toString();
	}

}
