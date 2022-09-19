/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice.checking;

import org.bitrepository.settings.referencesettings.ObsoleteChecksumSettings;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import java.time.Duration;

import static org.testng.Assert.assertEquals;

public class MaxChecksumAgeProviderTest extends ExtendedTestCase{

    DatatypeFactory factory;

    @BeforeMethod(alwaysRun = true)
    public void setUpFactory() throws DatatypeConfigurationException {
        factory = DatatypeFactory.newInstance();
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoSettings() {
        addDescription("Test the MaxChecksumAge when no settings are defined");
        addStep("Create a MaxChecksumAgeProvider with null settings and a default MaxAge of 100",

            "Test that the MaxAge for a random pillar is 100");
        Duration defaultMaxAge = Duration.ofMillis(100);
        MaxChecksumAgeProvider maxChecksumAgeProvider = new MaxChecksumAgeProvider(defaultMaxAge, null);
        assertEquals(maxChecksumAgeProvider.getMaxChecksumAge(""), defaultMaxAge);
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testNoPillarSpecificSetting() {
        addDescription("Test the MaxChecksumAge when no settings are defined for the specific pillar");

        addStep("Create a MaxChecksumAgeProvider with settings containing a default MaxAge of 10 and no pillar " +
            "specific settings",
            "Test that the MaxAge for a random pillar is 10");
        Duration defaultMaxAge = Duration.ofMillis(100);
        ObsoleteChecksumSettings settings = new ObsoleteChecksumSettings();
        settings.setDefaultMaxChecksumAge(factory.newDuration(10));
        MaxChecksumAgeProvider maxChecksumAgeProvider = new MaxChecksumAgeProvider(defaultMaxAge, settings);
        assertEquals(maxChecksumAgeProvider.getMaxChecksumAge(""), Duration.ofMillis(10));
    }

    @Test(groups = {"regressiontest", "integritytest"})
    public void testPillarSpecificSetting() {
        addDescription("Test the MaxChecksumAge when a value has been defined for specific pillars");


        addStep("Create a MaxChecksumAgeProvider with settings containing a default MaxAge of 10, pillar1" +
            "MaxChecksumAge of 1001 and pillar2 MaxChecksumAge of 1002" ,
            "Test that the MaxAge for pillar1 is 1001, pillar2 is 1002 and for a random pillar is 10");
        Duration defaultMaxAge = Duration.ofMillis(100);
        long defaultMaxAgeInSettings = 100;
        final String PILLAR1 = "PILLAR1";
        long pillar1MaxAge = 1000;
        final String PILLAR2 = "PILLAR2";
        long pillar2MaxAge = 10000;
        ObsoleteChecksumSettings settings = new ObsoleteChecksumSettings();
        settings.setDefaultMaxChecksumAge(factory.newDuration(defaultMaxAgeInSettings));
        settings.getMaxChecksumAgeForPillar().add(
            MaxChecksumAgeProvider.createMaxChecksumAgeForPillar(PILLAR1, pillar1MaxAge));
        settings.getMaxChecksumAgeForPillar().add(
            MaxChecksumAgeProvider.createMaxChecksumAgeForPillar(PILLAR2, pillar2MaxAge));
        MaxChecksumAgeProvider maxChecksumAgeProvider = new MaxChecksumAgeProvider(defaultMaxAge, settings);
        assertEquals(maxChecksumAgeProvider.getMaxChecksumAge(PILLAR1), Duration.ofMillis(pillar1MaxAge));
        assertEquals(maxChecksumAgeProvider.getMaxChecksumAge(PILLAR2), Duration.ofMillis(pillar2MaxAge));
        assertEquals(maxChecksumAgeProvider.getMaxChecksumAge(""), Duration.ofMillis(defaultMaxAgeInSettings));
    }
}
