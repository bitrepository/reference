package dk.bitmagasin.common;

/** 
 * The units for the measuring of time estimates.
 * 
 * @author jolf
 */
public enum TimeUnits {
	/** Seconds.*/
	SECONDS,
	/** Minutes = 60 x seconds */
	MINUTES,
	/** Hours = 3600 x seconds */
	HOURS,
	/** Days = 86400 x seconds */
	DAYS,
	/** (7 days) WEEKS = 604800 x seconds */
	WEEKS,
	/** (30 days) MONTHS = 2592000 x seconds */
	MONTHS,
	/** (365.25 days) YEARS = 31557600 x seconds */
	YEARS,
	/** Time indicating that something is wrong.*/
	NEVER;
	
	public static long getTimeInSeconds(TimeUnits t) {
		switch(t) {
		case SECONDS: return 1L;
		case MINUTES: return 60L;
		case HOURS: return 86400L;
		case WEEKS: return 604800L;
		case MONTHS: return 2592000L;
		case YEARS: return 31557600L;
		// only default is NEVER!
		default: return -1;
		}
	}
}
