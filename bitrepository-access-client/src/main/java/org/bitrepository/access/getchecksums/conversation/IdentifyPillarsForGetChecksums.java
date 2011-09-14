/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getchecksums.conversation;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.eventhandler.PillarOperationEvent;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.time.TimeMeasureComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IdentifyPillarsForGetChecksums extends GetChecksumsState {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The timer used for timeout checks. */
    final Timer timer = new Timer();
    /** The timer task for timeout of identify in this conversation. */
    final TimerTask identifyTimeoutTask = new IdentifyTimerTask();

    public IdentifyPillarsForGetChecksums(SimpleGetChecksumsConversation conversation) {
        super(conversation);
    }
    
    /**
     * Starts the identifying process by sending a <code>IdentifyPillarsForGetFileRequest</code> message.
     * 
     * Also sets up a timer task to avoid waiting to long for all the expected responses.
     */
    public void start() {
        IdentifyPillarsForGetChecksumsRequest identifyRequest = new IdentifyPillarsForGetChecksumsRequest();
        identifyRequest.setCorrelationID(conversation.getConversationID());
        identifyRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        identifyRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        identifyRequest.setBitRepositoryCollectionID(
                conversation.settings.getBitRepositoryCollectionID());
        identifyRequest.setFileIDs(conversation.fileIDs);
        identifyRequest.setReplyTo(conversation.settings.getProtocol().getLocalDestination());
        identifyRequest.setTo(conversation.settings.getProtocol().getCollectionDestination());
        
        // TODO insert these variables?
        identifyRequest.setAuditTrailInformation(null);
        identifyRequest.setFileChecksumSpec(null);

        conversation.messageSender.sendMessage(identifyRequest);
        timer.schedule(identifyTimeoutTask, 
                TimeMeasureComparator.getTimeMeasureInLong(
                        conversation.settings.getGetChecksums().getIdentificationTimeout()));
    }

    /**
     * Handles a reply for identifying the fastest pillar to deliver a given file.
     *
     * @param response The IdentifyPillarsForGetChecksumsResponse to handle.
     */
    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsResponse response) {
        log.info("(ConversationID: " + conversation.getConversationID() + ") "
                + "Received IdentifyPillarsForGetChecksumsResponse from " + response.getPillarID() + "");
        conversation.selector.processResponse(response);
        if (conversation.eventHandler != null) {
            conversation.eventHandler.handleEvent(new PillarOperationEvent(
                    OperationEventType.PillarIdentified, response.getPillarID(), response.getPillarID()));
        }

        if (conversation.selector.isFinished()) {
            // stop the timer task for this outstanding instance, and then get the file from the selected pillar
            identifyTimeoutTask.cancel();
            // TODO: Race condition, what if timeout task already triggered this just before now

            getChecksumsFromSelectedPillar();
        }
    }
    
    @Override
    public void onMessage(GetChecksumsProgressResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") Received GetChecksumsProgressResponse "
                + "from " + response.getPillarID() + " before sending GetChecksumsRequest.");
    }
    
    @Override
    public void onMessage(GetChecksumsFinalResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") Received GetChecksumsProgressResponse "
                + "from " + response.getPillarID() + " before sending GetChecksumsRequest.");
    }
    
    /**
     * Method for moving to the next stage: GettingChecksum.
     */
    protected void getChecksumsFromSelectedPillar() {
        identifyTimeoutTask.cancel();
        GettingChecksums nextConversationState = new GettingChecksums(conversation);
        conversation.conversationState = nextConversationState;
        nextConversationState.start();
    }
    
    /**
     * Method for handling the timeout of the identification.
     */
    private void handleIdentificationTimeout() {
        synchronized (conversation) {
            if (conversation.conversationState == this) {
                if (conversation.selector.getPillarDestination().isEmpty()) {
                    String message = "Time has run out for selecting a pillar. The following pillars did't respond: " + 
                            conversation.selector.getOutstandingPillars() + 
                    ". Using pillar based on uncomplete set of responses.";
                    log.warn(message);
                    if (conversation.eventHandler != null) {
                        conversation.eventHandler.handleEvent(new DefaultEvent(
                                OperationEventType.IdentifyPillarTimeout, 
                                message));
                    }
                    getChecksumsFromSelectedPillar();
                } else {
                    String message = "Unable to select a pillar, time has run out. " +
                            "The following pillars did't respond: " + 
                            conversation.selector.getOutstandingPillars();
                    log.warn(message);
                    if (conversation.eventHandler != null) {
                        conversation.eventHandler.handleEvent(new DefaultEvent(
                                OperationEventType.NoPillarFound, ""));
                    } else {
                        conversation.throwException(new NoPillarFoundException(message));
                    }
                    endConversation();
                }
            } else {
                log.info("Conversation(" + conversation.getConversationID() + ") identification timeout, but " +
                        "the conversation state has already changed to " + conversation.conversationState);
            }
        }
    }

    /**
     * The timer task class for the outstanding identify requests. When the time is reached the selected pillar should
     * be called requested for the delivery of the file.
     */
    private class IdentifyTimerTask extends TimerTask {
        @Override
        public void run() {
            handleIdentificationTimeout();
        }
    }
}
