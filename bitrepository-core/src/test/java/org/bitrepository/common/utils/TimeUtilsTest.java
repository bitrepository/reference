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

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class TimeUtilsTest {
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
        Assertions.assertTrue(millisInSec.startsWith(expectedSec));

        addStep("Test that milliseconds can be converted into human readable minutes.",
                "Pi days % hours");
        String millisInMin = TimeUtils.millisecondsToMinutes(millis % 3600000);
        String expectedMin = "23m";
        Assertions.assertTrue(millisInMin.startsWith(expectedMin));

        addStep("Test that milliseconds can be converted into human readable hours.",
                "Pi days % days");
        String millisInHour = TimeUtils.millisecondsToHours(millis % (3600000 * 24));
        String expectedHours = "3h";
        Assertions.assertTrue(millisInHour.startsWith(expectedHours));

        addStep("Test that milliseconds can be converted into human readable minutes.",
                "Pi days");
        String millisInDay = TimeUtils.millisecondsToDays(millis);
        String expectedDays = "3d";
        Assertions.assertTrue(millisInDay.startsWith(expectedDays));

        addStep("Test the human readable output.", "");
        String human = TimeUtils.millisecondsToHuman(millis);
        Assertions.assertTrue(human.contains(expectedSec), human);
        Assertions.assertTrue(human.contains(expectedMin), human);
        Assertions.assertTrue(human.contains(expectedHours), human);
        Assertions.assertTrue(human.contains(expectedDays), human);
    }

    @Test
    @Tag("regressiontest")
    public void printsHumanDuration() {
        Assertions.assertEquals("1y", TimeUtils.durationToHumanUsingEstimates(ChronoUnit.YEARS.getDuration()));
        Assertions.assertEquals("1m", TimeUtils.durationToHumanUsingEstimates(ChronoUnit.MONTHS.getDuration()));
        Assertions.assertEquals("1d", TimeUtils.durationToHumanUsingEstimates(ChronoUnit.DAYS.getDuration()));
        Assertions.assertEquals("1h", TimeUtils.durationToHumanUsingEstimates(ChronoUnit.HOURS.getDuration()));
        Assertions.assertEquals("1m", TimeUtils.durationToHumanUsingEstimates(ChronoUnit.MINUTES.getDuration()));
        // Don’t print seconds
        Assertions.assertEquals("0m", TimeUtils.durationToHumanUsingEstimates(ChronoUnit.SECONDS.getDuration()));
        Assertions.assertEquals("2h 3m", TimeUtils.durationToHumanUsingEstimates(Duration.parse("PT2H3M5S")));

        addStep("Test the limits of what the method handles", "0m and 500y respectively");
        Assertions.assertEquals("0m", TimeUtils.durationToHumanUsingEstimates(Duration.ZERO));
        Assertions.assertEquals("500y", TimeUtils.durationToHumanUsingEstimates(Duration.ofHours(4_382_910)));
    }

    @Test
    @Tag("regressiontest")
    public void zeroIntervalTest() throws Exception {
        addDescription("Verifies that a 0 ms interval is represented correctly");
        addStep("Call millisecondsToHuman with 0 ms", "The output should be '0 ms'");
        String zeroTimeString = TimeUtils.millisecondsToHuman(0);
        Assertions.assertEquals(" 0 ms", zeroTimeString);
    }

    @Test
    @Tag("regressiontest")
    public void durationsPrintHumanly() {
        addDescription("Tests durationToHuman()");

        Assertions.assertTrue(TimeUtils.durationToHuman(Duration.ZERO).contains("0"),
                "Zero duration should contain a 0 digit");

        Assertions.assertEquals("2d", TimeUtils.durationToHuman(Duration.ofDays(2)));
        Assertions.assertEquals("3h", TimeUtils.durationToHuman(Duration.ofHours(3)));
        Assertions.assertEquals("5m", TimeUtils.durationToHuman(Duration.ofMinutes(5)));
        Assertions.assertEquals("7s", TimeUtils.durationToHuman(Duration.ofSeconds(7)));
        Assertions.assertEquals("11 ms", TimeUtils.durationToHuman(Duration.ofMillis(11)));
        Assertions.assertEquals("13 ns", TimeUtils.durationToHuman(Duration.ofNanos(13)));
        // When there are nanoseconds, don't print millis
        Assertions.assertEquals("999999937 ns", TimeUtils.durationToHuman(Duration.ofNanos(999_999_937)));

        Assertions.assertEquals("minus 2d", TimeUtils.durationToHuman(Duration.ofDays(-2)));
        Assertions.assertEquals("minus 13 ns", TimeUtils.durationToHuman(Duration.ofNanos(-13)));

        Duration allUnits = Duration.parse("P3DT5H7M11.013000017S");
        Assertions.assertEquals("3d 5h 7m 11s 13000017 ns", TimeUtils.durationToHuman(allUnits));
    }

    @Test
    @Tag("regressiontest")
    public void differencesPrintHumanly() {
        addDescription("TimeUtils.humanDifference() should return" +
                " similar human readable strings to those from millisecondsToHuman()");

        addStep("Call humanDifference() with same time twice", "The output should be '0m'");
        String zeroTimeString = TimeUtils.humanDifference(BASE, BASE);
        Assertions.assertEquals("0m", zeroTimeString);

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
        Assertions.assertEquals("1d 23h 59m", oneDaySomethingString);
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
        String differenceString = TimeUtils.humanDifference(BASE, end);
        Assertions.assertEquals(expected, differenceString);
    }

    /*
     * The test only ensures that the output format is fixed. Which timezone the date is
     * formatted to depends on the default/system timezone. At some time the use of the old java Date
     * api should be discontinued and the new Java Time api used instead.
     */
    @Test
    @Tag("regressiontest")
    public void shortDateTest() {
        DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ROOT);
        Date date = new Date(1360069129256L);
        String shortDateString = TimeUtils.shortDate(date);
        Assertions.assertEquals(formatter.format(date), shortDateString);
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
        CountAndTimeUnit expectedZero = TimeUtils.durationToCountAndTimeUnit(Duration.ZERO);
        Assertions.assertEquals(0, expectedZero.getCount());
        Assertions.assertNotNull(expectedZero.getUnit());

        Assertions.assertEquals(new CountAndTimeUnit(1, TimeUnit.NANOSECONDS),
                TimeUtils.durationToCountAndTimeUnit(Duration.ofNanos(1)));
        Assertions.assertEquals(new CountAndTimeUnit(Long.MAX_VALUE, TimeUnit.NANOSECONDS),
                TimeUtils.durationToCountAndTimeUnit(Duration.ofNanos(Long.MAX_VALUE)));
        Assertions.assertEquals(new CountAndTimeUnit(Long.MAX_VALUE / 1000 + 1, TimeUnit.MICROSECONDS),
                TimeUtils.durationToCountAndTimeUnit(Duration.of(Long.MAX_VALUE / 1000 + 1, ChronoUnit.MICROS)));
        Assertions.assertEquals(new CountAndTimeUnit(Long.MAX_VALUE, TimeUnit.MICROSECONDS),
                TimeUtils.durationToCountAndTimeUnit(Duration.of(Long.MAX_VALUE, ChronoUnit.MICROS)));
        Assertions.assertEquals(new CountAndTimeUnit(Long.MAX_VALUE / 1000 + 1, TimeUnit.MILLISECONDS),
                TimeUtils.durationToCountAndTimeUnit(Duration.ofMillis(Long.MAX_VALUE / 1000 + 1)));
        Assertions.assertEquals(new CountAndTimeUnit(Long.MAX_VALUE, TimeUnit.MILLISECONDS),
                TimeUtils.durationToCountAndTimeUnit(Duration.ofMillis(Long.MAX_VALUE)));
        Assertions.assertEquals(new CountAndTimeUnit(Long.MAX_VALUE / 1000 + 1, TimeUnit.SECONDS),
                TimeUtils.durationToCountAndTimeUnit(Duration.ofSeconds(Long.MAX_VALUE / 1000 + 1)));
        Assertions.assertEquals(new CountAndTimeUnit(Long.MAX_VALUE, TimeUnit.SECONDS),
                TimeUtils.durationToCountAndTimeUnit(Duration.ofSeconds(Long.MAX_VALUE)));
    }

}
