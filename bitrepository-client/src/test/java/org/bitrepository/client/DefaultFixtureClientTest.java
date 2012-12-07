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
package org.bitrepository.client;

import java.lang.reflect.Method;
import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.message.ClientTestMessageFactory;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/**
 * Contains the generic parts for tests integrating to the message bus. 
 */
public abstract class DefaultFixtureClientTest extends IntegrationTest {
    protected static final String DEFAULT_FILE_ID = ClientTestMessageFactory.FILE_ID_DEFAULT;

    protected static String clientDestinationId;
    protected MessageReceiver clientReceiver;

    protected static String pillar1DestinationId;
    protected MessageReceiver pillar1Receiver;
    protected static final String PILLAR1_ID = "Pillar1";

    protected static String pillar2DestinationId;
    protected MessageReceiver pillar2Receiver;
    protected static final String PILLAR2_ID = "Pillar2";

    protected static ConversationMediator conversationMediator;

    /**
     *
     * @param method Injected by TestNG. Used for logging purposes, eg. to make it visible which method has been
     *               at work.
     */
    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(Method method) {
        super.beforeMethod(method);
        renewConversationMediator();
    }
    @AfterMethod(alwaysRun = true)
    public void afterMethod() {
        conversationMediator.shutdown();
        super.afterMethod();
    }

    /**
     * Indicated whether the embedded mockup pillars are going to be used in the test (means the test is run as a client 
     * component test, or if external pillar are going to be used. If external pillar are going to be used they need 
     * to be started before running the test, and have the following configuration: <ul>
     * <li>The pillar should contain one file, the {@link org.bitrepository.protocol.message.ClientTestMessageFactory#FILE_ID_DEFAULT} file. The c
     */
    public boolean useMockupPillar() {
        return System.getProperty("useMockupPillar", "true").equals("true");
    }

    /** Indicated whether reference pillars should be started should be started and used. Note that mockup pillars 
     * should be used in this case, e.g. the useMockupPillar() call should return false. */ 
    public boolean useEmbeddedReferencePillars() {
        return System.getProperty("useEmbeddedReferencePillars", "false").equals("true");
    }

    @Override
    protected void checkNoMessagesRemain() {
        clientReceiver.checkNoMessagesRemain();
        pillar1Receiver.checkNoMessagesRemain();
        pillar2Receiver.checkNoMessagesRemain();
    }

    @Override
    protected void teardownMessageBusListeners() {
        IntegrationTest.messageBus.removeListener(clientDestinationId, clientReceiver.getMessageListener());
        IntegrationTest.messageBus.removeListener(pillar1DestinationId, pillar1Receiver.getMessageListener());
        IntegrationTest.messageBus.removeListener(pillar2DestinationId, pillar2Receiver.getMessageListener());
        super.teardownMessageBusListeners();
    }
    
    @Override
    protected void initializeMessageBusListeners() {
        super.initializeMessageBusListeners();
        clientDestinationId = IntegrationTest.settingsForCUT.getReceiverDestinationID();
        pillar1DestinationId = "Pillar1_topic" + getTopicPostfix();
        pillar2DestinationId = "Pillar2_topic" + getTopicPostfix();
        
        clientReceiver = new MessageReceiver("Client topic receiver", IntegrationTest.testEventManager);
        pillar1Receiver = new MessageReceiver("Pillar1 topic receiver", IntegrationTest.testEventManager);
        pillar2Receiver = new MessageReceiver("Pillar2 topic receiver", IntegrationTest.testEventManager);
        IntegrationTest.messageBus.addListener(clientDestinationId, clientReceiver.getMessageListener());
        IntegrationTest.messageBus.addListener(pillar1DestinationId, pillar1Receiver.getMessageListener());
        IntegrationTest.messageBus.addListener(pillar2DestinationId, pillar2Receiver.getMessageListener());
    }

    /**
     * Used for creating a new conversationMediator between tests, and for tests needing to use a differently configured
     * mediator.
     */
    protected void renewConversationMediator() {
        if (conversationMediator != null) {
            conversationMediator.shutdown();
        }
        conversationMediator = new CollectionBasedConversationMediator(settingsForCUT, securityManager);
    }
}
