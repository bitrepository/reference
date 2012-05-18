/*
 * #%L
 * Bitrepository Modifying Client
 * 
 * $Id: DeletingFile.java 631 2011-12-13 17:56:54Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/main/java/org/bitrepository/modify/deletefile/conversation/DeletingFile.java $
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;

/**
 * Models the behavior of a ReplaceFile conversation during the operation phase. That is, it begins with the sending of
 * <code>ReplaceFileRequest</code> messages and finishes with the reception of the 
 * <code>ReplaceFileFinalResponse</code> messages from all responding pillars. 
 * 
 * Note that this is only used by the ReplaceFileConversation in the same package, therefore the visibility is package 
 * protected.
 */
public class ReplacingFile extends PerformingOperationState {
    private final ReplaceFileConversationContext context;
    private Map<String,String> activeContributors;
    /** Tracks who have responded */
    private final ContributorResponseStatus responseStatus;

    /**
     * Constructor.
     * @param conversation The conversation in this state.
     */
    public ReplacingFile(ReplaceFileConversationContext context, List<SelectedComponentInfo> contributors) {
        this.context = context;
        this.activeContributors = new HashMap<String,String>();
        for (SelectedComponentInfo contributorInfo : contributors) {
            activeContributors.put(contributorInfo.getID(), contributorInfo.getDestination());
        }
        this.responseStatus = new ContributorResponseStatus(activeContributors.keySet());
    }

    @Override
    protected void generateCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
        if (msg instanceof ReplaceFileFinalResponse) {
            ReplaceFileFinalResponse response = (ReplaceFileFinalResponse) msg;
            getContext().getMonitor().complete(new ReplaceFileCompletePillarEvent(
                    response.getChecksumDataForExistingFile(),
                    response.getChecksumDataForNewFile(),
                    response.getPillarID(),
                    "Received replace file result from " + response.getPillarID(),
                    response.getCorrelationID()));
        } else {
            throw new UnexpectedResponseException("Received unexpected msg " + msg.getClass().getSimpleName() +
                    " while waiting for ReplaceFile response.");
        }      
    }

    @Override
    protected ContributorResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    protected void sendRequest() {
        ReplaceFileRequest msg = new ReplaceFileRequest();
        initializeMessage(msg);
        msg.setChecksumDataForExistingFile(context.getChecksumForDeleteAtPillar());
        msg.setChecksumDataForNewFile(context.getChecksumForNewFileValidationAtPillar());
        msg.setChecksumRequestForExistingFile(context.getChecksumRequestedForDeletedFile());
        msg.setChecksumRequestForNewFile(context.getChecksumRequestsForNewFile());
        msg.setFileAddress(context.getUrlForFile().toExternalForm());
        msg.setFileID(context.getFileID());
        msg.setFileSize(context.getSizeOfNewFile());
        
        context.getMonitor().requestSent("Sending request for replace file", activeContributors.keySet().toString());
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
        return "Replacing file";
    }

}
