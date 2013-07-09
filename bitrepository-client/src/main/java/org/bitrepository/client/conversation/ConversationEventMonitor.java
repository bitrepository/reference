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
package org.bitrepository.client.conversation;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.eventhandler.AbstractOperationEvent;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.ContributorCompleteEvent;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.DefaultEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.IdentificationCompleteEvent;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.protocol.OperationType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bitrepository.client.eventhandler.OperationEvent.OperationEventType.*;

/**
 * Encapsulates the concrete handling of conversation events. Should be called every time a conversation event happens.
 */
public class ConversationEventMonitor {
    /** The general logger.*/
    private final ConversationLogger log;
    /** The event logger. Adds context information to the log entry. */
    private final Logger eventLogger = LoggerFactory.getLogger(getClass());
    private final String collectionID;
    private final String conversationID;
    private final OperationType operationType;
    private final String fileID;
    private final EventHandler eventHandler;
    /** Used to aggregate the conversation complete events, so these can be added to the final complete event. */
    private final List<ContributorEvent> contributorCompleteEvents = new LinkedList<ContributorEvent>();
    /** Used to aggregate the conversation failed events, so these can be added to the final failed event. */
    private final List<ContributorFailedEvent> contributorFailedEvents = new LinkedList<ContributorFailedEvent>();
    /**
     * Indicated whether the final operation event should be a complete event or a failed event in case of individual
     * component failures.
      */
    private boolean failOnComponentFailure = true;

    /**
     * @param conversationID Used for adding conversation context information to the information distributed. Will be
     *                       shorted to increase readability.
     * @param operationType The operation type to include in the events og logs.
     * @param fileID Optional file ID to include in the events and logs.
     * @param eventHandler The eventHandler to send updates to.
     */
    public ConversationEventMonitor(String collectionID, String conversationID, OperationType operationType, String fileID, 
            EventHandler eventHandler) {
        log = new ConversationLogger();
        this.collectionID = collectionID;
        this.conversationID = getShortConversationID(conversationID);
        this.operationType = operationType;
        this.fileID = fileID;
        this.eventHandler = eventHandler;
    }

    /**
     * Indicates a identify request has been sent to the contributors.
     * @param info Description
     */
    public void identifyRequestSent(String info) {
        notifyEventListerners(createDefaultEvent(IDENTIFY_REQUEST_SENT, info));
    }

    /**
     * Indicates a contributor has been identified and considered for selection.
     * @param response The identify response.
     */
    public void contributorIdentified(MessageResponse response) {
        notifyEventListerners(createContributorEvent(COMPONENT_IDENTIFIED, null, response.getFrom()));
    }

    /**
     * Indicates a identify request has timeout without all contributors responding.
     */
    public void identifyContributorsTimeout(Collection<String> unrespondingContributors) {
        StringBuilder failureMessage = new StringBuilder("Time has run out for looking up contributors");
        if (!unrespondingContributors.isEmpty()) {
            failureMessage.append("\nThe following contributors didn't respond: " + unrespondingContributors);
        }
        notifyEventListerners(createDefaultEvent(IDENTIFY_TIMEOUT, failureMessage.toString()));

        for (String contributorID : unrespondingContributors) {
            contributorFailed("Timeout for identifying contributor", contributorID, null);
        }
    }

    /**
     * Indicates a contributor has been selected for a operation request.
     * @param contributorIDList The contributors identified
     */
    public void contributorsSelected(List<String> contributorIDList) {
        notifyEventListerners(createContributorsIdentifiedEvent(contributorIDList));
    }

    /**
     * A request has been sent to a contributor.
     * @param info Description of the context.
     * @param contributorID The ID of the contributor
     */
    public void requestSent(String info, String contributorID) {
        notifyEventListerners(createContributorEvent(REQUEST_SENT, info, contributorID));
    }

    /**
     * New information regarding the progress of the operation has been received
     * @param progressEvent Contains information regarding the progress
     */
    public void progress(AbstractOperationEvent progressEvent) {
        addContextInfo(progressEvent);
        notifyEventListerners(progressEvent);
    }

    /**
     * New information regarding the progress of the operation has been received
     * @param progressInfo Contains information regarding the progress
     */
    public void progress(String progressInfo, String contributorID) {
        notifyEventListerners(createContributorEvent(PROGRESS, progressInfo, contributorID));
    }
    
    /**
     * Notify that a contributor has failed in a way resulting in a retry attempt. 
     * @param info Information regarding the retry attempt
     * @param contributorID the contributor that is retried.  
     */
    public void retry(String info, String contributorID) {
        notifyEventListerners(createContributorEvent(WARNING, info, contributorID));
    }

    /**
     * A pillar has completed the operation.
     * @param completeEvent Event containing any additional information regarding the completion. Might contain the
     * return value from the operation, in which case the event will be a <code>DefafaultEvent</code> subclass.
     */
    public void contributorComplete(ContributorCompleteEvent completeEvent) {
        contributorCompleteEvents.add(completeEvent);
        addContextInfo(completeEvent);
        notifyEventListerners(completeEvent);
    }

    /**
     * A pillar has failed to handle a request successfully.
     * @param info Cause information
     * @param contributorID The ID of the contributor
     */
    public void contributorFailed(String info, String contributorID, ResponseCode responseCode) {
        ContributorFailedEvent failedEvent = createContributorFailedEvent(info, contributorID, responseCode);
        contributorFailedEvents.add(failedEvent);
        notifyEventListerners(failedEvent);
    }

    /**
     * An operation has completed. Will generate a failed event, if any of the contributers have failed.
     */
    public void complete() {
        if (contributorFailedEvents.isEmpty() || !failOnComponentFailure) {
            notifyEventListerners(createCompleteEvent());
        } else {
            String info = "Failed operation. Cause(s):\n" + contributorFailedEvents;
            notifyEventListerners(createOperationFailedEvent(info));
        }
    }

