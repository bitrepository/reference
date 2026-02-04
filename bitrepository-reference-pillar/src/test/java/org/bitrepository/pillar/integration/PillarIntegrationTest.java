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

import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediatorManager;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.exceptions.OperationFailedException;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.SettingsProvider;
import org.bitrepository.common.settings.XMLFileSettingsLoader;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.PillarSettingsProvider;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.model.PillarFileManager;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizer;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizer;
import org.bitrepository.protocol.security.PermissionStore;
import org.jaccept.TestEventManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.jms.JMSException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Super class for all tests which should test functionality on a single pillar.
 * <p>
 * Note That no setup/teardown is possible in this test of external pillars, so tests need to be written
 * to be invariant against the initial pillar state.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(PillarSuiteExtension.class)
public abstract class PillarIntegrationTest extends IntegrationTest {
    /**
     * The path to the directory containing the integration test configuration files
     */
    protected static final String PATH_TO_CONFIG_DIR = System.getProperty(
            "pillar.integrationtest.settings.path",
            "conf");
    /**
     * The path to the directory containing the integration test configuration files
     */
    protected static final String PATH_TO_TESTPROPS_DIR = System.getProperty(
            "pillar.integrationtest.testprops.path",
            "testprops");
    public static final String TEST_CONFIGURATION_FILE_NAME = "pillar-integration-test.properties";
    private static String DEFAULT_UPLOAD_FILE_ADDRESS;
    private static Object DEFAULT_DOWNLOAD_FILE_ADDRESS;
    protected static PillarIntegrationTestConfiguration testConfiguration;
    protected EmbeddedPillar embeddedPillar;

    protected PillarFileManager pillarFileManager;
    protected static ClientProvider clientProvider;

    protected static String nonDefaultCollectionId;
    protected static String irrelevantCollectionId;
    protected static ClientEventLogger clientEventHandler;
    protected static URL DEFAULT_FILE_URL;
    protected static String DEFAULT_FILE_ID = "default-test-file.txt";

    @Override
    protected void initializeCUT() {
        super.initializeCUT();
        System.out.println("DEBUG: PillarIntegrationTest.initializeCUT - messageBus@" + System.identityHashCode(messageBus));

        clientProvider = new ClientProvider(securityManager, settingsForTestClient, testEventManager);
        pillarFileManager = new PillarFileManager(collectionID,
                getPillarID(), settingsForTestClient, clientProvider, testEventManager, httpServerConfiguration);
        clientEventHandler = new ClientEventLogger(testEventManager);
    }

    /**
     * Runs once before all test methods in each concrete subclass.
     *
     * <p>The sequence replicates the original TestNG {@code initializeSuite()}:
     * <ol>
     *   <li>Load {@link PillarIntegrationTestConfiguration}.</li>
     *   <li>Replicate {@code super.initializeSuite()}: load Settings, create SecurityManager, etc.</li>
     *   <li>Start the embedded pillar (guarded: only once per suite).</li>
     *   <li>Set up the message bus.</li>
     *   <li>Create {@link ClientProvider} and resolve collection IDs.</li>
     *   <li>Upload the default test file.</li>
     * </ol>
     */
    @BeforeAll
    static void setupClass() throws Exception {
        testConfiguration = new PillarIntegrationTestConfiguration(
                PATH_TO_TESTPROPS_DIR + "/" + TEST_CONFIGURATION_FILE_NAME);

        PillarIntegrationTestHelper helper = new PillarIntegrationTestHelper();

        settingsForCUT = helper.loadSettings(testConfiguration.getPillarUnderTestID());
        settingsForTestClient = helper.loadSettings("TestSuiteInitialiser");

        helper.makeUserSpecificSettings(settingsForCUT);
        helper.makeUserSpecificSettings(settingsForTestClient);

        httpServerConfiguration = new HttpServerConfiguration(
                settingsForTestClient.getReferenceSettings().getFileExchangeSettings());
        collectionID = settingsForTestClient.getCollections().get(0).getID();

        securityManager = helper.createSecurityManager();

        DEFAULT_FILE_ID = "DefaultFile";
        try {
            DEFAULT_FILE_URL = httpServerConfiguration.getURL(TestFileHelper.DEFAULT_FILE_ID);
            DEFAULT_DOWNLOAD_FILE_ADDRESS = DEFAULT_FILE_URL.toExternalForm();
            DEFAULT_UPLOAD_FILE_ADDRESS = DEFAULT_FILE_URL.toExternalForm() + "-" + DEFAULT_FILE_ID;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Never happens", e);
        }
    }

