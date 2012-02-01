/*
 * #%L
 * Bitrepository Modifying Client
 * 
 * $Id: DeletingFile.java 631 2011-12-13 17:56:54Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/main/java/org/bitrepository/modify/deletefile/conversation/DeletingFile.java $
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
package org.bitrepository.modify.replacefile.conversation;

import java.math.BigInteger;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.eventhandler.PillarOperationEvent;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;
import org.bitrepository.protocol.pillarselector.SelectedPillarInfo;

/**
 * Models the behavior of a ReplaceFile conversation during the operation phase. That is, it begins with the sending of
 * <code>ReplaceFileRequest</code> messages and finishes with the reception of the 
 * <code>ReplaceFileFinalResponse</code> messages from all responding pillars. 
 * 
 * Note that this is only used by the ReplaceFileConversation in the same package, therefore the visibility is package 
 * protected.
 */
public class ReplacingFile extends ReplaceFileState {
    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** The timer. Schedules conversation timeouts for this conversation. */
    final Timer timer = new Timer(TIMER_IS_DAEMON);
    /** The task to handle the timeouts for the identification. */
    private TimerTask timerTask = new ReplaceTimerTask();

    /** The pillars, which has not yet answered.*/
    private List<SelectedPillarInfo> pillarsSelectedForRequest; 
    /** The responses for the pillars.*/
    private final PillarsResponseStatus replaceResponseStatus;

    /**
     * Constructor.
     * @param conversation The conversation in this state.
     * @param pillarDest The destination of the pillar.
     */
    public ReplacingFile(SimpleReplaceFileConversation conversation) {
        super(conversation);
        pillarsSelectedForRequest = conversation.pillarSelector.getSelectedPillars();
        replaceResponseStatus = new PillarsResponseStatus(conversation.pillarId);
    }

    /**
     * Method for starting to replace a file on the pillar.
     * The ReplaceFileRequestMessage is created and sent to each of the pillars.
     */
    public void start() {
        // create the message
        ReplaceFileRequest request = new ReplaceFileRequest();
        request.setAuditTrailInformation(conversation.auditTrailInformation);
        request.setChecksumDataForExistingFile(conversation.checksumForFileToDelete);
        request.setChecksumDataForNewFile(conversation.checksumForNewFileValidationAtPillar);
        request.setCollectionID(conversation.settings.getCollectionID());
        request.setCorrelationID(conversation.getConversationID());
        request.setFileAddress(conversation.urlOfNewFile.toExternalForm());
        request.setChecksumRequestForNewFile(conversation.checksumRequestForNewFile);
        request.setFileID(conversation.fileID);
        request.setFileSize(BigInteger.valueOf(conversation.sizeOfNewFile));
        request.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        request.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        request.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        
        for(SelectedPillarInfo pillarInfo : pillarsSelectedForRequest) {
            request.setPillarID(pillarInfo.getID());
            request.setTo(pillarInfo.getDestination());
            
            conversation.messageSender.sendMessage(request);
            monitor.requestSent("Request to replace file " + conversation.fileID + " has been sent to the pillar + " +
                    conversation.pillarId, pillarInfo.getID());
        }

        timer.schedule(timerTask, 
                conversation.settings.getCollectionSettings().getClientSettings().getOperationTimeout().longValue());
    }

    /**
     * Method for handling the IdentifyPillarsForReplaceFileResponse message.
     * No such message should be received.
     * @param response The IdentifyPillarsForReplaceFileResponse message to handle.
     */
    @Override
    public void onMessage(IdentifyPillarsForReplaceFileResponse response) {
        monitor.debug("Received IdentifyPillarsForReplaceFileResponse from '" + response.getPillarID() 
                + "' after the ReplaceFileRequests has been sent.");
    }

    /**
     * Handles the ReplaceFileProgressResponse message.
     * Just logged to it.
     * 
     * @param response The ReplaceFileProgressResponse to be handled by this method.
     */
    @Override
    public void onMessage(ReplaceFileProgressResponse response) {
        monitor.progress(new PillarOperationEvent(OperationEvent.OperationEventType.PROGRESS, 
                "Received ReplaceFileProgressResponse from pillar " + response.getPillarID() + ": " + 
                        response.getResponseInfo().getResponseText(), response.getPillarID()));
    }

    /**
     * Handles the ReplaceFileFinalResponse message.
     * Is validated and removed from the list of pillars who should perform the ReplaceFile operation.
     * If it is the response from the last responding pillar, then the conversation is complete.
     * 
     * @param response The ReplaceFileFinalResponse message to be handled.
     */
    @Override
    public void onMessage(ReplaceFileFinalResponse response) {
        try {
            replaceResponseStatus.responseReceived(response.getPillarID());
        } catch (UnexpectedResponseException ure) {
            monitor.warning("Received unexpected final response from " + response.getPillarID() , ure);
        }

        if(isResponseSuccess(response.getResponseInfo())) {
            monitor.pillarComplete(new ReplaceFileCompletePillarEvent(
                    response.getChecksumDataForNewFile(),
                    response.getPillarID(),
                    "Received replace file result from " + response.getPillarID()));
        } else {
            monitor.pillarFailed("Received negativ FinalResponse from pillar: " + response.getResponseInfo());
        }
        
        // Check if the conversation has finished.
        if(replaceResponseStatus.haveAllPillarResponded()) {
            timerTask.cancel();
            monitor.complete(new DefaultEvent(OperationEvent.OperationEventType.COMPLETE,
                    "Finished the ReplaceFile operation on all the pillars."));
            conversation.getFlowController().unblock();

            ReplaceFileFinished finishState = new ReplaceFileFinished(conversation);
            conversation.conversationState = finishState;
        }
    }
    
    /**
     * Method for validating the FinalResponseInfo.
     * @param frInfo The FinalResponseInfo to be validated.
     * @return Whether the FinalRepsonseInfo tells that the operation has been a success or a failure.
     */
    private boolean isResponseSuccess(ResponseInfo frInfo) {
        // validate the response info.
        if(frInfo == null || frInfo.getResponseCode() == null) {
            return false;
        } else {
            if(ResponseCode.OPERATION_COMPLETED.equals(frInfo.getResponseCode())) {
                return true;
            } 
        }
        return false;
    }

    /**
     * What should we do if something fails during a replace operation.
     * @param info The information about why is was incomplete.
     */
    private void handleIncompleteReplace(String info) {
        conversation.failConversation(info);
    }

    /**
     * Class for handling a timeout for the conversation.
     */
    private class ReplaceTimerTask extends TimerTask {
        @Override
        public void run() {
            handleIncompleteReplace("Timeout occurred for the ReplaceFile operation.");
        }
    }

    @Override
    public boolean hasEnded() {
        return false;
    }
}
