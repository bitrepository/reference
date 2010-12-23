/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
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
