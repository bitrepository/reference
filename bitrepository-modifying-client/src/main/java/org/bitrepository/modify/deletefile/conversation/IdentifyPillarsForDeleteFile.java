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
package org.bitrepository.modify.deletefile.conversation;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.GeneralConversationState;
import org.bitrepository.client.conversation.IdentifyingState;
import org.bitrepository.common.exceptions.UnableToFinishException;

/**
 * Handles the identification of the deleteFile Contributors.
 */
public class IdentifyPillarsForDeleteFile extends IdentifyingState {
    private final DeleteFileConversationContext context;

    public IdentifyPillarsForDeleteFile(DeleteFileConversationContext context) {
        super(context.getContributors());
        this.context = context;
    }

    @Override
    public GeneralConversationState getOperationState() {
        return new DeletingFile(context, getSelector().getSelectedComponents());
    }

    /**
     * Extends the default behaviour with a idempotent aspects. This assumes that the delete on a pillar is successful if
     * if the file is already absent.
     *
     * Any other none-positive response is handled as a fatal problem.
     */
    @Override
    protected void handleFailureResponse(MessageResponse msg) throws UnableToFinishException {
        IdentifyPillarsForDeleteFileResponse response = (IdentifyPillarsForDeleteFileResponse) msg;
        ResponseCode responseCode = response.getResponseInfo().getResponseCode();
        if(responseCode.equals(ResponseCode.FILE_NOT_FOUND_FAILURE)) {
            //Idempotent
            getContext().getMonitor().contributorIdentified(response);
            getContext().getMonitor().contributorComplete(new DeleteFileCompletePillarEvent(
                    response.getFrom(), null ));
        } else {
            getContext().getMonitor().contributorFailed(
                    msg.getResponseInfo().getResponseText(), msg.getFrom(), msg.getResponseInfo().getResponseCode());
            throw new UnableToFinishException("Can not continue with delete operation, as " + msg.getFrom() +
                    " is unable to perform the deletion.");
        }
    }

    @Override
    protected void sendRequest() {
        IdentifyPillarsForDeleteFileRequest msg = new IdentifyPillarsForDeleteFileRequest();
        initializeMessage(msg);
        msg.setFileID(context.getFileID());
        msg.setDestination(context.getSettings().getCollectionDestination());
        context.getMessageSender().sendMessage(msg);
        context.getMonitor().identifyRequestSent("Identifying pillars for delete file");
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "IdentifyPillarsForDeleteFile";
    }

    @Override
    protected boolean canFinish() {
        return (getOutstandingComponents().isEmpty());
    }

    @Override
    protected void checkForChecksumPillar(MessageResponse msg) {
        IdentifyPillarsForDeleteFileResponse response = (IdentifyPillarsForDeleteFileResponse) msg;
        if (response.getPillarChecksumSpec() != null) {
            context.addChecksumPillar(response.getPillarID());
        }
    }
}
