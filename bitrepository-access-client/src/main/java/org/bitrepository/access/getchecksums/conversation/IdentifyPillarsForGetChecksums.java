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
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.protocol.ProtocolConstants;

/**
 * Models the behavior of a GetChecksums conversation during the identification phase. That is, it begins with the 
 * sending of <code>IdentifyPillarsForGetChecksumsRequest</code> messages and finishes with on the reception of the 
 * <code>IdentifyPillarsForGetChecksumsResponse</code> messages from the responding pillars.
 * 
 * Note that this is only used by the GetChecksumsConversation in the same package, therefore the visibility is package 
 * protected.
 * This is the initial state for the whole GetChecksums communication.
 */
public class IdentifyPillarsForGetChecksums extends GetChecksumsState {
    /** The timer used for timeout checks. */
    final Timer timer = new Timer();
    /** The timer task for timeout of identify in this conversation. */
    final TimerTask identifyTimeoutTask = new IdentifyTimerTask();

    /**
     * Constructor.
     * @param conversation The conversation for this state.
     */
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
                conversation.settings.getCollectionID());
        identifyRequest.setFileIDs(conversation.fileIDs);
        identifyRequest.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        identifyRequest.setTo(conversation.settings.getCollectionDestination());
        identifyRequest.setAuditTrailInformation(conversation.auditTrailInformation);
        identifyRequest.setFileChecksumSpec(conversation.checksumSpecifications);

        monitor.identifyPillarsRequestSent("Identifying pillars for getting file " + conversation.fileIDs);
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
    public void onMessage(IdentifyPillarsForGetChecksumsResponse response) {
        monitor.pillarIdentified("Received IdentifyPillarsForGetChecksumsResponse " + response, response.getPillarID());
        conversation.selector.processResponse(response);
        if (conversation.selector.isFinished()) {
            identifyTimeoutTask.cancel();
            
            if (conversation.selector.getPillarDestinations() == null || conversation.selector.getPillarDestinations().isEmpty()) {
                conversation.failConversation("Unable to getChecksums, no pillars were identified");
            }
            monitor.pillarSelected("Identified pillars for getChecksums", 
                    conversation.selector.getPillarDestinations().keySet().toString());
            getChecksumsFromSelectedPillar();
        }
    }
    
    @Override
    public void onMessage(GetChecksumsProgressResponse response) {
        monitor.outOfSequenceMessage("Received GetChecksumsProgressResponse "
                + "from " + response.getPillarID() + " before sending GetChecksumsRequest.");
    }
    
    @Override
    public void onMessage(GetChecksumsFinalResponse response) {
        monitor.outOfSequenceMessage("Received GetChecksumsProgressResponse "
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
                if (!conversation.selector.getPillarDestinations().isEmpty()) {
                    monitor.identifyPillarTimeout("Time has run out for selecting a pillar. The following pillars did't respond: " + 
                            conversation.selector.getOutstandingPillars() + 
                    ". Using pillar based on uncomplete set of responses.");
                    getChecksumsFromSelectedPillar();
                } else {
                    conversation.failConversation("Unable to select a pillar, time has run out. " +
                            "The following pillars did't respond: " + conversation.selector.getOutstandingPillars());
                }
            } else {
                monitor.warning("Identification timeout, but " +
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
