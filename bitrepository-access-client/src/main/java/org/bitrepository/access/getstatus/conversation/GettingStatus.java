/*
 * #%L
 * Bitrepository Access
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access.getstatus.conversation;

import java.util.Collection;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.PerformingOperationState;
import org.bitrepository.client.conversation.selector.SelectedComponentInfo;
import org.bitrepository.client.exceptions.UnexpectedResponseException;

public class GettingStatus extends PerformingOperationState {
    private final GetStatusConversationContext context;

    /*
    * @param context The conversation context.
    * @param contributors The list of components the fileIDs should be collected from.
    */
    public GettingStatus(GetStatusConversationContext context, Collection<SelectedComponentInfo> contributors) {
        super(contributors);
        this.context = context;
    }

    @Override
    protected void generateContributorCompleteEvent(MessageResponse msg) throws UnexpectedResponseException {
        GetStatusFinalResponse response = (GetStatusFinalResponse) msg;
        getContext().getMonitor().contributorComplete(
                new StatusCompleteContributorEvent(
                        "Received status result from " + response.getContributor(),
                        response.getContributor(), response.getResultingStatus(),
                        getContext().getConversationID()));
    }

    @Override
    protected void sendRequest() {
        GetStatusRequest request = new GetStatusRequest();
        initializeMessage(request);

        context.getMonitor().requestSent("Sending GetStatusRequest", activeContributors.keySet().toString());
        for(String ID : activeContributors.keySet()) {
            request.setContributor(ID);
            request.setTo(activeContributors.get(ID));
            context.getMessageSender().sendMessage(request);
        }
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getPrimitiveName() {
        return "GetStatus";
    }
}
