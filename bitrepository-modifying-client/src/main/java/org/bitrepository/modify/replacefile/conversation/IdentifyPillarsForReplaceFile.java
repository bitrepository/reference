/*
 * #%L
 * Bitrepository Access
 * 
 * $Id: IdentifyPillarsForDeleteFile.java 639 2011-12-15 10:24:45Z jolf $
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
package org.bitrepository.modify.replacefile.conversation;

import java.math.BigInteger;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.OperationFailedEvent;
import org.bitrepository.protocol.exceptions.NegativeResponseException;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;

/**
 * The first state of the ReplaceFile communication. The identification of the pillars involved.
 */
public class IdentifyPillarsForReplaceFile extends ReplaceFileState {
    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** The timer. Schedules conversation timeouts for this conversation. */
    final Timer timer = new Timer(TIMER_IS_DAEMON);
    /** The task to handle the timeouts for the identification. */
    private TimerTask identifyTimeoutTask = new IdentifyTimerTask();
    
    /**
     * Constructor.
     * @param conversation The conversation in this given state.
     */
    public IdentifyPillarsForReplaceFile(SimpleReplaceFileConversation conversation) {
        super(conversation);
    }
    
    /**
     * Starts the conversation by sending the request for identification of the pillars to perform the ReplaceFile
     * operation.
     */
    public void start() {
        IdentifyPillarsForReplaceFileRequest identifyRequest = new IdentifyPillarsForReplaceFileRequest();
        identifyRequest.setCorrelationID(conversation.getConversationID());
        identifyRequest.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        identifyRequest.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        identifyRequest.setCollectionID(conversation.settings.getCollectionID());
        identifyRequest.setFileID(conversation.fileID);
        identifyRequest.setAuditTrailInformation(conversation.auditTrailInformation);
        identifyRequest.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        identifyRequest.setTo(conversation.settings.getCollectionDestination());
        
        monitor.identifyPillarsRequestSent("Identifying pillars for ReplaceFile '" + conversation.fileID + "'");
        conversation.messageSender.sendMessage(identifyRequest);
        timer.schedule(identifyTimeoutTask, 
                conversation.settings.getCollectionSettings().getClientSettings().getIdentificationTimeout().longValue());
    }
    
    @Override
    public synchronized void onMessage(IdentifyPillarsForReplaceFileResponse response) {
        try {
            conversation.pillarSelector.processResponse(response);
            monitor.pillarIdentified("Received IdentifyPillarsForReplaceFileResponse for '" + response.getFileID() + 
                    "' from '" + response.getPillarID() + "' with response '" + 
                    response.getResponseInfo().getResponseText() + "'", response.getPillarID());
        } catch (UnexpectedResponseException e) {
            monitor.pillarFailed("Unable to handle IdentifyPillarsForReplaceFileResponse, ", e);
        } catch (NegativeResponseException e) {
            monitor.pillarFailed("Negativ IdentifyPillarsForReplaceFileResponse from pillar " + response.getPillarID(), e);
        }
        
        if (conversation.pillarSelector.isFinished()) {
            identifyTimeoutTask.cancel();
            
            if (conversation.pillarSelector.getSelectedPillars().isEmpty()) {
                conversation.failConversation("Unable to replace file, no pillars were identified");
            }
            monitor.pillarSelected("Identified pillars for replace file", 
                    conversation.pillarSelector.getSelectedPillars().toString());
            replaceFileAtSelectedPillar();
        }
    }
    
    /**
     * Method for handling the ReplaceFileProgressResponse message.
     * No such message should be received!
     * @param response The ReplaceFileProgressResponse message to handle.
     */
    @Override
    public synchronized void onMessage(ReplaceFileProgressResponse response) {
        monitor.outOfSequenceMessage("Received ReplaceFileProgressResponse from " + response.getPillarID() + 
                " before sending ReplaceFileRequest.");
    }
    
    /**
     * Method for handling the ReplaceFileFinalResponse message.
     * No such message should be received!
     * @param response The ReplaceFileFinalResponse message to handle.
     */
    @Override
    public synchronized void onMessage(ReplaceFileFinalResponse response) {
        monitor.outOfSequenceMessage("Received ReplaceFileFinalResponse from " + response.getPillarID() + 
                " before sending ReplaceFileRequest.");
    }
    
    /**
     * Method for moving to the next stage: ReplaceFile.
     */
    protected void replaceFileAtSelectedPillar() {
        identifyTimeoutTask.cancel();
        ReplacingFile nextConversationState = new ReplacingFile(conversation);
        conversation.conversationState = nextConversationState;
        nextConversationState.start();
    }
    
    /**
     * Class for handling the cases, when the identification time runs out.
     */
    private class IdentifyTimerTask extends TimerTask {
        @Override
        public void run() {
            conversation.failConversation(new OperationFailedEvent(
                    "Timeout for the identification of the pillars for the ReplaceFile operation.",
                    conversation.getConversationID()));
        }
    }
    
    @Override
    public boolean hasEnded() {
        return false;
    }
}
