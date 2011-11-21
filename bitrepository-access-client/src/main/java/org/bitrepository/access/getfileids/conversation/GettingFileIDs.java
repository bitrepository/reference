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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;
import org.bitrepository.protocol.pillarselector.SelectedPillarInfo;

/**
 * Models the behavior of a GetFileIDs conversation during the operation phase. That is, it begins with the 
 * sending of <code>GetFileIDsRequest</code> messages and finishes with on the reception of the 
 * <code>GetFileIDsFinalResponse</code> messages from the responding pillars.
 * 
 * Note that this is only used by the GetFileIDsConversation in the same package, therefore the visibility is package 
 * protected.
 */
public class GettingFileIDs extends GetFileIDsState {
    /** The pillars, which has not yet answered.*/
    private List<SelectedPillarInfo> pillarsSelectedForRequest; 
    /** The status for the expected responses.*/
    private final PillarsResponseStatus responseStatus;

    /** 
     * The timer for the getFileTimeout. It is run as a daemon thread, eg. it will not prevent the application from 
     * exiting */
    final Timer timer = new Timer(true);
    /** The timer task for timeout of getFile in this conversation. */
    final TimerTask getFileIDsTimeoutTask = new GetFileIDsTimerTask();

    /** The results of the checksums calculations. A map between the pillar and its calculated checksums.*/
    private Map<String, ResultingFileIDs> results = new HashMap<String, ResultingFileIDs>();

    /**
     * Constructor.
     * @param conversation The conversation where this state belongs.
     */
    public GettingFileIDs(SimpleGetFileIDsConversation conversation) {
        super(conversation);
        pillarsSelectedForRequest = conversation.selector.getSelectedPillars();
        responseStatus = new PillarsResponseStatus(
                pillarsSelectedForRequest.toArray(new SelectedPillarInfo[pillarsSelectedForRequest.size()]));
    }

    /**
     * Method for initiating this part of the conversation. Sending the GetFileIDsRequest.
     */
    public void start() {
        GetFileIDsRequest getFileIDsRequest = new GetFileIDsRequest();
        getFileIDsRequest.setCollectionID(conversation.settings.getCollectionID());
        getFileIDsRequest.setCorrelationID(conversation.getConversationID());
        getFileIDsRequest.setFileIDs(conversation.fileIDs);
        getFileIDsRequest.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        getFileIDsRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        getFileIDsRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        getFileIDsRequest.setAuditTrailInformation(conversation.auditTrailInformation);

        // Sending one request to each of the identified pillars.
        for(SelectedPillarInfo pillar : pillarsSelectedForRequest) {
            getFileIDsRequest.setPillarID(pillar.getID());
            getFileIDsRequest.setTo(pillar.getDestination());

            if(conversation.uploadUrl != null) {
                // making the URL: 'baseUrl'-'pillarId'
                getFileIDsRequest.setResultAddress(conversation.uploadUrl.toExternalForm() + "-" 
                        + pillar.getID());
            }

            conversation.messageSender.sendMessage(getFileIDsRequest); 
            monitor.requestSent("GetFileIDsRequest sent to: " + pillar.getID(), 
                    pillar.getID());
        }

        timer.schedule(getFileIDsTimeoutTask,
                conversation.settings.getCollectionSettings().getClientSettings().getOperationTimeout().longValue());
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileIDsResponse response) {
        monitor.outOfSequenceMessage("Received IdentifyPillarsForGetFileIDsResponse from " + response.getPillarID() 
                + " after the GetFileIDsRequest has been sent.");
    }

    @Override
    public void onMessage(GetFileIDsProgressResponse response) {
        monitor.progress(new DefaultEvent(OperationEvent.OperationEventType.Progress, 
                "Received progress response for retrieval of file ids " + response.getFileIDs()));
    }

    @Override
    public void onMessage(GetFileIDsFinalResponse response) {
        try {
            responseStatus.responseReceived(response.getPillarID());
            
            if(isReponseSuccess(response.getResponseInfo())) {
                monitor.pillarComplete(new FileIDsCompletePillarEvent(
                        response.getResultingFileIDs(),
                        response.getPillarID(),
                        "Received file ids result from " + response.getPillarID()));
                // If calculations in message, then put them into the results map.
                if(response.getResultingFileIDs() != null) {
                    results.put(response.getPillarID(), response.getResultingFileIDs());
                }
            } else {
                monitor.pillarFailed("Received negativ FinalResponse from pillar: " + response.getResponseInfo());
            } 
        } catch (UnexpectedResponseException ure) {
            monitor.warning("Received bad FinalResponse from pillar: " + response.getResponseInfo(), ure);
        }

        if(responseStatus.haveAllPillarResponded()) {
            monitor.complete(new DefaultEvent(OperationEvent.OperationEventType.Complete, 
                    "All pillars have delivered their FileIDs."));
            conversation.getFlowController().unblock();
            conversation.conversationState = new GetFileIDsFinished(conversation);
        }
    }

    /**
     * Method for validating the FinalResponseInfo.
     * @param frInfo The FinalResponseInfo to be validated.
     * @return Whether the FinalRepsonseInfo tells that the operation has been a success or a failure.
     */
    private boolean isReponseSuccess(ResponseInfo frInfo) throws UnexpectedResponseException { 
        if(ResponseCode.SUCCESS.equals(frInfo.getResponseCode())) {
            return true;
        } 
        return false;
    }
    
    /**
     * Method for handling a timeout for this operation.
     */
    protected void handleTimeout() {
        if (!conversation.hasEnded()) { 
            conversation.failConversation("No GetFileFinalResponse received before timeout");
        }
    }

    /**
     * The timer task class for the outstanding get file ids request. When the time is reached the conversation 
     * should be marked as ended.
     */
    private class GetFileIDsTimerTask extends TimerTask {
        @Override
        public void run() {
            handleTimeout();
        }
    }

    @Override
    public boolean hasEnded() {
        return false;
    }
}
