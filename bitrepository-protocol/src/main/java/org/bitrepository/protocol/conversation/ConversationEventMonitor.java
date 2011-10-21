package org.bitrepository.protocol.conversation;

import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.eventhandler.OperationFailedEvent;
import org.bitrepository.protocol.eventhandler.PillarOperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Encapsulates the concrete handling of conversation events. Should be called every time a conversation event happens.
 */
public class ConversationEventMonitor {
    /** The log for this class. */
    private final ConversationLogger log;
    private final Conversation conversation;
    private final EventHandler eventHandler;

    public ConversationEventMonitor(Conversation conversation, EventHandler eventHandler) {
        log = new ConversationLogger(conversation.getConversationID());
        this.conversation = conversation;
        this.eventHandler = eventHandler;
    }

    public void identifyPillarsRequestSent(String info) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(
                    OperationEventType.IdentifyPillarsRequestSent, info));
        }
    }

    public void pillarIdentified(String info, String pillarID) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new PillarOperationEvent(
                    OperationEventType.PillarIdentified, info, pillarID));
        }
    }
    
    public void identifyPillarTimeout(String info) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent( OperationEventType.IdentifyPillarTimeout, info));
        }
    }

    public void pillarSelected(String info, String pillarID) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new PillarOperationEvent(
                    OperationEventType.PillarSelected, info, pillarID));
        }
    }

    /**
     * A request has been sent to a pillar.
     * @param info Description of the context.
     * @param pillarID The receiving pillar.
     */
    public void requestSent(String info, String pillarID) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(
                    new PillarOperationEvent(OperationEvent.OperationEventType.RequestSent, 
                            pillarID, 
                            pillarID));
        }
    }

    /**
     * New information regarding the progress of the operation has been received
     * @param progressEvent Contains information regarding the progress
     */
    public void progress(OperationEvent<?> progressEvent) {
        log.debug(progressEvent.getInfo());
        if (eventHandler != null) {
            eventHandler.handleEvent(progressEvent);
        }
    }

    /**
     * An operation has completed. Note that only one complete event should be sent for each operation, so if multiple
     * pillars participate in the operation, this event should only be trigged after all the final response has been 
     * received.
     * @param completeEvent Description of the context
     */
    public void complete(OperationEvent<?> completeEvent) {
        log.debug(completeEvent.getInfo());
        if (eventHandler != null) {
            eventHandler.handleEvent(completeEvent);
        }
        conversation.endConversation();
    }

    /**
     * An invalid messages has been received
     * @param info Description of the context
     * @param message The invalid message
     */
    public void invalidMessage(String info) {
        log.warn(info);
    }

    /**
     * A message has been received with isn't consistent with the current conversation state.
     * @param info Problem description
     */
    public void outOfSequenceMessage(String info) {
        log.warn(info);
    }
    
    /**
     * Signifies a non-fatal event
     * @param info Problem description
     */
    public void warning(String info) {
        log.warn(info);
    }
    
    public void warning(String info, Exception e) {
        log.warn(info, e);
    }

    /**
     * General failure to complete the operation.
     * @param event Encapsulates the cause.
     */
    public void operationFailed(OperationFailedEvent event) {
        log.warn(event.getInfo(), event.getState());
        if (eventHandler != null) {
            eventHandler.handleEvent(event);
        }
    }

    /**
     * Custom logger for prefixing the log entries with the conversation ID.
     */
    private class ConversationLogger {
        /** The wrapped logger. */
        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final String conversationID;
        public ConversationLogger(String conversationID) {
            this.conversationID = conversationID;
        }

        public void debug(String info) {
            logger.debug("Conversation(" + conversationID + " ) event:" +info);
        }

        public void warn(String info) {
            logger.debug("Conversation(" + conversationID + " ) event:" +info);
        }
        
        public void warn(String info, Throwable e) {
            logger.debug("Conversation(" + conversationID + " ) event:" +info, e);
        }
    }
}
