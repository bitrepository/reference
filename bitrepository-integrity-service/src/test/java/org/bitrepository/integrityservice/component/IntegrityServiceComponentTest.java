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
package org.bitrepository.integrityservice.component;

import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.protocol.bus.MessageReceiver;

/**
 * Contains the generic parts for tests integrating to the message bus. 
 */
public abstract class IntegrityServiceComponentTest extends IntegrationTest {
    protected static MessageReceiver collectionReceiver;

    protected static String pillar1DestinationId;
    protected static MessageReceiver pillar1Receiver;
    protected static final String PILLAR1_ID = "Pillar1";

    protected static String pillar2DestinationId;
    protected static MessageReceiver pillar2Receiver;
    protected static final String PILLAR2_ID = "Pillar2";

    protected ConversationMediator conversationMediator;

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
}
