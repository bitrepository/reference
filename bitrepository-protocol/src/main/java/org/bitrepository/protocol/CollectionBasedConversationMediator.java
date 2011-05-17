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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
    /** Registered conversations, that did not have a correlation ID at last inspection. */
    private final Collection<T> idLessConversations;

    /**
     * Initiate a mediator, that generates conversations with the given factory, and mediates messages sent on the
     * given destination on the given messagebus.
     *
     * @param conversationFactory The factory that generates conversations.
     * @param messagebus The message bus to mediate messages on.
     * @param listenerDestination The destinations to mediate messages for.
     */
    public CollectionBasedConversationMediator(ConversationFactory conversationFactory, MessageBus messagebus,
                                               String listenerDestination) {
        this.conversationFactory = conversationFactory;
        this.conversations = new HashMap<String, T>();
        this.idLessConversations = new HashSet<T>();
        messagebus.addListener(listenerDestination, this);
    }

    @Override
    public T startConversation() {
        T conversation = conversationFactory.createConversation();
        conversation.setMediator(this);
        synchronized (this) {
            String conversationID = conversation.getConversationID();
            if (conversationID != null) {
                conversations.put(conversationID, conversation);
            } else {
                idLessConversations.add(conversation);
            }
        }
        return conversation;
    }

    @Override
    public synchronized void endConversation(T conversation) {
        String conversationID = conversation.getConversationID();
        if (conversationID != null) {
            conversations.remove(conversationID);
        }
        idLessConversations.remove(conversation);
    }

    @Override
    public void onMessage(GetChecksumsComplete message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetChecksumsRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetChecksumsResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileComplete message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileIDsComplete message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileIDsRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileIDsResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(GetFileResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(IdentifyPillarsForPutFileRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(PutFileComplete message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(PutFileRequest message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    @Override
    public void onMessage(PutFileResponse message) {
        String messageCorrelationID = message.getCorrelationID();
        T conversation = getConversation(messageCorrelationID);
        if (conversation != null) {
            conversation.onMessage(message);
        } else {
            log.debug("Message '" + messageCorrelationID + "' could not be delegated to any conversation.");
        }
    }

    /**
     * Look up a registered conversation, to find out if the message is for that given conversation.
     * @param messageCorrelationID The correlation id of the message.
     * @return The conversation, or null for no conversation.
     */
    private synchronized T getConversation(String messageCorrelationID) {
        // Look up conversation
        T conversation = conversations.get(messageCorrelationID);
        if (conversation != null) {
            return conversation;
        } else {
            // If not found, loop through conversations for which we do not know the message ID.
            for (Iterator<T> iterator = idLessConversations.iterator(); iterator.hasNext();) {
                conversation = iterator.next();
                if (conversation.getConversationID() != null) {
                    // If the conversation now has an ID, move it to the other collection for fast lookup.
                    conversations.put(conversation.getConversationID(), conversation);
                    iterator.remove();
                }
                if (conversation.getConversationID().equals(messageCorrelationID)) {
                    return conversation;
                }
            }
        }
        return null;
    }
}
