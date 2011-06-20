/*
 * #%L
 * Bitrepository Protocol
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
package org.bitrepository.clienttest;

import java.util.Date;

import org.bitrepository.common.IntegrationTest;
import org.bitrepository.protocol.LocalActiveMQBroker;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.TestMessageFactory;
import org.bitrepository.protocol.bitrepositorycollection.ClientSettings;
import org.bitrepository.protocol.bitrepositorycollection.MutableClientSettings;
import org.bitrepository.protocol.bus.MessageBusConfigurationFactory;
import org.bitrepository.protocol.bus.MessageBusWrapper;
import org.bitrepository.protocol.configuration.MessageBusConfigurations;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
import org.bitrepository.protocol.fileexchange.HttpServerConnector;
import org.bitrepository.protocol.messagebus.MessageBusFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;

/**
 * Contains the generic parts for tests integrating to the message bus. 
 * ToDo: pillar concerns should be separated into a TestPillarClass.
 */
public abstract class DefaultFixtureClientTest extends IntegrationTest {
    protected MessageBus messageBus;
    protected static final String DEFAULT_FILE_ID = TestMessageFactory.FILE_ID_DEFAULT;
    
    protected static String clientTopicId;
    protected MessageReceiver clientTopic;
    
    protected static String bitRepositoryCollectionTopicID;
    protected MessageReceiver bitRepositoryCollectionTopic; 
    
    protected static String pillar1TopicId;
    protected MessageReceiver pillar1Topic; 
    protected static final String PILLAR1_ID = "Pillar1";
    
    protected static String pillar2TopicId;
    protected MessageReceiver pillar2Topic; 
    protected static final String PILLAR2_ID = "Pillar2";
    
    protected ClientSettings settings;
    protected MessageBusConfigurations messageBusConfigurations;
    
    protected LocalActiveMQBroker broker;
    
    protected HttpServerConnector httpServer;

    @BeforeClass (alwaysRun = true)
    public void setupTest() throws Exception {
        defineTopics();
        if (useMockupPillar()) {
            initializeMessageBus();
        }
        initializeHttpServer();
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethodSetup(java.lang.reflect.Method testMethod) {
        configureBitRepositoryCollectionConfig(testMethod.getName());
    }

    @AfterClass (alwaysRun = true)
    public void teardownTest() {
        if (useMockupPillar()) {
            disconnectFromMessageBus(); 
        }
    }
    
    /**
     * Indicated whether the embedded mockup pillars are going to be used in the test (means the test is run as a client 
     * component test, or if external pillar are going to be used. If external pillar are going to be used they need 
     * to be started before running the test, and have the following configuration: <ul>
     * <li>The pillar should contain one file, the {@link TestMessageFactory#FILE_ID_DEFAULT} file. The c
     */
    public boolean useMockupPillar() {
        return System.getProperty("useMockupPillar", "true").equals("true");
    }

    /** Indicated whether an embedded avtive MQ should be started and used */ 
    public boolean useEmbeddedMessageBus() {
        return System.getProperty("useEmbeddedMessageBus", "false").equals("true");
    }
    
    private void initializeMessageBus() throws Exception {
    	if (useEmbeddedMessageBus()) { 
    		messageBusConfigurations = MessageBusConfigurationFactory.createEmbeddedMessageBusConfiguration();
    		broker = new LocalActiveMQBroker(messageBusConfigurations.getPrimaryMessageBusConfiguration());
    		broker.start();
    		new MessageBusWrapper(messageBus = MessageBusFactory.createMessageBus(messageBusConfigurations), testEventManager);
    	} else {
    		messageBusConfigurations = MessageBusConfigurationFactory.createDefaultConfiguration();
    		messageBus = new MessageBusWrapper(ProtocolComponentFactory.getInstance().getMessageBus(), testEventManager);
    	}
        clientTopic = new MessageReceiver("Client topic receiver", testEventManager);
        bitRepositoryCollectionTopic = new MessageReceiver("BitRepositoryCollection topic receiver", testEventManager);
        pillar1Topic = new MessageReceiver("Pillar1 topic receiver", testEventManager);
        pillar2Topic = new MessageReceiver("Pillar2 topic receiver", testEventManager);
        messageBus.addListener(clientTopicId, clientTopic.getMessageListener());    
        messageBus.addListener(bitRepositoryCollectionTopicID, bitRepositoryCollectionTopic.getMessageListener());    
        messageBus.addListener(pillar1TopicId, pillar1Topic.getMessageListener());  
        messageBus.addListener(pillar2TopicId, pillar2Topic.getMessageListener());       
    }
    
    private void initializeHttpServer() {
        HttpServerConfiguration config = new HttpServerConfiguration();
        // The commented line below is meant to separate different testsruns into separate folders on the server. The current 
        // challenge is howto do this programmatically. MSS tried Apache JackRabbit, but this invloved a lot of 
        // additional dependencies which caused inconsistencies in the dependency model (usage of incompatible 
        // SLJ4J API 1.5 and 1.6)
        //config.setHttpServerPath("/dav/" + System.getProperty("user.name") + "/");
        httpServer = new HttpServerConnector(config, testEventManager);
    }

    private void defineTopics() {
        String topicPostfix = "-" + System.getProperty("user.name") + "-" + new Date().getTime();
        clientTopicId = "Client_topic" + topicPostfix;
        bitRepositoryCollectionTopicID = "BitRepositoryCollection_topic" + topicPostfix;
        pillar1TopicId = "Pillar1_topic" + topicPostfix;
        pillar2TopicId = "Pillar2_topic" + topicPostfix;
    }

    private void configureBitRepositoryCollectionConfig(String testName) {
        String bitRepositoryCollectionID = testName + "-" + System.getProperty("user.name") + "-" + new Date().getTime();
        MutableClientSettings clientSettings = new MutableClientSettings();
        clientSettings.setBitRepositoryCollectionID(bitRepositoryCollectionID);
        clientSettings.setClientTopicID(clientTopicId);
        clientSettings.setBitRepositoryCollectionTopicID(bitRepositoryCollectionTopicID);
        clientSettings.setLocalFileStorage("target/fileDir");
        clientSettings.setMessageBusConfiguration(messageBusConfigurations);
        settings = clientSettings;
    }

    private void disconnectFromMessageBus() {
        messageBus.removeListener(clientTopicId, clientTopic.getMessageListener());
        messageBus.removeListener(bitRepositoryCollectionTopicID, bitRepositoryCollectionTopic.getMessageListener());
        messageBus.removeListener(pillar1TopicId, pillar1Topic.getMessageListener());
        messageBus.removeListener(pillar2TopicId, pillar2Topic.getMessageListener());

    	if (useEmbeddedMessageBus()) { 
    		try {
				broker.stop();
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    }
}
