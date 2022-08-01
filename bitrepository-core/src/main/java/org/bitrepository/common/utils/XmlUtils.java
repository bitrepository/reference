package org.bitrepository.common.utils;

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
     */
    public static Duration xmlDurationToDuration(javax.xml.datatype.Duration xmlDuration) {
        return unitsToDuration(xmlDuration.getField(DatatypeConstants.YEARS), ChronoUnit.YEARS)
                .plus(unitsToDuration(xmlDuration.getField(DatatypeConstants.MONTHS), ChronoUnit.MONTHS))
                .plus(unitsToDuration(xmlDuration.getField(DatatypeConstants.DAYS), ChronoUnit.DAYS))
                .plus(unitsToDuration(xmlDuration.getField(DatatypeConstants.HOURS), ChronoUnit.HOURS))
                .plus(unitsToDuration(xmlDuration.getField(DatatypeConstants.MINUTES), ChronoUnit.MINUTES))
                .plus(secondsToDuration(xmlDuration.getField(DatatypeConstants.SECONDS)));
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
}
