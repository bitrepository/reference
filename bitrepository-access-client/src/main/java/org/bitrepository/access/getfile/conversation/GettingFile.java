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
import org.bitrepository.client.eventhandler.DefaultEvent;
import org.bitrepository.client.eventhandler.OperationEvent;
/**
 * Models the behavior of a GetFile conversation during the file exchange phase. That is, it begins with the sending of
 * a <code>GetFileRequest</code> and finishes with on the reception of a <code>GetFileFinalResponse</code> message.
 * 
 * Note that this is only used by the GetFileConversation in the same package, therefore the visibility is package 
 * protected.
 */
class GettingFile extends GetFileState {

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
        GetFileRequest getFileRequest = new GetFileRequest();
        getFileRequest.setCollectionID(conversation.settings.getCollectionID());
        getFileRequest.setCorrelationID(conversation.getConversationID());
        getFileRequest.setFileAddress(conversation.uploadUrl.toExternalForm());
        getFileRequest.setFileID(conversation.fileID);
        getFileRequest.setPillarID(conversation.selector.getSelectedPillar().getID());
        getFileRequest.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        getFileRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        getFileRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        getFileRequest.setTo(conversation.selector.getSelectedPillar().getDestination());
        getFileRequest.setFrom(conversation.clientID);
        
        monitor.requestSent("Sending getFileRequest to " + conversation.selector.getSelectedPillar().getID(), 
                conversation.selector.getSelectedPillar().getID());
        conversation.messageSender.sendMessage(getFileRequest); 
        timer.schedule(getFileTimeoutTask, getMaxTimeToWaitForGetFileToComplete());
    }

    /**
     * Method for handling the GetFileProgressResponse messages.
     *
     * @param msg The GetFileProgressResponse message to be handled by this method.
     */
    @Override
    public void onMessage(GetFileProgressResponse msg) {
        monitor.progress(new DefaultEvent(OperationEvent.OperationEventType.PROGRESS, 
                "Received progress response for retrieval of file " + msg.getFileID() + ":\n" + 
                        msg.getResponseInfo()));
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
        monitor.complete(new DefaultEvent(OperationEvent.OperationEventType.COMPLETE, 
                "Finished getting file " + msg.getFileID() + " from " + msg.getPillarID()));
    }

    @Override
    public void onMessage(IdentifyPillarsForGetFileResponse response) {
        if (conversation.selector instanceof SpecificPillarSelectorForGetFile) {
            monitor.debug("Received IdentifyPillarsForGetFileResponse from " + response.getPillarID() 
                    + " after the specific pillar was selected. No problem at all.");
        } else if (conversation.selector instanceof FastestPillarSelectorForGetFile) {
            monitor.outOfSequenceMessage("Received IdentifyPillarsForGetFileResponse from " + response.getPillarID() + 
                    " after selecting fastest pillar " + conversation.selector.getSelectedPillar().getID());
        }
    }

    /**
     * @return Returns the maximum time to wait for a GetFile primitive to complete.
     */
    private long getMaxTimeToWaitForGetFileToComplete() {
        return conversation.settings.getCollectionSettings().getClientSettings().getOperationTimeout().longValue();
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
                    conversation.failConversation("No GetFileFinalResponse received before timeout for file " + conversation.fileID + 
                            " from pillar " + conversation.selector.getSelectedPillar().getID());
                }
            }
        }
    }
    
    @Override
    public boolean hasEnded() {
        return false;
    }
}
