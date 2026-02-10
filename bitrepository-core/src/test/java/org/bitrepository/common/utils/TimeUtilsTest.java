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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static java.lang.Long.MAX_VALUE;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofDays;
import static java.time.Duration.ofHours;
import static java.time.Duration.ofMillis;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofNanos;
import static java.time.Duration.ofSeconds;
import static java.time.Duration.parse;
import static java.time.Period.of;
import static java.time.Period.ofMonths;
import static java.time.Period.ofYears;
import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.HOURS;
import static java.time.temporal.ChronoUnit.MICROS;
import static java.time.temporal.ChronoUnit.MINUTES;
import static java.time.temporal.ChronoUnit.MONTHS;
import static java.time.temporal.ChronoUnit.SECONDS;
import static java.time.temporal.ChronoUnit.YEARS;
import static java.util.Locale.ROOT;
import static java.util.concurrent.TimeUnit.MICROSECONDS;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.bitrepository.common.utils.TimeUtils.durationToCountAndTimeUnit;
import static org.bitrepository.common.utils.TimeUtils.durationToHuman;
import static org.bitrepository.common.utils.TimeUtils.durationToHumanUsingEstimates;
import static org.bitrepository.common.utils.TimeUtils.humanDifference;
import static org.bitrepository.common.utils.TimeUtils.millisecondsToHuman;
import static org.bitrepository.common.utils.TimeUtils.shortDate;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeUtilsTest extends ExtendedTestCase {
    private static final ZonedDateTime BASE = Instant.EPOCH.atZone(ZoneOffset.UTC);

    @Test
    @Tag("regressiontest")
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
        String millisInHour = TimeUtils.millisecondsToHours(millis % (3600000 * 24));
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

    @Test
    @Tag("regressiontest")
    public void printsHumanDuration() {
        assertEquals("1y", durationToHumanUsingEstimates(YEARS.getDuration()));
        assertEquals("1m", durationToHumanUsingEstimates(MONTHS.getDuration()));
        assertEquals("1d", durationToHumanUsingEstimates(DAYS.getDuration()));
        assertEquals("1h", durationToHumanUsingEstimates(HOURS.getDuration()));
        assertEquals("1m", durationToHumanUsingEstimates(MINUTES.getDuration()));
        // Don’t print seconds
        assertEquals("0m", durationToHumanUsingEstimates(SECONDS.getDuration()));
        assertEquals("2h 3m", durationToHumanUsingEstimates(parse("PT2H3M5S")));

        addStep("Test the limits of what the method handles", "0m and 500y respectively");
        assertEquals("0m", durationToHumanUsingEstimates(ZERO));
        assertEquals("500y", durationToHumanUsingEstimates(ofHours(4_382_910)));
    }

    @Test
    @Tag("regressiontest")
    public void zeroIntervalTest() throws Exception {
        addDescription("Verifies that a 0 ms interval is represented correctly");
        addStep("Call millisecondsToHuman with 0 ms", "The output should be '0 ms'");
        String zeroTimeString = millisecondsToHuman(0);
        assertEquals(" 0 ms", zeroTimeString);
    }

    @Test
    @Tag("regressiontest")
    public void durationsPrintHumanly() {
        addDescription("Tests durationToHuman()");

        assertTrue(durationToHuman(ZERO).contains("0"),
                "Zero duration should contain a 0 digit");

        assertEquals("2d", durationToHuman(ofDays(2)));
        assertEquals("3h", durationToHuman(ofHours(3)));
        assertEquals("5m", durationToHuman(ofMinutes(5)));
        assertEquals("7s", durationToHuman(ofSeconds(7)));
        assertEquals("11 ms", durationToHuman(ofMillis(11)));
        assertEquals("13 ns", durationToHuman(ofNanos(13)));
        // When there are nanoseconds, don't print millis
        assertEquals("999999937 ns", durationToHuman(ofNanos(999_999_937)));

        assertEquals("minus 2d", durationToHuman(ofDays(-2)));
        assertEquals("minus 13 ns", durationToHuman(ofNanos(-13)));

        Duration allUnits = parse("P3DT5H7M11.013000017S");
        assertEquals("3d 5h 7m 11s 13000017 ns", durationToHuman(allUnits));
    }

    @Test
    @Tag("regressiontest")
    public void differencesPrintHumanly() {
        addDescription("TimeUtils.humanDifference() should return" +
                " similar human readable strings to those from millisecondsToHuman()");

        addStep("Call humanDifference() with same time twice", "The output should be '0m'");
        String zeroTimeString = humanDifference(BASE, BASE);
        assertEquals("0m", zeroTimeString);

        addStep("Call humanDifference() with a difference obtained from a Duration",
                "Expect corresponding readable output");
        // Don’t print seconds
        testHumanDifference("0m", ofSeconds(1));
        testHumanDifference("1m", ofMinutes(1));
        testHumanDifference("1h", ofHours(1));
        testHumanDifference("2h 3m", parse("PT2H3M5.000000007S"));

        addStep("Call humanDifference() with a difference obtained from a Period",
                "Expect corresponding readable output");
        testHumanDifference("1d", Period.ofDays(1));
        testHumanDifference("1m", ofMonths(1));
        testHumanDifference("1y", ofYears(1));
        testHumanDifference("2y 3m 5d", of(2, 3, 5));

        addStep("Call humanDifference() with a difference obtained from a combo of a Period and a Duration",
                "Expect corresponding readable output");
        testHumanDifference("3y 5m 7d",
                of(3, 5, 7), parse("PT11H13M17.023S"));
        testHumanDifference("2m 7d 11h",
                of(0, 2, 7), parse("PT11H13M17.023S"));
        testHumanDifference("1d 11h 13m", Period.ofDays(1), parse("PT11H13M17.023S"));

        addStep("Call humanDifference()" +
                        " with dates that are 2 days apart but times that cause the diff to be less than 2 full days",
                "Expect output 1d something");
        ZoneId testZoneId = ZoneId.of("Europe/Vienna");
        String oneDaySomethingString = humanDifference(
                ZonedDateTime.of(2021, 1, 31,
                        12, 0, 0, 0, testZoneId),
                ZonedDateTime.of(2021, 2, 2,
                        11, 59, 29, 0, testZoneId));
        assertEquals("1d 23h 59m", oneDaySomethingString);
    }

    @Test
    @Tag("regressiontest")
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
        for (TemporalAmount amount : amounts) {
            end = end.plus(amount);
        }
        String differenceString = humanDifference(BASE, end);
        assertEquals(expected, differenceString);
    }

    /*
     * The test only ensures that the output format is fixed. Which timezone the date is
     * formatted to depends on the default/system timezone. At some time the use of the old java Date
     * api should be discontinued and the new Java Time api used instead.
     */
    @Test
    @Tag("regressiontest")
    public void shortDateTest() {
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", ROOT);
        Date date = new Date(1360069129256L);
        String shortDateString = shortDate(date);
        assertEquals(formatter.format(date), shortDateString);
    }

    @Test
    @Tag("regressiontest")
    public void rejectsNegativeDuration() {
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> TimeUtils.durationToCountAndTimeUnit(Duration.ofSeconds(Long.MIN_VALUE)));
        Assertions.assertThrows(IllegalArgumentException.class,
                () -> TimeUtils.durationToCountAndTimeUnit(Duration.ofNanos(-1)));
    }

    @Test
    @Tag("regressiontest")
    public void convertsDurationToCountAndTimeUnit() {
        CountAndTimeUnit expectedZero = durationToCountAndTimeUnit(ZERO);
        assertEquals(0, expectedZero.getCount());
        assertNotNull(expectedZero.getUnit());

        assertEquals(new CountAndTimeUnit(1, NANOSECONDS), durationToCountAndTimeUnit(ofNanos(1)));
        assertEquals(new CountAndTimeUnit(MAX_VALUE, NANOSECONDS), durationToCountAndTimeUnit(ofNanos(MAX_VALUE)));
        assertEquals(new CountAndTimeUnit(MAX_VALUE / 1000 + 1, MICROSECONDS), durationToCountAndTimeUnit(Duration.of(MAX_VALUE / 1000 + 1, MICROS)));
        assertEquals(new CountAndTimeUnit(MAX_VALUE, MICROSECONDS), durationToCountAndTimeUnit(Duration.of(MAX_VALUE, MICROS)));
        assertEquals(new CountAndTimeUnit(MAX_VALUE / 1000 + 1, MILLISECONDS), durationToCountAndTimeUnit(ofMillis(MAX_VALUE / 1000 + 1)));
        assertEquals(new CountAndTimeUnit(MAX_VALUE, MILLISECONDS), durationToCountAndTimeUnit(ofMillis(MAX_VALUE)));
        assertEquals(new CountAndTimeUnit(MAX_VALUE / 1000 + 1, TimeUnit.SECONDS), durationToCountAndTimeUnit(ofSeconds(MAX_VALUE / 1000 + 1)));
        assertEquals(new CountAndTimeUnit(MAX_VALUE, TimeUnit.SECONDS), durationToCountAndTimeUnit(ofSeconds(MAX_VALUE)));
    }

}
