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
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.ContributorResponseStatus;
import org.bitrepository.protocol.pillarselector.SelectedPillarInfo;

public class GettingStatus extends GetStatusState {
    /** The pillars, which has not yet answered.*/
    private List<SelectedPillarInfo> contributorsSelectedForRequest; 
    /** Tracks who have responded */
    private final ContributorResponseStatus responseStatus;

    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** 
     * The timer for the getChecksumsTimeout. It is run as a daemon thread, eg. it will not prevent the application 
     * from exiting */
    final Timer timer = new Timer(TIMER_IS_DAEMON);
    /** The timer task for timeout of getFile in this conversation. */
    final TimerTask getChecksumsTimeoutTask = new GetStatusTimerTask();
    
    
    public GettingStatus(SimpleGetStatusConversation conversation) {
        super(conversation);
        contributorsSelectedForRequest = conversation.selector.getSelectedContributors();
        responseStatus = new ContributorResponseStatus(
                contributorsSelectedForRequest.toArray(
                        new SelectedPillarInfo[conversation.selector.getSelectedContributors().size()]));
    }

    @Override
    public void start() {
        GetStatusRequest request = new GetStatusRequest();
        request.setCollectionID(conversation.settings.getCollectionID());
        request.setCorrelationID(conversation.getConversationID());
        request.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        request.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        request.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));

        // Sending one request to each of the identified pillars.
        for(SelectedPillarInfo contributor : contributorsSelectedForRequest) {
            request.setContributor(contributor.getID());
            request.setTo(contributor.getDestination());
           
            monitor.requestSent("GetChecksumRequest sent to: " + contributor.getID(), 
                    contributor.getID());
            conversation.messageSender.sendMessage(request); 
        }

        timer.schedule(getChecksumsTimeoutTask,
                conversation.settings.getCollectionSettings().getClientSettings().getOperationTimeout().longValue());
    }

    @Override
    public synchronized void onMessage(GetStatusFinalResponse response) {
        try {
            responseStatus.responseReceived(response.getContributor());
        } catch (UnexpectedResponseException ure) {
            monitor.warning("Received unexpected final response from " + response.getContributor() , ure);
        }

        if(isReponseSuccess(response.getResponseInfo())) {
            monitor.pillarComplete(new StatusCompleteContributorEvent(
                    "Received status result from " + response.getContributor(), 
                    response.getContributor(), response.getResultingStatus()));
        } else {
            monitor.contributorFailed("Received negativ FinalResponse from contributor: " + response.getResponseInfo());
        } 

        if(responseStatus.haveAllPillarResponded()) {
            monitor.complete(new DefaultEvent(OperationEvent.OperationEventType.COMPLETE, 
                    "All contributors have delivered their status."));
            conversation.conversationState = new GetStatusFinished(conversation);
        }    }

    @Override
    public synchronized void onMessage(GetStatusProgressResponse response) {
        monitor.progress(new DefaultEvent(OperationEvent.OperationEventType.PROGRESS, 
                "Received progress response for retrieval of status from " + response.getContributor()));
    }

    @Override
    public synchronized void onMessage(IdentifyContributorsForGetStatusResponse response) {
        monitor.outOfSequenceMessage("Received IdentifyContributorsForGetStatusResponse from " + response.getContributor() 
                + " after the GetStatusRequest has been sent.");
    }
    
    @Override
    public boolean hasEnded() {
        return false;
    }

    /**
     * Method for validating the FinalResponseInfo.
     * @param frInfo The FinalResponseInfo to be validated.
     * @return Whether the FinalRepsonseInfo tells that the operation has been a success or a failure.
     */
    private boolean isReponseSuccess(ResponseInfo frInfo) { 
        if(ResponseCode.OPERATION_COMPLETED.equals(frInfo.getResponseCode())) {
            return true;
        }
        return false;
    }
    
    /**
     * Method for handling a timeout for this operation.
     */
    protected void handleTimeout() {
        if (!conversation.hasEnded()) { 
            conversation.failConversation("No GetStatusFinalResponse received before timeout");
        }
    }
    
    /**
     * The timer task class for the outstanding get file request. When the time is reached the conversation should be
     * marked as ended.
     */
    private class GetStatusTimerTask extends TimerTask {
        @Override
        public void run() {
            handleTimeout();
        }
    }

}
