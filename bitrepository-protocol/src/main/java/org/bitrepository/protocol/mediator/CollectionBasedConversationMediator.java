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
package org.bitrepository.protocol.mediator;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.protocol.conversation.Conversation;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Conversation handler that delegates messages to registered conversations.
 *
 * TODO: There is no persistence in this implementation. Should there be?
 */
public class CollectionBasedConversationMediator<T extends Conversation> implements ConversationMediator<T> {
    /** Logger for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** Registered conversations, mapping from correlation ID to conversation. */
    private final Map<String, T> conversations;

    /**
     * Create a mediator that handles conversations and mediates messages sent on the
     * given destination on the given messagebus.
     *
     * @param conversationFactory The factory that generates conversations.
     * @param messagebus The message bus to mediate messages on.
     * @param listenerDestination The destinations to mediate messages for.
     */
    public CollectionBasedConversationMediator(MessageBus messagebus,
                                               String listenerDestination) {
        log.debug("Initializing the CollectionBasedConversationMediator");
        this.conversations = Collections.synchronizedMap(new HashMap<String, T>());
        messagebus.addListener(listenerDestination, this);
    }

    @Override
    public void addConversation(T conversation) {
        conversation.setMediator(this);
        conversations.put(conversation.getConversationID(), conversation);
    }

    @Override
    public synchronized void endConversation(T conversation) {
        String conversationID = conversation.getConversationID();
        if (conversationID != null) {
            conversations.remove(conversationID);
        }
    }

    @Override
    public void onMessage(Alarm message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }
    
    @Override
    public void onMessage(GetAuditTrailsFinalResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }
    
    @Override
    public void onMessage(GetAuditTrailsRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }
    
    @Override
    public void onMessage(GetAuditTrailsProgressResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }
    
    @Override
    public void onMessage(GetChecksumsFinalResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }

    @Override
    public void onMessage(GetChecksumsRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }

    @Override
    public void onMessage(GetChecksumsProgressResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }

    @Override
    public void onMessage(GetFileFinalResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }

    @Override
    public void onMessage(GetFileIDsFinalResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }

    @Override
    public void onMessage(GetFileIDsRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }

    @Override
    public void onMessage(GetFileIDsProgressResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }

    @Override
    public void onMessage(GetFileRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any " +
            		"conversation.");
        }
    }

    @Override
    public void onMessage(GetFileProgressResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any "
            		+ "conversation.");
        }
    }

    @Override
    public void onMessage(GetStatusRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any "
            		+ "conversation.");
        }
    }

    @Override
    public void onMessage(GetStatusProgressResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any "
            		+ "conversation.");
        }
    }

    @Override
    public void onMessage(GetStatusFinalResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message with correlationID '" + messageCorrelationID + "' could not be delegated to any "
            		+ "conversation.");
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
    public void onMessage(PutFileFinalResponse message) {
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
    public void onMessage(PutFileProgressResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

}
