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

import org.bitrepository.client.conversation.mediator.CollectionBasedConversationMediator;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.bus.MessageReceiver;
import org.bitrepository.protocol.message.ClientTestMessageFactory;
import org.bitrepository.protocol.messagebus.MessageBusManager;

/**
 * Contains the generic parts for tests integrating to the message bus. 
 */
public abstract class DefaultFixtureClientTest extends IntegrationTest {
    protected static final String DEFAULT_FILE_ID = ClientTestMessageFactory.FILE_ID_DEFAULT;

    protected static MessageReceiver collectionReceiver;

    protected static String pillar1DestinationId;
    protected static MessageReceiver pillar1Receiver;
    protected static final String PILLAR1_ID = "Pillar1";

    protected static String pillar2DestinationId;
    protected static MessageReceiver pillar2Receiver;
    protected static final String PILLAR2_ID = "Pillar2";

    protected ConversationMediator conversationMediator;

    @Override
    protected void initializeCUT() {
        MessageBusManager.injectCustomMessageBus(MessageBusManager.DEFAULT_MESSAGE_BUS, messageBus);
        renewConversationMediator();
    }
    @Override
    protected void shutdownCUT() {
        shutdownConversationMediator();
    }

    @Override
    protected void registerMessageReceivers() {
        super.registerMessageReceivers();

        collectionReceiver = new MessageReceiver(settingsForCUT.getCollectionDestination(), testEventManager);
        addReceiver(collectionReceiver);

        pillar1DestinationId = "Pillar1_topic" + getTopicPostfix();
        pillar1Receiver = new MessageReceiver(pillar1DestinationId, testEventManager);
        addReceiver(pillar1Receiver);

        pillar2DestinationId = "Pillar2_topic" + getTopicPostfix();
        pillar2Receiver = new MessageReceiver(pillar2DestinationId, testEventManager);
        addReceiver(pillar2Receiver);
    }

    /**
     * Used for creating a new conversationMediator between tests, and for tests needing to use a differently configured
     * mediator.
     */
    protected void renewConversationMediator() {
        if (conversationMediator != null) {
            conversationMediator.close();
        }
        conversationMediator = new CollectionBasedConversationMediator(settingsForCUT, securityManager);
    }

    private void shutdownConversationMediator() {
        if (conversationMediator != null) {
            conversationMediator.close();
        }
        conversationMediator = null;
    }
}
