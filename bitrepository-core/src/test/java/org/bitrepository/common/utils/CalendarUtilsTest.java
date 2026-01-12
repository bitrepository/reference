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

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.bitrepository.protocol.utils.AllureTestUtils;

import static org.bitrepository.protocol.utils.AllureTestUtils.addDescription;
import static org.bitrepository.protocol.utils.AllureTestUtils.addStep;


public class CalendarUtilsTest {
    long DATE_IN_MILLIS = 123456789L;
    
    @Test
    @Tag("regressiontest")
    public void calendarTester() throws Exception {
        addDescription("Test the calendar utility class");
        addStep("Test the convertion of a date", "Should be the same date.");
        Date date = new Date(DATE_IN_MILLIS);
        XMLGregorianCalendar calendar = CalendarUtils.getXmlGregorianCalendar(date);
        Assertions.assertEquals(calendar.toGregorianCalendar().getTimeInMillis(), DATE_IN_MILLIS);

        addStep("Test that a 'null' date is equivalent to epoch", "Should be date '0'");
        calendar = CalendarUtils.getXmlGregorianCalendar((Date)null);
        Assertions.assertEquals(calendar.toGregorianCalendar().getTimeInMillis(), 0);
        
        addStep("Test epoch", "Should be date '0'");
        calendar = CalendarUtils.getEpoch();
        Assertions.assertEquals(calendar.toGregorianCalendar().getTimeInMillis(), 0);
        
        addStep("Test that a given time in millis is extractable in millis", "Should be same value");
        calendar = CalendarUtils.getFromMillis(DATE_IN_MILLIS);
        Assertions.assertEquals(calendar.toGregorianCalendar().getTimeInMillis(), DATE_IN_MILLIS);
        
        addStep("Test the 'getNow' function", "Should give a value very close to System.currentTimeInMillis");
        long beforeNow = System.currentTimeMillis();
        calendar = CalendarUtils.getNow();
        long afterNow = System.currentTimeMillis();
        Assertions.assertTrue(calendar.toGregorianCalendar().getTimeInMillis() <= afterNow);
        Assertions.assertTrue(calendar.toGregorianCalendar().getTimeInMillis() >= beforeNow);
        
        addStep("Test the reverse conversion, from XMLCalendar to Date", "Should give the same value");
        date = CalendarUtils.convertFromXMLGregorianCalendar(calendar);
        Assertions.assertTrue(date.getTime() <= afterNow);
        Assertions.assertTrue(date.getTime() >= beforeNow);
        Assertions.assertEquals(date.getTime(), calendar.toGregorianCalendar().getTimeInMillis());
    }

    @Test
    public void displaysNiceTimeZoneId() {
        addDescription("Test that the time zone ID logged is human readable (for example Europe/Copenhagen)");
        ZoneId zoneId = ZoneId.of("Europe/Copenhagen");
        String displayName = CalendarUtils.getTimeZoneDisplayName(TimeZone.getTimeZone(zoneId));
        Assertions.assertEquals(displayName, "Europe/Copenhagen");
    }

