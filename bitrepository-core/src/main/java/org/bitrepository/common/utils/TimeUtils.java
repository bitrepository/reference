/*
 * #%L
 * Bitrepository Integrity Service
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

import org.bitrepository.common.ArgumentValidator;
import org.jetbrains.annotations.NotNull;

import javax.xml.datatype.XMLGregorianCalendar;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.Period;
import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

/**
 * Util class to handle the presentation of time in a human-readable form
 */
public final class TimeUtils {
    public static final int MS_PER_S = 1000;
    public static final int S_PER_M = 60;
    public static final int M_PER_H = 60;
    public static final int H_PER_D = 24;
    public static final int DAYS_PER_YEAR = 365;
    public static final int MS_PER_MINUTE = MS_PER_S * S_PER_M;
    public static final int MS_PER_HOUR = MS_PER_MINUTE * M_PER_H;
    public static final long MS_PER_DAY = MS_PER_HOUR * H_PER_D;
    public static final long MS_PER_YEAR = DAYS_PER_YEAR * MS_PER_DAY;
    public final static DateFormat formatter = new SimpleDateFormat("yyyy/MM/dd HH:mm", Locale.ROOT);

    private TimeUtils() {
    }

    /**
     * Convert from milliseconds to seconds.
     *
     * @param ms The milliseconds to convert
     * @return The milliseconds converted into seconds.
     */
    public static String millisecondsToSeconds(long ms) {
        int seconds = (int) (ms / MS_PER_S);
        return seconds + "s";
    }

    /**
     * Convert from milliseconds to minutes.
     *
     * @param ms The milliseconds to convert
     * @return The milliseconds converted into minutes.
     */
    public static String millisecondsToMinutes(long ms) {
        int minutes = (int) (ms / MS_PER_MINUTE);
        return minutes + "m";
    }

    /**
     * Convert from milliseconds to hours.
     *
     * @param ms The milliseconds to convert
     * @return The milliseconds converted into hours.
     */
    public static String millisecondsToHours(long ms) {
        int hours = (int) (ms / MS_PER_HOUR);
        return hours + "h";
    }

    /**
     * Convert from milliseconds to days.
     *
     * @param ms The milliseconds to convert
     * @return The milliseconds converted into days.
     */
    public static String millisecondsToDays(long ms) {
        int days = (int) (ms / MS_PER_DAY);
        return days + "d";
    }

    /**
     * Convert from milliseconds to a human-readable format with days, hours, minutes, seconds and
     * the remaining milliseconds.
     *
     * @param ms The milliseconds to convert
     * @return The milliseconds converted a human-readable format.
     */
    public static String millisecondsToHuman(long ms) {
        StringBuilder sb = new StringBuilder();
        boolean includeRemainingMs = ms < MS_PER_MINUTE;
        if (ms >= 0) {
            if (ms >= MS_PER_DAY) {
                sb.append(millisecondsToDays(ms));
            }
            ms = (ms % MS_PER_DAY);
            if (ms >= MS_PER_HOUR) {
                sb.append(" ").append(millisecondsToHours(ms));
            }
            ms = (ms % MS_PER_HOUR);
            if (ms >= MS_PER_MINUTE) {
                sb.append(" ").append(millisecondsToMinutes(ms));
            }
            ms = (ms % MS_PER_MINUTE);
            if (ms >= MS_PER_S) {
                sb.append(" ").append(millisecondsToSeconds(ms));
            }
            ms = (ms % MS_PER_S);
            if (includeRemainingMs) {
                sb.append(" ").append(ms).append(" ms");
            }
        }
        return sb.toString();
    }

    /**
     * Formats a non-negative Duration to an approximate human-readable string like "1y 2m" or "3h 45m".
     * The conversion uses estimated/approximate average values for the lengths of days, months and years.
     * The method is therefore suitable for durations longer than a month.
     *
     * The duration must be non-negative and not longer than 4 382 910 hours (approximately 500 years).
     *
     * @throws IllegalArgumentException if dur is negative or longer than 4 382 910 hours
     */
    public static String durationToHumanUsingEstimates(Duration dur) {
        ArgumentValidator.checkTrue(! dur.isNegative(), "Cannot handle a negative duration; got " + dur);
        ArgumentValidator.checkTrue(dur.compareTo(Duration.ofHours(4_382_910)) <= 0,
                "Duration is too long: " + dur);

        int years = Math.toIntExact(dur.dividedBy(ChronoUnit.YEARS.getDuration()));
        dur = dur.minus(ChronoUnit.YEARS.getDuration().multipliedBy(years));
        int months = Math.toIntExact(dur.dividedBy(ChronoUnit.MONTHS.getDuration()));
        dur = dur.minus(ChronoUnit.MONTHS.getDuration().multipliedBy(months));
        int days = Math.toIntExact(dur.dividedBy(ChronoUnit.DAYS.getDuration()));
        dur = dur.minus(ChronoUnit.DAYS.getDuration().multipliedBy(days));

        Period p = Period.of(years, months, days);

        return humanPeriodAndDuration(p, dur);
    }

