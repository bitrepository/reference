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
package org.bitrepository.access.getfile.conversation;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.eventhandler.PillarOperationEvent;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.time.TimeMeasureComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Models the functionality for identifying pillars prior to a get file request.
 */
public class IdentifyingPillarsForGetFile extends GetFileState {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The timer used for timeout checks. */
    final Timer timer = new Timer();
    /** The timer task for timeout of identify in this conversation. */
    final TimerTask identifyTimeoutTask = new IdentifyTimerTask();

    /** 
     * The constructor for the indicated conversation.
     * @param conversation The related conversation containing context information.
     */
    public IdentifyingPillarsForGetFile(
            SimpleGetFileConversation conversation) {
        super(conversation);
    }

    /**
     * Starts the identifying process by sending a <code>IdentifyPillarsForGetFileRequest</code> message.
     * 
     * Also sets up a timer task to avoid waiting to long for all the expected responses.
     */
    public void start() {
        IdentifyPillarsForGetFileRequest identifyRequest = new IdentifyPillarsForGetFileRequest();
        identifyRequest.setCorrelationID(conversation.getConversationID());
        identifyRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        identifyRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        identifyRequest.setBitRepositoryCollectionID(conversation.settings.getBitRepositoryCollectionID());
        identifyRequest.setFileID(conversation.fileID);
        identifyRequest.setReplyTo(conversation.settings.getProtocol().getLocalDestination());
        identifyRequest.setTo(conversation.settings.getProtocol().getCollectionDestination());

        conversation.messageSender.sendMessage(identifyRequest);
        timer.schedule(identifyTimeoutTask, TimeMeasureComparator.getTimeMeasureInLong(
                conversation.settings.getGetFile().getIdentificationTimeout()));
    }

    /**
     * Handles a reply for identifying the fastest pillar to deliver a given file.
     * Removes the pillar responsible for the response from the outstanding list for the file in the reply. If no more
     * pillars are outstanding, then the file is requested from fastest pillar.
     *
     * @param response The IdentifyPillarsForGetFileResponse to handle.
     */
    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse response) {
        try {
            conversation.selector.processResponse(response);
            if (conversation.eventHandler != null) {
                conversation.eventHandler.handleEvent(new PillarOperationEvent(
                        OperationEventType.PillarIdentified, response.getPillarID(), response.getPillarID()));
            }
        } catch (UnexpectedResponseException e) {
            throw new IllegalArgumentException("Invalid IdentifyPillarsForGetFileResponse.");
        }

        try {
            if (conversation.selector.isFinished()) {
                // stop the timer task for this outstanding instance, and then get the file from the selected pillar
                identifyTimeoutTask.cancel();
                // TODO: Race condition, what if timeout task already triggered this just before now

                getFileFromSelectedPillar();
            }
        } catch (UnableToFinishException e) {
            conversation.throwException(
                    new NoPillarFoundException("Cannot request file, since no valid pillar could be found", e));
        }
    }
    
    @Override
    public void onMessage(GetFileProgressResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " +
                "Received GetFileProgressResponse from " + response.getPillarID() + " before sending GetFileRequest.");
    }
    
    @Override
    public void onMessage(GetFileFinalResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " +
                "Received GetFileFinalResponse from " + response.getPillarID() + "  before sending GetFileRequest.");
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
    
    /**
     * Encapsulates the functionality for handling a identification request timeout. An attempt will be made to continue 
     * based on the partial result. If this isn't possible the conversation will fail. 
     */
    private void handleIdentificationTimeout() {
        synchronized (conversation) {
            if (conversation.conversationState == this) {
                if (conversation.selector.getIDForSelectedPillar() != null) {
                    String message = "Time has run out for selecting a pillar. The following pillars did't respond: " + 
                    Arrays.toString(conversation.selector.getOutstandingPillars()) + 
                    ". Using pillar based on uncomplete set of responses.";
                    log.warn(message);
                    if (conversation.eventHandler != null) {
                        conversation.eventHandler.handleEvent(new DefaultEvent(
                                OperationEventType.IdentifyPillarTimeout, 
                                message));
                    }
                    getFileFromSelectedPillar();
                } else {
                    String message = "Unable to select a pillar, time has run out. " +
                    		"The following pillars did't respond: " + 
                    		Arrays.toString(conversation.selector.getOutstandingPillars());
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
     * Used when a suitable pillar has been found to move on to the Getting file state.
     */
    private void getFileFromSelectedPillar() {
        identifyTimeoutTask.cancel();
        GettingFile nextConversationState = new GettingFile(conversation);
        conversation.conversationState = nextConversationState;
        nextConversationState.start();
    }
}
