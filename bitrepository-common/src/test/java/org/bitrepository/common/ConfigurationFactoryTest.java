/*
 * #%L
 * bitrepository-common
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.common;

import org.bitrepository.core.configuration.CoreConfiguration;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;

/**
 * Tests the <code>ConfigurationFactory</code> by running the functionality on the common modules own configuration
 */
public class ConfigurationFactoryTest extends ExtendedTestCase {
    private ConfigurationFactory configurationFactory = new ConfigurationFactory();
    private static final String FS = File.separator;
    private static final ModuleCharacteristics moduleCharacteristics = new ModuleCharacteristics("common");

    private static final File ORIGINAL_TEST_CONFIG_LOCATION = 
            new File("target" + FS + "test-classes" + FS + "configuration" + 
                    FS + "xml" + FS + "common-test-configuration.xml");
    private static final File RENAMED_TEST_CONFIG_LOCATION = 
            new File("target" + FS + "test-classes" + FS + "configuration" + 
                    FS + "xml" + FS + "common-test-configuration.xml.orig");
    private static final File ORIGINAL_CONFIG_LOCATION = 
            new File("target" + FS + "classes/configuration" + FS + "xml" + FS + "common-configuration.xml");
    private static final File ORIGINAL_CONFIG_DIR = 
            new File("target" + FS + "classes" + FS + "configuration" + FS + "xml");

    private static final CoreConfiguration referenceConfig = createDefaultConfiguration();

    /** 
     * Set working dir to the target folder to avoid. The ConfigurationFactory will look for configuration 
     * files relatively to the current working dir, and we need to manipulate these files to validate the 
     * functionality. Because all code changed in the tests should be located in the target folder we therefore need 
     * to run the test with working dir set to the target folder
     */
    @BeforeMethod (alwaysRun=true)
    public final void setUp() {     
        ORIGINAL_TEST_CONFIG_LOCATION.renameTo(RENAMED_TEST_CONFIG_LOCATION);
        ORIGINAL_CONFIG_DIR.mkdirs();
    }

    /**
     * Attempt to reset working dir after each test. 
     */
    @AfterMethod  (alwaysRun=true)
    public final void tearDown() {		
        RENAMED_TEST_CONFIG_LOCATION.renameTo(ORIGINAL_TEST_CONFIG_LOCATION);
        ORIGINAL_CONFIG_LOCATION.renameTo(ORIGINAL_TEST_CONFIG_LOCATION);
    }

    private static CoreConfiguration createDefaultConfiguration() {
        CoreConfiguration config = new CoreConfiguration();
        config.setEnvironmentName("TEST");
        return config;
    }

    @Test(groups = { "regressiontest" })
    public void loadTestConfiguration() throws IOException {
        addDescription("Validates that the test configuration file is correctly loaded from the class path");

        addStep("Attempt to load a test configuration with no configuration file with the correct name", 
                "A Configuration exception should be thrown");
        try {
            configurationFactory.loadConfiguration(moduleCharacteristics, CoreConfiguration.class);
            Assert.fail("Expected a ConfigurationException with a non-existing test configuration");
        } catch (ConfigurationException ce) {
            // That apparently worked
        }

        addStep("Attempt to load the test configuration after it has been renamed to the correct name for a test " +
                "configuration", 
                "The configuration should be loaded sucessfully");
        RENAMED_TEST_CONFIG_LOCATION.renameTo(ORIGINAL_TEST_CONFIG_LOCATION);
        Assert.assertEquals(configurationFactory.loadConfiguration(moduleCharacteristics, CoreConfiguration.class),
                referenceConfig);
    }

    @Test(groups = { "regressiontest" })
    public void loadConfiguration() throws IOException {
        addDescription("Validates that the configuration file is correctly loaded from the class path");

        addStep("Attempt to load a configuration with no configuration file with the correct name", 
                "A Configuration exception should be thrown");
        try {
            configurationFactory.loadConfiguration(moduleCharacteristics, CoreConfiguration.class);
            Assert.fail("Expected a ConfigurationException with a non-existing configuration");
        } catch (ConfigurationException ce) {
            // That apparently worked
        }

        addStep("Attempt to load the configuration after it has been rename to the correct name", 
                "The configuration should be loaded sucessfully");
        RENAMED_TEST_CONFIG_LOCATION.renameTo(ORIGINAL_CONFIG_LOCATION);
        Assert.assertEquals(configurationFactory.loadConfiguration(moduleCharacteristics, CoreConfiguration.class),
                referenceConfig);
    }
}
