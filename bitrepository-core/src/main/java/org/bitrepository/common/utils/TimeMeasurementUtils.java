/*
 * #%L
 * Bitrepository Protocol
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

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureUnit;
import org.bitrepository.common.ArgumentValidator;

import java.math.BigInteger;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Provides helper method for accessing {@link TimeMeasureTYPE} objects.
 */
public class TimeMeasurementUtils {

    static final BigInteger MILLIS_PER_HOUR = BigInteger.valueOf(Duration.ofHours(1).toMillis());
    private static final BigInteger MILLISECONDS_PER_SECOND = BigInteger.valueOf(1000);

    /**
     * Private constructor. To prevent instantiation of this utility class.
     */
    private TimeMeasurementUtils() {
    }

    /**
     * Generates a TimeMeasureTYPE object based on a milliseconds value.
     *
     * @param milliseconds The time measure in milliseconds.
     * @return A corresponding <code>TimeMeasureTYPE</code> object.
     */
    public static TimeMeasureTYPE getTimeMeasurementFromMilliseconds(BigInteger milliseconds) {
        TimeMeasureTYPE timeMeasure = new TimeMeasureTYPE();
        timeMeasure.setTimeMeasureValue(milliseconds);
        timeMeasure.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
        return timeMeasure;
    }

    /**
     * Method for getting the maximum time. Uses the maximum value of Long and puts it in {@link TimeMeasureUnit#HOURS)}.
     *
     * @return The TimeMeasure for the maximum time.
     */
    public static TimeMeasureTYPE getMaximumTime() {
        TimeMeasureTYPE timeMeasure = new TimeMeasureTYPE();
        timeMeasure.setTimeMeasureValue(BigInteger.valueOf(Long.MAX_VALUE));
        timeMeasure.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        return timeMeasure;
    }

    /**
     * Compares its two arguments for order. Returns a negative integer, zero, or a positive integer as the first
     * argument is less than, equal to, or greater than the second.
     *
     * @param time1 time1
     * @param time2 time2
     * @return a negative integer, zero, or a positive integer as the first argument is less than, equal to, or
     * greater than the second.
     */
    public static int compare(TimeMeasureTYPE time1, TimeMeasureTYPE time2) {
        ArgumentValidator.checkNotNull(time1, "time1");
        ArgumentValidator.checkNotNull(time2, "time2");
        return convertToMilliSeconds(time1).compareTo(convertToMilliSeconds(time2));
    }

    /**
     * Normalizes {@link TimeMeasureTYPE} into milliseconds.
     *
     * @param timeMeasure The time measure to convert
     * @return The time measure value in milliseconds as a BigInteger
     */
    private static BigInteger convertToMilliSeconds(TimeMeasureTYPE timeMeasure) {
        return switch (timeMeasure.getTimeMeasureUnit()) {
            case MILLISECONDS -> timeMeasure.getTimeMeasureValue();
            case HOURS -> timeMeasure.getTimeMeasureValue().multiply(MILLIS_PER_HOUR);
        };
    }

    /**
     * Compares a {@link TimeMeasureTYPE} to a long representation in milliseconds.
     *
     * @param time1 The TimeMeasure to compare.
     * @param time2 The time in milliseconds to compare.
     * @return -1 if time2 is larger, 0 if they are equals, or 1 if time1 is larger.
     */
    public static int compare(TimeMeasureTYPE time1, long time2) {
        return convertToMilliSeconds(time1).compareTo(BigInteger.valueOf(time2));
    }

    /**
     * Method for converting a {@link TimeMeasureTYPE} into a long.
     *
     * @param time1 The TimeMeasureTYPE to convert.
     * @return The value of the TimeMeasure as a long.
     */
    public static long getTimeMeasureInLong(TimeMeasureTYPE time1) {
        return convertToMilliSeconds(time1).longValueExact();
    }

    /**
     * Converts a {@code TimeMeasureTYPE} to a {@code Duration}.
     *
     * @throws ArithmeticException by overflow of the range of {@code Duration}
     * (roughly +/- 292 277 024 626 years)
     */
    public static Duration timeMeasureToDuration(TimeMeasureTYPE timeMeasure) {
        BigInteger timeMeasureValue = timeMeasure.getTimeMeasureValue();
        return switch (timeMeasure.getTimeMeasureUnit()) {
            case MILLISECONDS -> {
                BigInteger[] secondsAndMillis = timeMeasureValue.divideAndRemainder(MILLISECONDS_PER_SECOND);
                yield Duration.ofSeconds(secondsAndMillis[0].longValueExact())
                        .plusMillis(secondsAndMillis[1].intValueExact());
            }
            case HOURS -> Duration.ofHours(timeMeasureValue.longValueExact());
        };
    }

    /**
     * <p>Converts a {@code Duration} to a {@code TimeMeasureTYPE}.</p>
     *
     * <p>Any fraction of milliseconds is rounded towards zero (truncated).
     * For example PT0.6666S is rounded to PT0.666S and PT-0.6666S to PT-0.666S</p>
     */
    public static TimeMeasureTYPE durationToTimeMeasure(Duration duration) {
        duration = duration.truncatedTo(ChronoUnit.MILLIS);

        if (duration.equals(duration.truncatedTo(ChronoUnit.HOURS))) { // Nothing smaller than hours
            // Use hours as unit in result
            TimeMeasureTYPE result = new TimeMeasureTYPE();
            result.setTimeMeasureValue(BigInteger.valueOf(duration.toHours()));
            result.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
            return result;
        } else {
            BigInteger totalMilliseconds = BigInteger.valueOf(duration.getSeconds()).multiply(MILLISECONDS_PER_SECOND)
                    .add(BigInteger.valueOf(duration.toMillisPart()));
            return getTimeMeasurementFromMilliseconds(totalMilliseconds);
        }
    }

}
