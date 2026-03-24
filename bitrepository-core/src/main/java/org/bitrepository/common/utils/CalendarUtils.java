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

import org.bitrepository.common.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.function.Consumer;

/**
 * Utility class for calendar issues.
 */
public final class CalendarUtils {
    private static final Logger log = LoggerFactory.getLogger(CalendarUtils.class);
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM/dd", Locale.ROOT);

    private TimeZone localTimeZone = TimeZone.getDefault();
    private ZoneId localZoneId = ZoneId.systemDefault();

    private CalendarUtils() {
    }

    /**
     * Get an instance of CalendarUtils with a non-server default timeZone
     *
     * @param timeZone The TimeZone to use
     * @return The CalendarUtils instance for the non-standard timeZone
     * @deprecated Use {@link #getInstance(ZoneId)} instead
     */
    @Deprecated( forRemoval = true)
    public static CalendarUtils getInstance(TimeZone timeZone) {
        CalendarUtils cu = new CalendarUtils();
        cu.setTimeZone(timeZone);
        return cu;
    }

    /**
     * Get an instance of CalendarUtils with a non-server default zoneId
     *
     * @param zoneId The ZoneId to use
     * @return The CalendarUtils instance for the non-standard zoneId
     */
    public static CalendarUtils getInstance(ZoneId zoneId) {
        CalendarUtils cu = new CalendarUtils();
        cu.setZoneId(zoneId);
        return cu;
    }

    /**
     * @deprecated Use {@link #setZoneId(ZoneId)} instead
     */
    @Deprecated( forRemoval = true)
    private void setTimeZone(TimeZone timeZone) {
        log.debug("Using time zone: '{}'", getTimeZoneDisplayName(timeZone));
        this.localTimeZone = timeZone;
        this.localZoneId = timeZone.toZoneId();
    }

    private void setZoneId(ZoneId zoneId) {
        log.debug("Using zone id: '{}'", zoneId.getId());
        this.localZoneId = zoneId;
        this.localTimeZone = TimeZone.getTimeZone(zoneId);
    }

    /**
     * @deprecated Use {@link ZoneId#getId()} instead
     */
    @Deprecated( forRemoval = true)
    public static String getTimeZoneDisplayName(TimeZone timeZone) {
        return timeZone.getID();
    }

    /**
     * Get the display name for the ZoneId.
     * @param zoneId the ZoneId
     * @return the ID
     */
    public static String getTimeZoneDisplayName(ZoneId zoneId) {
        return zoneId.getId();
    }

