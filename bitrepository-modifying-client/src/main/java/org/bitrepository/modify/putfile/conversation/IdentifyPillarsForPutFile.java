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
package org.bitrepository.modify.putfile.conversation;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.OperationFailedEvent;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.ContributorResponseStatus;

/**
 * The first state of the PutFile communication. The identification of the pillars involved.
 */
public class IdentifyPillarsForPutFile extends PutFileState {
    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** The timer. Schedules conversation timeouts for this conversation. */
    final Timer timer = new Timer(TIMER_IS_DAEMON);

    /**
     * The task to handle the timeouts for the identification.
     */
    private TimerTask timerTask = new IdentifyTimerTask();
    
    /** Response status for the pillars.*/
    final ContributorResponseStatus identifyResponseStatus;

    /**
     * Mapping between the identified pillars and their destinations.
     */
    Map<String, String> pillarDestinations = new HashMap<String, String>();

    /**
     * Constructor.
     * @param conversation The conversation in this given state.
     */
    public IdentifyPillarsForPutFile(SimplePutFileConversation conversation) {
        super(conversation);
        this.identifyResponseStatus = new ContributorResponseStatus(
                conversation.settings.getCollectionSettings().getClientSettings().getPillarIDs());
    }

    /**
     * Starts the conversation by sending the request for identification of the pillars to perform the put operation.
     */
    public void start() {
        IdentifyPillarsForPutFileRequest identifyRequest = new IdentifyPillarsForPutFileRequest();
        identifyRequest.setCorrelationID(conversation.getConversationID());
        identifyRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        identifyRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        identifyRequest.setCollectionID(conversation.settings.getCollectionID());
        identifyRequest.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        identifyRequest.setTo(conversation.settings.getCollectionDestination());
        identifyRequest.setFileID(conversation.fileID);
        identifyRequest.setAuditTrailInformation(conversation.auditTrailInformation);
        identifyRequest.setFileSize(conversation.fileSize);
        identifyRequest.setFrom(conversation.clientID);
        conversation.messageSender.sendMessage(identifyRequest);

        monitor.identifyPillarsRequestSent("Identifying pillars for put file " + conversation.fileID);
        timer.schedule(timerTask, 
                conversation.settings.getCollectionSettings().getClientSettings().getIdentificationTimeout().longValue());
    }

    @Override
    public synchronized void onMessage(IdentifyPillarsForPutFileResponse response) {
        try {
            identifyResponseStatus.responseReceived(response.getPillarID());
            validateIdentificationResponse(response);
        } catch (UnexpectedResponseException e) {
            monitor.invalidMessage("Unexcepted response from " + response.getPillarID() + " : " + e.getMessage());
        }

        // Check if ready to go to next state.
        if(identifyResponseStatus.haveAllPillarResponded()) {
            monitor.pillarSelected("Identified all pillars for put. Starting to put.", 
                    conversation.settings.getCollectionSettings().getClientSettings().getPillarIDs().toString());
            
            // go to next state.
            PuttingFile newState = new PuttingFile(conversation, pillarDestinations);
            conversation.conversationState = newState;
            newState.start();
        }
    }
    
    /**
     * Method for validating the ResponseInfo from a pillar.
     * If the response is positive, then the pillar is selected for the PutFile operation. 
     * @param response The identification response to validate.
     */
    private void validateIdentificationResponse(IdentifyPillarsForPutFileResponse response) {
        ResponseInfo rInfo = response.getResponseInfo();
        
        if(rInfo.getResponseCode() == ResponseCode.IDENTIFICATION_POSITIVE) {
            monitor.debug("Positive identification from pillar '" + response.getPillarID() + "' received: " + rInfo);
            
            pillarDestinations.put(response.getPillarID(), response.getReplyTo());
            monitor.pillarIdentified("Identified the pillar '" + response.getPillarID() + "' for Put.", 
                    response.getPillarID());
        } else {
            monitor.contributorFailed("Negative identification from pillar '" + response.getPillarID() + "' received: "
                                      + rInfo);
        }
    }

    /**
     * Method for handling the PutFileProgressResponse message.
     * No such message should be received!
     * @param response The PutFileProgressResponse message to handle.
     */
    @Override
    public synchronized void onMessage(PutFileProgressResponse response) {
        monitor.outOfSequenceMessage("Received PutFileProgressResponse from " + response.getPillarID() + 
                " before sending PutFileRequest.");
    }

    /**
     * Method for handling the PutFileFinalResponse message.
     * No such message should be received!
     * @param response The PutFileFinalResponse message to handle.
     */
    @Override
    public synchronized void onMessage(PutFileFinalResponse response) {
        monitor.outOfSequenceMessage("Received PutFileFinalResponse from " + response.getPillarID() + 
                " before sending PutFileRequest.");
    }

    /**
     * Class for handling the cases, when the identification time runs out.
     */
    private class IdentifyTimerTask extends TimerTask {
        @Override
        public void run() {
        	conversation.failConversation(
        	        new OperationFailedEvent("Timeout for the identification of the pillars for the PutFile operation."));
        }
    }
    
    @Override
    public boolean hasEnded() {
        return false;
    }
}
