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
package org.bitrepository.modify.put.conversation;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.TimerTask;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.flow.UnexpectedResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The first state of the PutFile communication. The identification of the pillars involved.
 */
public class IdentifyPillarsForPut extends PutFileState {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The task to handle the timeouts for the identification.
     */
    private TimerTask timerTask = new IdentifyTimerTask();
    
    /**
     * Mapping between the identified pillars and their destinations.
     */
    Map<String, String> pillarDestinations = new HashMap<String, String>();

    /**
     * Constructor.
     * @param conversation The conversation in this given state.
     */
    public IdentifyPillarsForPut(SimplePutFileConversation conversation) {
        super(conversation);
    }

    public void start() {
        IdentifyPillarsForPutFileRequest identifyRequest = new IdentifyPillarsForPutFileRequest();
        identifyRequest.setCorrelationID(conversation.getConversationID());
        identifyRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        identifyRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        identifyRequest.setBitRepositoryCollectionID(conversation.settings.getBitRepositoryCollectionID());
        identifyRequest.setReplyTo(conversation.settings.getClientTopicID());
        identifyRequest.setTo(conversation.settings.getBitRepositoryCollectionTopicID());

        conversation.messageSender.sendMessage(identifyRequest);
        timer.schedule(timerTask, conversation.settings.getIdentifyPillarsTimeout());
    }

    @Override
    public synchronized void onMessage(IdentifyPillarsForPutFileResponse response) {
        log.debug("(ConversationID: " + conversation.getConversationID() + ") " 
                + "Received IdentifyPillarsForPutFileResponse for '" + response.getPillarID() + "'.");

        try {
            conversation.identifyResponseStatus.responseReceived(response.getPillarID());
            pillarDestinations.put(response.getPillarID(), response.getReplyTo());
        } catch (UnexpectedResponseException e) {
            log.warn("Received exception:", e);
            if(conversation.eventHandler != null) {
                conversation.eventHandler.handleEvent(new DefaultEvent(OperationEventType.Failed, 
                        "Unexcepted response from " + response.getPillarID() + " : " + e.getMessage()));
            }
        }

        if (conversation.eventHandler != null) {
            conversation.eventHandler.handleEvent(
                    new DefaultEvent(OperationEvent.OperationEventType.PillarIdentified, 
                            "Identified the pillar '" + response.getPillarID() + "' for Put."));
        }

        // Check if ready to go to next state.
        if(conversation.identifyResponseStatus.haveAllPillarResponded()) {
            log.info("Identified all pillars. Moving to next state.");
            // Stop timeout.
            timerTask.cancel();
            
            if (conversation.eventHandler != null) {
                conversation.eventHandler.handleEvent(
                        new DefaultEvent(OperationEvent.OperationEventType.PillarSelected, 
                                "Finished identifying all pillars for the put."));
            }

            // go to next state.
            PuttingFile newState = new PuttingFile(conversation, pillarDestinations);
            conversation.conversationState = newState;
            newState.start();
        }
    }

    /**
     * Method for handling the PutFileProgressResponse message.
     * No such message should be received!
     * @param response The PutFileProgressResponse message to handle.
     */
    @Override
    public synchronized void onMessage(PutFileProgressResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " +
                "Received PutFileProgressResponse from " + response.getPillarID() + " before sending PutFileRequest.");
    }

    /**
     * Method for handling the PutFileFinalResponse message.
     * No such message should be received!
     * @param response The PutFileFinalResponse message to handle.
     */
    @Override
    public synchronized void onMessage(PutFileFinalResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " +
                "Received PutFileFinalResponse from " + response.getPillarID() + " before sending PutFileRequest.");
    }

    /**
     * Class for handling the cases, when the identification time runs out.
     */
    private class IdentifyTimerTask extends TimerTask {
        @Override
        public void run() {
            // TODO handle differently?
            endConversation();
        }
    }
}
