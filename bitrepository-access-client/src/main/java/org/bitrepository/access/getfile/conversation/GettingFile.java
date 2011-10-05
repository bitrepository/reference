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
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.access.getfile.selectors.FastestPillarSelectorForGetFile;
import org.bitrepository.access.getfile.selectors.SpecificPillarSelectorForGetFile;
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
import org.bitrepository.protocol.time.TimeMeasureComparator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Models the behavior of a GetFile conversation during the file exchange phase. That is, it begins with the sending of
 * a <code>GetFileRequest</code> and finishes with on the reception of a <code>GetFileFinalResponse</code> message.
 * 
 * Note that this is only used by the GetFileConversation in the same package, therefore the visibility is package 
 * protected.
 */
class GettingFile extends GetFileState {

    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** 
     * The timer for the getFileTimeout. It is run as a daemon thread, eg. it will not prevent the application from 
     * exiting */
    final Timer timer = new Timer(true);
    /** The timer task for timeout of getFile in this conversation. */
    final TimerTask getFileTimeoutTask = new GetFileTimerTask();

    /** 
     * The constructor for the indicated conversation.
     * @param conversation The related conversation containing context information.
     */
    public GettingFile(SimpleGetFileConversation conversation) {
        super(conversation);
    }
    /**
     * Sends a getFile request 
     * 
     * Also sets up a timer task to avoid waiting to long for the request to complete.
     */
    public void start() {
        String info = "Getting file " + conversation.fileID + " from pillar " + 
    conversation.selector.getIDForSelectedPillar();
        if (conversation.selector.getIDForSelectedPillar() == null) {
            conversation.throwException(new NoPillarFoundException("Unable to getFile, no pillar was selected"));
        } else {
            if (conversation.eventHandler != null) {
                conversation.eventHandler.handleEvent(
                        new PillarOperationEvent(OperationEvent.OperationEventType.PillarSelected, 
                                info,
                                conversation.selector.getIDForSelectedPillar()));
            }
        }

        GetFileRequest getFileRequest = new GetFileRequest();
        getFileRequest.setBitRepositoryCollectionID(conversation.settings.getBitRepositoryCollectionID());
        getFileRequest.setCorrelationID(conversation.getConversationID());
        getFileRequest.setFileAddress(conversation.uploadUrl.toExternalForm());
        getFileRequest.setFileID(conversation.fileID);
        getFileRequest.setPillarID(conversation.selector.getIDForSelectedPillar());
        getFileRequest.setReplyTo(conversation.settings.getProtocol().getLocalDestination());
        getFileRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        getFileRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        getFileRequest.setTo(conversation.selector.getDestinationForSelectedPillar());

        conversation.messageSender.sendMessage(getFileRequest); 
        timer.schedule(getFileTimeoutTask, getMaxTimeToWaitForGetFileToComplete());
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
        String info = "Received progress response for retrieval of file " + msg.getFileID() + " : \n{}";
        log.debug("(ConversationID: " + conversation.getConversationID() + ") " + info, msg);
        if (conversation.eventHandler != null) {
            conversation.eventHandler.handleEvent(
                    new DefaultEvent(OperationEvent.OperationEventType.Progress, 
                            info + msg.getProgressResponseInfo()));
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
        String message =  "Finished getting file " + msg.getFileID() + " from " + msg.getPillarID();
        if (conversation.eventHandler != null) {
            conversation.eventHandler.handleEvent(
                    new DefaultEvent(OperationEvent.OperationEventType.Complete, message));
        }
        log.debug("(ConversationID: " + conversation.getConversationID() +  ") " + message);
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

    /**
     * @return Returns the maximum time to wait for a GetFile primitive to complete.
     */
    private long getMaxTimeToWaitForGetFileToComplete() {
        return TimeMeasureComparator.getTimeMeasureInLong(conversation.settings.getGetFile().getOperationTimeout());
    }

    /**
     * The timer task class for the outstanding get file request. When the time is reached the conversation should be
     * marked as ended.
     */
    private class GetFileTimerTask extends TimerTask {
        @Override
        public void run() {
            synchronized (conversation) {
                if (!conversation.hasEnded()) { 
                    log.warn("No GetFileFinalResponse received before timeout for file " + conversation.fileID + 
                            " from pillar " + conversation.selector.getIDForSelectedPillar());
                    endConversation();
                    if (conversation.eventHandler != null) {
                        conversation.eventHandler.handleEvent(
                                new DefaultEvent(OperationEvent.OperationEventType.Failed, 
                                "No GetFileFinalResponse received before timeout"));
                    } else {
                        conversation.throwException(new OperationTimeOutException("No GetFileFinalResponse received before timeout"));
                    }		
                }
            }
        }
    }
}
