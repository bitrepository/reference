/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.common.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.xml.datatype.XMLGregorianCalendar;

/**
 * Util class to handle the presentation of time in a human readable form 
 */
public final class TimeUtils {
    /** Milliseconds per second.*/
    public static final int MS_PER_S = 1000;
    /** Seconds per minute.*/
    public static final int S_PER_M = 60;
    /** Minutes per hour.*/
    public static final int M_PER_H = 60;
    /** Hours per day.*/
    public static final int H_PER_D = 24;
    /** Days per year in a 'normal' 365 day year. */
    public static final int DAYS_PER_YEAR = 365;
    /** Milliseconds per minute.*/
    public static final int MS_PER_MINUTE = MS_PER_S * S_PER_M;
    /** Milliseconds per hour.*/
    public static final int MS_PER_HOUR = MS_PER_MINUTE * M_PER_H;
    /** Milliseconds per day.*/
    public static final long MS_PER_DAY = MS_PER_HOUR * H_PER_D;
    /** Milliseconds per normal year.*/
    public static final long MS_PER_YEAR = DAYS_PER_YEAR * MS_PER_DAY;

    /** DateFormat used for outputting data strings */
    public final static DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ROOT);
    
    /** Private constructor, util class.*/
    private TimeUtils() {}
    
    /**
     * Convert from milliseconds to seconds.
     * @param ms The milliseconds to convert
     * @return The milliseconds converted into seconds.
     */
    public static String millisecondsToSeconds(long ms) {
        int seconds = (int) (ms / MS_PER_S);
        return seconds + "s";
    }
    
    /**
     * Convert from milliseconds to minutes.
     * @param ms The milliseconds to convert
     * @return The milliseconds converted into minutes.
     */
    public static String millisecondsToMinutes(long ms) {
        int minutes = (int) (ms / MS_PER_MINUTE );
        return minutes + "m";
    }
    
    /**
     * Convert from milliseconds to hours.
     * @param ms The milliseconds to convert
     * @return The milliseconds converted into hours.
     */
    public static String millisecondsToHours(long ms) {
        int hours = (int) (ms / MS_PER_HOUR);
        return hours + "h";
    }
    
    /**
     * Convert from milliseconds to days.
     * @param ms The milliseconds to convert
     * @return The milliseconds converted into days.
     */
    public static String millisecondsToDays(long ms) {
        int days = (int) (ms / MS_PER_DAY);
        return days + "d";
    }
    
    /**
     * Convert from milliseconds to a human readable format with days, hours, minutes, seconds and 
     * the remaining milliseconds.
     * @param ms The milliseconds to convert
     * @return The milliseconds converted a human readable format..
     */
    public static String millisecondsToHuman(long ms) {
        StringBuilder sb = new StringBuilder();
        boolean includeRemainingMs = true;
        if(ms >= MS_PER_MINUTE) {
            includeRemainingMs = false;
        }
        if(ms >= 0) {
            if(ms >= MS_PER_DAY) {
                sb.append(millisecondsToDays(ms));
            }
            ms = (ms % MS_PER_DAY);
            if(ms >= 0) {
                if(ms >= MS_PER_HOUR) {
                    sb.append(" " + millisecondsToHours(ms));
                }
                ms = (ms % MS_PER_HOUR);
                if(ms >= 0) {
                    if(ms >= MS_PER_MINUTE) {
                        sb.append(" " + millisecondsToMinutes(ms));
                    }
                    ms = (ms % MS_PER_MINUTE);
                    if(ms >= 0) {
                        if(ms >= MS_PER_S) {
                            sb.append(" " + millisecondsToSeconds(ms));
                        }
                        ms = (ms % MS_PER_S);
                        if(ms >= 0 && includeRemainingMs) {
                            sb.append(" " + ms + " ms");
                        }
                    }
                }
            }
        }
        return sb.toString();
    }

    public static String shortDate(Date date) {
        return formatter.format(date);
    }
    
    public static String shortDate(XMLGregorianCalendar cal) {
        return formatter.format(cal.toGregorianCalendar().getTime());
    }
    
    /**
     * Get the latest date of the two inputs
     * @param currentMax The current latest date 
     * @param itemDate The new candidate latest
     * @return The date object that was the latest of the two inputs. 
     */
    public static Date getMaxDate(Date currentMax, Date itemDate) {
        if(itemDate.after(currentMax)) {
            return itemDate;
        } else {
            return currentMax;
        }
    }
}
