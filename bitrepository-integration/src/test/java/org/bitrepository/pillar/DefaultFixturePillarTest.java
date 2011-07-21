/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id: DefaultFixtureClientTest.java 209 2011-07-04 19:38:34Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-protocol/src/test/java/org/bitrepository/clienttest/DefaultFixtureClientTest.java $
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
package org.bitrepository.pillar;

import java.util.Date;

import org.bitrepository.clienttest.MessageReceiver;
import org.bitrepository.common.IntegrationTest;
import org.bitrepository.protocol.LocalActiveMQBroker;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.TestMessageFactory;
import org.bitrepository.protocol.bitrepositorycollection.ClientSettings;
import org.bitrepository.protocol.bitrepositorycollection.MutableClientSettings;
import org.bitrepository.protocol.bus.MessageBusConfigurationFactory;
import org.bitrepository.protocol.bus.MessageBusWrapper;
import org.bitrepository.protocol.configuration.MessageBusConfigurations;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
import org.bitrepository.protocol.fileexchange.HttpServerConnector;
import org.bitrepository.protocol.http.EmbeddedHttpServer;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageBusFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Contains the generic parts for pillar tests integrating to the message bus. 
 * Mostly copied from DefaultFixtureClientTest...
 */
public abstract class DefaultFixturePillarTest extends IntegrationTest {
    protected MessageBus messageBus;
    protected static final String DEFAULT_FILE_ID = TestMessageFactory.FILE_ID_DEFAULT;

    protected static String pillarDestinationId;
    protected MessageReceiver pillarTopic;
    
    protected static String clientDestinationId;
    protected MessageReceiver clientTopic;

    protected static String bitRepositoryCollectionDestinationID;
    protected MessageReceiver bitRepositoryCollectionDestination; 

    protected PillarSettings settings;
    protected MessageBusConfigurations messageBusConfigurations;

    protected LocalActiveMQBroker broker;
    private EmbeddedHttpServer server;

    protected HttpServerConnector httpServer;

    /** Indicated whether an embedded active MQ should be started and used */ 
    public boolean useEmbeddedMessageBus() {
        return System.getProperty("useEmbeddedMessageBus", "false").equals("true");
    }

    /** Indicated whether an embedded http server should be started and used */ 
    public boolean useEmbeddedHttpServer() {
        return System.getProperty("useEmbeddedHttpServer", "false").equals("true");
    }

    /** Indicated whether reference pillars should be started should be started and used. Note that mockup pillars 
     * should be used in this case, e.g. the useMockupPillar() call should return false. */ 
    public boolean useEmbeddedReferencePillars() {
        return System.getProperty("useEmbeddedReferencePillars", "false").equals("true");
    }

    @BeforeClass (alwaysRun = true)
    public void setupTest() throws Exception {
        defineDestinations();
        initializeMessageBus();
        initializeHttpServer();
    }

    /**
     * Defines the standard BitRepositoryCollection configuration
     * @param testMethod Used to grap the name of the test method used for naming.
     */
    @BeforeMethod (alwaysRun = true)
    public void beforeMethodSetup(java.lang.reflect.Method testMethod) {
        configureBitRepositoryCollectionConfig(testMethod.getName());
    }

    /**
     * Removes references to the message bus, such as listeners.
     * @throws Exception 
     */
    @AfterClass (alwaysRun = true)
    public void teardownTest() throws Exception {
        disconnectFromMessageBus(); 
        shutdownHttpServer();
    }

    /**
     * 
     */
    private void initializeMessageBus() throws Exception {
        if (useEmbeddedMessageBus()) { 
            messageBusConfigurations = MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration();
            broker = new LocalActiveMQBroker(messageBusConfigurations.getPrimaryMessageBusConfiguration());
            broker.start();
            messageBus = new MessageBusWrapper(MessageBusFactory.createMessageBus(messageBusConfigurations), testEventManager);
        } else {
            messageBusConfigurations = MessageBusConfigurationFactory.createDefaultConfiguration();
            messageBus = new MessageBusWrapper(ProtocolComponentFactory.getInstance().getMessageBus(), testEventManager);
        }
        pillarTopic = new MessageReceiver("pillar topic receiver", testEventManager);
        bitRepositoryCollectionDestination = new MessageReceiver("BitRepositoryCollection topic receiver", testEventManager);
        clientTopic = new MessageReceiver("client topic receiver", testEventManager);
        messageBus.addListener(pillarDestinationId, pillarTopic.getMessageListener());    
        messageBus.addListener(bitRepositoryCollectionDestinationID, bitRepositoryCollectionDestination.getMessageListener());
        messageBus.addListener(clientDestinationId, clientTopic.getMessageListener());    
    }

    private void initializeHttpServer() throws Exception {
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

    private void defineDestinations() {
        String topicPostfix = "-" + System.getProperty("user.name") + "-" + new Date().getTime();
        pillarDestinationId = "pillar_topic" + topicPostfix;
        bitRepositoryCollectionDestinationID = "BitRepositoryCollection_topic" + topicPostfix;
        clientDestinationId = "client" + topicPostfix;
    }

    private void configureBitRepositoryCollectionConfig(String testName) {
        String bitRepositoryCollectionID = testName + "-" + System.getProperty("user.name") + "-" + new Date().getTime();
        MutablePillarSettings pillarSettings = new MutablePillarSettings();
        pillarSettings.setBitRepositoryCollectionID(bitRepositoryCollectionID);
        pillarSettings.setBitRepositoryCollectionTopicID(bitRepositoryCollectionDestinationID);
        pillarSettings.setMessageBusConfiguration(messageBusConfigurations);
        pillarSettings.setFileDirName("target/fileDir");
        pillarSettings.setLocalQueue(pillarDestinationId);
        pillarSettings.setTimeToDownloadMeasure("MILLISECONDS");
        pillarSettings.setTimeToDownloadValue(1L);
        pillarSettings.setTimeToUploadMeasure("MILLISECONDS");
        pillarSettings.setTimeToUploadValue(1L);
        settings = pillarSettings;
    }

    private void disconnectFromMessageBus() {
        messageBus.removeListener(pillarDestinationId, pillarTopic.getMessageListener());
        messageBus.removeListener(bitRepositoryCollectionDestinationID, bitRepositoryCollectionDestination.getMessageListener());

        if (useEmbeddedMessageBus()) { 
            try {
                broker.stop();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void shutdownHttpServer() throws Exception {
        if (useEmbeddedHttpServer()) {
            server.stop();
        }
    }
}
