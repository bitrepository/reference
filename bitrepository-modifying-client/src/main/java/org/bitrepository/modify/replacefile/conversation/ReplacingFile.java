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

import java.util.Collection;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.utils.ChecksumUtils;

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

    /*
     * @param context The conversation context.
     * @param contributors The list of components the fileIDs should be collected from.
     */
    public ReplacingFile(ReplaceFileConversationContext context, Collection<SelectedComponentInfo> contributors) {
        super(contributors);
        this.context = context;
    }

    @Override
    protected void generateContributorCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
        ReplaceFileFinalResponse response = (ReplaceFileFinalResponse) msg;
        getContext().getMonitor().contributorComplete(new ReplaceFileCompletePillarEvent(
                response.getPillarID(),
                response.getChecksumDataForExistingFile(),
                response.getChecksumDataForNewFile()
                ));
    }

    @Override
    protected void sendRequest() {
        context.getMonitor().requestSent("Sending request for replace file", activeContributors.keySet().toString());
        for(String pillar : activeContributors.keySet()) {
            ReplaceFileRequest msg = createRequest(pillar);
            if (context.getChecksumRequestsForNewFile() != null) {
                if (!context.isChecksumPillar(pillar) ||
                        context.getChecksumRequestsForNewFile().equals(
                                ChecksumUtils.getDefault(context.getSettings()))) {
                    msg.setChecksumRequestForNewFile(context.getChecksumRequestsForNewFile());
                }
            }
            if (context.getChecksumRequestedForDeletedFile() != null) {
                if (!context.isChecksumPillar(pillar) ||
                        context.getChecksumRequestedForDeletedFile().equals(
                                ChecksumUtils.getDefault(context.getSettings()))) {
                    msg.setChecksumRequestForExistingFile(context.getChecksumRequestedForDeletedFile());
                }
            }
            context.getMessageSender().sendMessage(msg);
        }
    }

    private ReplaceFileRequest createRequest(String pillarID) {
        ReplaceFileRequest msg = new ReplaceFileRequest();
        initializeMessage(msg);
        msg.setChecksumDataForExistingFile(context.getChecksumForDeleteAtPillar());
        msg.setChecksumDataForNewFile(context.getChecksumForNewFileValidationAtPillar());
        msg.setFileAddress(context.getUrlForFile().toExternalForm());
        msg.setFileID(context.getFileID());
        msg.setFileSize(context.getSizeOfNewFile());
        msg.setPillarID(pillarID);
        msg.setDestination(activeContributors.get(pillarID));
        return msg;
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "ReplaceFile";
    }

}
