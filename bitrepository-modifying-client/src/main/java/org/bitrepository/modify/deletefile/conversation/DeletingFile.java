/*
 * #%L
 * Bitrepository Modifying Client
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
import org.bitrepository.client.conversation.selector.SelectedPillarInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;

/**
 * Models the behavior of a DeleteFile conversation during the operation phase. That is, it begins with the sending of
 * <code>DeleteFileRequest</code> messages and finishes with the reception of the <code>DeleteFileFinalResponse</code>
 * messages from all responding pillars. 
 * 
 * Note that this is only used by the DeleteFileConversation in the same package, therefore the visibility is package 
 * protected.
 */
public class DeletingFile extends PerformingOperationState {
    private final DeleteFileConversationContext context;

    private Map<String,String> activeContributors;
    /** Tracks who have responded */
    private final ContributorResponseStatus responseStatus;

    /**
     * Constructor.
     * @param context The context of the conversation.
     */
    public DeletingFile(DeleteFileConversationContext context, List<SelectedPillarInfo> contributors) {
        this.context = context;
        this.activeContributors = new HashMap<String,String>();
        for (SelectedPillarInfo contributorInfo : contributors) {
            activeContributors.put(contributorInfo.getID(), contributorInfo.getDestination());
        }
        this.responseStatus = new ContributorResponseStatus(activeContributors.keySet());
    }

    @Override
    protected void generateCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
        if (msg instanceof DeleteFileFinalResponse) {
            DeleteFileFinalResponse response = (DeleteFileFinalResponse) msg;
            getContext().getMonitor().complete(new DeleteFileCompletePillarEvent(
                    response.getChecksumDataForExistingFile(),
                    response.getPillarID(),
                    "Received delete file result from " + response.getPillarID(),
                    response.getCorrelationID()));
        } else {
            throw new UnexpectedResponseException("Received unexpected msg " + msg.getClass().getSimpleName() +
                    " while waiting for GetChecksums response.");
        }         
    }

    @Override
    protected ContributorResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    protected void sendRequest() {
        DeleteFileRequest msg = new DeleteFileRequest();
        initializeMessage(msg);
        msg.setFileID(context.getFileID());
        msg.setChecksumRequestForExistingFile(context.getChecksumRequestForValidation());
        msg.setChecksumDataForExistingFile(context.getChecksumForValidationAtPillar());

        context.getMonitor().requestSent("Sending request for deleting checksums", activeContributors.keySet().toString());
        for(String pillar : activeContributors.keySet()) {
            msg.setPillarID(pillar);
            msg.setTo(activeContributors.get(pillar));
            context.getMessageSender().sendMessage(msg);
        }        
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getName() {
        return "Deleting file";
    }

}
