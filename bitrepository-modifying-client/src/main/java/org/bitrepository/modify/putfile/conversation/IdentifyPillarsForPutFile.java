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
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.GeneralConversationState;
import org.bitrepository.client.conversation.IdentifyingState;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.common.utils.ChecksumUtils;

/**
 * Handles the identification phase of the PutFile operation.
 */
public class IdentifyPillarsForPutFile extends IdentifyingState {
    private final PutFileConversationContext context;

    /**
     * @param context The shared conversation context.
     */
    public IdentifyPillarsForPutFile(PutFileConversationContext context) {
        super(context.getContributors());
        this.context = context;
    }

    /**
     * Extends the default behaviour with a idempotent aspects. This assumes that the put to a pillar is successful if
     * the same file already exists.
     *
     * The existence of a different file on the other hand is a fatal problem.
     */
    @Override
    protected void handleFailureResponse(MessageResponse msg) throws UnableToFinishException {
        IdentifyPillarsForPutFileResponse response = (IdentifyPillarsForPutFileResponse) msg;
        ResponseCode responseCode = response.getResponseInfo().getResponseCode();
        switch (responseCode) {
            case DUPLICATE_FILE_FAILURE:
                if(response.isSetChecksumDataForExistingFile()) {
                    if(ChecksumUtils.areEqual(
                            response.getChecksumDataForExistingFile(),context.getChecksumForValidationAtPillar())) {
                        getContext().getMonitor().contributorComplete(
                                new PutFileCompletePillarEvent(response.getChecksumDataForExistingFile(),
                                        response.getPillarID(),
                                        "File already existed on " + response.getPillarID(),
                                        response.getCorrelationID()));
                    } else {
                        getContext().getMonitor().contributorFailed(
                                "Received negative response from component " + response.getFrom() +
                                        ":  " + response.getResponseInfo() + " (existing file checksum does not match)",
                                response.getFrom(), response.getResponseInfo().getResponseCode());
                        throw new UnableToFinishException("Can not put file " + context.getFileID() +
                                ", as an different file already exists on pillar " + response.getPillarID());
                    }
                } else {
                    getContext().getMonitor().contributorFailed(
                            "Received negative response from component " + response.getFrom() + ":  " +
                                    response.getResponseInfo(), response.getFrom(), response.getResponseInfo().getResponseCode());
                    throw new UnableToFinishException("Can not put file " + context.getFileID() +
                            ", as an file already exists on pillar " + response.getPillarID());
                }
                break;
            default:
                super.handleFailureResponse(msg);
        }
    }

    @Override
    public GeneralConversationState getOperationState() throws UnableToFinishException {
        return new PuttingFile(context, getSelector().getSelectedComponents());
    }

    @Override
    protected void sendRequest() {
        IdentifyPillarsForPutFileRequest msg = new IdentifyPillarsForPutFileRequest();
        initializeMessage(msg);
        msg.setFileID(context.getFileID());
        msg.setFileSize(context.getFileSize());
        msg.setTo(context.getSettings().getCollectionDestination());
        context.getMessageSender().sendMessage(msg);
        context.getMonitor().identifyRequestSent("Identifying pillars for put file");
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "IdentifyPillarsForPutFile";
    }

    /**
     * Extends the default behaviour with the possiblity of putting to a subset of pillars, if the
     * isSetPartialPutsAllow settings is true.
     * */
    @Override
    protected boolean canFinish() {
        return (isPartialPutsAllowed() && getSelector().getSelectedComponents().size() > 0 ||
                getOutstandingComponents().isEmpty());
    }

    private boolean isPartialPutsAllowed() {
        boolean isPartialPutsAllowed = true;
        if (context.getSettings().getReferenceSettings().getPutFileSettings() != null &&
                context.getSettings().getReferenceSettings().getPutFileSettings().isSetPartialPutsAllow()) {
            isPartialPutsAllowed = context.getSettings().getReferenceSettings().getPutFileSettings().isPartialPutsAllow();
        }
        return isPartialPutsAllowed;
    }

    @Override
    protected void checkForChecksumPillar(MessageResponse msg) {
        IdentifyPillarsForPutFileResponse response = (IdentifyPillarsForPutFileResponse) msg;
        if (response.getPillarChecksumSpec() != null) {
            context.addChecksumPillar(response.getPillarID());
        }
    }
}
