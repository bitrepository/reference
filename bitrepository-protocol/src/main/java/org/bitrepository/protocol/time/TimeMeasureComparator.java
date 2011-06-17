/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.protocol.time;

import java.math.BigInteger;
import java.util.Comparator;
import java.util.UnknownFormatConversionException;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.common.ArgumentValidator;

public class TimeMeasureComparator {

    /**
     * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer as the first 
     * argument is less than, equal to, or greater than the second.
     * @param time1
     * @param time2
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or 
     * greater than the second.
     * @see Comparator
     */
    public int compare(TimeMeasureTYPE time1, TimeMeasureTYPE time2) {
        ArgumentValidator.checkNotNull(time1, "time1");
        ArgumentValidator.checkNotNull(time2, "time2");
        return convertToMilliSeconds(time1).compareTo(convertToMilliSeconds(time2));
    }
    
    private BigInteger convertToMilliSeconds(TimeMeasureTYPE timeMeasure) throws UnknownFormatConversionException {
        if(timeMeasure.getTimeMeasureUnit().equals("MILLISECONDS")) {
            return timeMeasure.getTimeMeasureValue();
        } else if (timeMeasure.getTimeMeasureUnit().equals("SECONDS")) {
            return timeMeasure.getTimeMeasureValue().multiply(new BigInteger("1000"));
        } else if (timeMeasure.getTimeMeasureUnit().equals("MINUTES")) {
            return timeMeasure.getTimeMeasureValue().multiply(new BigInteger("60000"));
        } else if (timeMeasure.getTimeMeasureUnit().equals("HOURS")) {
            return timeMeasure.getTimeMeasureValue().multiply(new BigInteger("3600000"));     
        } else {
            throw new UnknownFormatConversionException ("Unable to compare times, unknown unit " + 
                    timeMeasure.getTimeMeasureUnit());
        }
    }
}
