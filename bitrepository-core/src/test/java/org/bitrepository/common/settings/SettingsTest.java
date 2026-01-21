package org.bitrepository.common.settings;

import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.math.BigInteger;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertThrows;

public class SettingsTest extends ExtendedTestCase  {

    private DatatypeFactory factory;

    @BeforeEach
    public void setUpFactory() throws DatatypeConfigurationException {
        factory = DatatypeFactory.newInstance();
    }

    @Test
    @Tag("regressiontest")
    public void getDurationFromXmlDurationOrMillisRequiresOneNonNullArg() {
        assertThrows(NullPointerException.class, () -> {
            addDescription("Tests that getDurationFromXmlDurationOrMillis() fails when given two nulls");
            addStep("null and null", "NPE");

            Settings.getDurationFromXmlDurationOrMillis(null, null);
        });
    }

    @Test
    @Tag("regressiontest")
    public void testGetDurationFromXmlDurationOrMillis() {
        addDescription("Tests conversions and selection by getDurationFromXmlDurationOrMillis()");

        addStep("null and some milliseconds", "Duration of millis");
        Assertions.assertEquals(Settings.getDurationFromXmlDurationOrMillis(null, BigInteger.valueOf(54321)),
                Duration.ofMillis(54321));

        addStep("XML duration and null", "XML duration converted");
        Assertions.assertEquals(
                Settings.getDurationFromXmlDurationOrMillis(
                        factory.newDuration("PT7M"), null),
                Duration.ofMinutes(7));

        addStep("Conflicting XML duration and millis", "XML duration should be preferred");
        Assertions.assertEquals(
                Settings.getDurationFromXmlDurationOrMillis(
                        factory.newDuration("PT2M"), BigInteger.valueOf(13)),
                Duration.ofMinutes(2));
    }

}