    /**
     * Turns an Instant into a XMLGregorianCalendar.
     *
     * @param instant The instant. If the argument is null, then epoch is returned.
     * @return The XMLGregorianCalendar.
     */
    public static XMLGregorianCalendar getXmlGregorianCalendar(Instant instant) {
        if (instant == null) {
            log.debug("Instant is null. Returning epoch instead.");
            instant = Instant.EPOCH;
        }

        ZonedDateTime zdt = instant.atZone(ZoneId.systemDefault());
        GregorianCalendar gc = GregorianCalendar.from(zdt);
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert the instant '" + instant + "' into the xml format.", e);
        }
    }

    /**
     * Turns a date into a XMLGregorianCalendar.
     *
     * @param date The date. If the argument is null, then epoch is returned.
     * @return The XMLGregorianCalendar.
     * @deprecated Use {@link #getXmlGregorianCalendar(Instant)} instead
     */
    @Deprecated( forRemoval = true)
    public static XMLGregorianCalendar getXmlGregorianCalendar(Date date) {
        if (date == null) {
            log.debug("Date is null. Returning epoch instead.");
            date = new Date(0);
        }

        GregorianCalendar gc = new GregorianCalendar(TimeZone.getDefault(), Locale.ROOT);
        try {
            gc.setTime(date);
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert the date '" + date + "' into the xml format.", e);
        }
    }

    /**
     * Turns a ZonedDateTime into a XMLGregorianCalendar.
     *
     * @param zonedDateTime The ZonedDateTime.
     * @return The XMLGregorianCalendar.
     */
    public static XMLGregorianCalendar getXmlGregorianCalendar(ZonedDateTime zonedDateTime) {
        ArgumentValidator.checkNotNull(zonedDateTime, "ZonedDateTime zonedDateTime");
        GregorianCalendar gc = GregorianCalendar.from(zonedDateTime);
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gc);
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert the ZonedDateTime '" + zonedDateTime + "' into the xml format.", e);
        }
    }

    /**
     * Method for easier retrieving the current date in XML format.
     *
     * @param gregorianCalendar the calendar
     * @return The current date in XML format
     * @deprecated Use {@link #getXmlGregorianCalendar(Instant)} or {@link #getXmlGregorianCalendar(ZonedDateTime)} instead
     */
    @Deprecated(forRemoval = true)
    public static XMLGregorianCalendar getXmlGregorianCalendar(GregorianCalendar gregorianCalendar) {
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
        } catch (Exception e) {
            throw new IllegalStateException("Could not convert the date '" + gregorianCalendar + "' into the xml format.", e);
        }
    }

    /**
     * Method for easier retrieving the current date in XML format.
     *
     * @return The current date in XML format
     */
    public static XMLGregorianCalendar getNow() {
        return getXmlGregorianCalendar(Instant.now());
    }

    /**
     * Method for easier retrieving the date for Epoch (January 1, 1970 00:00:00.000 GMT).
     *
     * @return Epoch in XMLGregorianCalendar format.
     */
    public static XMLGregorianCalendar getEpoch() {
        return getXmlGregorianCalendar(Instant.EPOCH);
    }

    /**
     * Method for easier retrieving the Date for a given time since Epoch in millis.
     *
     * @param millis The amount of milliseconds since Epoch.
     * @return The date in XMLGregorianCalendar format.
     */
    public static XMLGregorianCalendar getFromMillis(long millis) {
        return getXmlGregorianCalendar(Instant.ofEpochMilli(millis));
    }

    /**
     * Method for converting a date from the XML calendar type 'XMLGregorianCalendar' to Instant.
     *
     * @param xmlCal The XML calendar to convert from.
     * @return The Instant for the XML calendar.
     */
    public static Instant convertFromXMLGregorianCalendarToInstant(XMLGregorianCalendar xmlCal) {
        ArgumentValidator.checkNotNull(xmlCal, "XMLGregorianCalendar xmlCal");

        return xmlCal.toGregorianCalendar().toZonedDateTime().toInstant();
    }

    /**
     * Method for converting a date from the XML calendar type 'XMLGregorianCalendar' to the default java date.
     *
     * @param xmlCal The XML calendar to convert from.
     * @return The date for the XML calendar converted into the default java date class.
     * @deprecated Use {@link #convertFromXMLGregorianCalendarToInstant(XMLGregorianCalendar)} instead
     */
    @Deprecated( forRemoval = true)
    public static Date convertFromXMLGregorianCalendar(XMLGregorianCalendar xmlCal) {
        ArgumentValidator.checkNotNull(xmlCal, "XMLGregorianCalendar xmlCal");

        return xmlCal.toGregorianCalendar().getTime();
    }

    /**
     * Create an Instant representing the start of the day denoted by dateStr
     *
     * @param dateStr The string representation of the date, in the form '2015/02/26'
     * @return Instant An instant representing the start of the day, or null if the input cannot
     * be turned into a date.
     */
    public Instant makeStartInstant(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            return localDate.atStartOfDay(localZoneId).toInstant();
        } catch (DateTimeParseException e) {
            log.warn("Received something that could not be parsed: '{}'", dateStr, e);
            return null;
        }
    }

    /**
     * Create a date object representing the start of the day denoted by dateStr
     *
     * @param dateStr The string representation of the date, in the form '02/26/2015'
     * @return Date A date object representing the start of the day, or null if the input cannot
     * be turned into a date.
     * @deprecated Use {@link #makeStartInstant(String)} instead
     */
    @Deprecated( forRemoval = true)
    public Date makeStartDateObject(String dateStr) {
        Consumer<Calendar> dateAdjuster = (Calendar calendar) -> {
        };

        Calendar cal = makeCalendarObject(dateStr, dateAdjuster);
        if (cal == null) {
            return null;
        } else {
            return cal.getTime();
        }
    }

    /**
     * Create an Instant representing the end of the day denoted by dateStr
     *
     * @param dateStr The string representation of the date, in the form '2015/02/26'
     * @return Instant An instant representing the end of the day (one millisecond before midnight),
     * or null if the input cannot be turned into a date.
     */
    public Instant makeEndInstant(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        }
        try {
            LocalDate localDate = LocalDate.parse(dateStr, DATE_FORMATTER);
            // End of day is one millisecond before the next day starts
            return localDate.plusDays(1).atStartOfDay(localZoneId).toInstant().minusMillis(1);
        } catch (DateTimeParseException e) {
            log.warn("Received something that could not be parsed: '{}'", dateStr, e);
            return null;
        }
    }

    /**
     * Create a date object representing the end of the day denoted by dateStr
     *
     * @param dateStr The string representation of the date, in the form '02/26/2015'
     * @return Date A date object representing the end of the day, or null if the input cannot
     * be turned into a date.
     * @deprecated Use {@link #makeEndInstant(String)} instead
     */
    @Deprecated( forRemoval = true)
    public Date makeEndDateObject(String dateStr) {
        Consumer<Calendar> dateAdjuster = (Calendar calendar) -> {
            calendar.add(Calendar.DAY_OF_MONTH, 1);
            calendar.add(Calendar.MILLISECOND, -1);
        };

        Calendar cal = makeCalendarObject(dateStr, dateAdjuster);
        if (cal == null) {
            return null;
        } else {
            return cal.getTime();
        }
    }

    /**
     * Parses the input string and returns a calendar representation of the day in UTC.
     *
     * @param dateStr The string representation of the date, in the form '2015/02/26'
     * @return Calendar A calendar object representing the start of the date in UTC,
     * or null if the input cannot be parsed.
     * @deprecated Use {@link #makeStartInstant(String)} or {@link #makeEndInstant(String)} instead
     */
    @Deprecated( forRemoval = true)
    private Calendar makeCalendarObject(String dateStr, Consumer<Calendar> dateAdjust) {
        if (dateStr == null || dateStr.trim().isEmpty()) {
            return null;
        } else {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ROOT);
            sdf.setTimeZone(localTimeZone);
            try {
                Date baseDate = sdf.parse(dateStr);
                Calendar time = Calendar.getInstance(localTimeZone, Locale.ROOT);
                time.setTime(baseDate);
                dateAdjust.accept(time);
                return time;
            } catch (ParseException e) {
                log.warn("Received something that could not be parsed: '{}'", dateStr, e);
                return null;
            }
        }
    }

}
