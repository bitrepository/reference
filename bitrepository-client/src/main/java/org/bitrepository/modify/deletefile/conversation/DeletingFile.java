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

import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.common.utils.ChecksumUtils;

import java.util.Collection;

/**
 * Models the behavior of a DeleteFile conversation during the operation phase. That is, it begins with the sending of
 * <code>DeleteFileRequest</code> messages and finishes with the reception of the <code>DeleteFileFinalResponse</code>
 * messages from all responding pillars.
 * <p>
 * Note that this is only used by the DeleteFileConversation in the same package, therefore the visibility is package
 * protected.
 */
public class DeletingFile extends PerformingOperationState {
    private final DeleteFileConversationContext context;

    /**
     * @param context      The conversation context.
     * @param contributors The list of components the fileIDs should be collected from.
     */
    public DeletingFile(DeleteFileConversationContext context, Collection<SelectedComponentInfo> contributors) {
        super(contributors);
        this.context = context;
    }

    @Override
    protected void generateContributorCompleteEvent(MessageResponse msg) {
        DeleteFileFinalResponse response = (DeleteFileFinalResponse) msg;
        getContext().getMonitor().contributorComplete(new DeleteFileCompletePillarEvent(
                response.getPillarID(), response.getCollectionID(), response.getChecksumDataForExistingFile()));
    }

    @Override
    protected void sendRequest() {
        context.getMonitor().requestSent("Sending request for deleting file", activeContributors.keySet().toString());
        for (String pillarID : activeContributors.keySet()) {
            DeleteFileRequest msg = createRequest(pillarID);
            if (context.getChecksumRequestForValidation() != null) {
                if (!context.isChecksumPillar(pillarID) ||
                        context.getChecksumRequestForValidation().equals(ChecksumUtils.getDefault(context.getSettings()))) {
                    msg.setChecksumRequestForExistingFile(context.getChecksumRequestForValidation());
                }
            }
            msg.setPillarID(pillarID);
            msg.setDestination(activeContributors.get(pillarID));
            context.getMessageSender().sendMessage(msg);
        }
    }

    /**
     * @param pillarID the ID of the pillar
     * @return a newly created PutFileRequest based on the context. The ChecksumRequestForNewFile parameter is not added as this
     * should only be added in case of full pillars.
     */
    private DeleteFileRequest createRequest(String pillarID) {
        DeleteFileRequest request = new DeleteFileRequest();
        initializeMessage(request);
        request.setFileID(context.getFileID());
        request.setChecksumDataForExistingFile(context.getChecksumForValidationAtPillar());
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
        return "DeleteFile";
    }
}
