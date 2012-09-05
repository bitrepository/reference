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

import java.util.Date;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.common.TestValidationUtils;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class CalendarUtilsTest extends ExtendedTestCase {
    long DATE_IN_MILLIS = 123456789L;

    @Test(groups = { "regressiontest" })
    public void utilityTester() throws Exception {
        addDescription("Test that the utility class is a proper utility class.");
        TestValidationUtils.validateUtilityClass(CalendarUtils.class);
    }

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
