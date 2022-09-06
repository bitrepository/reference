package org.bitrepository.common.settings;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.time.Duration;

public class SettingsTest extends ExtendedTestCase {

    private DatatypeFactory factory;

    @BeforeMethod(alwaysRun = true)
    public void setUpFactory() throws DatatypeConfigurationException {
        factory = DatatypeFactory.newInstance();
    }

    @Test(groups = {"regressiontest"}, expectedExceptions = NullPointerException.class)
    public void getDurationFromXmlDurationOrMillisRequiresOneNonNullArg() {
        addDescription("Tests that getDurationFromXmlDurationOrMillis() fails when given two nulls");
        addStep("null and null", "NPE");

        Settings.getDurationFromXmlDurationOrMillis(null, null);
    }


    @Test(groups = {"regressiontest"})
    public void testGetDurationFromXmlDurationOrMillis() {
        addDescription("Tests conversions and selection by getDurationFromXmlDurationOrMillis()");

        addStep("null and some milliseconds", "Duration of millis");
        Assert.assertEquals(Settings.getDurationFromXmlDurationOrMillis(null, BigInteger.valueOf(54321)),
                Duration.ofMillis(54321));

        addStep("XML duration and null", "XML duration converted");
        Assert.assertEquals(
                Settings.getDurationFromXmlDurationOrMillis(
                        factory.newDuration("PT7M"), null),
                Duration.ofMinutes(7));

        addStep("Conflicting XML duration and millis", "XML duration should be preferred");
        Assert.assertEquals(
                Settings.getDurationFromXmlDurationOrMillis(
                        factory.newDuration("PT2M"), BigInteger.valueOf(13)),
                Duration.ofMinutes(2));
    }

        @Test(groups = {"regressiontest"}, expectedExceptions = IllegalArgumentException.class)
    public void negativeDurationIsRejected() {
        Settings.validateNonNegative(factory.newDuration("-PT0.00001S"));
    }

    @Test(groups = {"regressiontest"})
    public void testXmlDurationToDuration() {
        addDescription("Tests xmlDurationToDuration in sunshine scenario cases");

        addStep("Test correct and precise conversion",
                "Hours, minutes and seconds are converted with full precision");
        Assert.assertEquals(Settings.xmlDurationToDuration(factory.newDuration("PT3S")),
                Duration.ofSeconds(3));
        Assert.assertEquals(Settings.xmlDurationToDuration(factory.newDuration("PT3.3S")),
                Duration.ofSeconds(3, 300_000_000));
        Assert.assertEquals(Settings.xmlDurationToDuration(factory.newDuration("PT3.000000003S")),
                Duration.ofSeconds(3, 3));
        Assert.assertEquals(Settings.xmlDurationToDuration(factory.newDuration("PT3.123456789S")),
                Duration.ofSeconds(3, 123_456_789));

        Assert.assertEquals(Settings.xmlDurationToDuration(factory.newDuration("PT4M")),
                Duration.ofMinutes(4));

        Assert.assertEquals(Settings.xmlDurationToDuration(factory.newDuration("PT5H")),
                Duration.ofHours(5));

        Assert.assertEquals(Settings.xmlDurationToDuration(factory.newDuration("PT6H7M8.9S")),
                Duration.ofHours(6).plusMinutes(7).plusSeconds(8).plusMillis(900));

        addStep("Test approximate conversion",
                "Days, months and years are converted using estimated factors");
        Assert.assertEquals(Settings.xmlDurationToDuration(factory.newDuration("P2D")),
                Duration.ofDays(2));
        Assert.assertEquals(Settings.xmlDurationToDuration(factory.newDuration("P3DT4M")),
                Duration.ofDays(3).plusMinutes(4));

        // We require a month to be between 28 and 31 days exclusive
        Duration minMonthLengthExclusive = Duration.ofDays(28);
        Duration maxMonthLengthExclusive = Duration.ofDays(31);
        Duration convertedMonth = Settings.xmlDurationToDuration(factory.newDuration("P1M"));
        assertBetweenExclusive(convertedMonth, minMonthLengthExclusive, maxMonthLengthExclusive);

        // Two years is between 730 and 731 days
        Duration minTwoYearsLengthExclusive = Duration.ofDays(2 * 365);
        Duration maxTwoYearsLengthExclusive = Duration.ofDays(2 * 365 + 1);
        Duration convertedTwoYears = Settings.xmlDurationToDuration(factory.newDuration("P2Y"));
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
        Settings.xmlDurationToDuration(factory.newDuration("PT2.0123456789S"));
    }

}
