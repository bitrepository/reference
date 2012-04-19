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
package org.bitrepository.access.getfileids.conversation;

import java.math.BigInteger;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.client.exceptions.NegativeResponseException;
import org.bitrepository.client.exceptions.UnexpectedResponseException;

/**
 * Models the behavior of a GetFileIDs conversation during the identification phase. That is, it begins with the 
 * sending of <code>IdentifyPillarsForGetFileIDsRequest</code> messages and finishes with on the reception of the 
 * <code>IdentifyPillarsForGetFileIDsResponse</code> messages from the responding pillars.
 * 
 * Note that this is only used by the GetFileIDsConversation in the same package, therefore the visibility is package 
 * protected.
 * This is the initial state for the whole GetFileIDs communication.
 */
public class IdentifyPillarsForGetFileIDs extends GetFileIDsState {
    /** The timer used for timeout checks. */
    final Timer timer = new Timer();
    /** The timer task for timeout of identify in this conversation. */
    final TimerTask identifyTimeoutTask = new IdentifyTimerTask();
    
    /**
     * Constructor.
     * @param conversation The conversation where this belongs.
     */
    public IdentifyPillarsForGetFileIDs(SimpleGetFileIDsConversation conversation) {
        super(conversation);
    }
    
    /**
     * Starts the identifying process by sending a <code>IdentifyPillarsForGetFileRequest</code> message.
     * 
     * Also sets up a timer task to avoid waiting to long for all the expected responses.
     */
    public void start() {
        IdentifyPillarsForGetFileIDsRequest identifyRequest = new IdentifyPillarsForGetFileIDsRequest();
        identifyRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        identifyRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        identifyRequest.setCorrelationID(conversation.getConversationID());
        identifyRequest.setCollectionID(conversation.settings.getCollectionID());
        identifyRequest.setFileIDs(conversation.fileIDs);
        identifyRequest.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        identifyRequest.setFrom(conversation.clientID);
        identifyRequest.setTo(conversation.settings.getCollectionDestination());
        identifyRequest.setAuditTrailInformation(conversation.auditTrailInformation);
        
        monitor.identifyPillarsRequestSent("Identifying pillars for GetFileIDs " + conversation.fileIDs);
        conversation.messageSender.sendMessage(identifyRequest);
        timer.schedule(identifyTimeoutTask,
                        conversation.settings.getCollectionSettings().getClientSettings().getIdentificationTimeout().longValue());
    }
    
    /**
     * Handles a reply for identifying the fastest pillar to deliver a given file.
     *
     * @param response The IdentifyPillarsForGetChecksumsResponse to handle.
     */
    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsResponse response) {
        try {
            conversation.selector.processResponse(response);
            monitor.pillarIdentified("Received IdentifyPillarsForGetFileIDsResponse for " + response.getFileIDs() +
                    " from " + response.getPillarID() + " with response '" +
                    response.getResponseInfo().getResponseText() + "'", response.getPillarID());
        } catch (UnexpectedResponseException e) {
            monitor.contributorFailed("Unable to handle IdentifyPillarsForGetFileIDsResponse, ", e);
        } catch (NegativeResponseException e) {
            monitor.contributorFailed("Negativ response from pillar " + response.getPillarID(), e);
        }
        if (conversation.selector.isFinished()) {
            identifyTimeoutTask.cancel();
            
            if (conversation.selector.getSelectedPillars().isEmpty()) {
                conversation.failConversation("Unable to getFileIDs, no pillars were identified");
            }
            monitor.pillarSelected("Identified pillars for getFileIDs", 
                    conversation.selector.getSelectedPillars().toString());
            getFileIDsFromSelectedPillar();
        }
    }
    
    @Override
    public void onMessage(GetFileIDsProgressResponse response) {
        monitor.outOfSequenceMessage("Received GetChecksumsProgressResponse "
                + "from " + response.getPillarID() + " before sending GetChecksumsRequest.");
    }
    
    @Override
    public void onMessage(GetFileIDsFinalResponse response) {
        monitor.outOfSequenceMessage("Received GetChecksumsProgressResponse "
                + "from " + response.getPillarID() + " before sending GetChecksumsRequest.");
    }
    
    /**
     * Method for moving to the next stage: GettingChecksum.
     */
    protected void getFileIDsFromSelectedPillar() {
        identifyTimeoutTask.cancel();
        GettingFileIDs nextConversationState = new GettingFileIDs(conversation);
        conversation.conversationState = nextConversationState;
        nextConversationState.start();
    }
    
    /**
     * Method for handling the timeout of the identification.
     */
    private void handleIdentificationTimeout() {
        synchronized (conversation) {
            if (conversation.conversationState == this) {
                if (!conversation.selector.getSelectedPillars().isEmpty()) {
                    monitor.identifyPillarTimeout("Time has run out for selecting a pillar. The following pillars "
                            + "didn't respond: " + conversation.selector.getOutstandingPillars() 
                            + ". Using pillars based on uncomplete set of responses.");
                    getFileIDsFromSelectedPillar();
                } else {
                    conversation.failConversation("Unable to select a pillar, time has run out. " +
                            "The following pillars did't respond: " + conversation.selector.getOutstandingPillars());
                }
            } else {
                monitor.outOfSequenceMessage("Identification timeout, but " +
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
    
    @Override
    public boolean hasEnded() {
        return false;
    }    
}
