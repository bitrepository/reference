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
package org.bitrepository.modify.putfile.conversation;

import java.math.BigInteger;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.protocol.ProtocolConstants;
import org.bitrepository.protocol.eventhandler.DefaultEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.exceptions.UnexpectedResponseException;
import org.bitrepository.protocol.pillarselector.PillarsResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The state for the PutFile communication, where the file is put to the pillars (the pillars are requested to retrieve
 * the file).
 */
public class PuttingFile extends PutFileState {
    /** The log for this class. */
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** Defines that the timer is a daemon thread. */
    private static final Boolean TIMER_IS_DAEMON = true;
    /** The timer. Schedules conversation timeouts for this conversation. */
    final Timer timer = new Timer(TIMER_IS_DAEMON);

    /**
     * The task to handle the timeouts for the identification.
     */
    private TimerTask timerTask = new PutTimerTask();

    /**The responses for the pillars.*/
    final PillarsResponseStatus putResponseStatus;

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
        putResponseStatus = new PillarsResponseStatus(conversation.settings.getCollectionSettings().getClientSettings().getPillarIDs());
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
        putMsg.setBitRepositoryCollectionID(conversation.settings.getCollectionID());
        putMsg.setReplyTo(conversation.settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        putMsg.setFileAddress(conversation.downloadUrl.toExternalForm());
        putMsg.setFileID(conversation.fileID);
        putMsg.setFileSize(conversation.fileSize);
        // TODO
        //        putMsg.setAuditTrailInformation(conversation.settings);
        putMsg.setChecksumsDataForNewFile(conversation.validationChecksums);
        //        putMsg.setChecksumSpecs(conversation.requestChecksums);

        // Send the message to each pillar.
        for(Map.Entry<String, String> pillarDest : pillarDestinations.entrySet()) {
            putMsg.setPillarID(pillarDest.getKey());
            putMsg.setTo(pillarDest.getValue());
            conversation.messageSender.sendMessage(putMsg);
        }


        monitor.requestSent("Request to put file " + conversation.fileID + " has been sent to pillars + " +
                conversation.settings.getCollectionSettings().getClientSettings().getPillarIDs().toString(), 
                conversation.settings.getCollectionSettings().getClientSettings().getPillarIDs().toString());

        timer.schedule(timerTask, 
                conversation.settings.getReferenceSettings().getClientSettings().getConversationTimeout().longValue());
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
     * Just logged to it.
     * 
     * @param response The PutFileProgressResponse to be handled by this method.
     */
    @Override
    public void onMessage(PutFileProgressResponse response) {
        monitor.progress(new DefaultEvent(OperationEvent.OperationEventType.Progress, 
                "Received PutFileProgressResponse from pillar " + response.getPillarID() + ": " + 
                        response.getProgressResponseInfo().getProgressResponseText()));
    }

    @Override
    public void onMessage(PutFileFinalResponse response) {

        // validate the response info.
        FinalResponseInfo frInfo = response.getFinalResponseInfo();
        if(frInfo == null || frInfo.getFinalResponseCode() == null || frInfo.getFinalResponseCode().isEmpty()) {
            handleIncompletePut("The final response info is invalid: '" + frInfo + "'");
        } else {
            // TODO validate the actual values of the response info. Was it success or failure?
            if(frInfo.getFinalResponseText().contains("Error")) {
                handleIncompletePut("The Final Response Info contain an error: '" + frInfo.getFinalResponseCode() + " : " 
                        + frInfo.getFinalResponseText() + "'");
            }
        }

        try {
            putResponseStatus.responseReceived(response.getPillarID());
            monitor.progress(new DefaultEvent(OperationEvent.OperationEventType.Progress,
                    "Finished put on pillar '" + response.getPillarID() + "'."));
        } catch (UnexpectedResponseException e) {
            // TODO Is this a failure? Can happen if a pillar sends two final responses, or no response was expected 
            // from this pillar, or the pillarId is null.
            monitor.warning("Unable to handle this response: '" + response + "'.", e);
            return;
        }

        // Check if the conversation has finished.
        if(putResponseStatus.haveAllPillarResponded()) {
            timerTask.cancel();
            monitor.complete(new DefaultEvent(OperationEvent.OperationEventType.Complete,
                    "Finished put on all the pillars."));

            PutFileFinished finishState = new PutFileFinished(conversation);
            conversation.conversationState = finishState;
        }
    }

    /**
     * What should we do if something fails during a put operation, try again, rollback???.
     * @param info
     */
    private void handleIncompletePut(String info) {
        conversation.failConversation(info);
    }

    /**
     * Class for handling a timeout for the conversation.
     */
    private class PutTimerTask extends TimerTask {
        @Override
        public void run() {
            handleIncompletePut("Timeout occurred for the Putting of the file.");
        }
    }
}