    @BeforeEach()
    public void setupPillarIntegrationTest(TestInfo testInfo) {
        if (testConfiguration == null) {
            testConfiguration = new PillarIntegrationTestConfiguration(PATH_TO_TESTPROPS_DIR + "/" + TEST_CONFIGURATION_FILE_NAME);
        }

        if (testConfiguration.useEmbeddedPillar()
                && !PillarSuiteExtension.isPillarAlreadyStarted()) {

            SettingsUtils.initialize(settingsForCUT);

            EmbeddedPillar pillar;
            if (PillarSuiteExtension.isChecksumPillar()) {
                pillar = EmbeddedPillar.createChecksumPillar(settingsForCUT);
            } else {
                pillar = EmbeddedPillar.createReferencePillar(settingsForCUT);
            }

            PillarSuiteExtension.registerPillar(pillar);
        }

        clientProvider = new ClientProvider(securityManager, settingsForTestClient, testEventManager);
        nonDefaultCollectionId = settingsForTestClient.getCollections().get(1).getID();
        irrelevantCollectionId = settingsForTestClient.getCollections().get(2).getID();
//        putDefaultFile();
    }


    /**
     * Helper class to access instance methods from static context.
     * Temporary instance used only during setupClass().
     */
    private static class PillarIntegrationTestHelper extends PillarIntegrationTest {
        @Override
        protected Settings loadSettings(String componentID) {
            return super.loadSettings(componentID);
        }

        @Override
        protected org.bitrepository.protocol.security.SecurityManager createSecurityManager() {
            return super.createSecurityManager();
        }

        protected void makeUserSpecificSettings(Settings settings) {
            // From IntegrationTest - modify settings to add username postfix
            String topicPostfix = getTopicPostfix();
            if (topicPostfix != null && !topicPostfix.isEmpty()) {
                settings.getRepositorySettings().getProtocolSettings()
                        .setCollectionDestination(settings.getRepositorySettings()
                                .getProtocolSettings().getCollectionDestination() + topicPostfix);
                settings.getRepositorySettings().getProtocolSettings()
                        .setAlarmDestination(settings.getRepositorySettings()
                                .getProtocolSettings().getAlarmDestination() + topicPostfix);
            }
        }
    }
//
//    @AfterAll
//    static void teardownClass() {
//        if (!testConfiguration.useEmbeddedMessagebus()) {
//            MessageBusManager.clear();
//            if (messageBus != null) {
//                try {
//                    messageBus.close();
//                } catch (JMSException e) {
//                    e.printStackTrace();
//                }
//                messageBus = null;
//            }
//        }
//    }

