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

import java.util.Collection;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.common.utils.ChecksumUtils;

/**
 * The state for the PutFile communication, where the file is put to the pillars (the pillars are requested to retrieve
 * the file).
 */
public class PuttingFile extends PerformingOperationState {
    private final PutFileConversationContext context;

    /*
     * @param context The conversation context.
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
            getContext().getMonitor().contributorComplete(
                    new PutFileCompletePillarEvent(response.getPillarID(), response.getChecksumDataForNewFile()));
        } else {
            throw new UnexpectedResponseException("Received unexpected msg " + msg.getClass().getSimpleName() +
                    " while waiting for Put file response.");
        }
    }

    /**
     * Will only add ChecksumRequestForNewFile parameter if the pillar hasn't been marked as a Checksum pillar.
     */
    @Override
    protected void sendRequest() {
        context.getMonitor().requestSent("Sending request for put file", activeContributors.keySet().toString());
        for(String pillar : activeContributors.keySet()) {
            PutFileRequest msg = createRequest(pillar);
            if (context.getChecksumRequestForValidation() != null) {
                if (!context.isChecksumPillar(pillar) ||
                        context.getChecksumRequestForValidation().equals(ChecksumUtils.getDefault(context.getSettings()))) {
                    msg.setChecksumRequestForNewFile(context.getChecksumRequestForValidation());
                }
            }
            context.getMessageSender().sendMessage(msg);
        }
    }

    /**
     * Will create a PutFileRequest based on the context. The ChecksumRequestForNewFile parameter is not added as this
     * should only be added in case of full pillars.
     */
    private PutFileRequest createRequest(String pillar) {
        PutFileRequest request = new PutFileRequest();
        initializeMessage(request);
        request.setFileAddress(context.getUrlForFile().toExternalForm());
        request.setFileID(context.getFileID());
        request.setFileSize(context.getFileSize());
        request.setChecksumDataForNewFile(context.getChecksumForValidationAtPillar());
        request.setPillarID(pillar);
        request.setTo(activeContributors.get(pillar));
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
}
