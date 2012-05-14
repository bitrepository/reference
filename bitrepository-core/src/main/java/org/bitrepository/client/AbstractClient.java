/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.client.conversation.Conversation;
import org.bitrepository.client.conversation.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;

/**
 * Implements the generic functionality for a reference client
 */
public class AbstractClient implements BitrepositoryClient {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The settings for this instance.*/
    protected final Settings settings;
    /** The messagebus for communication.*/
    protected final MessageBus messageBus;
    /** The mediator which should manage the conversations. */
    protected final String clientID;
    /** The mediator which should manage the conversations. */
    private final ConversationMediator conversationMediator;

    
    public AbstractClient(Settings settings, ConversationMediator conversationMediator, MessageBus messageBus, String clientID) {
        ArgumentValidator.checkNotNull(clientID, "clientID");
        this.settings = settings;
        this.messageBus = messageBus;
        this.conversationMediator = conversationMediator;
        this.clientID = clientID;
        settings.setComponentID(clientID);
    }
    
    protected void startConversation(Conversation conversation) {
        conversationMediator.addConversation(conversation);
        conversation.startConversation();
    }

    @Override
    public void shutdown() {
        try {
            messageBus.close();
            // TODO Kill any lingering timer threads
        } catch (JMSException e) {
            log.info("Error during shutdown of messagebus ", e);
        }
    }

}
