package org.bitrepository.common.utils;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureUnit;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.time.Duration;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class XmlUtilsTest {

    private DatatypeFactory factory;

    @BeforeEach
    public void setUpFactory() throws DatatypeConfigurationException {
        factory = DatatypeFactory.newInstance();
    }

    @Test
    @Tag("regressiontest")
    public void negativeDurationIsRejected() {
        Assertions.assertThrows(IllegalArgumentException.class, () -> {
            XmlUtils.validateNonNegative(factory.newDuration("-PT0.00001S"));
        });
    }

    @Test
    @Tag("regressiontest")
    public void testXmlDurationToDuration() {
        addDescription("Tests xmlDurationToDuration in sunshine scenario cases");

        addStep("Durations of 0 of some time unit", "Duration.ZERO");
        Assertions.assertEquals(Duration.ZERO, XmlUtils.xmlDurationToDuration(factory.newDuration("P0Y")));
        Assertions.assertEquals(Duration.ZERO, XmlUtils.xmlDurationToDuration(factory.newDuration("P0M")));
        Assertions.assertEquals(Duration.ZERO, XmlUtils.xmlDurationToDuration(factory.newDuration("P0D")));
        Assertions.assertEquals(Duration.ZERO, XmlUtils.xmlDurationToDuration(factory.newDuration("PT0H")));
        Assertions.assertEquals(Duration.ZERO, XmlUtils.xmlDurationToDuration(factory.newDuration("PT0M")));
        Assertions.assertEquals(Duration.ZERO, XmlUtils.xmlDurationToDuration(factory.newDuration("PT0S")));
        Assertions.assertEquals(Duration.ZERO, XmlUtils.xmlDurationToDuration(factory.newDuration("PT0.0000S")));

        addStep("Test correct and precise conversion",
                "Hours, minutes and seconds are converted with full precision");
        Assertions.assertEquals(Duration.ofSeconds(3), XmlUtils.xmlDurationToDuration(factory.newDuration("PT3S")));
        Assertions.assertEquals(Duration.ofSeconds(3, 300_000_000), XmlUtils.xmlDurationToDuration(factory.newDuration("PT3.3S")));
        Assertions.assertEquals(Duration.ofSeconds(3, 3), XmlUtils.xmlDurationToDuration(factory.newDuration("PT3.000000003S")));
        Assertions.assertEquals(Duration.ofSeconds(3, 123_456_789), XmlUtils.xmlDurationToDuration(factory.newDuration("PT3.123456789S")));

        Assertions.assertEquals(Duration.ofMinutes(4), XmlUtils.xmlDurationToDuration(factory.newDuration("PT4M")));

        Assertions.assertEquals(Duration.ofHours(5), XmlUtils.xmlDurationToDuration(factory.newDuration("PT5H")));

        Assertions.assertEquals(Duration.ofHours(6).plusMinutes(7).plusSeconds(8).plusMillis(900), XmlUtils.xmlDurationToDuration(factory.newDuration("PT6H7M8.9S")));

        addStep("Test approximate conversion",
                "Days, months and years are converted using estimated factors");
        Assertions.assertEquals(Duration.ofDays(2), XmlUtils.xmlDurationToDuration(factory.newDuration("P2D")));
        Assertions.assertEquals(Duration.ofDays(3).plusMinutes(4), XmlUtils.xmlDurationToDuration(factory.newDuration("P3DT4M")));

        // We require a month to be between 28 and 31 days exclusive
        Duration minMonthLengthExclusive = Duration.ofDays(28);
        Duration maxMonthLengthExclusive = Duration.ofDays(31);
        Duration convertedMonth = XmlUtils.xmlDurationToDuration(factory.newDuration("P1M"));
        assertBetweenExclusive(convertedMonth, minMonthLengthExclusive, maxMonthLengthExclusive);

        // Two years is between 730 and 731 days
        Duration minTwoYearsLengthExclusive = Duration.ofDays(2 * 365);
        Duration maxTwoYearsLengthExclusive = Duration.ofDays(2 * 365 + 1);
        Duration convertedTwoYears = XmlUtils.xmlDurationToDuration(factory.newDuration("P2Y"));
        assertBetweenExclusive(convertedTwoYears, minTwoYearsLengthExclusive, maxTwoYearsLengthExclusive);
    }

    @Test
    @Tag("regressiontest")
    public void testNegativeXmlDurationToDuration() {
        // WorkflowInterval may be negative (meaning don’t run automatically)
        addDescription("Tests that xmlDurationToDuration() accepts a negative duration and converts it correctly");
        addStep("Negative XML durations", "Corresponding negative java.time durations");
        Assertions.assertEquals(Duration.ofSeconds(-3), XmlUtils.xmlDurationToDuration(factory.newDuration("-PT3S")));
        Assertions.assertEquals(Duration.ofNanos(-1000), XmlUtils.xmlDurationToDuration(factory.newDuration("-PT0.000001S")));
        Assertions.assertEquals(Duration.ofHours(-24), XmlUtils.xmlDurationToDuration(factory.newDuration("-PT24H")));
        Assertions.assertEquals(Duration.ofDays(-1), XmlUtils.xmlDurationToDuration(factory.newDuration("-P1D")));

        // We require minus 1 month to be between -31 and -28 days exclusive
        Duration minNegativeMonthLengthExclusive = Duration.ofDays(-31);
        Duration maxNegativeMonthLengthExclusive = Duration.ofDays(-28);
        Duration convertedMinusOneMonth = XmlUtils.xmlDurationToDuration(factory.newDuration("-P1M"));
        assertBetweenExclusive(convertedMinusOneMonth, minNegativeMonthLengthExclusive, maxNegativeMonthLengthExclusive);

        // Minus 1 year is between -366 and -365 days
        Duration minMinusOneYearLengthExclusive = Duration.ofDays(-366);
        Duration maxMinusOneYearLengthExclusive = Duration.ofDays(-365);
        Duration convertedMinusOneYears = XmlUtils.xmlDurationToDuration(factory.newDuration("-P1Y"));
        assertBetweenExclusive(convertedMinusOneYears, minMinusOneYearLengthExclusive, maxMinusOneYearLengthExclusive);
    }

    private static <T extends Comparable<T>> void assertBetweenExclusive(T actual, T minExclusive, T maxExclusive) {
        Assertions.assertTrue(actual.compareTo(minExclusive) > 0);
        Assertions.assertTrue(actual.compareTo(maxExclusive) < 0);
    }

    @Test
    @Tag("regressiontest")
    public void tooManyDecimalsAreRejected() {
        Assertions.assertThrows(ArithmeticException.class, () -> {
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

        Assertions.assertEquals(1, XmlUtils.xmlDurationToMilliseconds(factory.newDuration(1)));
        Assertions.assertEquals(1000, XmlUtils.xmlDurationToMilliseconds(factory.newDuration(1000)));

        Assertions.assertEquals(1, XmlUtils.xmlDurationToMilliseconds(factory.newDuration("PT0.001S")));
        Assertions.assertEquals(1, XmlUtils.xmlDurationToMilliseconds(factory.newDuration("PT0.001999S")));
        Assertions.assertEquals(2000, XmlUtils.xmlDurationToMilliseconds(factory.newDuration("PT2S")));
    }

    @Test
    @Tag("regressiontest")
    public void testNegativeXmlDurationToMilliseconds() {
        Assertions.assertEquals(-1000, XmlUtils.xmlDurationToMilliseconds(factory.newDuration("-PT1S")));
    }

    @Test
    @Tag("regressiontest")
    public void convertsToTimeMeasure() {
        TimeMeasureTYPE shortTimeMeasure = XmlUtils.xmlDurationToTimeMeasure(factory.newDuration(1));
        Assertions.assertEquals(TimeMeasureUnit.MILLISECONDS, shortTimeMeasure.getTimeMeasureUnit());
        Assertions.assertEquals(BigInteger.ONE, shortTimeMeasure.getTimeMeasureValue());

        long hours = 2_562_047_788_015L;
        TimeMeasureTYPE longTimeMeasure = XmlUtils.xmlDurationToTimeMeasure(factory.newDurationDayTime(
                true, BigInteger.ZERO, BigInteger.valueOf(hours), BigInteger.ZERO, BigInteger.ZERO));
        if (longTimeMeasure.getTimeMeasureUnit() == TimeMeasureUnit.HOURS) {
            Assertions.assertEquals(BigInteger.valueOf(hours), longTimeMeasure.getTimeMeasureValue());
        } else {
            Assertions.assertEquals(TimeMeasureUnit.MILLISECONDS, longTimeMeasure.getTimeMeasureUnit());
            Assertions.assertEquals(BigInteger.valueOf(Duration.ofHours(hours).toMillis()), longTimeMeasure.getTimeMeasureValue());
        }
    }

}
