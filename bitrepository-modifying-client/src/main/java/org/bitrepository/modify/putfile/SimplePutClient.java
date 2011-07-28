/*
 * #%L
 * Bitmagasin modify client
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.modify.putfile;

import java.math.BigInteger;
import java.net.URL;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.modify.putfile.conversation.SimplePutFileConversation;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.mediator.CollectionBasedConversationMediator;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple implementation of the PutClient.
 *
 * TODO perhaps merge the 'outstanding' and the 'FileIdForPut'?
 */
public class SimplePutClient implements PutClient {
    /** The log for this class.*/
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    /** The mediator for the conversations for the PutFileClient.*/
    private final ConversationMediator<SimplePutFileConversation> conversationMediator;
    /** The message bus for communication.*/
    private final MessageBus bus;
    /** The settings. */
    private PutFileClientSettings settings;

    /**
     * Constructor.
     * @param messageBus The messagebus for communication.
     * @param settings The configurations and settings.
     */
    public SimplePutClient(MessageBus messageBus, PutFileClientSettings settings) {
        this.conversationMediator = new CollectionBasedConversationMediator<SimplePutFileConversation>(settings, messageBus,
                settings.getClientTopicID());
        this.bus = messageBus;
        this.settings = settings;
    }

    @Override
    public void putFileWithId(URL url, String fileId, Long sizeOfFile, EventHandler eventHandler) 
            throws OperationFailedException {
        ArgumentValidator.checkNotNull(url, "URL url");
        ArgumentValidator.checkNotNull(fileId, "String fileId");
        ArgumentValidator.checkNotNull(sizeOfFile, "Long sizeOfFile");
        
        SimplePutFileConversation conversation = new SimplePutFileConversation(bus, settings, url, fileId, 
                BigInteger.valueOf(sizeOfFile), eventHandler);
        conversationMediator.addConversation(conversation);
        conversation.startConversion();
    }
}
