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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.math.BigInteger;
import java.time.Duration;
import java.util.Set;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests the <code>TimeMeasureComparator</code> class.
 */
class TimeMeasurementUtilsTest {

    public static final BigInteger MILLISECONDS_PER_SECOND = BigInteger.valueOf(1000);

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

    @Test
    void convertsTimeMeasureTypeToDuration() {
        assertEquals(Duration.ofMillis(7),
                TimeMeasurementUtils.timeMeasureToDuration(createTimeMeasure(7, TimeMeasureUnit.MILLISECONDS)));
        assertEquals(Duration.ofHours(19),
                TimeMeasurementUtils.timeMeasureToDuration(createTimeMeasure(19, TimeMeasureUnit.HOURS)));
        assertEquals(Duration.ofMillis(654321),
                TimeMeasurementUtils.timeMeasureToDuration(
                        createTimeMeasure(654321, TimeMeasureUnit.MILLISECONDS)));

        assertEquals(Duration.ofMillis(Long.MAX_VALUE),
                TimeMeasurementUtils.timeMeasureToDuration(
                        createTimeMeasure(Long.MAX_VALUE, TimeMeasureUnit.MILLISECONDS)));
        assertEquals(Duration.ofSeconds(Long.MAX_VALUE),
                TimeMeasurementUtils.timeMeasureToDuration(
                        createTimeMeasure(BigInteger.valueOf(Long.MAX_VALUE).multiply(MILLISECONDS_PER_SECOND),
                                TimeMeasureUnit.MILLISECONDS)));
        assertEquals(Duration.ofSeconds(Long.MIN_VALUE),
                TimeMeasurementUtils.timeMeasureToDuration(
                        createTimeMeasure(BigInteger.valueOf(Long.MIN_VALUE).multiply(MILLISECONDS_PER_SECOND),
                                TimeMeasureUnit.MILLISECONDS)));
        assertEquals(Duration.ofHours(2_562_047_788_015_215L),
                TimeMeasurementUtils.timeMeasureToDuration(
                        createTimeMeasure(2_562_047_788_015_215L, TimeMeasureUnit.HOURS)));
        assertEquals(Duration.ofHours(-2_562_047_788_015_215L),
                TimeMeasurementUtils.timeMeasureToDuration(
                        createTimeMeasure(-2_562_047_788_015_215L, TimeMeasureUnit.HOURS)));
    }

    @Test
    void overflowingDurationFails() {
        assertThrows(ArithmeticException.class,
                () -> TimeMeasurementUtils.timeMeasureToDuration(
                        createTimeMeasure(BigInteger.valueOf(Long.MAX_VALUE).add(BigInteger.ONE)
                                        .multiply(MILLISECONDS_PER_SECOND),
                                TimeMeasureUnit.MILLISECONDS)));
        assertThrows(ArithmeticException.class,
                () -> TimeMeasurementUtils.timeMeasureToDuration(
                        createTimeMeasure(BigInteger.valueOf(Long.MIN_VALUE).multiply(MILLISECONDS_PER_SECOND)
                                        .subtract(BigInteger.ONE),
                                TimeMeasureUnit.MILLISECONDS)));

        assertThrows(ArithmeticException.class,
                () -> TimeMeasurementUtils.timeMeasureToDuration(
                        createTimeMeasure(2_562_047_788_015_216L, TimeMeasureUnit.HOURS)));
        assertThrows(ArithmeticException.class,
                () -> TimeMeasurementUtils.timeMeasureToDuration(
                        createTimeMeasure(-2_562_047_788_015_216L, TimeMeasureUnit.HOURS)));
    }

    @Test
    void convertsDurationToTimeMeasureType() {
        assertEquals(createTimeMeasure(37, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(Duration.ofMillis(37)));
        assertEquals(createTimeMeasure(2_490_000, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(Duration.ofMinutes(41).plusSeconds(30)));
        assertEquals(createTimeMeasure(10_830_000, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(Duration.ofHours(3).plusSeconds(30)));
        assertEquals(createTimeMeasure(-10_830_000, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(Duration.ofHours(3).plusSeconds(30).negated()));

        // More than Long.MAX_VALUE milliseconds:
        BigInteger manyMillis = BigInteger.valueOf(Long.MAX_VALUE).multiply(MILLISECONDS_PER_SECOND)
                .add(BigInteger.ONE);
        assertEquals(createTimeMeasure(manyMillis, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(Duration.ofSeconds(Long.MAX_VALUE).plusMillis(1)));
        BigInteger manyMillis2 = BigInteger.valueOf(Long.MAX_VALUE).multiply(MILLISECONDS_PER_SECOND)
                .add(BigInteger.valueOf(999));
        assertEquals(createTimeMeasure(manyMillis2, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(
                        Duration.ofSeconds(Long.MAX_VALUE).plusMillis(999)));
        assertEquals(createTimeMeasure(manyMillis2.negate(), TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(
                        Duration.ofSeconds(Long.MAX_VALUE).plusMillis(999).negated()));

        // No test requiring HOURS
        // because any TimeMeasureTYPE with HOURS can be represented by another TimeMeasureTYPE in milliseconds.
        // Instead see convertsDurationToTimeMeasureTypeWithOneOrTheOtherUnit().
    }

    @ParameterizedTest
    @ValueSource(longs = { -2_562_047_788_015_215L, -37, -1, 0, 1, 13, 73, 2_562_047_788_015_215L })
    void convertsDurationToTimeMeasureTypeWithOneOrTheOtherUnit(long hours) {
        // For some Durations either time unit is acceptable in the TimeMeasureTYPE,
        // and the implementation is free to choose.

        Duration duration = Duration.ofHours(hours);
        TimeMeasureTYPE converted =  TimeMeasurementUtils.durationToTimeMeasure(duration);
        // The two possible time measures
        Set<TimeMeasureTYPE> expected = Set.of(createTimeMeasure(hours, TimeMeasureUnit.HOURS),
                createTimeMeasure(BigInteger.valueOf(hours).multiply(TimeMeasurementUtils.MILLIS_PER_HOUR),
                        TimeMeasureUnit.MILLISECONDS));
        assertTrue(expected.contains(converted),
                "Expected one of " + expected + "; actual " + converted);
    }

    @Test
    void truncatesMicroseconds() {
        assertEquals(createTimeMeasure(37, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(Duration.ofMillis(37).plusNanos(1)));
        assertEquals(createTimeMeasure(37, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(Duration.ofMillis(37).plusNanos(999_999)));
        assertEquals(createTimeMeasure(89_000, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(Duration.ofSeconds(89).plusNanos(999_999)));

        assertEquals(createTimeMeasure(-41, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(Duration.ofMillis(-41).minusNanos(1)));
        assertEquals(createTimeMeasure(-41, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(Duration.ofMillis(-41).minusNanos(999_999)));
        assertEquals(createTimeMeasure(-97_000, TimeMeasureUnit.MILLISECONDS),
                TimeMeasurementUtils.durationToTimeMeasure(Duration.ofSeconds(-97).minusNanos(999_999)));
    }

    private static TimeMeasureTYPE createTimeMeasure(long value, TimeMeasureUnit unit) {
        return createTimeMeasure(BigInteger.valueOf(value), unit);
    }

    private static TimeMeasureTYPE createTimeMeasure(BigInteger value, TimeMeasureUnit unit) {
        TimeMeasureTYPE timeMeasure = new TimeMeasureTYPE();
        timeMeasure.setTimeMeasureValue(value);
        timeMeasure.setTimeMeasureUnit(unit);
        return timeMeasure;
    }

}
