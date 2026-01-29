/*
 * #%L
 * Bitrepository Common
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
package org.bitrepository.protocol;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.protocol.bus.LocalActiveMQBroker;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
import org.bitrepository.protocol.http.EmbeddedHttpServer;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.messagebus.SimpleMessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.utils.TestWatcherExtension;
import org.jaccept.TestEventManager;
import org.jaccept.structure.ExtendedTestCase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.platform.suite.api.Suite;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Suite
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(TestWatcherExtension.class)
@ExtendWith(GlobalSuiteExtension.class)
public class IntegrationTest extends ExtendedTestCase {
    protected static TestEventManager testEventManager = TestEventManager.getInstance();
    public static LocalActiveMQBroker broker;
    public static EmbeddedHttpServer server;
    public static HttpServerConfiguration httpServerConfiguration;
    public static MessageBus messageBus;
    private MessageReceiverManager receiverManager;
    protected static String alarmDestinationID;
    protected static MessageReceiver alarmReceiver;
    protected static SecurityManager securityManager;
    protected static Settings settingsForCUT;
    protected static Settings settingsForTestClient;
    protected static String collectionID;
    protected String nonDefaultFileId;
    protected static String defaultFileId;
    protected static URL defaultFileUrl;
    protected static String defaultDownloadFileAddress;
    protected static String defaultUploadFileAddress;
    protected String defaultAuditInformation;

    @RegisterExtension
    TestWatcherExtension testWatcher = new TestWatcherExtension();
    protected String testMethodName;

    private void initializationMethod() {
        settingsForCUT = loadSettings(getComponentID());
        settingsForTestClient = loadSettings("TestSuiteInitialiser");
        makeUserSpecificSettings(settingsForCUT);
        makeUserSpecificSettings(settingsForTestClient);
        httpServerConfiguration = new HttpServerConfiguration(settingsForTestClient.getReferenceSettings().getFileExchangeSettings());
        collectionID = settingsForTestClient.getCollections().get(0).getID();

        securityManager = createSecurityManager();
        defaultFileId = "DefaultFile";
        try {
            defaultFileUrl = httpServerConfiguration.getURL(TestFileHelper.DEFAULT_FILE_ID);
            defaultDownloadFileAddress = defaultFileUrl.toExternalForm();
            defaultUploadFileAddress = defaultFileUrl.toExternalForm() + "-" + defaultFileId;
        } catch (MalformedURLException e) {
            throw new RuntimeException("Never happens");
        }
    }

    /**
     * May be extended by subclasses needing to have their receivers managed. Remember to still call
     * <code>super.registerReceivers()</code> when overriding
     */
    protected void registerMessageReceivers() {
        alarmReceiver = new MessageReceiver(settingsForCUT.getAlarmDestination(), testEventManager);
        addReceiver(alarmReceiver);
    }

    protected void addReceiver(MessageReceiver receiver) {
        receiverManager.addReceiver(receiver);
    }

    @BeforeAll
    public void initMessagebus() {
        initializationMethod();
        setupMessageBus();
        if (System.getProperty("enableLogStatus", "false").equals("true")) {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            StatusPrinter.print(lc);
        }
    }

    @AfterAll
    public void shutdownSuite() {
        teardownMessageBus();
        teardownHttpServer();
    }

    /**
     * Defines the standard BitRepositoryCollection configuration
     */
    @BeforeEach
    public final void beforeMethod(TestInfo testInfo) {
        testMethodName = testInfo.getTestMethod().get().getName();
        setupSettings();
        nonDefaultFileId = TestFileHelper.createUniquePrefix(testMethodName);
        defaultAuditInformation = testMethodName;
        receiverManager = new MessageReceiverManager(messageBus);
        registerMessageReceivers();
        messageBus.setCollectionFilter(List.of());
        messageBus.setComponentFilter(List.of());
        receiverManager.startListeners();
        initializeCUT();
    }

    protected void initializeCUT() {
    }

    @AfterEach
    public final void afterMethod() {
        if (receiverManager != null) {
            receiverManager.stopListeners();
        }
        if (testWatcher.isTestSuccessful()) {
            afterMethodVerification();
        }
        shutdownCUT();
    }

    /**
     * May be used by specific tests for general verification when the test method has finished. Will only be run
     * if the test has passed (so far).
     */
    protected void afterMethodVerification() {
        receiverManager.checkNoMessagesRemainInReceivers();
    }

    /**
     * Purges all messages from the receivers.
     */
    protected void clearReceivers() {
        receiverManager.clearMessagesInReceivers();
    }

    /**
     * May be overridden by specific tests wishing to do stuff. Remember to call super if this is overridden.
     */
    protected void shutdownCUT() {
    }

    /**
     * Initializes the settings. Will postfix the alarm and collection topics with '-${user.name}
     */
    protected void setupSettings() {
        settingsForCUT = loadSettings(getComponentID());
        makeUserSpecificSettings(settingsForCUT);
        SettingsUtils.initialize(settingsForCUT);

        alarmDestinationID = settingsForCUT.getRepositorySettings().getProtocolSettings().getAlarmDestination();

        settingsForTestClient = loadSettings(testMethodName);
        makeUserSpecificSettings(settingsForTestClient);
    }


    protected Settings loadSettings(String componentID) {
        return TestSettingsProvider.reloadSettings(componentID);
    }

    protected void makeUserSpecificSettings(Settings settings) {
        settings.getRepositorySettings().getProtocolSettings()
                .setCollectionDestination(settings.getCollectionDestination() + getTopicPostfix());
        settings.getRepositorySettings().getProtocolSettings().setAlarmDestination(settings.getAlarmDestination() + getTopicPostfix());
    }

    /**
     * Indicated whether an embedded active MQ should be started and used
     */
    public boolean useEmbeddedMessageBus() {
        return System.getProperty("useEmbeddedMessageBus", "true").equals("true");
    }

    /**
     * Indicated whether an embedded http server should be started and used
     */
    public boolean useEmbeddedHttpServer() {
        return System.getProperty("useEmbeddedHttpServer", "false").equals("true");
    }

    /**
     * Hooks up the message bus.
     */
    protected void setupMessageBus() {
        if (useEmbeddedMessageBus()) {
            if (messageBus == null) {
                messageBus = new SimpleMessageBus();
            }
        }
    }

    /**
     * Shutdown the message bus.
     */
    public void teardownMessageBus() {
        MessageBusManager.clear();
        if (messageBus != null) {
            try {
                messageBus.close();
                messageBus = null;
            } catch (JMSException e) {
                throw new RuntimeException(e);
            }
        }

        if (broker != null) {
            try {
                broker.stop();
                broker = null;
            } catch (Exception e) {
                // No reason to pollute the test output with this
            }
        }
    }

    /**
     * Shutdown the embedded http server if any.
     */
    public void teardownHttpServer() {
        if (useEmbeddedHttpServer()) {
            server.stop();
        }
    }

    /**
     * Returns the postfix string to use when accessing user specific topics, which is the mechanism we use in the
     * bit repository tests.
     *
     * @return The string to postfix all topix names with.
     */
    protected String getTopicPostfix() {
        return "-" + System.getProperty("user.name");
    }

    protected String getComponentID() {
        return getClass().getSimpleName();
    }

    protected String createDate() {
        return Long.toString(System.currentTimeMillis());
    }

    protected SecurityManager createSecurityManager() {
        return new DummySecurityManager();
    }
}
