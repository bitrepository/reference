/*
 * #%L
 * Bitrepository Access
 * 
 * $Id: IdentifyPillarsForDeleteFile.java 455 2011-11-10 15:18:22Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/main/java/org/bitrepository/modify/deletefile/conversation/IdentifyPillarsForDeleteFile.java $
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
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.OperationFailedEvent;
import org.bitrepository.protocol.exceptions.NegativeResponseException;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;

/**
 * The first state of the DeleteFile communication. The identification of the pillars involved.
 */
public class IdentifyPillarsForDeleteFile extends DeleteFileState {
    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** The timer. Schedules conversation timeouts for this conversation. */
    final Timer timer = new Timer(TIMER_IS_DAEMON);
    /** The task to handle the timeouts for the identification. */
    private TimerTask timerTask = new IdentifyTimerTask();
    
    /** Response status for the pillars.*/
    final PillarsResponseStatus identifyResponseStatus;

    /**
     * Constructor.
     * @param conversation The conversation in this given state.
     */
    public IdentifyPillarsForDeleteFile(SimpleDeleteFileConversation conversation) {
        super(conversation);
        this.identifyResponseStatus = new PillarsResponseStatus(
                conversation.settings.getCollectionSettings().getClientSettings().getPillarIDs());
    }

    /**
     * Starts the conversation by sending the request for identification of the pillars to perform the Delete operation.
     */
    public void start() {
        IdentifyPillarsForDeleteFileRequest identifyRequest = new IdentifyPillarsForDeleteFileRequest();
        identifyRequest.setCorrelationID(conversation.getConversationID());
        identifyRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        identifyRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        identifyRequest.setCollectionID(conversation.settings.getCollectionID());
        identifyRequest.setFileID(conversation.fileID);
        identifyRequest.setAuditTrailInformation(conversation.auditTrailInformation);
        identifyRequest.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        identifyRequest.setTo(conversation.settings.getCollectionDestination());
        conversation.messageSender.sendMessage(identifyRequest);

        monitor.identifyPillarsRequestSent("Identifying pillars for Delete file " + conversation.fileID);
        timer.schedule(timerTask, 
                conversation.settings.getCollectionSettings().getClientSettings().getIdentificationTimeout().longValue());
    }

    @Override
    public synchronized void onMessage(IdentifyPillarsForDeleteFileResponse response) {
        try {
            identifyResponseStatus.responseReceived(response.getPillarID());
        } catch (UnexpectedResponseException e) {
            monitor.invalidMessage("Unexcepted response from " + response.getPillarID() + " : " + e.getMessage());
        }
        
        if(response.getPillarID().equals(conversation.pillarId)) {
            monitor.pillarIdentified("Identified the pillar '" + response.getPillarID() + "' for Delete.", 
                    response.getPillarID());
            monitor.pillarSelected("Identified all pillars for Delete. Starting to Delete.", 
                    response.getPillarID());
            
            // go to next state.
            DeletingFile newState = new DeletingFile(conversation, response.getReplyTo());
            conversation.conversationState = newState;
            newState.start();
        }
    }

    /**
     * Method for handling the DeleteFileProgressResponse message.
     * No such message should be received!
     * @param response The DeleteFileProgressResponse message to handle.
     */
    @Override
    public synchronized void onMessage(DeleteFileProgressResponse response) {
        monitor.outOfSequenceMessage("Received DeleteFileProgressResponse from " + response.getPillarID() + 
                " before sending DeleteFileRequest.");
    }

    /**
     * Method for handling the DeleteFileFinalResponse message.
     * No such message should be received!
     * @param response The DeleteFileFinalResponse message to handle.
     */
    @Override
    public synchronized void onMessage(DeleteFileFinalResponse response) {
        monitor.outOfSequenceMessage("Received DeleteFileFinalResponse from " + response.getPillarID() + 
                " before sending DeleteFileRequest.");
    }

    /**
     * Class for handling the cases, when the identification time runs out.
     */
    private class IdentifyTimerTask extends TimerTask {
        @Override
        public void run() {
        	conversation.failConversation(new OperationFailedEvent(
        	                "Timeout for the identification of the pillars for the DeleteFile operation."));
        }
    }
    
    @Override
    public boolean hasEnded() {
        return false;
    }
}
