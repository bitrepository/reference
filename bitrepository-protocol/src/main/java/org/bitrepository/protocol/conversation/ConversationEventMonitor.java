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

    /**
     * @param conversation Used for adding conversation context information to the information distributed
     * @param eventHandler The eventHandler to send updates to.
     */
    public ConversationEventMonitor(Conversation conversation, EventHandler eventHandler) {
        log = new ConversationLogger(conversation.getConversationID());
        this.conversation = conversation;
        this.eventHandler = eventHandler;
    }

    /**
     * Indicates a identify request has been sent to the pillars.
     * @param info Description
     */
    public void identifyPillarsRequestSent(String info) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(
                    OperationEventType.IdentifyPillarsRequestSent, info));
        }
    }

    /**
     * Indicates a pillar has been identified and considered for selection.
     * @param info Description
     * @param pillarID The pillar identified
     */
    public void pillarIdentified(String info, String pillarID) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new PillarOperationEvent(
                    OperationEventType.PillarIdentified, info, pillarID));
        }
    }
    
    /**
     * Indicates a identify request has timeout without all pillars responding.
     * @param info Description
     */
    public void identifyPillarTimeout(String info) {
        log.debug(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent( OperationEventType.IdentifyPillarTimeout, info));
        }
    }

    /**
     * Indicates a pillar has been selected for a operation request.
     * @param info Description
     * @param pillarID The pillar identified
     */
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
     * A pillar has completed the operation.
     * @param defaultEvent Event containing any additional information regarding the completion. Might contain the 
     * return value from the operation, in which case the event will be a <code>DefafaultEvent</code> subclass.
     */
    public void pillarComplete(OperationEvent<?> completeEvent) {
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
    public void complete(OperationEvent<?> completeEvent) {
        log.info(completeEvent.getInfo());
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
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(OperationEventType.Warning, info));
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
            eventHandler.handleEvent(new DefaultEvent(OperationEventType.Warning, info + ", " + e.getMessage()));
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
    public void pillarFailed(String info) {
        log.warn(info);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(OperationEventType.PillarFailed, info));
        }    
    }
    
    /**
     * A pillar has failed to handle a request successfully.
     * @param info Cause information
     * @param e The cause
     */
    public void pillarFailed(String info, Exception e) {
        if (e == null) {
            pillarFailed(info);
        }
        log.warn(info, e);
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent(OperationEventType.PillarFailed, info + ", " + e.getMessage()));
        }
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
