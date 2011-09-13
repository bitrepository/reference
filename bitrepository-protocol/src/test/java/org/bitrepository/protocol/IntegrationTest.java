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

import javax.swing.JFrame;

import org.bitrepository.clienttest.MessageReceiver;
import org.bitrepository.collection.settings.standardsettings.MessageBusConfigurationTYPE;
import org.bitrepository.collection.settings.standardsettings.Settings;
import org.bitrepository.protocol.bus.MessageBusConfigurationFactory;
import org.bitrepository.protocol.bus.MessageBusWrapper;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
import org.bitrepository.protocol.fileexchange.HttpServerConnector;
import org.bitrepository.protocol.http.EmbeddedHttpServer;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusFactory;
import org.jaccept.TestEventManager;
import org.jaccept.gui.ComponentTestFrame;
import org.jaccept.structure.ExtendedTestCase;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Contains the generic parts for integration testing.
 */
public abstract class IntegrationTest extends ExtendedTestCase {
    protected TestEventManager testEventManager = TestEventManager.getInstance();
    public LocalActiveMQBroker broker;
    public EmbeddedHttpServer server;
    public HttpServerConnector httpServer;
    public MessageBus messageBus;

    protected static String bitRepositoryCollectionDestinationID;
    protected MessageReceiver bitRepositoryCollectionDestination; 
    
    protected Settings settings;

    @BeforeClass (alwaysRun = true)
    public void setupTest() throws Exception {
        System.out.println("SetupSettings");
        setupSettings();
        System.out.println("SetupMessageBus");
        setupMessageBus();
        System.out.println("DefineDestination");
        defineDestinations();
        System.out.println("SetupHttpServer");
        setupHttpServer();
    }
    
    /**
     * Defines the standard BitRepositoryCollection configuration
     * @param testMethod Used to grap the name of the test method used for naming.
     */
    @BeforeMethod(alwaysRun = true)
    public void beforeMethodSetup(java.lang.reflect.Method testMethod) {
        setupSettings();
    }    

    // Experimental, use at own risk.
    @BeforeTest (alwaysRun = true)
    public void startTestGUI() {  
        if (System.getProperty("enableTestGUI", "false").equals("true") ) {
            JFrame hmi = new ComponentTestFrame();
            hmi.pack();
            hmi.setVisible(true);
        }
    }

    @BeforeTest (alwaysRun = true)
    public void writeLogStatus() {  
        if (System.getProperty("enableLogStatus", "false").equals("true")) {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            StatusPrinter.print(lc);
        }
    }

    /** Indicated whether an embedded active MQ should be started and used */
    public boolean useEmbeddedMessageBus() {
        return System.getProperty("useEmbeddedMessageBus", "false").equals("true");
    }

    /** Indicated whether an embedded http server should be started and used */
    public boolean useEmbeddedHttpServer() {
        return System.getProperty("useEmbeddedHttpServer", "false").equals("true");
    }

    /**
     * 
     */
    protected void setupMessageBus() throws Exception {
        if (useEmbeddedMessageBus()) { 
            MessageBusConfigurationTYPE messageBusConfiguration = MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration();
            settings.getProtocol().setMessageBusConfiguration(messageBusConfiguration);
            broker = new LocalActiveMQBroker(messageBusConfiguration);
            broker.start();
            messageBus = new MessageBusWrapper(MessageBusFactory.createMessageBus(messageBusConfiguration), testEventManager);
        } else {
            MessageBusConfigurationTYPE messageBusConfiguration = MessageBusConfigurationFactory.createDefaultConfiguration();
            settings.getProtocol().setMessageBusConfiguration(messageBusConfiguration);
            messageBus = new MessageBusWrapper(
                    ProtocolComponentFactory.getInstance().getMessageBus(settings), testEventManager);
        }
    }

    protected void setupHttpServer() throws Exception {
        HttpServerConfiguration config = new HttpServerConfiguration();
        if (useEmbeddedHttpServer()) { // Note that the embedded server isn't fully functional yet
            server = new EmbeddedHttpServer();
            server.start();
        }
        // The commented line below is meant to separate different test runs into separate folders on the server. 
        // The current challenge is how to do this programatically. MSS tried Apache JackRabbit, but this involved a 
        // lot of additional dependencies which caused inconsistencies in the dependency model (usage of incompatible 
        // SLJ4J API 1.5 and 1.6)
        //config.setHttpServerPath("/dav/" + System.getProperty("user.name") + "/");
        httpServer = new HttpServerConnector(config, testEventManager);
    }

    /** 
     * Creates the collection settings with the default values. The collection settings creation is delegates to the 
     * implementing classes, which might also add extra settings or override the values set from here.
     */
    protected void setupSettings() {
        initCollectionSettings();
        String bitRepositoryCollectionID = System.getProperty("user.name") + "-test-collection";
        settings.setBitRepositoryCollectionID(bitRepositoryCollectionID);
        settings.getProtocol().setCollectionDestination(bitRepositoryCollectionDestinationID);
    }
    
    protected void defineDestinations() {
        bitRepositoryCollectionDestinationID = "BitRepositoryCollection_topic" + getTopicPostfix();
        bitRepositoryCollectionDestination = new MessageReceiver("BitRepositoryCollection topic receiver", testEventManager);
        messageBus.addListener(bitRepositoryCollectionDestinationID, bitRepositoryCollectionDestination.getMessageListener());
    }

    protected void teardownMessageBus() {
        if (useEmbeddedMessageBus()) { 
            try {
                broker.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    protected void teardownHttpServer() throws Exception {
        if (useEmbeddedHttpServer()) {
            server.stop();
        }
    }

    /**
     * Removes references to the message bus, such as listeners.
     */
    @AfterClass(alwaysRun = true)
    public void teardownTest() throws Exception {
        teardownMessageBus(); 
        teardownHttpServer();
    }
    
    /** 
     * Returns the postfix string to use when accessing user specific topics, which is the mechanism we use in the 
     * bit repository tests.
     * @return The string to postfix all topix names with.
     */
    public String getTopicPostfix() {
        return "-" + System.getProperty("user.name");
    }
    
    /**
     * Defines the method for retrieving the the collections settings as implemented by the concrete test case.
     */
    protected abstract void initCollectionSettings();
}
