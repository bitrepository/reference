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

import java.io.IOException;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.PillarSettingsProvider;
import org.bitrepository.pillar.referencepillar.ReferencePillar;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
import org.bitrepository.protocol.fileexchange.HttpServerConnector;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.jaccept.TestEventManager;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;

/**
 * Super class for all tests which should test functionality on a single pillar.
 *
 * Note That no setup/teardown is possible in this test of external pillars, so tests need to be written
 * to be invariant against the initial pillar state.
 */
public abstract class PillarIntegrationTest extends DefaultFixturePillarTest {
    /**
     * The path to the default settings used for integration test of pillars. The value is
     * <code>settings/xml/integretion-test</code>
     */
    public static final String PATH_TO_DEFAULT_SETTINGS = "settings/xml/integration-test/";
    /**
     * Environment variable used to override the default path to the pillar settings whci should be used for the
     * integration test. Value = <code>pillar.integrationtest.settings.path</code>
     */
    public static final String PILLAR_INTEGRATIONTEST_SETTINGS_PATH = "pillar.integrationtest.settings.path";

    public static final String PROPERTY_FILE_NAME = "pillar-integration-test.properties";

    protected String EXISTING_PILLAR_FILE;

    private PillarIntegrationTestSettings testSettings;

    protected TestFileHelper fileHelper;

    /** Indicated whether reference pillars should be started should be started and used. Note that mockup pillars
     * should be used in this case, e.g. the useMockupPillar() call should return false. */
    public boolean useEmbeddedPillar() {
        return System.getProperty("useEmbeddedPillar", "false").equals("true");
    }
    @BeforeClass(alwaysRun = true)
    protected void prepareIntegrationTest() throws Exception {
        loadTestSettings();
        startEmbeddedReferencePillar();
        configureFileHelper();
        ingestDefaultFile();
    }

    @AfterClass(alwaysRun = true)
    protected void shutdownIntegrationTest() throws Exception {
        removeDefaultFile();
        stopEmbeddedReferencePillar();
    }

    protected void startEmbeddedReferencePillar() throws Exception {
        if (testSettings.useEmbeddedPillar()) {
            String pathToReferencePillarSettings = getPathToReferencePillarSettings();
            String pillarID = "embeddedReferencePillar";
            PillarSettingsProvider settingsLoader =
                    new PillarSettingsProvider(new XMLFileSettingsLoader(pathToReferencePillarSettings), pillarID);

            ReferencePilllarDerbyDBTestUtils.createEmptyDatabases(componentSettings);

            org.bitrepository.protocol.security.SecurityManager securityManager = loadSecurityManager(componentSettings);

            MessageBus messageBus = ProtocolComponentFactory.getInstance().getMessageBus(componentSettings, securityManager);

            new ReferencePillar(messageBus, componentSettings);
        }
    }

    protected void stopEmbeddedReferencePillar() throws Exception {
        if (testSettings.useEmbeddedPillar()) {
        }
    }

    protected void configureFileHelper() {
        HttpServerConfiguration config = testSettings.getHttpServerConfig();
        fileHelper = new TestFileHelper(
                componentSettings,
                new HttpServerConnector(config, TestEventManager.getInstance()));
    }

    @Override
    protected String getComponentID() {
        return "PillarUnderIntegrationTest";
    }

    private String getPathToReferencePillarSettings() {
        return System.getProperty(PILLAR_INTEGRATIONTEST_SETTINGS_PATH, PATH_TO_DEFAULT_SETTINGS);
    }

    private void loadTestSettings() throws IOException {
        testSettings = new PillarIntegrationTestSettings(PATH_TO_DEFAULT_SETTINGS + PROPERTY_FILE_NAME);
    }

    /**
     * Instantiates the security manager based on the settings and the path to the key file.
     * @param settings The settings.
     * @return The security manager.
     */
    private static BasicSecurityManager loadSecurityManager(Settings settings) {
        String privateKeyFile = "conf/client.pem";

        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
        return new BasicSecurityManager(settings.getCollectionSettings(), privateKeyFile,
                authenticator, signer, authorizer, permissionStore,
                settings.getComponentID());
    }

    private String getPutFilePillarTopic() {
        return null;
    }

    private void ingestDefaultFile() {
        EXISTING_PILLAR_FILE =
                "existing-pillar-file-" +
                        System.getProperty("user.name") + "-" +
                        System.currentTimeMillis() +
                        ".txt";
    }


    private void removeDefaultFile() {
    }
}
