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
package org.bitrepository.common.utils;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureUnit;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigInteger;

/**
 * Tests the <code>TimeMeasureComparator</code> class.
 */
public class TimeMeasurementUtilsTest extends ExtendedTestCase {
    @Test (groups = { "regressiontest" })
    public void testCompareMilliSeconds() {
        addDescription("Test the comparison between TimeMeasure units.");
        TimeMeasureTYPE referenceTime = new TimeMeasureTYPE();
        referenceTime.setTimeMeasureValue(new BigInteger("2"));
        referenceTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
        
        TimeMeasureTYPE compareTime = new TimeMeasureTYPE();
        compareTime.setTimeMeasureValue(new BigInteger("3"));
        compareTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
        
        Assert.assertTrue(TimeMeasurementUtils.compare(referenceTime, compareTime) < 0, referenceTime +
                " should be smaller than " + compareTime);
        
        compareTime.setTimeMeasureValue(new BigInteger("1"));
        Assert.assertTrue(TimeMeasurementUtils.compare(referenceTime, compareTime) > 0, referenceTime + 
                " should be larger than " + compareTime);
        
        compareTime.setTimeMeasureValue(new BigInteger("2"));
        Assert.assertTrue(TimeMeasurementUtils.compare(referenceTime, compareTime) == 0, referenceTime + 
                " should be same as " + compareTime);
    }

    @Test (groups = { "regressiontest" })
    public void testCompareMilliSecondsToHours() {
        addDescription("Test the comparison between milliseconds and hours.");
        long millis = 7200000L;
        TimeMeasureTYPE referenceTime = new TimeMeasureTYPE();
        referenceTime.setTimeMeasureValue(BigInteger.valueOf(millis));
        referenceTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
        
        TimeMeasureTYPE compareTime = new TimeMeasureTYPE();
        compareTime.setTimeMeasureValue(new BigInteger("3"));
        compareTime.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        
        Assert.assertTrue(TimeMeasurementUtils.compare(referenceTime, compareTime) < 0, referenceTime + 
                " should be smaller than " + compareTime);
        
        compareTime.setTimeMeasureValue(new BigInteger("1"));
        Assert.assertTrue(TimeMeasurementUtils.compare(referenceTime, compareTime) > 0, referenceTime + 
                " should be larger than " + compareTime);
        
        compareTime.setTimeMeasureValue(new BigInteger("2"));
        Assert.assertTrue(TimeMeasurementUtils.compare(referenceTime, compareTime) == 0, referenceTime + 
                " should be same as " + compareTime);
        
        Assert.assertEquals(TimeMeasurementUtils.getTimeMeasureInLong(referenceTime), millis);
    }

    @Test (groups = { "regressiontest" })
    public void testMaxValue() {
        addDescription("Test the Maximum value");
        TimeMeasureTYPE time = TimeMeasurementUtils.getMaximumTime();
        Assert.assertEquals(time.getTimeMeasureValue().longValue(), Long.MAX_VALUE);
        Assert.assertEquals(time.getTimeMeasureUnit(), TimeMeasureUnit.HOURS);
        
        TimeMeasureTYPE time2 = TimeMeasurementUtils.getTimeMeasurementFromMilliseconds(
                BigInteger.valueOf(Long.MAX_VALUE));
        time2.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        Assert.assertEquals(TimeMeasurementUtils.compare(time, time2), 0);
    }

}
