/*
 * #%L
 * Bitrepository Modifying Client
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

import java.util.Date;
import java.util.GregorianCalendar;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.common.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Utility class for calendar issues. 
 */
public final class CalendarUtils {
    /** The log.*/
    private static Logger log = LoggerFactory.getLogger(CalendarUtils.class);

    /**
     * Private constructor to prevent instantiation of utility class.
     */
    private CalendarUtils() { }
    
    /**
     * Turns a date into a XMLGregorianCalendar.
     * @param date The date. If the argument is null, then epoch is returned.
     * @return The XMLGregorianCalendar.
     */
    public static XMLGregorianCalendar getXmlGregorianCalendar(Date date) {
        if(date == null) {
            log.info("Cannot convert the date '" + date + "'. Returning epoch instead.");
            date = new Date(0);
        }
        
        GregorianCalendar gc = new GregorianCalendar();
        try {
            gc.setTime(date);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert the date '" + date + "' into the xml format.", e);
        }
    }
    
    /**
     * Method for easier retrieving the current date in XML format.
     * @return The current date in XML format
     */
    public static XMLGregorianCalendar getNow() {
        return getXmlGregorianCalendar(new Date());
    }
    
    /**
     * Method for easier retrieving the date for Epoch (January 1, 1970 00:00:00.000 GMT).
     * @return Epoch in XMLGregorianCalendar format.
     */
    public static XMLGregorianCalendar getEpoch() {
        return getXmlGregorianCalendar(new Date(0));
    }
    
    /**
     * Method for easier retrieving the Date for a given time since Epoch in millis.
     * @param millis The amount of milliseconds since Epoch.
     * @return The date in XMLGregorianCalendar format.
     */
    public static XMLGregorianCalendar getFromMillis(long millis) {
        return getXmlGregorianCalendar(new Date(millis));
    }
    
    /**
     * Method for converting a date from the XML calendar type 'XMLGregorianCalendar' to the default java date.
     * 
     * @param xmlCal The XML calendar to convert from.
     * @return The date for the XML calendar converted into the default java date class.
     */
    public static Date convertFromXMLGregorianCalendar(XMLGregorianCalendar xmlCal) {
        ArgumentValidator.checkNotNull(xmlCal, "XMLGregorianCalendar xmlCal");
        
        return xmlCal.toGregorianCalendar().getTime();
    }
}
