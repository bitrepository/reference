package org.bitrepository.common.utils;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.time.Duration;

import static java.math.BigInteger.ONE;
import static java.math.BigInteger.valueOf;
import static java.time.Duration.ZERO;
import static java.time.Duration.ofDays;
import static java.time.Duration.ofHours;
import static java.time.Duration.ofMinutes;
import static java.time.Duration.ofNanos;
import static java.time.Duration.ofSeconds;
import static org.bitrepository.bitrepositoryelements.TimeMeasureUnit.HOURS;
import static org.bitrepository.bitrepositoryelements.TimeMeasureUnit.MILLISECONDS;
import static org.bitrepository.common.utils.XmlUtils.xmlDurationToDuration;
import static org.bitrepository.common.utils.XmlUtils.xmlDurationToMilliseconds;
import static org.bitrepository.common.utils.XmlUtils.xmlDurationToTimeMeasure;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XmlUtilsTest extends ExtendedTestCase {

    private DatatypeFactory factory;

    @BeforeEach
    public void setUpFactory() throws DatatypeConfigurationException {
        factory = DatatypeFactory.newInstance();
    }

    @Test
    @Tag("regressiontest")
    public void negativeDurationIsRejected() {
        assertThrows(IllegalArgumentException.class, () -> {
            XmlUtils.validateNonNegative(factory.newDuration("-PT0.00001S"));
        });
    }

    @Test
    @Tag("regressiontest")
    public void testXmlDurationToDuration() {
        addDescription("Tests xmlDurationToDuration in sunshine scenario cases");

        addStep("Durations of 0 of some time unit", "Duration.ZERO");
        assertEquals(ZERO, xmlDurationToDuration(factory.newDuration("P0Y")));
        assertEquals(ZERO, xmlDurationToDuration(factory.newDuration("P0M")));
        assertEquals(ZERO, xmlDurationToDuration(factory.newDuration("P0D")));
        assertEquals(ZERO, xmlDurationToDuration(factory.newDuration("PT0H")));
        assertEquals(ZERO, xmlDurationToDuration(factory.newDuration("PT0M")));
        assertEquals(ZERO, xmlDurationToDuration(factory.newDuration("PT0S")));
        assertEquals(ZERO, xmlDurationToDuration(factory.newDuration("PT0.0000S")));

        addStep("Test correct and precise conversion",
                "Hours, minutes and seconds are converted with full precision");
        assertEquals(ofSeconds(3), xmlDurationToDuration(factory.newDuration("PT3S")));
        assertEquals(ofSeconds(3, 300_000_000), xmlDurationToDuration(factory.newDuration("PT3.3S")));
        assertEquals(ofSeconds(3, 3), xmlDurationToDuration(factory.newDuration("PT3.000000003S")));
        assertEquals(ofSeconds(3, 123_456_789), xmlDurationToDuration(factory.newDuration("PT3.123456789S")));

        assertEquals(ofMinutes(4), xmlDurationToDuration(factory.newDuration("PT4M")));

        assertEquals(ofHours(5), xmlDurationToDuration(factory.newDuration("PT5H")));

        assertEquals(ofHours(6).plusMinutes(7).plusSeconds(8).plusMillis(900), xmlDurationToDuration(factory.newDuration("PT6H7M8.9S")));

        addStep("Test approximate conversion",
                "Days, months and years are converted using estimated factors");
        assertEquals(ofDays(2), xmlDurationToDuration(factory.newDuration("P2D")));
        assertEquals(ofDays(3).plusMinutes(4), xmlDurationToDuration(factory.newDuration("P3DT4M")));

        // We require a month to be between 28 and 31 days exclusive
        Duration minMonthLengthExclusive = ofDays(28);
        Duration maxMonthLengthExclusive = ofDays(31);
        Duration convertedMonth = xmlDurationToDuration(factory.newDuration("P1M"));
        assertBetweenExclusive(convertedMonth, minMonthLengthExclusive, maxMonthLengthExclusive);

        // Two years is between 730 and 731 days
        Duration minTwoYearsLengthExclusive = ofDays(2 * 365);
        Duration maxTwoYearsLengthExclusive = ofDays(2 * 365 + 1);
        Duration convertedTwoYears = xmlDurationToDuration(factory.newDuration("P2Y"));
        assertBetweenExclusive(convertedTwoYears, minTwoYearsLengthExclusive, maxTwoYearsLengthExclusive);
    }

    @Test
    @Tag("regressiontest")
    public void testNegativeXmlDurationToDuration() {
        // WorkflowInterval may be negative (meaning don’t run automatically)
        addDescription("Tests that xmlDurationToDuration() accepts a negative duration and converts it correctly");
        addStep("Negative XML durations", "Corresponding negative java.time durations");
        assertEquals(ofSeconds(-3), xmlDurationToDuration(factory.newDuration("-PT3S")));
        assertEquals(ofNanos(-1000), xmlDurationToDuration(factory.newDuration("-PT0.000001S")));
        assertEquals(ofHours(-24), xmlDurationToDuration(factory.newDuration("-PT24H")));
        assertEquals(ofDays(-1), xmlDurationToDuration(factory.newDuration("-P1D")));

        // We require minus 1 month to be between -31 and -28 days exclusive
        Duration minNegativeMonthLengthExclusive = ofDays(-31);
        Duration maxNegativeMonthLengthExclusive = ofDays(-28);
        Duration convertedMinusOneMonth = xmlDurationToDuration(factory.newDuration("-P1M"));
        assertBetweenExclusive(convertedMinusOneMonth, minNegativeMonthLengthExclusive, maxNegativeMonthLengthExclusive);

        // Minus 1 year is between -366 and -365 days
        Duration minMinusOneYearLengthExclusive = ofDays(-366);
        Duration maxMinusOneYearLengthExclusive = ofDays(-365);
        Duration convertedMinusOneYears = xmlDurationToDuration(factory.newDuration("-P1Y"));
        assertBetweenExclusive(convertedMinusOneYears, minMinusOneYearLengthExclusive, maxMinusOneYearLengthExclusive);
    }

    private static <T extends Comparable<T>> void assertBetweenExclusive(T actual, T minExclusive, T maxExclusive) {
        Assertions.assertTrue(actual.compareTo(minExclusive) > 0);
        Assertions.assertTrue(actual.compareTo(maxExclusive) < 0);
    }

    @Test
    @Tag("regressiontest")
    public void tooManyDecimalsAreRejected() {
        assertThrows(ArithmeticException.class, () -> {
            addDescription("Tests that xmlDurationToDuration() rejects more than 9 decimals on seconds");
            addStep("Duration with 10 decimals, PT2.0123456789S", "ArithmeticException");
            XmlUtils.xmlDurationToDuration(factory.newDuration("PT2.0123456789S"));
        });
    }

    @Test
    @Tag("regressiontest")
    public void testXmlDurationToMilliseconds() {
        addDescription("Tests xmlDurationToMilliseconds in sunshine scenario cases");
        addStep("Test correct and precise conversion",
                "Hours, minutes and seconds are converted with full precision");

        assertEquals(1, xmlDurationToMilliseconds(factory.newDuration(1)));
        assertEquals(1000, xmlDurationToMilliseconds(factory.newDuration(1000)));

        assertEquals(1, xmlDurationToMilliseconds(factory.newDuration("PT0.001S")));
        assertEquals(1, xmlDurationToMilliseconds(factory.newDuration("PT0.001999S")));
        assertEquals(2000, xmlDurationToMilliseconds(factory.newDuration("PT2S")));
    }

    @Test
    @Tag("regressiontest")
    public void testNegativeXmlDurationToMilliseconds() {
        assertEquals(-1000, xmlDurationToMilliseconds(factory.newDuration("-PT1S")));
    }

    @Test
    @Tag("regressiontest")
    public void convertsToTimeMeasure() {
        TimeMeasureTYPE shortTimeMeasure = xmlDurationToTimeMeasure(factory.newDuration(1));
        assertEquals(MILLISECONDS, shortTimeMeasure.getTimeMeasureUnit());
        assertEquals(ONE, shortTimeMeasure.getTimeMeasureValue());

        long hours = 2_562_047_788_015L;
        TimeMeasureTYPE longTimeMeasure = xmlDurationToTimeMeasure(factory.newDurationDayTime(
                true, BigInteger.ZERO, valueOf(hours), BigInteger.ZERO, BigInteger.ZERO));
        if (longTimeMeasure.getTimeMeasureUnit() == HOURS) {
            assertEquals(valueOf(hours), longTimeMeasure.getTimeMeasureValue());
        } else {
            assertEquals(MILLISECONDS, longTimeMeasure.getTimeMeasureUnit());
            assertEquals(valueOf(ofHours(hours).toMillis()), longTimeMeasure.getTimeMeasureValue());
        }
    }

}
