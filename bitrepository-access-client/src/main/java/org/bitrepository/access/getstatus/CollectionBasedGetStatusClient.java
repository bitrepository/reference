/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getstatus;

import javax.jms.JMSException;

import org.bitrepository.access.getstatus.conversation.SimpleGetStatusConversation;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CollectionBasedGetStatusClient implements GetStatusClient {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The injected settings for the instance */
    private final Settings settings;

    /** The injected messagebus to use */
    private final MessageBus messageBus;
    /** The mediator which should manage the conversations. */
    private final ConversationMediator conversationMediator;
    /** The ID of the client */
    private final String clientID;
    
    /**
     * Constructor
     * @param messageBus the message bus to use.
     * @param conversationMediator the mediator to facilitate message tranmission
     * @param settings the settings to use.  
     */
    public CollectionBasedGetStatusClient(MessageBus messageBus, ConversationMediator conversationMediator, 
            Settings settings, String clientID) {
        ArgumentValidator.checkNotNull(messageBus, "messageBus");
        ArgumentValidator.checkNotNull(settings, "settings");
        this.settings = settings;
        ArgumentValidator.checkNotNullOrEmpty(settings.getCollectionSettings().getGetStatusSettings().getContributorIDs(),
                "ContributorIDs");
        ArgumentValidator.checkNotNullOrEmpty(clientID, "ClientID");
        this.conversationMediator = conversationMediator;
        this.messageBus = messageBus;
        this.clientID = clientID;
    }
    

    @Override
    public void getStatus(EventHandler eventHandler) {
        ArgumentValidator.checkNotNull(eventHandler, "eventHandler");

        log.info("Requesting status for collection of components.");
        SimpleGetStatusConversation conversation = new SimpleGetStatusConversation(messageBus, 
                settings, settings.getCollectionSettings().getGetStatusSettings().getContributorIDs(), 
                clientID, eventHandler, new FlowController(settings));
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
