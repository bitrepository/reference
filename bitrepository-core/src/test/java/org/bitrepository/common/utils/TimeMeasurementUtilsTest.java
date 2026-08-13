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

import org.bitrepository.TestGroups;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.time.Duration;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;

/**
 * Tests the <code>TimeMeasureComparator</code> class.
 */
class TimeMeasurementUtilsTest {
    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void testCompareMilliSeconds() {
        addDescription("Test the comparison between TimeMeasure units.");
        TimeMeasureTYPE referenceTime = new TimeMeasureTYPE();
        referenceTime.setTimeMeasureValue(new BigInteger("2"));
        referenceTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);

        TimeMeasureTYPE compareTime = new TimeMeasureTYPE();
        compareTime.setTimeMeasureValue(new BigInteger("3"));
        compareTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);

        Assertions.assertTrue(TimeMeasurementUtils.compare(referenceTime, compareTime) < 0, referenceTime +
                " should be smaller than " + compareTime);

        compareTime.setTimeMeasureValue(new BigInteger("1"));
        Assertions.assertTrue(TimeMeasurementUtils.compare(referenceTime, compareTime) > 0, referenceTime +
                " should be larger than " + compareTime);

        compareTime.setTimeMeasureValue(new BigInteger("2"));
        Assertions.assertEquals(0, TimeMeasurementUtils.compare(referenceTime, compareTime), referenceTime +
                " should be same as " + compareTime);
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void testCompareMilliSecondsToHours() {
        addDescription("Test the comparison between milliseconds and hours.");
        long millis = Duration.ofHours(2).toMillis();
        TimeMeasureTYPE referenceTime = new TimeMeasureTYPE();
        referenceTime.setTimeMeasureValue(BigInteger.valueOf(millis));
        referenceTime.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);

        TimeMeasureTYPE compareTime = new TimeMeasureTYPE();
        compareTime.setTimeMeasureValue(new BigInteger("3"));
        compareTime.setTimeMeasureUnit(TimeMeasureUnit.HOURS);

        Assertions.assertTrue(TimeMeasurementUtils.compare(referenceTime, compareTime) < 0, referenceTime +
                " should be smaller than " + compareTime);

        compareTime.setTimeMeasureValue(new BigInteger("1"));
        Assertions.assertTrue(TimeMeasurementUtils.compare(referenceTime, compareTime) > 0, referenceTime +
                " should be larger than " + compareTime);

        compareTime.setTimeMeasureValue(new BigInteger("2"));
        Assertions.assertEquals(0, TimeMeasurementUtils.compare(referenceTime, compareTime), referenceTime +
                " should be same as " + compareTime);

        Assertions.assertEquals(millis, TimeMeasurementUtils.getTimeMeasureInLong(referenceTime));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void testMaxValue() {
        addDescription("Test the Maximum value");
        TimeMeasureTYPE time = TimeMeasurementUtils.getMaximumTime();
        Assertions.assertEquals(Long.MAX_VALUE, time.getTimeMeasureValue().longValue());
        Assertions.assertEquals(TimeMeasureUnit.HOURS, time.getTimeMeasureUnit());

        TimeMeasureTYPE time2 = TimeMeasurementUtils.getTimeMeasurementFromMilliseconds(
                BigInteger.valueOf(Long.MAX_VALUE));
        time2.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        Assertions.assertEquals(0, TimeMeasurementUtils.compare(time, time2));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void testGetTimeMeasureInLongOverflow() {
        addDescription("Test that getTimeMeasureInLong throws ArithmeticException when the value overflows long.");
        TimeMeasureTYPE millisOverflow = new TimeMeasureTYPE();
        millisOverflow.setTimeMeasureValue(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE));
        millisOverflow.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);

        Assertions.assertThrows(ArithmeticException.class, () -> {
            TimeMeasurementUtils.getTimeMeasureInLong(millisOverflow);
        }, "Should throw ArithmeticException when millisecond value exceeds Long.MAX_VALUE");
        TimeMeasureTYPE hoursOverflow = new TimeMeasureTYPE();
        hoursOverflow.setTimeMeasureValue(BigInteger.valueOf(Long.MAX_VALUE));
        hoursOverflow.setTimeMeasureUnit(TimeMeasureUnit.HOURS);

        Assertions.assertThrows(ArithmeticException.class, () -> {
            TimeMeasurementUtils.getTimeMeasureInLong(hoursOverflow);
        }, "Should throw ArithmeticException when hour value converted to milliseconds exceeds Long.MAX_VALUE");
    }
}
