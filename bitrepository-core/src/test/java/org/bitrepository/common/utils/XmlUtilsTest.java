package org.bitrepository.common.utils;

import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureUnit;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.time.Duration;

public class XmlUtilsTest extends ExtendedTestCase {

    private DatatypeFactory factory;

    @BeforeMethod(alwaysRun = true)
    public void setUpFactory() throws DatatypeConfigurationException {
        factory = DatatypeFactory.newInstance();
    }

    @Test(groups = {"regressiontest"}, expectedExceptions = IllegalArgumentException.class)
    public void negativeDurationIsRejected() {
        XmlUtils.validateNonNegative(factory.newDuration("-PT0.00001S"));
    }

    @Test(groups = {"regressiontest"})
    public void testXmlDurationToDuration() {
        addDescription("Tests xmlDurationToDuration in sunshine scenario cases");

        addStep("Durations of 0 of some time unit", "Duration.ZERO");
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("P0Y")), Duration.ZERO);
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("P0M")), Duration.ZERO);
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("P0D")), Duration.ZERO);
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("PT0H")), Duration.ZERO);
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("PT0M")), Duration.ZERO);
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("PT0S")), Duration.ZERO);
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("PT0.0000S")),
                Duration.ZERO);

        addStep("Test correct and precise conversion",
                "Hours, minutes and seconds are converted with full precision");
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("PT3S")),
                Duration.ofSeconds(3));
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("PT3.3S")),
                Duration.ofSeconds(3, 300_000_000));
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("PT3.000000003S")),
                Duration.ofSeconds(3, 3));
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("PT3.123456789S")),
                Duration.ofSeconds(3, 123_456_789));

        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("PT4M")),
                Duration.ofMinutes(4));

        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("PT5H")),
                Duration.ofHours(5));

        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("PT6H7M8.9S")),
                Duration.ofHours(6).plusMinutes(7).plusSeconds(8).plusMillis(900));

        addStep("Test approximate conversion",
                "Days, months and years are converted using estimated factors");
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("P2D")),
                Duration.ofDays(2));
        Assert.assertEquals(XmlUtils.xmlDurationToDuration(factory.newDuration("P3DT4M")),
                Duration.ofDays(3).plusMinutes(4));

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

    private static <T extends Comparable<T>> void assertBetweenExclusive(T actual, T minExclusive, T maxExclusive) {
        Assert.assertTrue(actual.compareTo(minExclusive) > 0);
        Assert.assertTrue(actual.compareTo(maxExclusive) < 0);
    }

    @Test(groups = {"regressiontest"}, expectedExceptions = ArithmeticException.class)
    public void tooManyDecimalsAreRejected() {
        addDescription("Tests that xmlDurationToDuration() rejects more than 9 decimals on seconds");
        addStep("Duration with 10 decimals, PT2.0123456789S", "ArithmeticException");
        XmlUtils.xmlDurationToDuration(factory.newDuration("PT2.0123456789S"));
    }

    @Test(groups = {"regressiontest"})
    public void testXmlDurationToMilliseconds() {
        addDescription("Tests xmlDurationToMilliseconds in sunshine scenario cases");
        addStep("Test correct and precise conversion",
                "Hours, minutes and seconds are converted with full precision");

        Assert.assertEquals(XmlUtils.xmlDurationToMilliseconds(factory.newDuration(1)), 1);
        Assert.assertEquals(XmlUtils.xmlDurationToMilliseconds(factory.newDuration(1000)), 1000);

        Assert.assertEquals(XmlUtils.xmlDurationToMilliseconds(factory.newDuration("PT0.001S")), 1);
        Assert.assertEquals(XmlUtils.xmlDurationToMilliseconds(factory.newDuration("PT0.001999S")), 1);
        Assert.assertEquals(XmlUtils.xmlDurationToMilliseconds(factory.newDuration("PT2S")), 2000);
    }

    @Test(groups = {"regressiontest"})
    public void convertsToTimeMeasure() {
        TimeMeasureTYPE shortTimeMeasure = XmlUtils.xmlDurationToTimeMeasure(factory.newDuration(1));
        Assert.assertEquals(shortTimeMeasure.getTimeMeasureUnit(), TimeMeasureUnit.MILLISECONDS);
        Assert.assertEquals(shortTimeMeasure.getTimeMeasureValue(), BigInteger.ONE);

        long hours = 2_562_047_788_015L;
        TimeMeasureTYPE longTimeMeasure = XmlUtils.xmlDurationToTimeMeasure(factory.newDurationDayTime(
                true, BigInteger.ZERO, BigInteger.valueOf(hours), BigInteger.ZERO, BigInteger.ZERO));
        if (longTimeMeasure.getTimeMeasureUnit() == TimeMeasureUnit.HOURS) {
            Assert.assertEquals(longTimeMeasure.getTimeMeasureValue(), BigInteger.valueOf(hours));
        } else {
            Assert.assertEquals(longTimeMeasure.getTimeMeasureUnit(), TimeMeasureUnit.MILLISECONDS);
            Assert.assertEquals(longTimeMeasure.getTimeMeasureValue(),
                    BigInteger.valueOf(Duration.ofHours(hours).toMillis()));
        }
    }

}
