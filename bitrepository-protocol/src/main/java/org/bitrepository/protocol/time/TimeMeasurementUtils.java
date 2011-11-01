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

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE.TimeMeasureUnit;

/**
 * Provides helper method for accessing {@link TimeMeasurementTYPE} objects. 
 */
public class TimeMeasurementUtils {

    /**
     * Generates a TimeMeasureTYPE object based on a milliseconds value.
     * @param milliseconds The time measure in milliseconds.
     * @return A corresponding <code>TimeMeasureTYPE</code> object.
     */
    public static TimeMeasureTYPE getTimeMeasurementFromMiliseconds(BigInteger milliseconds) {
        TimeMeasureTYPE timeMeasure = new TimeMeasureTYPE();
        timeMeasure.setTimeMeasureValue(milliseconds);
        timeMeasure.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
        return timeMeasure; 
    }
    
    /**
     * Method for getting the maximum time. Uses the maximum value of Long and puts it in HOURS.
     * @return The TimeMeasure for the maximum time.
     */
    public static TimeMeasureTYPE getMaximumTime() {
        TimeMeasureTYPE timeMeasure = new TimeMeasureTYPE();
        timeMeasure.setTimeMeasureValue(BigInteger.valueOf(Long.MAX_VALUE));
        timeMeasure.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        return timeMeasure; 
    }
}
