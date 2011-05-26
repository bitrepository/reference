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

import javax.swing.JFrame;

import org.bitrepository.common.sla.MutableSLAConfiguration;
import org.bitrepository.common.sla.SLAConfiguration;
import org.bitrepository.protocol.MessageBus;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.jaccept.TestEventManager;
import org.jaccept.gui.ComponentTestFrame;
import org.jaccept.structure.ExtendedTestCase;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeTest;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.util.StatusPrinter;

/**
 * Contains the generic parts for tests integrating to the message bus. 
 * ToDo: pillar concerns should be separated into a TestPillarClass.
 */
public abstract class ClientTest extends ExtendedTestCase {
    //ToDo convert to TestLogger
    protected Logger log = LoggerFactory.getLogger(getClass());

    protected MessageBus messageBus;
    protected static final String DEFAULT_FILE_ID = "FileID";
    
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
    
    protected SLAConfiguration slaConfiguration;
    protected TestEventManager testEventManager = TestEventManager.getInstance();
    protected boolean useMockupPillar;

    @BeforeTest (alwaysRun = true)
    public void startTestGUI() {  
        
        if (System.getProperty("enableLogStatus", "false").equals("true")) {
            LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
            StatusPrinter.print(lc);
        }

        defineTopics();
        // Experimental, use at own risk.
        if (System.getProperty("enableTestGUI", "false").equals("true") ) {
            JFrame hmi = new ComponentTestFrame();
            hmi.pack();
            hmi.setVisible(true);
        }
        
        hookupMessageBus();
    }

    @BeforeMethod (alwaysRun = true)
    public void setupMethod(java.lang.reflect.Method testMethod) {
        configureSLA(testMethod.getName());
    }

    @AfterTest (alwaysRun = true)
    public void teardownTest() {
        disconnectFromMessageBus();
    }

    private void hookupMessageBus() {
        System.out.println("Hooking up meesagebus");
        messageBus = ProtocolComponentFactory.getInstance().getMessageBus();
        clientTopic = new MessageReceiver("Client topic receiver", testEventManager);
        slaTopic = new MessageReceiver("SLA topic receiver", testEventManager);
        pillar1Topic = new MessageReceiver("Pillar1 topic receiver", testEventManager);
        pillar2Topic = new MessageReceiver("Pillar2 topic receiver", testEventManager);
        messageBus.addListener(clientTopicId, clientTopic.getMessageListener());    
        messageBus.addListener(slaTopicId, slaTopic.getMessageListener());    
        messageBus.addListener(pillar1TopicId, pillar1Topic.getMessageListener());  
        messageBus.addListener(pillar2TopicId, pillar2Topic.getMessageListener());       
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
        MutableSLAConfiguration mutableSLAConfiguration = new MutableSLAConfiguration();
        mutableSLAConfiguration.setSlaId(slaID);
        mutableSLAConfiguration.setClientTopicId(clientTopicId);
        mutableSLAConfiguration.setSlaTopicId(slaTopicId);
        mutableSLAConfiguration.setLocalFileStorage("fileDir");
        slaConfiguration = mutableSLAConfiguration;
    }

    private void disconnectFromMessageBus() {
        messageBus.removeListener(clientTopicId, clientTopic.getMessageListener());
        messageBus.removeListener(slaTopicId, slaTopic.getMessageListener());
        messageBus.removeListener(pillar1TopicId, pillar1Topic.getMessageListener());
        messageBus.removeListener(pillar2TopicId, pillar2Topic.getMessageListener());
    }
}