    /**
     * General failure to complete the operation.
     * @param info Encapsulates the cause.
     */
    public void operationFailed(String info) {
        notifyEventListerners(createOperationFailedEvent(info));
    }

    /**
     * General failure to complete the operation.
     * @param event Encapsulates the cause.
     */
    public void operationFailed(OperationFailedEvent event) {
        addContextInfo(event);
        notifyEventListerners(event);
    }

    /**
     * An invalid messages has been received
     * @param message the invalid message
     * @param e Description of the context
     */
    public void invalidMessage(Message message, Exception e) {
        log.warn("Received invalid " + message.getClass().getSimpleName() + " from " + message.getFrom() +
                "\nMessage: " + message, e);
        notifyEventListerners((createContributorEvent(WARNING, e.getMessage(), message.getFrom())));
    }

    /**
     * A message has been received with isn't consistent with the current conversation state.
     */
    public void outOfSequenceMessage(Message message) {
        log.warn("Can not handle messages of type " + message.getClass().getSimpleName());
    }

    /**
     * Signifies a non-fatal event
     * @param info Problem description
     */
    public void warning(String info) {
        notifyEventListerners(createDefaultEvent(WARNING, info));
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
        notifyEventListerners(createDefaultEvent(WARNING, info + ", " + e.getMessage()));
    }

    /**
     * Logs debug information.
     * @param info The debug info to log.
     */
    public void debug(String info) {
        log.debug(info);
    }

    /**
     * Logs debug information.
     * @param info The debug info to log.
     *
     */
    public void debug(String info, Exception e) {
        log.debug(info, e);
    }

    /**
     * Returns a shorted conversationID. Only the first part up til the first '-' is used
     * (but at least 4 long).
     */
    private String getShortConversationID(String fullConversationID) {
        return fullConversationID.substring(0, fullConversationID.indexOf("-", 4));
    }

    private DefaultEvent createDefaultEvent(OperationEvent.OperationEventType eventType, String info) {
        DefaultEvent event = new DefaultEvent(collectionID);
        event.setEventType(eventType);
        event.setInfo(info);
        addContextInfo(event);
        return event;
    }
    private ContributorEvent createContributorEvent(
            OperationEvent.OperationEventType eventType, String info, String contributorID) {
        ContributorEvent event = new ContributorEvent(contributorID, collectionID);
        event.setEventType(eventType);
        event.setInfo(info);
        addContextInfo(event);
        return event;
    }
    private ContributorFailedEvent createContributorFailedEvent(String info, String contributorID,
            ResponseCode responseCode) {
        ContributorFailedEvent event = new ContributorFailedEvent(contributorID, collectionID, responseCode);
        event.setInfo(info);
        addContextInfo(event);
        return event;
    }

    private IdentificationCompleteEvent createContributorsIdentifiedEvent(List<String> contributorIDList) {
        IdentificationCompleteEvent event = new IdentificationCompleteEvent(collectionID, contributorIDList);
        addContextInfo(event);
        return event;
    }

    private OperationFailedEvent createOperationFailedEvent(String info) {
        OperationFailedEvent event = new OperationFailedEvent(collectionID, info, contributorCompleteEvents);
        addContextInfo(event);
        return event;
    }

    private CompleteEvent createCompleteEvent() {
        CompleteEvent event = new CompleteEvent(collectionID, contributorCompleteEvents);
        addContextInfo(event);
        return event;
    }

    /**
     * Adds the general operation context information for the conversation to the event.
     * @return
     */
    private OperationEvent addContextInfo(AbstractOperationEvent event) {
        event.setConversationID(conversationID);
        event.setFileID(fileID);
        event.setOperationType(operationType);
        return event;
    }
    /**
     * Indicates whether a operation should be consider failed because one or more of the contributors have failed.
     * This is <code>true</code> by default.
     *
     * Note that the operation can be falied explicitly by calling the operationFailed method.
     */
    public void markAsFailedOnContributorFailure(boolean failOnComponentFailure) {
        this.failOnComponentFailure = failOnComponentFailure;
    }

    /**
     * Custom logger for prefixing the log entries with the conversation ID.
     */
    private class ConversationLogger {

        /** Delegates to the normal logger debug */
        public void debug(String info) {
            eventLogger.debug(conversationID + ": " + contextInfo() + ":" +info);
        }

        /** Delegates to the normal logger debug */
        public void debug(String info, Exception e) {
            eventLogger.debug(conversationID + ": " + contextInfo() + ":" + info, e);
        }

        /** Delegates to the normal logger info */
        public void info(String info) {
            eventLogger.info(conversationID + ": " + contextInfo() + ":" +info);
        }

        /** Delegates to the normal logger warn */
        public void warn(String info, Throwable e) {
            eventLogger.warn(conversationID + ": " + contextInfo() + ":" +info, e);
        }

        /** Delegates to the normal logger warn */
        public void warn(String info) {
            eventLogger.warn(conversationID + ": " + contextInfo() + ":" +info);
        }

        private String contextInfo() {
            StringBuilder sb = new StringBuilder();
            sb.append(operationType);
            if (fileID != null) {
                sb.append(" for file " + fileID);
            }
            return sb.toString();
        }

    }

    private void notifyEventListerners(OperationEvent event) {
        eventLogger.trace(event.toString());
        if (eventHandler != null) {
            try {
                eventHandler.handleEvent(event);
            } catch (Throwable thr) {
                log.warn("The eventhandler (" + eventHandler + ") failed when called with event " + event, thr);
            }
        }
    }
}
