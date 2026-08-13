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
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.read.ListAppender;
import ch.qos.logback.core.util.StatusPrinter;
import jakarta.jms.JMSException;
import org.apache.commons.lang3.exception.UncheckedException;
import org.bitrepository.SuiteInfo;
import org.bitrepository.SuiteInfoParameterResolver;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
import org.bitrepository.protocol.http.EmbeddedHttpServer;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusManager;
import org.bitrepository.protocol.messagebus.SimpleMessageBus;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.bitrepository.protocol.utils.TestWatcherExtension;
import org.bitrepository.settings.repositorysettings.ProtocolSettings;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Abstract base class for integration tests that manages shared infrastructure and test lifecycle hooks.
 * This class initializes and tears down common resources such as message brokers, HTTP servers, and security managers
 * at the suite level, while providing method-level setup and verification steps for individual tests.
 * Subclasses can extend this class to define component-under-test initialization, custom receiver registration,
 * and post-execution verification logic. The test environment is configurable via system properties to control
 * the use of embedded message buses and HTTP servers. Logging is intercepted during method execution to capture
 * events for failure diagnostics.
 */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(SuiteInfoParameterResolver.class)
public abstract class IntegrationTest {
    public EmbeddedHttpServer server;
    public HttpServerConfiguration httpServerConfiguration;
    public MessageBus messageBus;
    
    protected static String alarmDestinationID;
    protected static MessageReceiver alarmReceiver;
    protected static SecurityManager securityManager;
    protected static Settings settingsForCUT;
    protected static Settings settingsForTestClient;
    protected static String collectionID;
    protected static String defaultFileId;
    protected static URL defaultFileUrl;
    protected static String defaultDownloadFileAddress;
    protected static String defaultUploadFileAddress;
    private static List<Appender<ILoggingEvent>> appenders;
    
    protected MessageReceiverManager receiverManager;
    protected String nonDefaultFileId;
    protected String defaultAuditInformation;
    
    @RegisterExtension
    protected TestWatcherExtension testWatcher = new TestWatcherExtension();
    protected String testMethodName;
    
    private ListAppender<ILoggingEvent> appender;
    
    @BeforeAll
    public void initializeSuite(SuiteInfo testInfo) {
        settingsForCUT = loadSettings(getComponentID());
        settingsForTestClient = loadSettings("TestSuiteInitialiser");
        makeUserSpecificSettings(settingsForCUT);
        makeUserSpecificSettings(settingsForTestClient);
        httpServerConfiguration =
            new HttpServerConfiguration(settingsForTestClient.getReferenceSettings().getFileExchangeSettings());
        collectionID = settingsForTestClient.getCollections().get(0).getID();
        securityManager = createSecurityManager();
        defaultFileId = "DefaultFile";
        constructDefaultFileUrls();
        setupMessageBus();
    }
    
    protected Settings loadSettings(String componentID) {
        return TestSettingsProvider.reloadSettings(componentID);
    }
    
    protected String getComponentID() {
        return getClass().getSimpleName();
    }
    
    private void makeUserSpecificSettings(Settings settings) {
        ProtocolSettings protocolSettings = settings.getRepositorySettings().getProtocolSettings();
        protocolSettings.setCollectionDestination(settings.getCollectionDestination() + getTopicPostfix());
        protocolSettings.setAlarmDestination(settings.getAlarmDestination() + getTopicPostfix());
    }
    
    protected SecurityManager createSecurityManager() {
        return new DummySecurityManager();
    }
    
    private void constructDefaultFileUrls() {
        try {
            defaultFileUrl = httpServerConfiguration.getURL(TestFileHelper.DEFAULT_FILE_ID);
            defaultDownloadFileAddress = defaultFileUrl.toExternalForm();
            defaultUploadFileAddress = defaultFileUrl.toExternalForm() + "-" + defaultFileId;
        } catch (MalformedURLException e) {
            throw new UncheckedException(e);
        }
    }
    
