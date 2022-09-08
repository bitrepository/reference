package org.bitrepository.common.utils;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureUnit;

import javax.xml.datatype.DatatypeConstants;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class XmlUtils {

    public static void validateNonNegative(javax.xml.datatype.Duration xmlDuration) {
        if (xmlDuration.getSign() < 0) {
            throw new IllegalArgumentException("Unexpected negative duration: " + xmlDuration);
        }
    }

    /**
     * Converts a javax.xml.datatype.Duration to a java.time.Duration using estimated values for days, months and years.
     *
     * @throws ArithmeticException
     * if either a number in the string exceeds a Java long or the total duration exceeds a java.time.Duration.
     */
    public static Duration xmlDurationToDuration(javax.xml.datatype.Duration xmlDuration) {
        boolean negative = xmlDuration.getSign() == -1;
        Duration magnitude = unitsToDuration(xmlDuration.getField(DatatypeConstants.YEARS), ChronoUnit.YEARS)
                .plus(unitsToDuration(xmlDuration.getField(DatatypeConstants.MONTHS), ChronoUnit.MONTHS))
                .plus(unitsToDuration(xmlDuration.getField(DatatypeConstants.DAYS), ChronoUnit.DAYS))
                .plus(unitsToDuration(xmlDuration.getField(DatatypeConstants.HOURS), ChronoUnit.HOURS))
                .plus(unitsToDuration(xmlDuration.getField(DatatypeConstants.MINUTES), ChronoUnit.MINUTES))
                .plus(secondsToDuration(xmlDuration.getField(DatatypeConstants.SECONDS)));
        return negative ? magnitude.negated() : magnitude;
    }

    /** @param count a BigInteger or null */
    private static Duration unitsToDuration(Number count, ChronoUnit unit) {
        if (count == null) {
            return Duration.ZERO;
        }
        return unit.getDuration().multipliedBy(((BigInteger) count).longValueExact());
    }

    /** @param secondsValue a BigDecimal denoting the number of seconds with fraction or null */
    private static Duration secondsToDuration(Number secondsValue) {
        if (secondsValue == null) {
            return Duration.ZERO;
        }
        BigDecimal secondsBigDecimal = (BigDecimal) secondsValue;
        long wholeSeconds = secondsBigDecimal.toBigInteger().longValueExact();
        int nanos = secondsBigDecimal.subtract(BigDecimal.valueOf(wholeSeconds)).scaleByPowerOfTen(9).intValueExact();
        return Duration.ofSeconds(wholeSeconds, nanos);
    }

    public static long xmlDurationToMilliseconds(javax.xml.datatype.Duration duration) {
        return xmlDurationToDuration(duration).toMillis();
    }

    public static TimeMeasureTYPE xmlDurationToTimeMeasure(javax.xml.datatype.Duration xmlDuration) {
        Duration duration = xmlDurationToDuration(xmlDuration);

        TimeMeasureTYPE result = new TimeMeasureTYPE();
        result.setTimeMeasureUnit(TimeMeasureUnit.MILLISECONDS);
        BigInteger totalMilliseconds = BigInteger.valueOf(duration.toSeconds())
                .multiply(BigInteger.valueOf(1000))
                .add(BigInteger.valueOf(duration.toMillisPart()));
        result.setTimeMeasureValue(totalMilliseconds);

        return result;
    }
}
