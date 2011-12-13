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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;
import org.bitrepository.protocol.pillarselector.SelectedPillarInfo;

/**
 * Models the behavior of a GetChecksums conversation during the operation phase. That is, it begins with the 
 * sending of <code>GetChecksumsRequest</code> messages and finishes with on the reception of the 
 * <code>GetChecksumsFinalResponse</code> messages from the responding pillars.
 * 
 * Note that this is only used by the GetChecksumsConversation in the same package, therefore the visibility is package 
 * protected.
 */
public class GettingChecksums extends GetChecksumsState {
    /** The pillars, which has not yet answered.*/
    private List<SelectedPillarInfo> pillarsSelectedForRequest; 
    /** Tracks who have responded */
    private final PillarsResponseStatus responseStatus;

    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** 
     * The timer for the getChecksumsTimeout. It is run as a daemon thread, eg. it will not prevent the application 
     * from exiting */
    final Timer timer = new Timer(TIMER_IS_DAEMON);
    /** The timer task for timeout of getFile in this conversation. */
    final TimerTask getChecksumsTimeoutTask = new GetChecksumsTimerTask();

    /** The results of the checksums calculations. A map between the pillar and its calculated checksums.*/
    private Map<String, ResultingChecksums> results = new HashMap<String, ResultingChecksums>();

    /**
     * Constructor.
     * @param conversation The conversation where this state belongs.
     */
    public GettingChecksums(SimpleGetChecksumsConversation conversation) {
        super(conversation);
        pillarsSelectedForRequest = conversation.selector.getSelectedPillars();
        responseStatus = new PillarsResponseStatus(
                pillarsSelectedForRequest.toArray(new SelectedPillarInfo[pillarsSelectedForRequest.size()]));
    }

    /**
     * Method for initiating this part of the conversation. Sending the GetChecksumsRequest.
     */
    public void start() {
        GetChecksumsRequest getChecksumsRequest = new GetChecksumsRequest();
        getChecksumsRequest.setCollectionID(
                conversation.settings.getCollectionID());
        getChecksumsRequest.setCorrelationID(conversation.getConversationID());
        getChecksumsRequest.setFileIDs(conversation.fileIDs);
        getChecksumsRequest.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        getChecksumsRequest.setFileChecksumSpec(conversation.checksumSpecifications);
        getChecksumsRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        getChecksumsRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        getChecksumsRequest.setAuditTrailInformation(conversation.auditTrailInformation);

        // Sending one request to each of the identified pillars.
        for(SelectedPillarInfo pillar : pillarsSelectedForRequest) {
            getChecksumsRequest.setPillarID(pillar.getID());
            getChecksumsRequest.setTo(pillar.getDestination());

            if(conversation.uploadUrl != null) {
                // making the URL: 'baseUrl'-'pillarId'
                getChecksumsRequest.setResultAddress(conversation.uploadUrl.toExternalForm() + "-" 
                        + pillar.getID());
            }

            conversation.messageSender.sendMessage(getChecksumsRequest); 
            monitor.requestSent("GetChecksumRequest sent to: " + pillar.getID(), 
                    pillar.getID());
        }

        timer.schedule(getChecksumsTimeoutTask,
                conversation.settings.getCollectionSettings().getClientSettings().getOperationTimeout().longValue());
    }

    @Override
    public void onMessage(IdentifyPillarsForGetChecksumsResponse response) {
        monitor.outOfSequenceMessage("Received IdentifyPillarsForGetChecksumsResponse from " + response.getPillarID() 
                + " after the GetChecksumsRequest has been sent.");
    }

    @Override
    public void onMessage(GetChecksumsProgressResponse response) {
        monitor.progress(new DefaultEvent(OperationEvent.OperationEventType.Progress, 
                "Received progress response for retrieval of checksums " + response.getFileIDs() + ": response"));
    }

    @Override
    public void onMessage(GetChecksumsFinalResponse response) {
        try {
            responseStatus.responseReceived(response.getPillarID());
        } catch (UnexpectedResponseException ure) {
            monitor.warning("Received unexpected final response from " + response.getPillarID() , ure);
        }

        if(isReponseSuccess(response.getResponseInfo())) {
            monitor.pillarComplete(new ChecksumsCompletePillarEvent(
                    response.getResultingChecksums(),
                    conversation.checksumSpecifications,
                    response.getPillarID(),
                    "Received checksum result from " + response.getPillarID()));
            // If calculations in message, then put them into the results map.
            if(response.getResultingChecksums() != null) {
                results.put(response.getPillarID(), response.getResultingChecksums());
            }
        } else {
            monitor.pillarFailed("Received negativ FinalResponse from pillar: " + response.getResponseInfo());
        } 

        if(responseStatus.haveAllPillarResponded()) {
            monitor.complete(new DefaultEvent(OperationEvent.OperationEventType.Complete, 
                    "All pillars have delivered their checksums."));
            conversation.getFlowController().unblock();
            conversation.setResults(results);
            conversation.conversationState = new GetChecksumsFinished(conversation);
        }
    }

    /**
     * Method for validating the FinalResponseInfo.
     * @param frInfo The FinalResponseInfo to be validated.
     * @return Whether the FinalRepsonseInfo tells that the operation has been a success or a failure.
     */
    private boolean isReponseSuccess(ResponseInfo frInfo) { 
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
     * The timer task class for the outstanding get file request. When the time is reached the conversation should be
     * marked as ended.
     */
    private class GetChecksumsTimerTask extends TimerTask {
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
