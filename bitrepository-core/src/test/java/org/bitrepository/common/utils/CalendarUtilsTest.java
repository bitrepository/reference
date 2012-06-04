package org.bitrepository.common.utils;

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CalendarUtilsTest extends ExtendedTestCase {
    long DATE_IN_MILLIS = 123456789L;
    
    @Test(groups = {"regressiontest"})
    public void calendarTester() throws Exception {
        addDescription("Test the calendar utility class");
        addStep("Test the convertion of a date", "Should be the same date.");
        Date date = new Date(DATE_IN_MILLIS);
        XMLGregorianCalendar calendar = CalendarUtils.getXmlGregorianCalendar(date);
        Assert.assertEquals(calendar.toGregorianCalendar().getTimeInMillis(), DATE_IN_MILLIS);

        addStep("Test that a 'null' date is equivalent to epoch", "Should be date '0'");
        calendar = CalendarUtils.getXmlGregorianCalendar(null);
        Assert.assertEquals(calendar.toGregorianCalendar().getTimeInMillis(), 0);
        
        addStep("Test epoch", "Should be date '0'");
        calendar = CalendarUtils.getEpoch();
        Assert.assertEquals(calendar.toGregorianCalendar().getTimeInMillis(), 0);
        
        addStep("Test that a given time in millis is extractable in millis", "Should be same value");
        calendar = CalendarUtils.getFromMillis(DATE_IN_MILLIS);
        Assert.assertEquals(calendar.toGregorianCalendar().getTimeInMillis(), DATE_IN_MILLIS);
        
        addStep("Test the 'getNow' function", "Should give a value very close to System.currentTimeInMillis");
        long beforeNow = System.currentTimeMillis();
        calendar = CalendarUtils.getNow();
        long afterNow = System.currentTimeMillis();
        Assert.assertTrue(calendar.toGregorianCalendar().getTimeInMillis() <= afterNow);
        Assert.assertTrue(calendar.toGregorianCalendar().getTimeInMillis() >= beforeNow);
        
        addStep("Test the reverse conversion, from XMLCalendar to Date", "Should give the same value");
        date = CalendarUtils.convertFromXMLGregorianCalendar(calendar);
        Assert.assertTrue(date.getTime() <= afterNow);
        Assert.assertTrue(date.getTime() >= beforeNow);
        Assert.assertTrue(calendar.toGregorianCalendar().getTimeInMillis() == date.getTime());
    }
}