    @AfterAll
    public void shutdownRealMessageBus() {
        if (!useEmbeddedMessageBus()) {
            MessageBusManager.clear();
            if (messageBus != null) {
                try {
                    messageBus.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
                messageBus = null;
            }
        }
    }

    /**
     * Runs before each test method.
     *
     * <p>Mirrors the original {@code initializeCUT()}: re-injects the ConversationMediator and
     * creates the per-test helpers.
     */
    @BeforeEach
    void setupTest() {
        reloadMessageBus();

        clientProvider = new ClientProvider(securityManager, settingsForTestClient, testEventManager);

        pillarFileManager = new PillarFileManager(
                collectionID, getPillarID(), settingsForTestClient,
                clientProvider, testEventManager, httpServerConfiguration);

        clientEventHandler = new ClientEventLogger(testEventManager);
    }

    @AfterEach
    public void addFailureContextInfo() {
    }

    @Override
    protected void setupMessageBus() {
        //Shortcircuit this so the messagebus is NOT INITIALISED BEFORE THE CONFIGURATION
        super.setupMessageBus();
        setupRealMessageBus();
    }

    /**
     * Sets up the message bus after the test configuration is available.
     *
     * <ul>
     *   <li>Real message bus: clear any stale instance, then obtain a fresh one.</li>
     *   <li>Embedded message bus: inject the already-started embedded instance so
     *       all components share it.</li>
     * </ul>
     */
    private static void setupRealMessageBus() {
        if (!testConfiguration.useEmbeddedMessagebus()) {
            MessageBusManager.clear();
            messageBus = MessageBusManager.getMessageBus(settingsForCUT, securityManager);
        } else {
            MessageBusManager.injectCustomMessageBus(MessageBusManager.DEFAULT_MESSAGE_BUS, messageBus);
        }
    }

    protected static void reloadMessageBus() {
        ConversationMediatorManager.injectCustomConversationMediator(
                new CollectionBasedConversationMediator(settingsForTestClient, securityManager));
    }

    /**
     * Will start an embedded reference pillar if specified in the <code>pillar-integration-test.properties</code>.<p>
     * The type of pillar (full or checksum) is baed on the test group used, eg. if the group is
     * <code>checksumPillarTest</code> a checksum pillar is started, else a normal 'full' reference pillar is started.
     * </p>
     *
     * @param testInfo
     */
    protected void startEmbeddedPillar(TestInfo testInfo) {
        System.out.println("DEBUG: startEmbeddedPillar - useEmbeddedPillar=" + testConfiguration.useEmbeddedPillar());
        System.out.println("DEBUG: startEmbeddedPillar - tags=" + testInfo.getTags());
        if (testConfiguration.useEmbeddedPillar()) {
            SettingsUtils.initialize(settingsForCUT);
            if (testInfo.getTags().contains(PillarTestGroups.CHECKSUM_PILLAR_TEST)) {
                System.out.println("DEBUG: Creating CHECKSUM pillar");
                embeddedPillar = EmbeddedPillar.createChecksumPillar(settingsForCUT);
            } else {
                System.out.println("DEBUG: Creating REFERENCE pillar");
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

    /**
     * Loads the pillar test specific settings
     */
    @Override
    protected Settings loadSettings(String componentID) {
        SettingsProvider settingsLoader =
                new PillarSettingsProvider(new XMLFileSettingsLoader(PATH_TO_CONFIG_DIR), componentID);
        return settingsLoader.getSettings();
    }

    protected String getPillarID() {
        return testConfiguration.getPillarUnderTestID();
    }

    protected long getOperationTimeout() {
        return testConfiguration.getPillarOperationTimeout();
    }

    /**
     * Overrides the default settings modification, as this only works if the test can inject the modified settings into
     * the pillar. This means that if we are not using an embedded pillar we need to use the 'raw' collection settings,
     * eg. we can not add a special postfix.
     *
     * @Override
     */
    protected String getTopicPostfix() {
        if (testConfiguration.useEmbeddedPillar()) {
            return "-" + System.getProperty("user.name");
        } else return "";
    }

    /**
     * Creates the SecurityManager.  With an embedded pillar the parent implementation is
     * sufficient; with an external pillar we build one from the private-key location in the
     * test configuration.
     */
    @Override
    protected org.bitrepository.protocol.security.SecurityManager createSecurityManager() {
        if (testConfiguration.useEmbeddedPillar()) {
            return super.createSecurityManager();
        }

        PermissionStore permissionStore = new PermissionStore();
        MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
        MessageSigner signer = new BasicMessageSigner();
        OperationAuthorizer authorizer = new BasicOperationAuthorizer(permissionStore);

        return new BasicSecurityManager(
                settingsForTestClient.getRepositorySettings(),
                testConfiguration.getPrivateKeyFileLocation(),
                authenticator, signer, authorizer, permissionStore,
                settingsForTestClient.getComponentID());
    }

//    @Override
//    protected SecurityManager createSecurityManager() {
//        if (testConfiguration.useEmbeddedPillar()) {
//            return super.createSecurityManager();
//        } else {
//            PermissionStore permissionStore = new PermissionStore();
//            MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
//            MessageSigner signer = new BasicMessageSigner();
//            OperationAuthorizer authorizer = new BasicOperationAuthorizer(permissionStore);
//            return new BasicSecurityManager(settingsForTestClient.getRepositorySettings(),
//                    testConfiguration.getPrivateKeyFileLocation(),
//                    authenticator, signer, authorizer, permissionStore, settingsForTestClient.getComponentID());
//        }
//    }

    @Override
    protected String getComponentID() {
        return getPillarID() + "-test-client";
    }

    @Override
    protected void afterMethodVerification() {
        // Do not run the normal verification of all messages been handled. Message receivers are only used for
        // logging purposes here.
    }

    protected static void putDefaultFile() {
        try {
            FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(settingsForCUT);
            try (InputStream fis = Thread.currentThread().getContextClassLoader().getResourceAsStream("default-test-file.txt")) {
                fe.putFile(fis, defaultFileUrl);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }


            clientProvider.getPutClient().putFile(
                    collectionID, defaultFileUrl, defaultFileId, 10L, TestFileHelper.getDefaultFileChecksum(),
                    null, clientEventHandler, null);
            clientProvider.getPutClient().putFile(
                    nonDefaultCollectionId, defaultFileUrl, defaultFileId, 10L, TestFileHelper.getDefaultFileChecksum(),
                    null, clientEventHandler, null);
        } catch (OperationFailedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used to listen for operation event and log this.
     */
    public class ClientEventLogger implements EventHandler {

        /**
         * The <code>TestEventManager</code> used to manage the event for the associated test.
         */
        private final TestEventManager testEventManager;

        /**
         * The constructor.
         *
         * @param testEventManager The <code>TestEventManager</code> used to manage the event for the associated test.
         */
        public ClientEventLogger(TestEventManager testEventManager) {
            super();
            this.testEventManager = testEventManager;
        }

        @Override
        public void handleEvent(OperationEvent event) {
            testEventManager.addResult("Received event: " + event);
        }
    }
}
