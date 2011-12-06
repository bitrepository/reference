/*
 * #%L
 * Bitrepository Modifying Client
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
package org.bitrepository.modify.deletefile.conversation;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.eventhandler.PillarOperationEvent;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;

public class DeletingFile extends DeleteFileState {
    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** The timer. Schedules conversation timeouts for this conversation. */
    final Timer timer = new Timer(TIMER_IS_DAEMON);
    /** The task to handle the timeouts for the identification. */
    private TimerTask timerTask = new DeleteTimerTask();

    /** The responses for the pillars.*/
    final PillarsResponseStatus deleteResponseStatus;

    /** The destination of the pillar to delete the file from.*/
    final String pillarDest;

    /**
     * Constructor.
     * @param conversation The conversation in this state.
     * @param pillarDest The destination of the pillar.
     */
    public DeletingFile(SimpleDeleteFileConversation conversation, String pillarDest) {
        super(conversation);
        this.pillarDest = pillarDest;
        deleteResponseStatus = new PillarsResponseStatus(Arrays.asList(conversation.pillarId));
    }

    /**
     * Method for starting to delete a file on the pillar.
     * The DeleteFileRequestMessage is created and sent to each of the pillars.
     */
    public void start() {
        // create the message
        DeleteFileRequest request = new DeleteFileRequest();
        request.setCorrelationID(conversation.getConversationID());
        request.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        request.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        request.setCollectionID(conversation.settings.getCollectionID());
        request.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        request.setAuditTrailInformation(conversation.auditTrailInformation);
        request.setFileID(conversation.fileID);
        request.setPillarID(conversation.pillarId);
        request.setTo(pillarDest);
        request.setFileChecksumSpec(conversation.checksumSpecRequested);
        
        ChecksumDataForFileTYPE checksumData = new ChecksumDataForFileTYPE();
        checksumData.setChecksumSpec(conversation.checksumSpecOfFileToDelete);
        checksumData.setChecksumValue(conversation.checksumOfFileToDelete);
        // TODO retrieve the actual date?
        checksumData.setCalculationTimestamp(CalendarUtils.getEpoch());
        request.setChecksumDataForFile(checksumData);
        
        conversation.messageSender.sendMessage(request);
        
        monitor.requestSent("Request to delete file " + conversation.fileID + " has been sent to the pillar + " +
                conversation.pillarId, conversation.pillarId);

        timer.schedule(timerTask, 
                conversation.settings.getReferenceSettings().getClientSettings().getConversationTimeout().longValue());
    }

    /**
     * Method for handling the IdentifyPillarsForDeleteFileResponse message.
     * No such message should be received.
     * @param response The IdentifyPillarsForDeleteFileResponse message to handle.
     */
    @Override
    public void onMessage(IdentifyPillarsForDeleteFileResponse response) {
        monitor.debug("Received IdentifyPillarsForDeleteFileResponse from '" + response.getPillarID() 
                + "' after the DeleteFileRequests has been sent.");
    }

    /**
     * Handles the DeleteFileProgressResponse message.
     * Just logged to it.
     * 
     * @param response The DeleteFileProgressResponse to be handled by this method.
     */
    @Override
    public void onMessage(DeleteFileProgressResponse response) {
        monitor.progress(new PillarOperationEvent(OperationEvent.OperationEventType.Progress, 
                "Received DeleteFileProgressResponse from pillar " + response.getPillarID() + ": " + 
                        response.getResponseInfo().getResponseText(), response.getPillarID()));
    }

    @Override
    public void onMessage(DeleteFileFinalResponse response) {
        try {
            deleteResponseStatus.responseReceived(response.getPillarID());
        } catch (UnexpectedResponseException ure) {
            monitor.pillarFailed("Received unexpected final response from " + response.getPillarID() , ure);
        }

        if(isResponseSuccess(response.getResponseInfo())) {
            monitor.pillarComplete(new PillarOperationEvent(
                    OperationEventType.PillarComplete,
                    response.getPillarID(),
                    "Received checksum result from " + response.getPillarID()));
        } else {
            monitor.pillarFailed("Received negativ FinalResponse from pillar: " + response.getResponseInfo());
        } 

        // Check if the conversation has finished.
        if(deleteResponseStatus.haveAllPillarResponded()) {
            timerTask.cancel();
            monitor.complete(new DefaultEvent(OperationEvent.OperationEventType.Complete,
                    "Finished Delete on all the pillars."));

            DeleteFileFinished finishState = new DeleteFileFinished(conversation);
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
            if(ResponseCode.SUCCESS.equals(frInfo.getResponseCode())) {
                return true;
            } 
        }
        return false;
    }

    /**
     * What should we do if something fails during a delete operation.
     * @param info The information about why is was incomplete.
     */
    private void handleIncompleteDelete(String info) {
        conversation.failConversation(info);
    }

    /**
     * Class for handling a timeout for the conversation.
     */
    private class DeleteTimerTask extends TimerTask {
        @Override
        public void run() {
            handleIncompleteDelete("Timeout occurred for the Deleting of the file.");
        }
    }

    @Override
    public boolean hasEnded() {
        return false;
    }
}