    /**
     * Hooks up the message bus.
     */
    protected void setupMessageBus() {
        if (useEmbeddedMessageBus()) {
            if (messageBus == null) {
                messageBus = new SimpleMessageBus();
            }
            MessageBusManager.injectCustomMessageBus(MessageBusManager.DEFAULT_MESSAGE_BUS, messageBus);
            if (settingsForTestClient != null) {
                MessageBusManager.injectCustomMessageBus(settingsForTestClient.getComponentID(), messageBus);
            }
            if (settingsForCUT != null) {
                MessageBusManager.injectCustomMessageBus(settingsForCUT.getComponentID(), messageBus);
            }
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

    /**
     * Indicated whether an embedded active MQ should be started and used
     */
    public boolean useEmbeddedMessageBus() {
        return System.getProperty("useEmbeddedMessageBus", "true").equals("true");
    }

    @BeforeEach
    public void logTestStart(TestInfo testInfo) {
        Class<?> aClass = testInfo.getTestClass().orElse(null);
        String displayName = testInfo.getDisplayName();
        System.out.println("Running Integration Test " + aClass + "#" + displayName);
    }

    /**
     * Defines the standard BitRepositoryCollection configuration
     */
    @BeforeEach
    public void beforeMethod(TestInfo testInfo) {
        testMethodName = testInfo.getTestMethod().get().getName();
        setupSettings();
        nonDefaultFileId = TestFileHelper.createUniquePrefix(testMethodName);
        defaultAuditInformation = testMethodName;
        receiverManager = new MessageReceiverManager(messageBus);
        registerMessageReceivers();
        messageBus.setCollectionFilter(List.of());
        messageBus.setComponentFilter(List.of());
        initializeCUT();
        receiverManager.startListeners();
        appender = hookLogger();
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

    /**
     * May be extended by subclasses needing to have their receivers managed. Remember to still call
     * <code>super.registerReceivers()</code> when overriding
     */
    protected void registerMessageReceivers() {
        alarmReceiver = addReceiver(new MessageReceiver(settingsForCUT.getAlarmDestination()));
    }
    
    protected void initializeCUT() {
    }

    private static ListAppender<ILoggingEvent> hookLogger() {
        // get Logback Logger
        var fooLogger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        
        // create and start a ListAppender
        ListAppender<ILoggingEvent> listAppender = new ListAppender<>();
        listAppender.start();

        //Save the appenders and detach them
        appenders = new ArrayList<>();
        Iterator<Appender<ILoggingEvent>> iterator = fooLogger.iteratorForAppenders();
        while (iterator.hasNext()) {
            Appender<ILoggingEvent> next = iterator.next();
            fooLogger.detachAppender(next);
            appenders.add(next);
        }
        // add the appender to the logger
        fooLogger.addAppender(listAppender);
        return listAppender;
    }

    protected MessageReceiver addReceiver(MessageReceiver receiver) {
        receiverManager.addReceiver(receiver);
        return receiver;
    }

    @BeforeEach
    public void writeLogStatus() {
        if (System.getProperty("enableLogStatus", "false").equals("true")) {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            StatusPrinter.print(lc);
        }
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
        if (!testWatcher.isTestSuccessful()) {
            replayLoggedEvents();
        }
    }

    /**
     * May be used by specific tests for general verification when the test method has finished. Will only be run
     * if the test has passed (so far).
     */
    protected void afterMethodVerification() {
        receiverManager.checkNoMessagesRemainInReceivers();
    }

    /**
     * May be overridden by specific tests wishing to do stuff. Remember to call super if this is overridden.
     */
    protected void shutdownCUT() {
    }

    private void replayLoggedEvents() {
        for (ILoggingEvent loggingEvent : appender.list) {
            for (Appender<ILoggingEvent> appender : appenders) {
                appender.doAppend(loggingEvent);
            }
        }
    }

    @AfterAll
    public void shutdownSuite() {
        teardownMessageBus();
        teardownHttpServer();
    }

    /**
     * Shutdown the message bus.
     */
    protected void teardownMessageBus() {
        if (useEmbeddedMessageBus()) {
            MessageBusManager.clear();
            if (messageBus != null) {
                try {
                    messageBus.close();
                    messageBus = null;
                } catch (JMSException e) {
                    throw new RuntimeException(e);
                }
            }
            
        }
    }

    /**
     * Shutdown the embedded http server if any.
     */
    protected void teardownHttpServer() {
        if (useEmbeddedHttpServer()) {
            server.stop();
        }
    }

    /**
     * Indicated whether an embedded http server should be started and used
     */
    public boolean useEmbeddedHttpServer() {
        return System.getProperty("useEmbeddedHttpServer", "false").equals("true");
    }

    /**
     * Purges all messages from the receivers.
     */
    protected void clearReceivers() {
        receiverManager.clearMessagesInReceivers();
    }

    protected String createDate() {
        return Long.toString(System.currentTimeMillis());
    }
}