/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.pillar.integration;

import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.pillar.PillarSettingsProvider;
import org.bitrepository.protocol.IntegrationTest;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeSuite;

/**
 * Super class for all tests which should test functionality on a single pillar.
 *
 * Note That no setup/teardown is possible in this test of external pillars, so tests need to be written
 * to be invariant against the initial pillar state.
 */
public abstract class PillarIntegrationTest extends IntegrationTest {
    /** The path to the directory containing the integration test configuration files */
    protected static final String PATH_TO_CONFIG_DIR = System.getProperty(
            "pillar.integrationtest.settings.path",
            "settings/xml/integration-test/");
    public static final String TEST_CONFIGURATION_FILE_NAME = "pillar-integration-test.properties";
    protected static PillarIntegrationTestConfiguration testConfiguration;

    @BeforeSuite(alwaysRun = true)
    @Override
    public void initializeSuite() {
        super.initializeSuite();
        startEmbeddedReferencePillar();
    }

    @AfterSuite(alwaysRun = true)
    @Override
    public void shutdownSuite() {
        stopEmbeddedReferencePillar();
        super.shutdownSuite();
    }

    protected void startEmbeddedReferencePillar() {
        if (testConfiguration.useEmbeddedPillar()) {
            EmbeddedReferencePillar pillar = new EmbeddedReferencePillar(
                    PATH_TO_CONFIG_DIR, testConfiguration.getPillarUnderTestID());
        }
    }

    protected void stopEmbeddedReferencePillar() {
    }

    private void loadTestSettings() {
        testConfiguration = new PillarIntegrationTestConfiguration(PATH_TO_CONFIG_DIR + TEST_CONFIGURATION_FILE_NAME);
    }

    @Override
    public boolean useEmbeddedMessageBus() {
        return testConfiguration.useEmbeddedMessagebus();
    }

    @Override
    protected void setupSettings() {
        loadTestSettings();
        SettingsProvider settingsLoader =
                new PillarSettingsProvider(new XMLFileSettingsLoader(PATH_TO_CONFIG_DIR), "Test client");
        componentSettings = settingsLoader.getSettings();
    }
    /**
     * Disable test collection messagebus listeners.
     */
    @Override
    protected void initializeMessageBusListeners() {
        super.initializeMessageBusListeners();
        messageBus.removeListener(componentSettings.getCollectionDestination(), collectionReceiver.getMessageListener());
   }

   protected String getPillarID() {
       return componentSettings.getCollectionSettings().getClientSettings().getPillarIDs().get(0);
   }
}
