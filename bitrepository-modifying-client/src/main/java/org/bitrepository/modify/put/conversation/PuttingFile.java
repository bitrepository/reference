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
package org.bitrepository.modify.put.conversation;

import java.math.BigInteger;
import java.util.Map;
import java.util.TimerTask;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The state for the PutFile communication, where the file is put to the pillars (the pillars are requested to retrieve
 * the file).
 */
public class PuttingFile extends PutFileState {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The task to handle the timeouts for the identification.
     */
    private TimerTask timerTask = new PutTimerTask();
    
    /**
     * Map between the pillars and their destinations.
     */
    final Map<String, String> pillarDestinations;

    /**
     * Constructor.
     * @param conversation The conversation in this state.
     */
    public PuttingFile(SimplePutFileConversation conversation, Map<String, String> pillarsDests) {
        super(conversation);
        this.pillarDestinations = pillarsDests;
    }

    /**
     * Method for starting to put the files to the pillars.
     * The PutFileRequestMessage is created and sent to each of the pillars.
     */
    public void start() {
        // Create the message.
        PutFileRequest putMsg = new PutFileRequest();
        putMsg.setCorrelationID(conversation.getConversationID());
        putMsg.setMinVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_MIN_VERSION));
        putMsg.setVersion(BigInteger.valueOf(ProtocolConstants.PROTOCOL_VERSION));
        putMsg.setBitRepositoryCollectionID(conversation.settings.getBitRepositoryCollectionID());
        putMsg.setReplyTo(conversation.settings.getClientTopicID());
        putMsg.setFileAddress(conversation.downloadUrl.toExternalForm());
        putMsg.setFileID(conversation.fileID);
        putMsg.setFileSize(conversation.fileSize);
        putMsg.setAuditTrailInformation(conversation.settings.getAuditTrailInformation());
        
        // TODO handle these
//        putMsg.setChecksumsDataForNewFile(null);
//        putMsg.setChecksumSpecs(null);
        
        // Send the message to each pillar.
        for(Map.Entry<String, String> pillarDest : pillarDestinations.entrySet()) {
            putMsg.setPillarID(pillarDest.getKey());
            putMsg.setTo(pillarDest.getValue());
            conversation.messageSender.sendMessage(putMsg);
        }
        
        // Tell the eventhandle that the requests has been sent.
        if(conversation.eventHandler != null) {
            conversation.eventHandler.handleEvent(new DefaultEvent(OperationEventType.RequestSent, 
                    "Request to put file has been sent to pilars in collection '" 
                    + conversation.settings.getBitRepositoryCollectionID() + "'."));
        }
        
        // Set timeout.
        timer.schedule(timerTask, conversation.settings.getConversationTimeout());
    }

    /**
     * Method for handling the IdentifyPillarsForPutFileResponse message.
     * No such message should be received.
     * @param response The IdentifyPillarsForPutFileResponse message to handle.
     */
    @Override
    public void onMessage(IdentifyPillarsForPutFileResponse response) {
        log.warn("(ConversationID: " + conversation.getConversationID() + ") " 
                + "Received IdentifyPillarsForPutFileResponse from '" + response.getPillarID() 
                + "' after the PutFileRequests has been sent.");
    }

    /**
     * Handles the PutFileProgressResponse message.
     * Just logged to the 
     * 
     * @param response The PutFileProgressResponse to be handled by this method.
     */
    @Override
    public void onMessage(PutFileProgressResponse response) {
        log.debug("(ConversationID: " + conversation.getConversationID() + ") " +
                "Received PutFileProgressResponse from " + response.getPillarID() + " : \n{}", response);
        if (conversation.eventHandler != null) {
            conversation.eventHandler.handleEvent(
                    new DefaultEvent(OperationEvent.OperationEventType.Progress, 
                            "Received progress report from "));
        }
    }

    @Override
    public void onMessage(PutFileFinalResponse response) {
        log.debug("(ConversationID: " + conversation.getConversationID() + ") " + "Received PutFileFinalResponse from " 
                + response.getPillarID() + "'.");
        
        try {
            conversation.putResponseStatus.responseReceived(response.getPillarID());
            if (conversation.eventHandler != null) {
                conversation.eventHandler.handleEvent(
                        new DefaultEvent(OperationEvent.OperationEventType.PartiallyComplete, 
                                "Finished put on pillar '" + response.getPillarID() + "'."));
            }
        } catch (UnexpectedResponseException e) {
            if (conversation.eventHandler != null) {
                conversation.eventHandler.handleEvent(
                        new DefaultEvent(OperationEvent.OperationEventType.Failed, e.getMessage()));
            }
        }
        
        // Check if the conversation has finished.
        if(conversation.putResponseStatus.haveAllPillarResponded()) {
            timerTask.cancel();
            
            if (conversation.eventHandler != null) {
                conversation.eventHandler.handleEvent(
                        new DefaultEvent(OperationEvent.OperationEventType.Complete, 
                                "Finished put on all the pillars."));
            }
            
            PutFileFinished finishState = new PutFileFinished(conversation);
            conversation.conversationState = finishState;
        }
    }

    /**
     * Class for handling a timeout for the conversation.
     */
    private class PutTimerTask extends TimerTask {
        @Override
        public void run() {
            endConversation();
        }
    }
}
