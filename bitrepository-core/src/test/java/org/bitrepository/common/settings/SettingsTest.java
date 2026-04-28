package org.bitrepository.common.settings;

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

class SettingsTest  {

    private DatatypeFactory factory;

    @BeforeEach
    void setUpFactory() throws DatatypeConfigurationException {
        factory = DatatypeFactory.newInstance();
    }

    @Test
    @Tag("regressiontest")
    void getDurationFromXmlDurationOrMillisRequiresOneNonNullArg() {
        Assertions.assertThrows(NullPointerException.class, () -> {
            addDescription("Tests that getDurationFromXmlDurationOrMillis() fails when given two nulls");
            addStep("null and null", "NPE");

            Settings.getDurationFromXmlDurationOrMillis(null, null);
        });
    }

    @Test
    @Tag("regressiontest")
    void testGetDurationFromXmlDurationOrMillis() {
        addDescription("Tests conversions and selection by getDurationFromXmlDurationOrMillis()");

        addStep("null and some milliseconds", "Duration of millis");
        Assertions.assertEquals(Duration.ofMillis(54321),
                Settings.getDurationFromXmlDurationOrMillis(null, BigInteger.valueOf(54321)));

        addStep("XML duration and null", "XML duration converted");
        Assertions.assertEquals(Duration.ofMinutes(7), Settings.getDurationFromXmlDurationOrMillis(
                factory.newDuration("PT7M"), null));

        addStep("Conflicting XML duration and millis", "XML duration should be preferred");
        Assertions.assertEquals(Duration.ofMinutes(2), Settings.getDurationFromXmlDurationOrMillis(
                factory.newDuration("PT2M"), BigInteger.valueOf(13)));
    }

}
