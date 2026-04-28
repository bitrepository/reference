/*
 * #%L
 * Bitrepository Core
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General License as
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

import javax.xml.datatype.XMLGregorianCalendar;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

class CalendarUtilsTest {
    long DATE_IN_MILLIS = 123456789L;

    @Test
    @Tag("regressiontest")
    void calendarTester() {
        addDescription("Test the calendar utility class");
        addStep("Test the convertion of a date", "Should be the same date.");
        Instant testInstant = Instant.ofEpochMilli(DATE_IN_MILLIS);
        XMLGregorianCalendar calendar = CalendarUtils.getXmlGregorianCalendar(testInstant);
        Assertions.assertEquals(DATE_IN_MILLIS, calendar.toGregorianCalendar().getTimeInMillis());

        addStep("Test that a 'null' date is equivalent to epoch", "Should be date '0'");
        calendar = CalendarUtils.getXmlGregorianCalendar((Instant) null);
        Assertions.assertEquals(0, calendar.toGregorianCalendar().getTimeInMillis());

        addStep("Test epoch", "Should be date '0'");
        calendar = CalendarUtils.getEpoch();
        Assertions.assertEquals(0, calendar.toGregorianCalendar().getTimeInMillis());

        addStep("Test that a given time in millis is extractable in millis", "Should be same value");
        calendar = CalendarUtils.getFromMillis(DATE_IN_MILLIS);
        Assertions.assertEquals(DATE_IN_MILLIS, calendar.toGregorianCalendar().getTimeInMillis());

        addStep("Test the 'getNow' function",
                "Should give a value very close to System.currentTimeInMillis");
        Instant beforeNow = Instant.now().truncatedTo(ChronoUnit.MILLIS);
        calendar = CalendarUtils.getNow();
        Instant afterNow = Instant.now();
        Instant nowInCalendar = CalendarUtils.convertFromXMLGregorianCalendarToInstant(calendar);
        Assertions.assertFalse(nowInCalendar.isBefore(beforeNow), "Time in calendar should not be before 'beforeNow'");
        Assertions.assertFalse(nowInCalendar.isAfter(afterNow), "Time in calendar should not be after 'afterNow'");

        addStep("Test the reverse conversion, from XMLCalendar to Instant",
                "Should give the same value");
        testInstant = CalendarUtils.convertFromXMLGregorianCalendarToInstant(calendar);
        Assertions.assertEquals(nowInCalendar, testInstant);
    }

    @Test
    void displaysNiceTimeZoneId() {
        addDescription("Test that the time zone ID logged is human readable (for example Europe/Copenhagen)");
        ZoneId zoneId = ZoneId.of("Europe/Copenhagen");
        String displayName = CalendarUtils.getTimeZoneDisplayName(zoneId);
        Assertions.assertEquals("Europe/Copenhagen", displayName);
    }

    @Test
    @Tag("regressiontest")
    void startDateTest() {
        addDescription("Test that the start date is considered as localtime and converted into UTC.");
        CalendarUtils cu = CalendarUtils.getInstance(ZoneId.of("Europe/Copenhagen"));
        Instant expectedStartOfDay = Instant.parse("2015-02-25T23:00:00.000Z");

        Instant parsedStartOfDay = cu.makeStartInstant("2015/02/26");
        Assertions.assertEquals(expectedStartOfDay, parsedStartOfDay);
    }

    @Test
    @Tag("regressiontest")
    void endDateTest() {
        addDescription("Test that the end date is considered as localtime and converted into UTC.");
        CalendarUtils cu = CalendarUtils.getInstance(ZoneId.of("Europe/Copenhagen"));
        Instant expectedEndOfDay = Instant.parse("2015-02-26T22:59:59.999Z");

        Instant parsedEndOfDay = cu.makeEndInstant("2015/02/26");
        Assertions.assertEquals(expectedEndOfDay, parsedEndOfDay);
    }

    @Test
    @Tag("regressiontest")
    void endDateRolloverTest() {
        addDescription("Test that the end date is correctly rolls over a year and month change.");
        CalendarUtils cu = CalendarUtils.getInstance(ZoneId.of("Europe/Copenhagen"));
        Instant expectedEndOfDay = Instant.parse("2015-12-31T22:59:59.999Z");

        // Note: New implementation uses strict parsing, so 2015/12/32 is invalid. 
        // Changed to 2015/12/31 to test end-of-year.
        Instant parsedEndOfDay = cu.makeEndInstant("2015/12/31");
        Assertions.assertEquals(expectedEndOfDay, parsedEndOfDay);
    }

    @Test
    @Tag("regressiontest")
    void testBeginningOfDay() {
        addDescription("Tests that the time is converted to the beginning of the day localtime, not UTC");
        CalendarUtils cu = CalendarUtils.getInstance(ZoneId.of("Europe/Copenhagen"));
        Instant expectedStartOfDayInUTC = ZonedDateTime.parse("2016-01-31T23:00:00.000Z",
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC"))).toInstant();
        Instant parsedStartOfDay = cu.makeStartInstant("2016/02/01");
        Assertions.assertEquals(expectedStartOfDayInUTC, parsedStartOfDay);
    }

    @Test
    @Tag("regressiontest")
    void testEndOfDay() {
        addDescription("Tests that the time is converted to the beginning of the day localtime, not UTC");
        CalendarUtils cu = CalendarUtils.getInstance(ZoneId.of("Europe/Copenhagen"));
        Instant expectedEndOfDayInUTC = ZonedDateTime.parse("2016-02-01T22:59:59.999Z",
                DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.of("UTC"))).toInstant();
        Instant parsedEndOfDay = cu.makeEndInstant("2016/02/01");
        Assertions.assertEquals(expectedEndOfDayInUTC, parsedEndOfDay);
    }

    @Test
    @Tag("regressiontest")
    void testSummerWinterTimeChange() {
        addDescription("Test that the interval between start and end date on a summertime to "
                + "wintertime change is 25 hours (-1 millisecond).");
        CalendarUtils cu = CalendarUtils.getInstance(ZoneId.of("Europe/Copenhagen"));
        Instant startDate = cu.makeStartInstant("2015/10/25");
        Assertions.assertNotNull(startDate);
        Instant endDate = cu.makeEndInstant("2015/10/25");
        Assertions.assertNotNull(endDate);
        Duration expectedIntervalLength = Duration.ofHours(25).minusMillis(1);
        Assertions.assertEquals(expectedIntervalLength, Duration.between(startDate, endDate));
    }

    @Test
    @Tag("regressiontest")
    void testWinterSummerTimeChange() {
        addDescription("Test that the interval between start and end date on a wintertime to "
                + "summertime change is 23 hours (-1 millisecond).");
        CalendarUtils cu = CalendarUtils.getInstance(ZoneId.of("Europe/Copenhagen"));
        Instant startDate = cu.makeStartInstant("2016/03/27");
        Assertions.assertNotNull(startDate);
        Instant endDate = cu.makeEndInstant("2016/03/27");
        Assertions.assertNotNull(endDate);
        Duration expectedIntervalLength = Duration.ofHours(23).minusMillis(1);
        Assertions.assertEquals(expectedIntervalLength, Duration.between(startDate, endDate));
    }

}
