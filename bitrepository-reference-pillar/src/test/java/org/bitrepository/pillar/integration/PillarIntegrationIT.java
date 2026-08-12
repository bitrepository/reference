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

import io.qameta.allure.Allure;
import jakarta.jms.JMSException;
import org.apache.commons.io.FileUtils;
import org.bitrepository.SuiteInfo;
import org.bitrepository.SuiteInfoParameterResolver;
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
import org.bitrepository.pillar.integration.model.PillarFileManager;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.messagebus.SimpleMessageBus;
import org.bitrepository.protocol.security.BasicMessageAuthenticator;
import org.bitrepository.protocol.security.BasicMessageSigner;
import org.bitrepository.protocol.security.BasicOperationAuthorizer;
import org.bitrepository.protocol.security.BasicSecurityManager;
import org.bitrepository.protocol.security.MessageAuthenticator;
import org.bitrepository.protocol.security.MessageSigner;
import org.bitrepository.protocol.security.OperationAuthorizer;
import org.bitrepository.protocol.security.PermissionStore;
import org.bitrepository.protocol.security.SecurityManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.activemq.ArtemisContainer;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.InternetProtocol;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.bitrepository.common.utils.AllureTestUtils.isTestRunning;

/**
 * Super class for all tests which should test functionality on a single pillar.
 * <p>
 * Note that no setup/teardown is possible in this test of external pillars, so tests need to be written
 * to be invariant against the initial pillar state.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SuiteInfoParameterResolver.class)
@Testcontainers(parallel = true, disabledWithoutDocker = true)
public abstract class PillarIntegrationIT extends IntegrationTest {
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
    protected static PillarIntegrationTestConfiguration testConfiguration;
    private EmbeddedPillar embeddedPillar;

    protected PillarFileManager pillarFileManager;
    protected static ClientProvider clientProvider;

    protected static String nonDefaultCollectionId;
    protected static String irrelevantCollectionId;
    protected static ClientEventLogger clientEventHandler;
    private static final Logger log = LoggerFactory.getLogger(PillarIntegrationIT.class);

    @Container
    static ArtemisContainer activemq = new ArtemisFixedPortContainer("apache/artemis:2.55.0")
                                                .withFixedExposedPort(61616, 61616, InternetProtocol.TCP)
                                                   .withEnv("ANONYMOUS_LOGIN","true");
    @Container
    static PostgreSQLContainer auditTrailDB = new PostgresFixedPortContainer("postgres:18-alpine")
                                                      .withFixedExposedPort(65432, 5432, InternetProtocol.TCP)
                                                      .withDatabaseName("auditcontributerdb")
                                                      .withUsername("testcontainerUser")
                                                      .withPassword("testcontainerPassword")
                                                      .withLabel("purpose","auditcontributerdb");
    @Container
    static PostgreSQLContainer checksumDB = new PostgresFixedPortContainer("postgres:18-alpine")
                                                      .withFixedExposedPort(54321, 5432, InternetProtocol.TCP)
                                                      .withDatabaseName("checksumdb")
                                                      .withUsername("testcontainerUser")
                                                      .withPassword("testcontainerPassword")
                                                      .withLabel("purpose","checksumdb");
  /**
     * Initializes the test suite environment.
     * <p>
     * This method is annotated with {@link BeforeAll} and is responsible for:
     * <ul>
     *     <li>Loading the test configuration.</li>
     *     <li>Setting up the message bus.</li>
     *     <li>Starting the embedded pillar if configured.</li>
     *     <li>Initializing client providers and file managers.</li>
     *     <li>Uploading a default test file to the repository.</li>
     * </ul>
     *
     * @param testInfo Information about the test suite being initialized.
     */
    @Override
    @BeforeAll
    public void initializeSuite(SuiteInfo testInfo) {
        if (testConfiguration == null) {
            testConfiguration =
                    new PillarIntegrationTestConfiguration(PATH_TO_TESTPROPS_DIR + "/" + TEST_CONFIGURATION_FILE_NAME);
        }
        super.initializeSuite(testInfo);

        setupRealMessageBus();


        startEmbeddedPillar(testInfo);
        reloadMessageBus();
        clientProvider = new ClientProvider(securityManager, settingsForTestClient);
        nonDefaultCollectionId = settingsForTestClient.getCollections().get(1).getID();
        irrelevantCollectionId = settingsForTestClient.getCollections().get(2).getID();
        putDefaultFile();
    }

    /**
     * Shuts down the real message bus after all tests in the class have run.
     * <p>
     * This method checks if an embedded message bus is NOT being used before attempting to close and clear
     * the message bus manager.
     */
    @AfterAll
    public void shutdownRealMessageBus() {
        if (!useEmbeddedMessageBus()) {
            MessageBusManager.clear();
            if (messageBus != null) {
                try {
                    messageBus.close();
                } catch (JMSException e) {
                    log.warn("Failed to close message bus", e);
                }
                messageBus = null;
            }
            activemq.stop();
        }
    }

    /**
     * Performs teardown operations for the entire suite.
     * <p>
     * This includes stopping the embedded reference pillar and calling the superclass's shutdown method.
     */
    @AfterAll
    @Override
    public void shutdownSuite() {
        if (embeddedPillar != null) {
            embeddedPillar.shutdown();
            auditTrailDB.stop();
            checksumDB.stop();
        }
        super.shutdownSuite();
    }

    @Override
    protected void initializeCUT() {
        super.initializeCUT();
        reloadMessageBus();
        clientProvider = new ClientProvider(securityManager, settingsForTestClient);
        pillarFileManager = new PillarFileManager(collectionID,
                                                  getPillarID(),
                                                  settingsForTestClient,
                                                  clientProvider,
                                                  httpServerConfiguration);
        clientEventHandler = new ClientEventLogger();
    }

    /**
     * Adds context information to the test result in case of failure.
     * <p>
     * This method is called after each test execution. Currently, it provides an empty implementation
     * intended to be overridden or populated for debugging purposes.
     *
     * @param result Information about the executed test.
     */
    @AfterEach
    public void addFailureContextInfo(TestInfo result) {
    }

    protected void setupRealMessageBus() {
        if (!useEmbeddedMessageBus()) {
            MessageBusManager.clear();
            startContainer(activemq);
            messageBus = MessageBusManager.getMessageBus(settingsForCUT, securityManager);
        } else {
            messageBus = new SimpleMessageBus();
            MessageBusManager.injectCustomMessageBus(MessageBusManager.DEFAULT_MESSAGE_BUS, messageBus);
            if (settingsForTestClient != null) {
                MessageBusManager.injectCustomMessageBus(settingsForTestClient.getComponentID(), messageBus);
            }
            if (settingsForCUT != null) {
                MessageBusManager.injectCustomMessageBus(settingsForCUT.getComponentID(), messageBus);
            }
        }
    }

    private static void startContainer(final GenericContainer<?> container) {
        container.start();
        while (!container.isRunning()) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    /**
     * Will start an embedded reference pillar if specified in the <code>pillar-integration-test.properties</code>.<p>
     * The type of pillar (full or checksum) is baed on the test group used, i.e. if the group is
     * <code>checksumPillarTest</code> a checksum pillar is started, else a normal 'full' reference pillar is started.
     * </p>
     *
     * @param testInfo The suite info containing the pillar type.
     */
    protected void startEmbeddedPillar(SuiteInfo testInfo) {
        if (testConfiguration.useEmbeddedPillar()) {
            startContainer(auditTrailDB);
            startContainer(checksumDB);
            SettingsUtils.initialize(settingsForCUT);
            //TODO the tags are for the tags on the class, not the method
            // And they are not the tags from Suite, so you will not get the behaivour you want...
            if (testInfo.getPillarType().filter(pillarType -> pillarType.equals("Checksum")).isPresent()) {
                embeddedPillar = EmbeddedPillar.createChecksumPillar(settingsForCUT);
            } else {
                embeddedPillar = EmbeddedPillar.createReferencePillar(settingsForCUT);
            }
        }
    }

    @Override
    public boolean useEmbeddedMessageBus() {
        return false;
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
     * i.e. we can not add a special postfix.
     *
     * @Override
     */
    protected String getTopicPostfix() {
        if (testConfiguration.useEmbeddedPillar()) {
            return "-" + System.getProperty("user.name");
        } else {
            return "";
        }
    }

    @Override
    protected SecurityManager createSecurityManager() {
        if (testConfiguration.useEmbeddedPillar()) {
            return super.createSecurityManager();
        } else {
            PermissionStore permissionStore = new PermissionStore();
            MessageAuthenticator authenticator = new BasicMessageAuthenticator(permissionStore);
            MessageSigner signer = new BasicMessageSigner();
            OperationAuthorizer authorizer = new BasicOperationAuthorizer(permissionStore);
            return new BasicSecurityManager(settingsForTestClient.getRepositorySettings(),
                    testConfiguration.getPrivateKeyFileLocation(),
                    authenticator, signer, authorizer, permissionStore, settingsForTestClient.getComponentID());
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

    protected void putDefaultFile() {
        try {

            Path path = new File(settingsForCUT.getReferenceSettings().getFileExchangeSettings().getPath()).toPath();

            try {
                FileUtils.forceMkdir(path.toFile());
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            URL resource = Thread.currentThread()
                                 .getContextClassLoader()
                                 .getResource("default-test-file.txt");
            Assertions.assertNotNull(resource);
            Path srcFile = new File(resource.getFile()).toPath();

            FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(settingsForCUT);
            try (InputStream fis = new BufferedInputStream(Files.newInputStream(srcFile))) {
                fe.putFile(fis, defaultFileUrl);
            } catch (IOException e) {
                throw new UncheckedIOException("Failed to upload default test file", e);
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
         * The constructor.
         */
        public ClientEventLogger() {
            super();
        }

        @Override
        public void handleEvent(OperationEvent event) {
            if (!isTestRunning()) {
                return;
            }
            Allure.step("Received event: " + event.getEventType(), () -> {
                Allure.addAttachment("Event Details", event.toString());
            });
        }
    }
}
