/*
 * #%L
 * Bitrepository Access
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getstatus.conversation;

import java.math.BigInteger;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.exceptions.NegativeResponseException;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;

public class IdentifyingContributorsForGetStatus extends GetStatusState {
    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** The timer used for timeout checks. */
    final Timer timer = new Timer(TIMER_IS_DAEMON);
    /** The timer task for timeout of identify in this conversation. */
    final TimerTask identifyTimeoutTask = new IdentifyTimerTask();    
    
    public IdentifyingContributorsForGetStatus(SimpleGetStatusConversation conversation) {
        super(conversation);
    }

    @Override
    public void start() {
        IdentifyContributorsForGetStatusRequest request = new IdentifyContributorsForGetStatusRequest();
        request.setCorrelationID(conversation.getConversationID());
        request.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        request.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        request.setCollectionID(conversation.settings.getCollectionID());
        request.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        request.setTo(conversation.settings.getCollectionDestination());
        
        monitor.identifyPillarsRequestSent("Identifying contributors for getting status");
        conversation.messageSender.sendMessage(request);
        timer.schedule(identifyTimeoutTask,
                        conversation.settings.getCollectionSettings().getClientSettings().getIdentificationTimeout().longValue());
    }
    
    @Override
    public synchronized void onMessage(GetStatusFinalResponse response) {
        monitor.outOfSequenceMessage("Received GetStatusFinalResponse "
                + "from " + response.getContributor() + " before sending GetStatusRequest.");
    }

    @Override
    public synchronized void onMessage(GetStatusProgressResponse response) {
        monitor.outOfSequenceMessage("Received GetStatusProgressResponse "
                + "from " + response.getContributor() + " before sending GetStatusRequest.");    
    }

    @Override
    public synchronized void onMessage(IdentifyContributorsForGetStatusResponse response) {
        try {
            conversation.selector.processResponse(response);
            monitor.pillarIdentified("Received IdentifyContributorsForGetStatusResponse " + response, response.getContributor());
        } catch (UnexpectedResponseException e) {
            monitor.pillarFailed("Unable to handle IdentifyContributorsForGetStatusResponse, ", e);
        } catch (NegativeResponseException e) {
            monitor.pillarFailed("Negativ IdentifyContributorsForGetStatusResponse from pillar " + response.getContributor(), e);
        }
        
        if (conversation.selector.isFinished()) {
            identifyTimeoutTask.cancel();
            
            if (conversation.selector.getSelectedContributors().isEmpty()) {
                conversation.failConversation("Unable to getStatus, no contributors were identified");
            }
            monitor.pillarSelected("Identified contributors for getStatus", 
                    conversation.selector.getSelectedContributors().toString());
            getStatusFromSelectedPillar();
        }
    }

    @Override
    public boolean hasEnded() {
        return false;
    }

    /**
     * Method for moving to the next stage: GettingChecksum.
     */
    protected void getStatusFromSelectedPillar() {
        identifyTimeoutTask.cancel();
        GettingStatus nextConversationState = new GettingStatus(conversation);
        conversation.conversationState = nextConversationState;
        nextConversationState.start();
    }
    
    /**
     * Method for handling the timeout of the identification.
     */
    private void handleIdentificationTimeout() {
        synchronized (conversation) {
            if (conversation.conversationState == this) {
                if (!conversation.selector.getSelectedContributors().isEmpty()) {
                    monitor.identifyPillarTimeout("Time has run out for identifying contributors. The following " +
                    		"contributors didn't respond: " + conversation.selector.getOutstandingContributors() + 
                    ". Using contributors based on uncomplete set of responses.");
                    getStatusFromSelectedPillar();
                } else {
                    conversation.failConversation("Unable to select contributors, time has run out. " +
                            "The following contributors did't respond: " +
                            conversation.selector.getOutstandingContributors());
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