    @Test
    @Tag("regressiontest")
    public void startDateTest() throws ParseException {
        addDescription("Test that the start date is considered as localtime and converted into UTC.");
        CalendarUtils cu = CalendarUtils.getInstance(TimeZone.getTimeZone("Europe/Copenhagen"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT);
        Date expectedStartOfDay = sdf.parse("2015-02-25T23:00:00.000Z");
        
        Date parsedStartOfDay = cu.makeStartDateObject("2015/02/26");
        Assertions.assertEquals(parsedStartOfDay, expectedStartOfDay);
    }
    
    @Test
    @Tag("regressiontest")
    public void endDateTest() throws ParseException {
        addDescription("Test that the end date is considered as localtime and converted into UTC.");
        CalendarUtils cu = CalendarUtils.getInstance(TimeZone.getTimeZone("Europe/Copenhagen"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT);
        Date expectedStartOfDay = sdf.parse("2015-02-26T22:59:59.999Z");
        
        Date parsedStartOfDay = cu.makeEndDateObject("2015/02/26");
        Assertions.assertEquals(parsedStartOfDay, expectedStartOfDay);
    }
    
    @Test
    @Tag("regressiontest")
    public void endDateRolloverTest() throws ParseException {
        addDescription("Test that the end date is correctly rolls over a year and month change.");
        CalendarUtils cu = CalendarUtils.getInstance(TimeZone.getTimeZone("Europe/Copenhagen"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT);
        Date expectedStartOfDay = sdf.parse("2016-01-01T22:59:59.999Z");
        
        Date parsedStartOfDay = cu.makeEndDateObject("2015/12/32");
        Assertions.assertEquals(parsedStartOfDay, expectedStartOfDay);
    }
    
    @Test
    @Tag("regressiontest")
    public void testBeginningOfDay() throws ParseException {
        addDescription("Tests that the time is converted to the beginning of the day localtime, not UTC");
        CalendarUtils cu = CalendarUtils.getInstance(TimeZone.getTimeZone("Europe/Copenhagen"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT);
        Date expectedStartOfDayInUTC = sdf.parse("2016-01-31T23:00:00.000Z");
        System.out.println("expectedStartOfDayInUTC parsed: " + expectedStartOfDayInUTC.getTime());
        Date parsedStartOfDay = cu.makeStartDateObject("2016/02/01");
        Assertions.assertEquals(parsedStartOfDay, expectedStartOfDayInUTC);
    }
    
    @Test
    @Tag("regressiontest")
    public void testEndOfDay() throws ParseException {
        addDescription("Tests that the time is converted to the beginning of the day localtime, not UTC");
        CalendarUtils cu = CalendarUtils.getInstance(TimeZone.getTimeZone("Europe/Copenhagen"));
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", Locale.ROOT);
        Date expectedEndOfDayInUTC = sdf.parse("2016-02-01T22:59:59.999Z");
        Date parsedEndOfDay = cu.makeEndDateObject("2016/02/01");
        Assertions.assertEquals(parsedEndOfDay, expectedEndOfDayInUTC);
    }
    
    @Test
    @Tag("regressiontest")
    public void testSummerWinterTimeChange() {
        addDescription("Test that the interval between start and end date on a summertime to "
                + "wintertime change is 25 hours (-1 millisecond).");
        CalendarUtils cu = CalendarUtils.getInstance(TimeZone.getTimeZone("Europe/Copenhagen"));
        Date startDate = cu.makeStartDateObject("2015/10/25");
        Assertions.assertNotNull(startDate);
        Date endDate = cu.makeEndDateObject("2015/10/25");
        Assertions.assertNotNull(endDate);
        long MS_PER_HOUR = 1000 * 60 * 60;
        long expectedIntervalLength = (MS_PER_HOUR * 25) - 1;
        Assertions.assertEquals(endDate.getTime() - startDate.getTime(), expectedIntervalLength);
    }
    
    @Test
    @Tag("regressiontest")
    public void testWinterSummerTimeChange() {
        addDescription("Test that the interval between start and end date on a wintertime to "
                + "summertime change is 23 hours (-1 millisecond).");
        CalendarUtils cu = CalendarUtils.getInstance(TimeZone.getTimeZone("Europe/Copenhagen"));
        Date startDate = cu.makeStartDateObject("2016/03/27");
        Assertions.assertNotNull(startDate);
        Date endDate = cu.makeEndDateObject("2016/03/27");
        Assertions.assertNotNull(endDate);
        long MS_PER_HOUR = 1000 * 60 * 60;
        long expectedIntervalLength = (MS_PER_HOUR * 23) - 1;
        Assertions.assertEquals(endDate.getTime() - startDate.getTime(), expectedIntervalLength);
    }

}
