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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
import org.bitrepository.client.conversation.selector.SelectedPillarInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;

/**
 * The state for the PutFile communication, where the file is put to the pillars (the pillars are requested to retrieve
 * the file).
 */
public class PuttingFile extends PerformingOperationState {
    private final PutFileConversationContext context;
    private Map<String,String> activeContributors;
    private ContributorResponseStatus responseStatus;

    /**
     * Constructor.
     * @param conversation The conversation in this state.
     */
    public PuttingFile(PutFileConversationContext context, List<SelectedPillarInfo> contributors) {
        this.context = context;
        this.activeContributors = new HashMap<String,String>();
        for (SelectedPillarInfo contributorInfo : contributors) {
            activeContributors.put(contributorInfo.getID(), contributorInfo.getDestination());
        }
        this.responseStatus = new ContributorResponseStatus(activeContributors.keySet());
    }

    @Override
    protected void generateCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
        if (msg instanceof PutFileFinalResponse) {
            PutFileFinalResponse response = (PutFileFinalResponse) msg;
            getContext().getMonitor().complete(
                    new PutFileCompletePillarEvent(response.getChecksumDataForNewFile(),
                            response.getPillarID(),
                            "Received put file result from " + response.getPillarID(),
                            response.getCorrelationID()));
        } else {
            throw new UnexpectedResponseException("Received unexpected msg " + msg.getClass().getSimpleName() +
                    " while waiting for Put file response.");
        }        
    }

    @Override
    protected ContributorResponseStatus getResponseStatus() {
        return responseStatus;
    }

    @Override
    protected void sendRequest() {
        PutFileRequest msg = new PutFileRequest();
        initializeMessage(msg);
        msg.setFileAddress(context.getUrlForFile().toExternalForm());
        msg.setFileID(context.getFileID());
        msg.setFileSize(context.getFileSize());
        context.getMonitor().requestSent("Sending request for put file", activeContributors.keySet().toString());
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
        return "Putting file";
    }

}
