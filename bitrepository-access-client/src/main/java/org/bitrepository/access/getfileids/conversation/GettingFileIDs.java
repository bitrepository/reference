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
package org.bitrepository.access.getfileids.conversation;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.exceptions.UnexpectedResponseException;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.ContributorResponseStatus;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;

/**
 * Models the behavior of a GetFileIDs conversation during the operation phase. That is, it begins with the 
 * sending of <code>GetFileIDsRequest</code> messages and finishes with on the reception of the 
 * <code>GetFileIDsFinalResponse</code> messages from the responding pillars.
 * 
 * Note that this is only used by the GetFileIDsConversation in the same package, therefore the visibility is package 
 * protected.
 */
public class GettingFileIDs extends PerformingOperationState {
    private final GetFileIDsConversationContext context;
    private Map<String,String> activeContributors;
    /** The status for the expected responses.*/
    private final ContributorResponseStatus responseStatus;

    /*
     * @param context The conversation context.
     * @param contributors The list of components the fileIDs should be collected from.
     */
    public GettingFileIDs(GetFileIDsConversationContext context, List<SelectedComponentInfo> contributors) {
        super();
        this.context = context;
        this.activeContributors = new HashMap<String,String>();
        for (SelectedComponentInfo contributorInfo : contributors) {
            activeContributors.put(contributorInfo.getID(), contributorInfo.getDestination());
        }
        this.responseStatus = new ContributorResponseStatus(activeContributors.keySet());
    }
    
    @Override
    protected void sendRequest() {
        context.getMonitor().requestSent("Sending request for get fileIDs", activeContributors.keySet().toString());
        for(String pillar : activeContributors.keySet()) {
            GetFileIDsRequest msg = new GetFileIDsRequest();
            initializeMessage(msg);
            msg.setFileIDs(context.getFileIDs());
            if(context.getUrlForResult() != null) {
                msg.setResultAddress(context.getUrlForResult().toExternalForm() + "-" + pillar);    
            }
            msg.setPillarID(pillar);
            msg.setTo(activeContributors.get(pillar));
            context.getMessageSender().sendMessage(msg);
        }
    }
    
    @Override
    protected void generateContributorCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
        if (msg instanceof GetFileIDsFinalResponse) {
            GetFileIDsFinalResponse response = (GetFileIDsFinalResponse)msg;
            getContext().getMonitor().contributorComplete(
                    new FileIDsCompletePillarEvent(response.getResultingFileIDs(), response.getFrom(),
                            "FileIDs received from " + response.getFrom(), response.getCorrelationID()));
        } else {
            throw new UnexpectedResponseException("Received unexpected msg " + msg.getClass().getSimpleName() +
                    " while waiting for FileIDs response.");
        }
    }
    
    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getName() {
        return "Get fileIDs";
    }

    @Override
    protected ContributorResponseStatus getResponseStatus() {
        return responseStatus;
    }

}
