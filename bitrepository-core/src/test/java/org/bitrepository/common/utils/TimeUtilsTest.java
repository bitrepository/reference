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
import java.time.temporal.TemporalAmount;
import java.util.Date;
import java.util.Locale;

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
    public void zeroIntervalTest() throws Exception {
        addDescription("Verifies that a 0 ms interval is represented correctly");
        addStep("Call millisecondsToHuman with 0 ms", "The output should be '0 ms'");
        String zeroTimeString = TimeUtils.millisecondsToHuman(0);
        assertEquals(zeroTimeString, " 0 ms");
    }

    @Test(groups = {"regressiontest"})
    public void differencesPrintHumanly() {
        addDescription("TimeUtils.humanDifference() should return" +
                " similar human readable strings to those from millisecondsToHuman()");

<<<<<<< HEAD
        addStep("Call humanDifference() with same time twice", "The output should be '0s'");
        String zeroTimeString = TimeUtils.humanDifference(BASE, BASE);
        assertEquals(zeroTimeString, "0s");

        addStep("Call humanDifference() with a difference obtained from a Duration",
                "Expect corresponding readable output");
        // Don’t print fraction of second
        testHumanDifference("0s", Duration.ofNanos(1));
        testHumanDifference("0s", Duration.ofMillis(1));
        testHumanDifference("0s", Duration.ofNanos(499_999_999));
        testHumanDifference("1s", Duration.ofNanos(500_000_000));
        testHumanDifference("1s", Duration.ofSeconds(1));
        testHumanDifference("1m", Duration.ofMinutes(1));
        testHumanDifference("1h", Duration.ofHours(1));
        testHumanDifference("2h 3m 5s", Duration.parse("PT2H3M5.000000007S"));

        addStep("Call humanDifference() with a difference obtained from a Period",
                "Expect corresponding readable output");
        testHumanDifference("1d", Period.ofDays(1));
        testHumanDifference("1m", Period.ofMonths(1));
        testHumanDifference("1y", Period.ofYears(1));
        testHumanDifference("2y 3m 5d", Period.of(2, 3, 5));

        addStep("Call humanDifference() with a difference obtained from a combo of a Period and a Duration",
                "Expect corresponding readable output");
        testHumanDifference("3y 5m 7d 11h 13m 17s",
                Period.of(3, 5, 7), Duration.parse("PT11H13M17.023S"));

        addStep("Call humanDifference()" +
                        " with dates that are 2 days apart but times that cause the diff to be less than 2 full days",
                "Expect output 1d something");
        ZoneId testZoneId = ZoneId.of("Europe/Vienna");
        String oneDaySomethingString = TimeUtils.humanDifference(
                ZonedDateTime.of(2021, 1, 31,
                        12, 0, 0, 0, testZoneId),
                ZonedDateTime.of(2021, 2, 2,
                        11, 59, 59, 0, testZoneId));
        assertEquals("1d 23h 59m 59s", oneDaySomethingString);
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
     * The test only ensures that the output format is fixed. Which timezone the the date is 
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
}
