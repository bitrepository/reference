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

import java.util.LinkedList;
import java.util.List;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.eventhandler.AbstractOperationEvent;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.DefaultEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bitrepository.client.eventhandler.OperationEvent.OperationEventType.*;

/**
 * Encapsulates the concrete handling of conversation events. Should be called every time a conversation event happens.
 */
public class ConversationEventMonitor {
    private final ConversationLogger log;
    private final String conversationID;
    private final EventHandler eventHandler;
    private final List<ContributorEvent> contributorCompleteEvents = new LinkedList<ContributorEvent>();
    private final List<ContributorFailedEvent> contributorFailedEvents = new LinkedList<ContributorFailedEvent>();
    private boolean failOnComponentFailure = true;

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
    public void identifyRequestSent(String info) {
        log.debug(info);
        notifyEventListerners(new DefaultEvent(IDENTIFY_REQUEST_SENT, info, conversationID));
    }

    /**
     * Indicates a contributor has been identified and considered for selection.
     * @param response The identify response.
     */
    public void contributorIdentified(MessageResponse response) {
        String info = "Received positive identification response from " + response.getFrom() + ": " +
                response.getResponseInfo().getResponseText();
        log.debug(info);
        notifyEventListerners(new ContributorEvent(COMPONENT_IDENTIFIED, info, response.getFrom(), conversationID));
    }

    /**
     * Indicates a identify request has timeout without all pillars responding.
     * @param info Description
     */
    public void identifyContributorsTimeout(String info, List<String> unrespondingContributors) {
        StringBuilder failureMessage = new StringBuilder(info);
        if (!unrespondingContributors.isEmpty()) {
            failureMessage.append("\nMissing contributors: " + unrespondingContributors);
        }
        if (!contributorFailedEvents.isEmpty()) {
            failureMessage.append("\nFailing contributors: " + unrespondingContributors);
            for (ContributorFailedEvent failedEvent:contributorFailedEvents) {
                failureMessage.append(failedEvent.getContributorID() + "(" + failedEvent.getInfo() + "),");
            }
        }
        log.debug(failureMessage.toString());
        if (eventHandler != null) {
            eventHandler.handleEvent(new DefaultEvent( IDENTIFY_TIMEOUT, failureMessage.toString(), conversationID));
        }
    }

    /**
     * Indicates a contributor has been selected for a operation request.
     * @param info Description
     * @param contributorID The pillar identified
     */
    public void contributorSelected(String info, String contributorID) {
        log.debug(info);
        notifyEventListerners(new ContributorEvent(IDENTIFICATION_COMPLETE, info, contributorID, conversationID));
    }

    /**
     * A request has been sent to a contributor.
     * @param info Description of the context.
     * @param contributorID The receiving pillar.
     */
    public void requestSent(String info, String contributorID) {
        log.debug(info);
        notifyEventListerners(new ContributorEvent(REQUEST_SENT, contributorID, contributorID, conversationID));
    }

    /**
     * New information regarding the progress of the operation has been received
     * @param progressEvent Contains information regarding the progress
     */
    public void progress(AbstractOperationEvent progressEvent) {
        progressEvent.setConversationID(conversationID);
        log.debug(progressEvent.getInfo());
        notifyEventListerners(progressEvent);
    }

    /**
     * New information regarding the progress of the operation has been received
     * @param progressInfo Contains information regarding the progress
     */
    public void progress(String progressInfo, String contributorID) {
        log.debug(progressInfo);
        notifyEventListerners(new ContributorEvent(PROGRESS, progressInfo, contributorID, conversationID));
    }

    /**
     * A pillar has completed the operation.
     * @param completeEvent Event containing any additional information regarding the completion. Might contain the
     * return value from the operation, in which case the event will be a <code>DefafaultEvent</code> subclass.
     */
    public void contributorComplete(ContributorEvent completeEvent) {
        log.info(completeEvent.getInfo());
        contributorCompleteEvents.add(completeEvent);
        notifyEventListerners(completeEvent);
    }

    /**
     * A pillar has failed to handle a request successfully.
     * @param info Cause information
     */
    public void contributorFailed(String info, String contributor, ResponseCode responseCode) {
        log.warn(info);
        ContributorFailedEvent failedEvent = new ContributorFailedEvent(info, contributor, responseCode, conversationID);
        contributorFailedEvents.add(failedEvent);
        notifyEventListerners(failedEvent);
    }

    /**
     * An operation has completed. Will generate a failed event, if any of the contributers have failed.
     */
    public void complete() {
        if (contributorFailedEvents.isEmpty()) {
            String message = "Completed successfully.";
            log.info(message);
            notifyEventListerners(new CompleteEvent(message, contributorCompleteEvents, conversationID));
        } else {
            String message = "Failed operation. Cause(s):\n" + contributorFailedEvents;
            log.warn(message);
            notifyEventListerners(new OperationFailedEvent(message, contributorCompleteEvents, conversationID));
        }
    }

    /**
     * General failure to complete the operation.
     * @param info Encapsulates the cause.
     */
    public void operationFailed(String info) {
        log.warn(info);
        OperationFailedEvent event = new OperationFailedEvent(info, conversationID);
        notifyEventListerners(event);
    }

    /**
     * General failure to complete the operation.
     * @param exception Encapsulates the cause.
     */
    public void operationFailed(Exception exception) {
        log.warn(exception.getMessage(), exception);
        OperationFailedEvent event = new OperationFailedEvent(exception.getMessage(), conversationID);
        notifyEventListerners(event);
    }

    /**
     * General failure to complete the operation.
     * @param event Encapsulates the cause.
     */
    public void operationFailed(OperationFailedEvent event) {
        log.warn(event.getInfo());
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
        notifyEventListerners((new ContributorEvent(WARNING, e.getMessage(), message.getFrom(), conversationID)));
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
        notifyEventListerners(new DefaultEvent(WARNING, info, conversationID));
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
        notifyEventListerners(new DefaultEvent(WARNING, info + ", " + e.getMessage(), conversationID));
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
     * Should this operation be considered failed, eg. something critical has gone wrong.
     * Note that a operation may be considered a success, even if some contributors are unable to respond successfully.
     */
    public boolean hasFailed() {
        return !contributorFailedEvents.isEmpty() && failOnComponentFailure;
    }

    /**
     * Indicates whether a operation should be consider failed because one or more of the contributors have failed.
     * This is <code>true</code> by default.
     *
     * Note that the operation can be falied explicitly by calling the operationFailed method.
     */
    public void markAsFailedOnContributorFailure(boolean enable) {
        failOnComponentFailure = enable;
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

        /** Delegates to the normal logger debug */
        public void debug(String info, Exception e) {
            logger.debug("Conversation(" + conversationID + " ) event:" + info, e);
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

    private void notifyEventListerners(OperationEvent event) {
        if (eventHandler != null) {
            eventHandler.handleEvent(event);
        }
    }
}
