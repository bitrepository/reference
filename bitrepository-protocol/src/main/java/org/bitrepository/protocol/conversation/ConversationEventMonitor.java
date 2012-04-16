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
package org.bitrepository.protocol.conversation;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.protocol.eventhandler.*;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** 
 * Encapsulates the concrete handling of conversation events. Should be called every time a conversation event happens.
 */
public class ConversationEventMonitor {
    /** The log for this class. */
    private final ConversationLogger log;
    private final String conversationID;
    private final EventHandler eventHandler;

    /**
     * @param conversationID Used for adding conversation context information to the information distributed
     * @param eventHandler The eventHandler to send updates to.
     */
    public ConversationEventMonitor(String conversationID, EventHandler eventHandler) {
        log = new ConversationLogger(conversationID);
        this.conversationID = conversationID;
        this.eventHandler = eventHandler;
    }

    /**
     * Indicates a identify request has been sent to the contributors.
     * @param info Description
     */
    public void identifyPillarsRequestSent(String info) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(
                    OperationEventType.IDENTIFY_REQUEST_SENT, info, conversationID));
        }
    }

    /**
     * Indicates a contributor has been identified and considered for selection.
     * @param info Description
     * @param contributorID The pillar identified
     */
    public void pillarIdentified(String info, String contributorID) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new ContributorEvent(
                    OperationEventType.COMPONENT_IDENTIFIED, info, contributorID, conversationID));
        }
    }

    /**
     * Indicates a contributor has been identified and considered for selection.
     * @param response The identify response.
     */
    public void pillarIdentified(MessageResponse response) {
        String info = "Received positive identification response from " + response.getFrom() + ": " +
                response.getResponseInfo().getResponseText();
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new ContributorEvent(
                    OperationEventType.COMPONENT_IDENTIFIED, info, response.getFrom(), conversationID));
        }
    }
    
    /**
     * Indicates a identify request has timeout without all pillars responding.
     * @param info Description
     */
    public void identifyPillarTimeout(String info) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent( OperationEventType.IDENTIFY_TIMEOUT, info, 
                    conversationID));
        }
    }

    /**
     * Indicates a contributor has been selected for a operation request.
     * @param info Description
     * @param contributorID The pillar identified
     */
    public void pillarSelected(String info, String contributorID) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new ContributorEvent(
                    OperationEventType.IDENTIFICATION_COMPLETE, info, contributorID, conversationID));
        }
    }

    /**
     * A request has been sent to a contributor.
     * @param info Description of the context.
     * @param contributorID The receiving pillar.
     */
    public void requestSent(String info, String contributorID) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(
                    new ContributorEvent(OperationEvent.OperationEventType.REQUEST_SENT,
                            contributorID,
                            contributorID, conversationID));
        }
    }

    /**
     * New information regarding the progress of the operation has been received
     * @param progressEvent Contains information regarding the progress
     */
    public void progress(AbstractOperationEvent progressEvent) {
        progressEvent.setConversationID(conversationID);
        log.debug(progressEvent.getInfo());
        if (eventHandler != null) {
            eventHandler.handleEvent(progressEvent);
        }
    }

    /**
     * New information regarding the progress of the operation has been received
     * @param progressInfo Contains information regarding the progress
     */
    public void progress(String progressInfo) {
        log.debug(progressInfo);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(OperationEventType.PROGRESS, progressInfo, conversationID));
        }
    }
    
    /** 
     * A pillar has completed the operation.
     * @param completeEvent Event containing any additional information regarding the completion. Might contain the
     * return value from the operation, in which case the event will be a <code>DefafaultEvent</code> subclass.
     */
    public void pillarComplete(OperationEvent completeEvent) {
        log.debug(completeEvent.getInfo());
        if (eventHandler != null) {
            eventHandler.handleEvent(completeEvent);
        }
    }

    /**
     * An operation has completed. Note that only one complete event should be sent for each operation, so if multiple
     * pillars participate in the operation, this event should only be trigged after all the final response has been
     * received.
     * @param completeEvent Description of the context
     */
    public void complete(AbstractOperationEvent completeEvent) {
        completeEvent.setConversationID(conversationID);
        log.info(completeEvent.getInfo());
        if (eventHandler != null) {
            eventHandler.handleEvent(completeEvent);
        }
    }

    /**
     * An operation has completed. Note that only one complete event should be sent for each operation, so if multiple
     * pillars participate in the operation, this event should only be trigged after all the final response has been
     * received.
     * @param info Description of the context
     */
    public void complete(String info) {
        log.info(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(
                    OperationEvent.OperationEventType.COMPLETE, info, conversationID));
        }
    }

    /**
     * An invalid messages has been received
     * @param info Description of the context
     */
    public void invalidMessage(String info) {
        log.warn(info);
    }

    /**
     * An invalid messages has been received
     * @param message the invalid message
     * @param e Description of the context
     */
    public void invalidMessage(Message message, Exception e) {
        log.warn("Received invalid " + message.getClass().getSimpleName() + " from " + message.getFrom(), e);
        eventHandler.handleEvent(new ContributorEvent(
                OperationEventType.WARNING, e.getMessage(), message.getFrom(), conversationID));
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
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(OperationEventType.WARNING, info, conversationID));
        }
    }

    /**
     * Signifies a non-fatal event
     * @param info Problem description
     * @param e The cause
     */
    public void warning(String info, Exception e) {
        if (e == null) {
            warning(info);
        }
        log.warn(info, e);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(OperationEventType.WARNING, info + ", " + e.getMessage(), 
                    conversationID));
        }
    }
    
    /**
     * Logs debug information.
     * @param info The debug info to log.
     */
    public void debug(String info) {
        log.debug(info);
    }

    /**
     * A pillar has failed to handle a request successfully.
     * @param info Cause information
     */
    public void contributorFailed(String info) {
        log.warn(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(OperationEventType.COMPONENT_FAILED, info, 
                    conversationID));
        }    
    }
    
    /**
     * A pillar has failed to handle a request successfully.
     * @param info Cause information
     * @param e The cause
     */
    public void contributorFailed(String info, Exception e) {
        if (e == null) {
            contributorFailed(info);
        }
        log.warn(info, e);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(OperationEventType.COMPONENT_FAILED, info + ", " + e.getMessage(), 
                    conversationID));
        }
    }
    /**
     * General failure to complete the operation.
     * @param info Encapsulates the cause.
     */
    public void operationFailed(String info) {
        OperationFailedEvent event = new OperationFailedEvent(info, conversationID);
        log.warn(event.getInfo(), event.getException());
        if (eventHandler != null) {
            eventHandler.handleEvent(event);
        }
    }
    /**
     * General failure to complete the operation.
     * @param exception Encapsulates the cause.
     */
    public void operationFailed(Exception exception) {
        log.warn(exception.getMessage(), exception);
        OperationFailedEvent event = new OperationFailedEvent(exception.getMessage(), conversationID);
        if (eventHandler != null) {
            eventHandler.handleEvent(event);
        }
    }
    /**
     * General failure to complete the operation.
     * @param event Encapsulates the cause.
     */
    public void operationFailed(OperationFailedEvent event) {
        log.warn(event.getInfo(), event.getException());
        if (eventHandler != null) {
            eventHandler.handleEvent(event);
        }
    }

    /**
     * Custom logger for prefixing the log entries with the conversation ID.
     */
    private static class ConversationLogger {
        /** The wrapped logger. */
        private final Logger logger = LoggerFactory.getLogger(getClass());
        private final String conversationID;
        public ConversationLogger(String conversationID) {
            this.conversationID = conversationID;
        }

        /** Delegates to the normal logger debug */
        public void debug(String info) {
            logger.debug("Conversation(" + conversationID + " ) event:" +info);
        }

        /** Delegates to the normal logger info */
        public void info(String info) {
            logger.info("Conversation(" + conversationID + " ) event:" +info);
        }

        /** Delegates to the normal logger warn */
        public void warn(String info, Throwable e) {
            logger.warn("Conversation(" + conversationID + " ) event:" +info, e);
        }

        /** Delegates to the normal logger warn */
        public void warn(String info) {
            logger.warn("Conversation(" + conversationID + " ) event:" +info);
        }
    }
}
