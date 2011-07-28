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
import java.util.Timer;
import java.util.TimerTask;

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
import org.bitrepository.protocol.bitrepositorycollection.ClientSettings;
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
    /** The injected settings defining the mediator behavior */
    private final ClientSettings settings;
    /** The timer used to schedule cleaning of conversations.
     * @see ConversationCleaner 
     */
    private final Timer cleanTimer;

    /**
     * Create a mediator that handles conversations and mediates messages sent on the
     * given destination on the given messagebus.
     *
     * @param settings The general client settings.
     * @param messagebus The message bus to mediate messages on.
     * @param listenerDestination The destinations to mediate messages for.
     */
    public CollectionBasedConversationMediator(
            ClientSettings settings, 
            MessageBus messagebus,
            String listenerDestination) {
        log.debug("Initializing the CollectionBasedConversationMediator");
        this.conversations = Collections.synchronizedMap(new HashMap<String, T>());
        messagebus.addListener(listenerDestination, this);
        this.settings = settings;
        cleanTimer = new Timer(true);
        cleanTimer.scheduleAtFixedRate(new ConversationCleaner(), 0, settings.getMediatorCleanInterval());
    }

    @Override
    public void addConversation(T conversation) {
        conversations.put(conversation.getConversationID(), conversation);
    }

    /**
     * Will try to fail a conversation gracefully. This entitles: <ul>
     * <li> Removing the conversation from the list of conversations.
     * <li> Attempt to call the failConversation operation on the conversation. The call is made in a separate thread to 
     * avoid having the failing conversation blocking the calling thread.
     * @param conversation The conversation to fail.
     * @param message A message describing the failure symptoms.
     */
    private void failConversation(final Conversation conversation, final String message) {
        String conversationID = conversation.getConversationID();
        if (conversationID != null) {
            conversations.remove(conversationID);
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    conversation.failConversion(message);
                }
            });
            t.start();
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
    
    /**
     * Will clean out obsolete conversations in each run. An obsolete conversation is a conversation which satisfies on 
     * of the following criterias: <ol>
     * <li> Returns true for the <code>hasEnded()</code> method.
     * <li> Is older than the conversationTImeout limit allows.
     * </ol>
     * 
     * A copy of the current conversations is created before running through the conversations to avoid having to lock 
     * the conversations map while cleaning.
     */
    private final class ConversationCleaner extends TimerTask {
        @SuppressWarnings("rawtypes")
        @Override
        public void run() {
            Conversation[] conversationArray = 
                conversations.values().toArray(new Conversation[conversations.size()]);
            long currentTime = System.currentTimeMillis();
            for (Conversation conversation: conversationArray) {
                if (conversation.hasEnded()) { 
                    conversations.remove(conversation.getConversationID());
                } else if (currentTime - conversation.getStartTime() > settings.getConversationTimeout()) {
                    log.warn("Failing timed out conversation " + conversation.getConversationID());
                    failConversation(conversation, 
                            "Failing timed out conversation " + conversation.getConversationID());
                }
            }
        }       
    }
}
