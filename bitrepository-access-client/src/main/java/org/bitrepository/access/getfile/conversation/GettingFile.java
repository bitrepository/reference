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
package org.bitrepository.access.getfile.conversation;

import java.math.BigInteger;
import java.util.TimerTask;

import org.bitrepository.access.getfile.selectors.FastestPillarSelectorForGetFile;
import org.bitrepository.access.getfile.selectors.SpecificPillarSelectorForGetFile;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.eventhandler.PillarOperationEvent;
import org.bitrepository.protocol.exceptions.NoPillarFoundException;
import org.bitrepository.protocol.exceptions.OperationTimeOutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GettingFile extends GetFileState {

    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The timer task for timeout of getFile in this conversation. */
    final TimerTask getFileTimeoutTask = new GetFileTimerTask();

    public GettingFile(SimpleGetFileConversation conversation) {
        super(conversation);
    }
    /**
     * Sends a getFile request 
     * 
     * Also sets up a timer task to avoid waiting to long for the request to complete.
     */
    void start() {

        if (conversation.selector.getIDForSelectedPillar() == null) {
            conversation.throwException(new NoPillarFoundException("Unable to getFile, no pillar was selected"));
        } else {
            if (conversation.eventHandler != null) {
                conversation.eventHandler.handleEvent(
                        new PillarOperationEvent(OperationEvent.OperationEventType.PillarSelected, 
                                conversation.selector.getIDForSelectedPillar(),
                                conversation.selector.getIDForSelectedPillar()));
            }
        }

        GetFileRequest getFileRequest = new GetFileRequest();
        getFileRequest.setBitRepositoryCollectionID(conversation.settings.getBitRepositoryCollectionID());
        getFileRequest.setCorrelationID(conversation.getConversationID());
        getFileRequest.setFileAddress(conversation.uploadUrl.toExternalForm());
        getFileRequest.setFileID(conversation.fileID);
        getFileRequest.setPillarID(conversation.selector.getIDForSelectedPillar());
        getFileRequest.setReplyTo(conversation.settings.getClientTopicID());
        getFileRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        getFileRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        getFileRequest.setTo(conversation.selector.getDestinationForSelectedPillar());

        conversation.messageSender.sendMessage(getFileRequest); 
        timer.schedule(getFileTimeoutTask, getMaxTimeToWaitForGetFileToComplete(conversation.selector.getTimeToDeliver()));
        if (conversation.eventHandler != null) {
            conversation.eventHandler.handleEvent(
                    new PillarOperationEvent(OperationEvent.OperationEventType.RequestSent, 
                            conversation.selector.getIDForSelectedPillar(), 
                            conversation.selector.getIDForSelectedPillar()));
        }
    }

    /**
     * Method for handling the GetFileProgressResponse messages.
     * Currently logs the progress response, but does nothing else.
     *
     * @param msg The GetFileProgressResponse message to be handled by this method.
     */
    @Override
    public void onMessage(GetFileProgressResponse msg) {
        log.debug("Received progress response for retrieval of file " + msg);
        if (conversation.eventHandler != null) {
            conversation.eventHandler.handleEvent(
                    new DefaultEvent(OperationEvent.OperationEventType.Progress, 
                            msg.getProgressResponseInfo().toString()));
        }
    }

    /**
     * Method for final response the get.
     *
     * @param msg The GetFileFinalResponse message to be handled by this method.
     */
    @Override
    public void onMessage(GetFileFinalResponse msg) {
        ArgumentValidator.checkNotNull(msg, "GetFileFinalResponse");
        getFileTimeoutTask.cancel();
        if (conversation.eventHandler != null) {
            conversation.eventHandler.handleEvent(
                    new DefaultEvent(OperationEvent.OperationEventType.Complete, ""));
        }
        log.debug("(ConversationID: " + conversation.getConversationID() +  ") " +
                "Finished getting file " + msg.getFileID() + " from " + msg.getPillarID());
        // TODO: Race condition, what if timeout task already triggered this just before now
        endConversation();
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse response) {
        if (conversation.selector instanceof SpecificPillarSelectorForGetFile) {
            log.debug("(ConversationID: " + conversation.getConversationID() +  ") " +
                    "Received IdentifyPillarsForGetFileResponse from " + response.getPillarID() + 
                    " after selecting specific pillar " + conversation.selector.getIDForSelectedPillar());
        } else if (conversation.selector instanceof FastestPillarSelectorForGetFile) {
            log.warn("(ConversationID: " + conversation.getConversationID() + ") " +
                    "Received IdentifyPillarsForGetFileResponse from " + response.getPillarID() + 
                    " after selecting fastest pillar " + conversation.selector.getIDForSelectedPillar());
        }
    }

    private long getMaxTimeToWaitForGetFileToComplete(TimeMeasureTYPE estimatedTimeToDeliver) {
        //ToDo What to do
        return 10000;
    }

    /**
     * The timer task class for the outstanding get file request. When the time is reached the conversation should be
     * marked as ended.
     */
    private class GetFileTimerTask extends TimerTask {
        @Override
        public void run() {
            synchronized (conversation) {
                log.warn("No GetFileFinalResponse received before timeout for file " + conversation.fileID + 
                        " from pillar " + conversation.selector.getIDForSelectedPillar());
                endConversation();
                if (conversation.eventHandler != null) {
                    conversation.eventHandler.handleEvent(
                            new DefaultEvent(OperationEvent.OperationEventType.RequestTimeOut, 
                            "No GetFileFinalResponse received before timeout"));
                } else {
                    conversation.throwException(new OperationTimeOutException("No GetFileFinalResponse received before timeout"));
                }		
            }
        }
    }
}
