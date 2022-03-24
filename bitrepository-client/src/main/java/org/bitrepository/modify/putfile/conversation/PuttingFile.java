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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.common.utils.ChecksumUtils;

import java.math.BigInteger;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * The state for the PutFile communication, where the file is put to the pillars (the pillars are requested to retrieve
 * the file).
 */
public class PuttingFile extends PerformingOperationState {
    private final PutFileConversationContext context;
    private final Map<String, Integer> componentRequestCount = new HashMap<>();

    /**
     * @param context      The conversation context.
     * @param contributors The list of components the fileIDs should be collected from.
     */
    public PuttingFile(PutFileConversationContext context, Collection<SelectedComponentInfo> contributors) {
        super(contributors);
        this.context = context;
    }

    @Override
    protected void generateContributorCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
        if (msg instanceof PutFileFinalResponse) {
            PutFileFinalResponse response = (PutFileFinalResponse) msg;
            getContext().getMonitor().contributorComplete(new PutFileCompletePillarEvent(response.getPillarID(), response.getCollectionID(),
                    response.getChecksumDataForNewFile()));
        } else {
            throw new UnexpectedResponseException(
                    "Received unexpected msg " + msg.getClass().getSimpleName() + " while waiting for Put file response.");
        }
    }

    /**
     * Will only add ChecksumRequestForNewFile parameter if the pillar hasn't been marked as a Checksum pillar.
     */
    @Override
    protected void sendRequest() {
        context.getMonitor().requestSent("Sending request for put file", activeContributors.keySet().toString());
        for (String pillar : activeContributors.keySet()) {
            sendPillarRequest(pillar);
            componentRequestCount.put(pillar, 1);
        }
    }

    /**
     * Method to send the PutFileRequest to a given pillarID.
     *
     * @param pillarID The pillarID to which should have the message sent.
     */
    private void sendPillarRequest(String pillarID) {
        PutFileRequest msg = createRequest(pillarID);
        if (context.getChecksumRequestForValidation() != null) {
            if (!context.isChecksumPillar(pillarID) ||
                    context.getChecksumRequestForValidation().equals(ChecksumUtils.getDefault(context.getSettings()))) {
                msg.setChecksumRequestForNewFile(context.getChecksumRequestForValidation());
            }
        }
        context.getMessageSender().sendMessage(msg);
    }

    /**
     * Will create a PutFileRequest based on the context. The ChecksumRequestForNewFile parameter is not added as this
     * should only be added in case of full pillars.
     *
     * @param pillarID the Pillar ID
     * @return a PutFileRequest
     */
    private PutFileRequest createRequest(String pillarID) {
        PutFileRequest request = new PutFileRequest();
        initializeMessage(request);
        request.setFileAddress(context.getUrlForFile().toExternalForm());
        request.setFileID(context.getFileID());
        request.setFileSize(context.getFileSize());
        request.setChecksumDataForNewFile(context.getChecksumForValidationAtPillar());
        request.setPillarID(pillarID);
        request.setDestination(activeContributors.get(pillarID));
        return request;
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "PutFile";
    }

    @Override
    protected boolean handleFailureResponse(MessageResponse msg) throws UnableToFinishException {
        boolean isFinalResponse = true;
        if (msg instanceof PutFileFinalResponse) {
            PutFileFinalResponse response = (PutFileFinalResponse) msg;
            ResponseCode responseCode = response.getResponseInfo().getResponseCode();
            switch (responseCode) {
                case FILE_TRANSFER_FAILURE:
                    String pillarID = response.getPillarID();
                    if (canRetry(pillarID)) {
                        isFinalResponse = false;
                        sendPillarRequest(pillarID);
                        componentRequestCount.put(pillarID, componentRequestCount.get(pillarID) + 1);
                        context.getMonitor()
                                .retry("Retrying putfile (attempt number " + componentRequestCount.get(pillarID) + ")", pillarID);
                    } else {
                        getContext().getMonitor().contributorFailed(msg.getResponseInfo().getResponseText(), msg.getFrom(),
                                msg.getResponseInfo().getResponseCode());
                    }
                    break;
                case DUPLICATE_FILE_FAILURE:
                    if (ChecksumUtils.areEqual(response.getChecksumDataForExistingFile(), context.getChecksumForValidationAtPillar())) {
                        PutFileCompletePillarEvent event = new PutFileCompletePillarEvent(response.getPillarID(),
                                response.getCollectionID(), response.getChecksumDataForExistingFile());
                        event.setInfo("File already existed on " + response.getPillarID());
                        getContext().getMonitor().contributorComplete(event);
                    } else {
                        getContext().getMonitor().contributorFailed(
                                "Received negative response from component " + response.getFrom() + ":  " + response.getResponseInfo() +
                                        " (existing file checksum does not match)", response.getFrom(),
                                response.getResponseInfo().getResponseCode());
                        throw new UnableToFinishException(
                                "Can not put file " + context.getFileID() + ", as an different file already exists on pillar " +
                                        response.getPillarID());
                    }
                    break;
                default:
                    getContext().getMonitor().contributorFailed(msg.getResponseInfo().getResponseText(), msg.getFrom(),
                            msg.getResponseInfo().getResponseCode());
            }
        }
        return isFinalResponse;
    }

    /**
     * Method to determine whether it should be allowed to retry a component.
     *
     * @param pillarID The id of the pillar that we'd like to retry.
     * @return boolean indicating whether a retry is allowed.
     */
    private boolean canRetry(String pillarID) {
        boolean allowed = false;
        if (context.getSettings().getReferenceSettings().getClientSettings().isSetOperationRetryCount()) {
            BigInteger allowedRetryCount = context.getSettings().getReferenceSettings().getClientSettings().getOperationRetryCount();
            Integer pillarRequestCount = componentRequestCount.get(pillarID);
            if (pillarRequestCount != null) {
                if (pillarRequestCount <= allowedRetryCount.intValue()) {
                    allowed = true;
                }
            }
        }
        return allowed;
    }
}

