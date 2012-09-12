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
import org.bitrepository.client.conversation.selector.ComponentSelector;
import org.bitrepository.client.conversation.selector.MultipleComponentSelector;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.exceptions.UnableToFinishException;
import org.bitrepository.common.utils.Base16Utils;

/**
 * The first state of the PutFile communication. The identification of the pillars involved.
 */
public class IdentifyPillarsForPutFile extends IdentifyingState {
    private final PutFileConversationContext context;
    private final MultipleComponentSelector selector;

    public IdentifyPillarsForPutFile(PutFileConversationContext context) {
        this.context = context;
        selector = new PutFilePillarSelector(
                context.getSettings().getCollectionSettings().getClientSettings().getPillarIDs());
    }

    @Override
    protected void processMessage(MessageResponse msg) throws UnexpectedResponseException, UnableToFinishException {
        if(msg instanceof IdentifyPillarsForPutFileResponse) {
            IdentifyPillarsForPutFileResponse response = (IdentifyPillarsForPutFileResponse) msg;
            ResponseCode responseCode = response.getResponseInfo().getResponseCode();
            switch (responseCode) {
                case IDENTIFICATION_POSITIVE:
                    getContext().getMonitor().contributorIdentified(response);
                    break;
                case DUPLICATE_FILE_FAILURE:
                    if(response.isSetChecksumDataForExistingFile()) {
                        if(context.getChecksumForValidationAtPillar() != null &&
                           Base16Utils.decodeBase16(response.getChecksumDataForExistingFile().getChecksumValue()).equals(
                           Base16Utils.decodeBase16(context.getChecksumForValidationAtPillar().getChecksumValue()))) {
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
                    getContext().getMonitor().contributorFailed(
                            "Received negative response from component " + response.getFrom() + ":  " +
                                    response.getResponseInfo(), response.getFrom(), response.getResponseInfo().getResponseCode());
            }
            if (response.getPillarChecksumSpec() != null) {
                context.addChecksumPillar(response.getPillarID());
            }
            getSelector().processResponse(response);
        } else {
            throw new UnexpectedResponseException("Are currently only expecting IdentifyPillarsForPutFileResponse's");
        }
    }

    @Override
    public ComponentSelector getSelector() {
        return selector;
    }

    @Override
    public GeneralConversationState getOperationState() {
        return new PuttingFile(context, selector.getSelectedComponents());
    }

    @Override
    protected boolean handleIdentificationTimeout() {
        boolean isPartialPutsAllowed = true;
        if (context.getSettings().getReferenceSettings().getPutFileSettings() != null &&
                context.getSettings().getReferenceSettings().getPutFileSettings().isSetPartialPutsAllow()) {
            isPartialPutsAllowed = context.getSettings().getReferenceSettings().getPutFileSettings().isPartialPutsAllow();
        }

        if(selector.hasSelectedComponent()) {
            if (isPartialPutsAllowed || selector.haveSelectedAllComponents()) {
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
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
    protected String getName() {
        return "Identifying pillars for put file";
    }

    @Override
    public boolean continueWithOperation() {
        return !context.getMonitor().hasFailed();
    }
}
