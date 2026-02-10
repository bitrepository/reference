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
import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;

import static java.lang.Long.MAX_VALUE;
import static java.math.BigInteger.valueOf;
import static org.bitrepository.bitrepositoryelements.TimeMeasureUnit.HOURS;
import static org.bitrepository.bitrepositoryelements.TimeMeasureUnit.MILLISECONDS;
import static org.bitrepository.common.utils.TimeMeasurementUtils.compare;
import static org.bitrepository.common.utils.TimeMeasurementUtils.getMaximumTime;
import static org.bitrepository.common.utils.TimeMeasurementUtils.getTimeMeasureInLong;
import static org.bitrepository.common.utils.TimeMeasurementUtils.getTimeMeasurementFromMilliseconds;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the <code>TimeMeasureComparator</code> class.
 */
public class TimeMeasurementUtilsTest extends ExtendedTestCase {
    @Test
    @Tag("regressiontest")
    public void testCompareMilliSeconds() {
        addDescription("Test the comparison between TimeMeasure units.");
        TimeMeasureTYPE referenceTime = new TimeMeasureTYPE();
        referenceTime.setTimeMeasureValue(new BigInteger("2"));
        referenceTime.setTimeMeasureUnit(MILLISECONDS);

        TimeMeasureTYPE compareTime = new TimeMeasureTYPE();
        compareTime.setTimeMeasureValue(new BigInteger("3"));
        compareTime.setTimeMeasureUnit(MILLISECONDS);

        assertTrue(compare(referenceTime, compareTime) < 0, referenceTime +
                " should be smaller than " + compareTime);

        compareTime.setTimeMeasureValue(new BigInteger("1"));
        assertTrue(compare(referenceTime, compareTime) > 0, referenceTime +
                " should be larger than " + compareTime);

        compareTime.setTimeMeasureValue(new BigInteger("2"));
        assertEquals(0, compare(referenceTime, compareTime), referenceTime +
                " should be same as " + compareTime);
    }

    @Test
    @Tag("regressiontest")
    public void testCompareMilliSecondsToHours() {
        addDescription("Test the comparison between milliseconds and hours.");
        long millis = 7200000L;
        TimeMeasureTYPE referenceTime = new TimeMeasureTYPE();
        referenceTime.setTimeMeasureValue(valueOf(millis));
        referenceTime.setTimeMeasureUnit(MILLISECONDS);

        TimeMeasureTYPE compareTime = new TimeMeasureTYPE();
        compareTime.setTimeMeasureValue(new BigInteger("3"));
        compareTime.setTimeMeasureUnit(HOURS);

        assertTrue(compare(referenceTime, compareTime) < 0, referenceTime +
                " should be smaller than " + compareTime);

        compareTime.setTimeMeasureValue(new BigInteger("1"));
        assertTrue(compare(referenceTime, compareTime) > 0, referenceTime +
                " should be larger than " + compareTime);

        compareTime.setTimeMeasureValue(new BigInteger("2"));
        assertEquals(0, compare(referenceTime, compareTime), referenceTime +
                " should be same as " + compareTime);

        assertEquals(millis, getTimeMeasureInLong(referenceTime));
    }

    @Test
    @Tag("regressiontest")
    public void testMaxValue() {
        addDescription("Test the Maximum value");
        TimeMeasureTYPE time = getMaximumTime();
        assertEquals(MAX_VALUE, time.getTimeMeasureValue().longValue());
        assertEquals(HOURS, time.getTimeMeasureUnit());

        TimeMeasureTYPE time2 = getTimeMeasurementFromMilliseconds(
                valueOf(MAX_VALUE));
        time2.setTimeMeasureUnit(HOURS);
        assertEquals(0, compare(time, time2));
    }

}