    /**
     * Generate a human-readable difference between start and end like "5y 2m 23d" or "7d 23m".
     *
     * Include years, months and days if they are non-zero. Include hours if months are 6 or less.
     * Include minutes if days are 8 or less. Never include seconds.
     * This generally gives the user a precision of 0.5 % of the difference or finer.
     */
    public static String humanDifference(ZonedDateTime start, ZonedDateTime end) {
        ArgumentValidator.checkTrue(! end.isBefore(start), start + " > " + end);

        Period periodBetween = Period.between(start.toLocalDate(), end.toLocalDate());
        ZonedDateTime afterPeriod = start.plus(periodBetween);
        if (afterPeriod.isAfter(end)) { // Too far
            // One day fewer
            periodBetween = Period.between(start.toLocalDate(), end.toLocalDate().minusDays(1));
            afterPeriod = start.plus(periodBetween);
        }
        Duration durationBetween = Duration.between(afterPeriod, end);

        return humanPeriodAndDuration(periodBetween, durationBetween);
    }

    @NotNull
    private static String humanPeriodAndDuration(Period period, Duration dur) {
        // Round duration to whole minutes
        dur = dur.plusSeconds(30).truncatedTo(ChronoUnit.MINUTES);

        if (period.isZero() && dur.isZero()) {
            return "0m";
        }

        boolean includeHours = period.getYears() == 0 && period.getMonths() <= 6;
        boolean includeMinutes = period.getYears() == 0
                && period.getMonths() == 0
                && period.getDays() <= 8;

        // The following gives an ambiguous string like "3m"
        // in the very rare cases where months or minutes are non-zero and days and hours are zero.
        // It is not expected to be a problem for the user in practice.
        List<String> elements = new ArrayList<>(6);
        if (period.getYears() != 0) {
            elements.add(period.getYears() + "y");
        }
        if (period.getMonths() != 0) {
            elements.add(period.getMonths() + "m");
        }
        if (period.getDays() != 0) {
            elements.add(period.getDays() + "d");
        }
        if (includeHours && dur.toHours() != 0) {
            elements.add(dur.toHours() + "h");
        }
        if (includeMinutes && dur.toMinutesPart() != 0) {
            elements.add(dur.toMinutesPart() + "m");
        }

        return String.join(" ", elements);
    }

    /**
     * @throws ArithmeticException if dur is negative by more than Long.MAX_VALUE seconds
     */
    public static String durationToHuman(Duration dur) {
        if (dur.isZero()) {
            return "0 ms";
        }

        List<String> parts = new ArrayList<>(5);

        if (dur.isNegative()) {
            parts.add("minus");
            dur = dur.negated();
        }

        if (dur.toDays() > 0) {
            parts.add("" + dur.toDays() + "d");
        }
        if (dur.toHoursPart() > 0) {
            parts.add("" + dur.toHoursPart() + "h");
        }
        if (dur.toMinutesPart() > 0) {
            parts.add("" + dur.toMinutesPart() + "m");
        }
        if (dur.toSecondsPart() > 0) {
            parts.add("" + dur.toSecondsPart() + "s");
        }
        if (dur.toNanosPart() > 0) {
            // If the fraction of second is a whole number of millis, print as such; otherwise print only as nanos
            Duration fraction = Duration.ofNanos(dur.getNano());
            Duration remainderAfterMillis = fraction.minusMillis(fraction.toMillisPart());
            if (remainderAfterMillis.isZero()) {
                parts.add("" + fraction.toMillisPart() + " ms");
            } else {
                parts.add("" + fraction.toNanosPart() + " ns");
            }
        }

        return String.join(" ", parts);
    }

    public static String shortDate(Date date) {
        return formatter.format(date);
    }

    public static String shortDate(XMLGregorianCalendar cal) {
        return formatter.format(cal.toGregorianCalendar().getTime());
    }

    /**
     * Get the latest date of the two inputs
     *
     * @param currentMax The current latest date
     * @param itemDate   The new candidate latest
     * @return The date object that was the latest of the two inputs.
     */
    public static Date getMaxDate(Date currentMax, Date itemDate) {
        if (itemDate.after(currentMax)) {
            return itemDate;
        } else {
            return currentMax;
        }
    }

    /**
     *
     * @param duration a non-negative duration
     * @return the duration converted to a long in either SECONDS, MILLISECONDS, MICROSECONDS or NANOSECONDS
     * maintaining the best possible precision and truncated if necessary.
     * @throws IllegalArgumentException if duration is negative
     */
    public static CountAndTimeUnit durationToCountAndTimeUnit(Duration duration) {
        if (duration.isNegative()) {
            throw new IllegalArgumentException("duration must be 0 or positive, was " + duration);
        }

        if (duration.compareTo(Duration.ofNanos(Long.MAX_VALUE)) <= 0) { // fits in nanos
            return new CountAndTimeUnit(duration.toNanos(), TimeUnit.NANOSECONDS);
        }
        if (duration.compareTo(Duration.of(Long.MAX_VALUE, ChronoUnit.MICROS)) <= 0) { // fits in micros
            return new CountAndTimeUnit(duration.dividedBy(ChronoUnit.MICROS.getDuration()), TimeUnit.MICROSECONDS);
        }
        if (duration.compareTo(Duration.ofMillis(Long.MAX_VALUE)) <= 0) { // fits in millis
            return new CountAndTimeUnit(duration.toMillis(), TimeUnit.MILLISECONDS);
        }
        if (duration.compareTo(Duration.ofSeconds(Long.MAX_VALUE)) <= 0) { // fits in seconds
            return new CountAndTimeUnit(duration.toSeconds(), TimeUnit.SECONDS);
        }
        return new CountAndTimeUnit(duration.toMinutes(), TimeUnit.MINUTES);
    }

}
