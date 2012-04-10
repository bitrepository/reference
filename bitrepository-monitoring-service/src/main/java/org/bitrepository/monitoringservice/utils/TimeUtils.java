/*
 * #%L
 * Bitrepository Monitoring Service
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
package org.bitrepository.monitoringservice.utils;

/**
 * Util class to handle the presentation of time in a human readable form 
 */
public final class TimeUtils {
    
    private static final int MS_PER_S = 1000;
    private static final int S_PER_M = 60;
    private static final int M_PER_H = 60;
    private static final int H_PER_D = 24;
    private static final int MS_PER_MINUTE = MS_PER_S * S_PER_M;
    private static final int MS_PER_HOUR = MS_PER_MINUTE * M_PER_H;
    private static final long MS_PER_DAY = MS_PER_HOUR * H_PER_D;
    
    /** Private constructor, util class.*/
    private TimeUtils() {}
    
    public static String millisecondsToSeconds(long ms) {
        return "" + (ms / MS_PER_S) + "seconds";
    }
    
    public static String millisecondsToMinutes(long ms) {
        return "" + (ms / ( MS_PER_S * S_PER_M )) + "minutes";
    }
    
    public static String millisecondsToHours(long ms) {
        return "" + (ms /( MS_PER_S * S_PER_M * M_PER_H )) + "hours";
    }
    
    public static String millisecondsToDays(long ms) {
        return "" + (ms /( MS_PER_S * S_PER_M * M_PER_H * H_PER_D)) + "days";
    }
    
    public static String millisecondsToHuman(long ms) {
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
