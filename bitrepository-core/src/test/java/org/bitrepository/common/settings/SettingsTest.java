package org.bitrepository.common.settings;

import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.time.Duration;

public class SettingsTest extends ExtendedTestCase  {

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

}
