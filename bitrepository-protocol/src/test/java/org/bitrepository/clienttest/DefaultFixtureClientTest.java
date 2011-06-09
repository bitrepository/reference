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
import org.bitrepository.common.bitrepositorycollection.ClientSettings;
import org.bitrepository.common.bitrepositorycollection.MutableClientSettings;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.TestMessageFactory;
import org.bitrepository.protocol.bus.MessageBusWrapper;
import org.bitrepository.protocol.fileexchange.HttpServerConfiguration;
import org.bitrepository.protocol.fileexchange.HTTPServer;
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
    
    protected static String slaTopicId;
    protected MessageReceiver slaTopic; 
    
    protected static String pillar1TopicId;
    protected MessageReceiver pillar1Topic; 
    protected static final String PILLAR1_ID = "Pillar1";
    
    protected static String pillar2TopicId;
    protected MessageReceiver pillar2Topic; 
    protected static final String PILLAR2_ID = "Pillar2";
    
    protected ClientSettings slaConfiguration;
    
    private boolean useMockupPillar = true;
    
    protected HTTPServer httpServer;

    @BeforeClass (alwaysRun = true)
    public void setupTest() {
        defineTopics();
        if (useMockupPillar) {
            hookupMessageBus();
        }
        hookupHttpServer();
    }

    @BeforeMethod (alwaysRun = true)
    public void beforeMethodSetup(java.lang.reflect.Method testMethod) {
        configureSLA(testMethod.getName());
    }

    @AfterClass (alwaysRun = true)
    public void teardownTest() {
        if (useMockupPillar) {
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
        return useMockupPillar;
    }

    private void hookupMessageBus() {
        messageBus = new MessageBusWrapper(ProtocolComponentFactory.getInstance().getMessageBus(), testEventManager);
        clientTopic = new MessageReceiver("Client topic receiver", testEventManager);
        slaTopic = new MessageReceiver("SLA topic receiver", testEventManager);
        pillar1Topic = new MessageReceiver("Pillar1 topic receiver", testEventManager);
        pillar2Topic = new MessageReceiver("Pillar2 topic receiver", testEventManager);
        messageBus.addListener(clientTopicId, clientTopic.getMessageListener());    
        messageBus.addListener(slaTopicId, slaTopic.getMessageListener());    
        messageBus.addListener(pillar1TopicId, pillar1Topic.getMessageListener());  
        messageBus.addListener(pillar2TopicId, pillar2Topic.getMessageListener());       
    }
    
    private void hookupHttpServer() {
        HttpServerConfiguration config = new HttpServerConfiguration();
        config.setHttpServerPath("/dav/" + System.getProperty("user.name"));
        httpServer = new HTTPServer(new HttpServerConfiguration(), testEventManager);
    }

    private void defineTopics() {
        String topicPostfix = "-" + System.getProperty("user.name") + "-" + new Date().getTime();
        clientTopicId = "Client_topic" + topicPostfix;
        slaTopicId = "SLA_topic" + topicPostfix;
        pillar1TopicId = "Pillar1_topic" + topicPostfix;
        pillar2TopicId = "Pillar2_topic" + topicPostfix;
    }

    private void configureSLA(String testName) {
        String slaID = testName + "-" + System.getProperty("user.name") + "-" + new Date().getTime();
        MutableClientSettings mutableSLAConfiguration = new MutableClientSettings();
        mutableSLAConfiguration.setId(slaID);
        mutableSLAConfiguration.setClientTopicId(clientTopicId);
        mutableSLAConfiguration.setSlaTopicId(slaTopicId);
        mutableSLAConfiguration.setLocalFileStorage("target/fileDir");
        slaConfiguration = mutableSLAConfiguration;
    }

    private void disconnectFromMessageBus() {
        messageBus.removeListener(clientTopicId, clientTopic.getMessageListener());
        messageBus.removeListener(slaTopicId, slaTopic.getMessageListener());
        messageBus.removeListener(pillar1TopicId, pillar1Topic.getMessageListener());
        messageBus.removeListener(pillar2TopicId, pillar2Topic.getMessageListener());
    }
}
