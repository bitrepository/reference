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
package org.bitrepository.protocol;

import org.bitrepository.bitrepositorymessages.GetChecksumsComplete;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.GetFileComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsComplete;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileComplete;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.bitrepositorymessages.PutFileResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Conversation handler that delegates messages to registered conversations.
 *
 * TODO: There is no persistence in this implementation. Should there be?
 */
public class CollectionBasedConversationMediator<T extends Conversation> implements ConversationMediator<T> {
    /** Logger for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The factory for generating conversations. */
    private final ConversationFactory<T> conversationFactory;
    /** Registered conversations, mapping from correlation ID to conversation. */
    private final Map<String, T> conversations;

    /**
     * Initiate a mediator, that generates conversations with the given factory, and mediates messages sent on the
     * given destination on the given messagebus.
     *
     * @param conversationFactory The factory that generates conversations.
     * @param messagebus The message bus to mediate messages on.
     * @param listenerDestination The destinations to mediate messages for.
     */
    public CollectionBasedConversationMediator(ConversationFactory<T> conversationFactory, MessageBus messagebus,
                                               String listenerDestination) {
        log.debug("Initializing the CollectionBasedConversationMediator");
        this.conversationFactory = conversationFactory;
        this.conversations = Collections.synchronizedMap(new HashMap<String, T>());
        messagebus.addListener(listenerDestination, this);
    }

    @Override
    public T startConversation() {
        T conversation = conversationFactory.createConversation();
        conversation.setMediator(this);
        conversations.put(conversation.getConversationID(), conversation);
        return conversation;
    }

    @Override
    public synchronized void endConversation(T conversation) {
        String conversationID = conversation.getConversationID();
        if (conversationID != null) {
            conversations.remove(conversationID);
        }
    }

    @Override
    public void onMessage(GetChecksumsComplete message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetChecksumsRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetChecksumsResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileComplete message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileIDsComplete message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileIDsRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileIDsResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID'" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(PutFileComplete message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(PutFileRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(PutFileResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

}
