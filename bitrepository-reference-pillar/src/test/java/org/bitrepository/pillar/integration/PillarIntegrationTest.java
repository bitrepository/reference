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

import java.util.Arrays;

import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediatorManager;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.pillar.PillarSettingsProvider;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.model.PillarFileManager;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizor;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizor;
import org.bitrepository.protocol.security.PermissionStore;
import org.bitrepository.protocol.security.SecurityManager;
import org.testng.ITestContext;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
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
            "conf");   /** The path to the directory containing the integration test configuration files */
    protected static final String PATH_TO_TESTPROPS_DIR = System.getProperty(
            "pillar.integrationtest.testprops.path",
            "testprops");
    public static final String TEST_CONFIGURATION_FILE_NAME = "pillar-integration-test.properties";
    protected static PillarIntegrationTestConfiguration testConfiguration;
    private EmbeddedPillar embeddedPillar;

    protected CollectionTestHelper collectionHelper;
    protected PillarFileManager pillarFileManager;
    protected static ClientProvider clientProvider;

    protected static String nonDefaultCollectionId;
    protected static String irrelevantCollectionId;

    @Override
    protected void initializeCUT() {
        super.initializeCUT();
        reloadMessageBus();
        clientProvider = new ClientProvider(securityManager, settingsForTestClient, testEventManager);
        pillarFileManager = new PillarFileManager(collectionID,
            getPillarID(), settingsForTestClient, clientProvider, testEventManager, httpServer);
    }

    @BeforeSuite(alwaysRun = true)
    @Override
    public void initializeSuite(ITestContext testContext) {
        testConfiguration =
                new PillarIntegrationTestConfiguration(PATH_TO_TESTPROPS_DIR + "/" + TEST_CONFIGURATION_FILE_NAME);
        super.initializeSuite(testContext);
        startEmbeddedPillar(testContext);
        MessageBusManager.injectCustomMessageBus(MessageBusManager.DEFAULT_MESSAGE_BUS, messageBus);
        reloadMessageBus();
        clientProvider = new ClientProvider(securityManager, settingsForTestClient, testEventManager);
        nonDefaultCollectionId = settingsForTestClient.getCollections().get(1).getID();
        irrelevantCollectionId = settingsForTestClient.getCollections().get(2).getID();
    }

    @AfterSuite(alwaysRun = true)
    @Override
    public void shutdownSuite() {
        stopEmbeddedReferencePillar();
        super.shutdownSuite();
    }

    @AfterMethod(alwaysRun = true)
    public void addFailureContextInfo(ITestResult result) {
    }

    /**
     * Will start an embedded reference pillar if specified in the <code>pillar-integration-test.properties</code>.<p>
     * The type of pillar (full or checksum) is baed on the test group used, eg. if the group is
     * <code>checksumPillarTest</code> a checksum pillar is started, else a normal 'full' reference pillar is started.
     * </p>
     * @param testContext
     */
    protected void startEmbeddedPillar(ITestContext testContext) {
        if (testConfiguration.useEmbeddedPillar()) {
            if (Arrays.asList(testContext.getIncludedGroups()).contains(PillarTestGroups.CHECKSUM_PILLAR_TEST)) {
                embeddedPillar = EmbeddedPillar.createChecksumPillar(settingsForCUT);
            } else {
                embeddedPillar = EmbeddedPillar.createReferencePillar(settingsForCUT);
            }
        }
    }

    protected void stopEmbeddedReferencePillar() {
        if (embeddedPillar != null) {
            embeddedPillar.shutdown();
        }
    }
    @Override
    public boolean useEmbeddedMessageBus() {
        return testConfiguration.useEmbeddedMessagebus();
    }

    /** Loads the pillar test specific settings */
    @Override
    protected Settings loadSettings(String componentID) {
        SettingsProvider settingsLoader =
                new PillarSettingsProvider(new XMLFileSettingsLoader(PATH_TO_CONFIG_DIR), componentID);
        return settingsLoader.getSettings();
    }

    protected String getPillarID() {
        return testConfiguration.getPillarUnderTestID();
    }

    /**
     * Overrides the default settings modification, as this only works if the test can inject the modified settings into
     * the pillar. This means that if we are not using an embedded pillar we need to use the 'raw' collection settings,
     * eg. we can not add a special postfix.
     * @Override
     */
    protected String getTopicPostfix() {
        if (testConfiguration.useEmbeddedPillar()) {
            return "-" + System.getProperty("user.name");
        } else return "";
    }

    @Override
    protected SecurityManager createSecurityManager() {
        if (testConfiguration.useEmbeddedPillar()) {
            return super.createSecurityManager();
        } else {
            PermissionStore permissionStore = new PermissionStore();
            MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
            MessageSigner signer = new BasicMessageSigner();
            OperationAuthorizor authorizer = new BasicOperationAuthorizor(permissionStore);
            org.bitrepository.protocol.security.SecurityManager securityManager =
                    new BasicSecurityManager(settingsForTestClient.getRepositorySettings(),
                            testConfiguration.getPrivateKeyFileLocation(),
                            authenticator, signer, authorizer, permissionStore, settingsForTestClient.getComponentID());
            return securityManager;
        }
    }

    @Override
    protected String getComponentID() {
        return getPillarID();
    }

    protected void reloadMessageBus() {
        ConversationMediatorManager.injectCustomConversationMediator(
                new CollectionBasedConversationMediator(settingsForTestClient, securityManager));
    }

    @Override
    protected void afterMethodVerification() {
        // Do not run the normal verification of all messages been handled. Message receivers are only used for
        // logging purposes here.
    }
}
