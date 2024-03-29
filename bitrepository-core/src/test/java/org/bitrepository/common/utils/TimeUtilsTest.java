/*
 * #%L
 * Bitrepository Core
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

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class TimeUtilsTest extends ExtendedTestCase {
    private static final ZonedDateTime BASE = Instant.EPOCH.atZone(ZoneOffset.UTC);

    @Test(groups = {"regressiontest"})
    public void timeTester() throws Exception {
        addDescription("Tests the TimeUtils. Pi days = 271433605 milliseconds");
        addStep("Test that milliseconds can be converted into human readable seconds", 
                "Pi days % minutes");
        long millis = 271433605;
        String millisInSec = TimeUtils.millisecondsToSeconds(millis % 60000);
        String expectedSec = "53s";
        assertTrue(millisInSec.startsWith(expectedSec));
        
        addStep("Test that milliseconds can be converted into human readable minutes.", 
                "Pi days % hours");
        String millisInMin = TimeUtils.millisecondsToMinutes(millis % 3600000);
        String expectedMin = "23m";
        assertTrue(millisInMin.startsWith(expectedMin));
        
        addStep("Test that milliseconds can be converted into human readable hours.", 
                "Pi days % days");
        String millisInHour = TimeUtils.millisecondsToHours(millis % (3600000*24));
        String expectedHours = "3h";
        assertTrue(millisInHour.startsWith(expectedHours));
        
        addStep("Test that milliseconds can be converted into human readable minutes.", 
                "Pi days");
        String millisInDay = TimeUtils.millisecondsToDays(millis);
        String expectedDays = "3d";
        assertTrue(millisInDay.startsWith(expectedDays));
        
        addStep("Test the human readable output.", "");
        String human = TimeUtils.millisecondsToHuman(millis);
        assertTrue(human.contains(expectedSec), human);
        assertTrue(human.contains(expectedMin), human);
        assertTrue(human.contains(expectedHours), human);
        assertTrue(human.contains(expectedDays), human);
    }

    @Test(groups = {"regressiontest"})
    public void printsHumanDuration() {
        assertEquals(TimeUtils.durationToHumanUsingEstimates(ChronoUnit.YEARS.getDuration()), "1y");
        assertEquals(TimeUtils.durationToHumanUsingEstimates(ChronoUnit.MONTHS.getDuration()), "1m");
        assertEquals(TimeUtils.durationToHumanUsingEstimates(ChronoUnit.DAYS.getDuration()), "1d");
        assertEquals(TimeUtils.durationToHumanUsingEstimates(ChronoUnit.HOURS.getDuration()), "1h");
        assertEquals(TimeUtils.durationToHumanUsingEstimates(ChronoUnit.MINUTES.getDuration()), "1m");
        // Don’t print seconds
        assertEquals(TimeUtils.durationToHumanUsingEstimates(ChronoUnit.SECONDS.getDuration()), "0m");
        assertEquals(TimeUtils.durationToHumanUsingEstimates(Duration.parse("PT2H3M5S")), "2h 3m");

        addStep("Test the limits of what the method handles", "0m and 500y respectively");
        assertEquals(TimeUtils.durationToHumanUsingEstimates(Duration.ZERO), "0m");
        assertEquals(TimeUtils.durationToHumanUsingEstimates(Duration.ofHours(4_382_910)), "500y");
    }

    @Test(groups = {"regressiontest"})
    public void zeroIntervalTest() throws Exception {
        addDescription("Verifies that a 0 ms interval is represented correctly");
        addStep("Call millisecondsToHuman with 0 ms", "The output should be '0 ms'");
        String zeroTimeString = TimeUtils.millisecondsToHuman(0);
        assertEquals(zeroTimeString, " 0 ms");
    }

    @Test(groups = {"regressiontest"})
    public void durationsPrintHumanly() {
        addDescription("Tests durationToHuman()");

        assertTrue(TimeUtils.durationToHuman(Duration.ZERO).contains("0"),
                "Zero duration should contain a 0 digit");

        assertEquals(TimeUtils.durationToHuman(Duration.ofDays(2)), "2d");
        assertEquals(TimeUtils.durationToHuman(Duration.ofHours(3)), "3h");
        assertEquals(TimeUtils.durationToHuman(Duration.ofMinutes(5)), "5m");
        assertEquals(TimeUtils.durationToHuman(Duration.ofSeconds(7)), "7s");
        assertEquals(TimeUtils.durationToHuman(Duration.ofMillis(11)), "11 ms");
        assertEquals(TimeUtils.durationToHuman(Duration.ofNanos(13)), "13 ns");
        // When there are nanoseconds, don't print millis
        assertEquals(TimeUtils.durationToHuman(Duration.ofNanos(999_999_937)), "999999937 ns");

        assertEquals(TimeUtils.durationToHuman(Duration.ofDays(-2)), "minus 2d");
        assertEquals(TimeUtils.durationToHuman(Duration.ofNanos(-13)), "minus 13 ns");

        Duration allUnits = Duration.parse("P3DT5H7M11.013000017S");
        assertEquals(TimeUtils.durationToHuman(allUnits), "3d 5h 7m 11s 13000017 ns");
    }
    @Test(groups = {"regressiontest"})
    public void differencesPrintHumanly() {
        addDescription("TimeUtils.humanDifference() should return" +
                " similar human readable strings to those from millisecondsToHuman()");

        addStep("Call humanDifference() with same time twice", "The output should be '0m'");
        String zeroTimeString = TimeUtils.humanDifference(BASE, BASE);
        assertEquals(zeroTimeString, "0m");

        addStep("Call humanDifference() with a difference obtained from a Duration",
                "Expect corresponding readable output");
        // Don’t print seconds
        testHumanDifference("0m", Duration.ofSeconds(1));
        testHumanDifference("1m", Duration.ofMinutes(1));
        testHumanDifference("1h", Duration.ofHours(1));
        testHumanDifference("2h 3m", Duration.parse("PT2H3M5.000000007S"));

        addStep("Call humanDifference() with a difference obtained from a Period",
                "Expect corresponding readable output");
        testHumanDifference("1d", Period.ofDays(1));
        testHumanDifference("1m", Period.ofMonths(1));
        testHumanDifference("1y", Period.ofYears(1));
        testHumanDifference("2y 3m 5d", Period.of(2, 3, 5));

        addStep("Call humanDifference() with a difference obtained from a combo of a Period and a Duration",
                "Expect corresponding readable output");
        testHumanDifference("3y 5m 7d",
                Period.of(3, 5, 7), Duration.parse("PT11H13M17.023S"));
        testHumanDifference("2m 7d 11h",
                Period.of(0, 2, 7), Duration.parse("PT11H13M17.023S"));
        testHumanDifference("1d 11h 13m", Period.ofDays(1), Duration.parse("PT11H13M17.023S"));

        addStep("Call humanDifference()" +
                        " with dates that are 2 days apart but times that cause the diff to be less than 2 full days",
                "Expect output 1d something");
        ZoneId testZoneId = ZoneId.of("Europe/Vienna");
        String oneDaySomethingString = TimeUtils.humanDifference(
                ZonedDateTime.of(2021, 1, 31,
                        12, 0, 0, 0, testZoneId),
                ZonedDateTime.of(2021, 2, 2,
                        11, 59, 29, 0, testZoneId));
        assertEquals(oneDaySomethingString, "1d 23h 59m");
    }

    @Test(groups = {"regressiontest"})
    public void differencesPrintsWithAppropriatePrecision() {
        // Include hours if months are 6 or less.
        testHumanDifference("11m", Period.ofMonths(11), Duration.ofHours(23));
        testHumanDifference("1y 1d", Period.of(1, 0, 1), Duration.ofHours(23));
        testHumanDifference("2m 1h", Period.ofMonths(2), Duration.ofHours(1));
        // Include minutes if days are 8 or less.
        testHumanDifference("1y", Period.ofYears(1), Duration.ofMinutes(23));
        testHumanDifference("1m", Period.ofMonths(1), Duration.ofMinutes(23));
        testHumanDifference("27d", Period.ofDays(27), Duration.ofMinutes(23));
        testHumanDifference("2d 3m", Period.ofDays(2), Duration.ofMinutes(3));
        // Round to whole minutes
        testHumanDifference("2d 3m", Period.ofDays(2), Duration.ofMinutes(2).plusSeconds(30));
        testHumanDifference("2d 3m", Period.ofDays(2), Duration.ofMinutes(3).plusSeconds(29));
        // Never include seconds.
        testHumanDifference("1y", Period.ofYears(1), Duration.ofSeconds(55));
        testHumanDifference("1m", Period.ofMonths(1), Duration.ofSeconds(55));
        testHumanDifference("1d", Period.ofDays(1), Duration.ofSeconds(29));
        testHumanDifference("22h", Duration.ofHours(22).plusSeconds(29));
        testHumanDifference("4m", Duration.ofMinutes(4).plusSeconds(29));
        testHumanDifference("0m", Duration.ofSeconds(2).plusMillis(1));
        testHumanDifference("0m", Duration.ofNanos(500_000_000));
        testHumanDifference("0m", Duration.ofNanos(499_999_999));
        testHumanDifference("0m", Duration.ofMillis(1));
        testHumanDifference("0m", Duration.ofNanos(1));
    }

    /**
     * Note that the expected result comes first in the argument list
     * so that we can use varargs to pass a number of amounts, for example both a Period and a Duration.
     */
    private void testHumanDifference(String expected, TemporalAmount... amounts) {
        ZonedDateTime end = BASE;
        for (TemporalAmount amount: amounts) {
            end = end.plus(amount);
        }
        String differenceString = TimeUtils.humanDifference(BASE, end);
        assertEquals(differenceString, expected);
    }

    /*
     * The test only ensures that the output format is fixed. Which timezone the date is
     * formatted to depends on the default/system timezone. At some time the use of the old java Date 
     * api should be discontinued and the new Java Time api used instead.
     */
    @Test(groups = {"regressiontest"})
    public void shortDateTest() {
    	DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ROOT);
        Date date = new Date(1360069129256L);
        String shortDateString = TimeUtils.shortDate(date);
        Assert.assertEquals(shortDateString, formatter.format(date));
    }

    @Test(groups = {"regressiontest"})
    public void rejectsNegativeDuration() {
        Assert.assertThrows(IllegalArgumentException.class,
                () -> TimeUtils.durationToCountAndTimeUnit(Duration.ofSeconds(Long.MIN_VALUE)));
        Assert.assertThrows(IllegalArgumentException.class,
                () -> TimeUtils.durationToCountAndTimeUnit(Duration.ofNanos(-1)));
    }

    @Test(groups = {"regressiontest"})
    public void convertsDurationToCountAndTimeUnit() {
        CountAndTimeUnit expectedZero = TimeUtils.durationToCountAndTimeUnit(Duration.ZERO);
        Assert.assertEquals(expectedZero.getCount(), 0);
        Assert.assertNotNull(expectedZero.getUnit());

        Assert.assertEquals(TimeUtils.durationToCountAndTimeUnit(Duration.ofNanos(1)),
                new CountAndTimeUnit(1, TimeUnit.NANOSECONDS));
        Assert.assertEquals(TimeUtils.durationToCountAndTimeUnit(Duration.ofNanos(Long.MAX_VALUE)),
                new CountAndTimeUnit(Long.MAX_VALUE, TimeUnit.NANOSECONDS));
        Assert.assertEquals(
                TimeUtils.durationToCountAndTimeUnit(Duration.of(Long.MAX_VALUE / 1000 + 1, ChronoUnit.MICROS)),
                new CountAndTimeUnit(Long.MAX_VALUE / 1000 + 1, TimeUnit.MICROSECONDS));
        Assert.assertEquals(TimeUtils.durationToCountAndTimeUnit(Duration.of(Long.MAX_VALUE, ChronoUnit.MICROS)),
                new CountAndTimeUnit(Long.MAX_VALUE, TimeUnit.MICROSECONDS));
        Assert.assertEquals(
                TimeUtils.durationToCountAndTimeUnit(Duration.ofMillis(Long.MAX_VALUE / 1000 + 1)),
                new CountAndTimeUnit(Long.MAX_VALUE / 1000 + 1, TimeUnit.MILLISECONDS));
        Assert.assertEquals(TimeUtils.durationToCountAndTimeUnit(Duration.ofMillis(Long.MAX_VALUE)),
                new CountAndTimeUnit(Long.MAX_VALUE, TimeUnit.MILLISECONDS));
        Assert.assertEquals(
                TimeUtils.durationToCountAndTimeUnit(Duration.ofSeconds(Long.MAX_VALUE / 1000 + 1)),
                new CountAndTimeUnit(Long.MAX_VALUE / 1000 + 1, TimeUnit.SECONDS));
        Assert.assertEquals(TimeUtils.durationToCountAndTimeUnit(Duration.ofSeconds(Long.MAX_VALUE)),
                new CountAndTimeUnit(Long.MAX_VALUE, TimeUnit.SECONDS));
    }

}
