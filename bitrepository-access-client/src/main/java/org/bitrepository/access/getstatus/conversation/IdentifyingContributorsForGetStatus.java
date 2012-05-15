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

import java.util.ArrayList;
import java.util.List;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.GeneralConversationState;
import org.bitrepository.client.conversation.IdentifyingState;
import org.bitrepository.client.conversation.selector.ComponentSelector;
import org.bitrepository.client.conversation.selector.MultipleComponentSelector;

public class IdentifyingContributorsForGetStatus extends IdentifyingState {
    private final GetStatusConversationContext context;
    private final MultipleComponentSelector selector;

    public IdentifyingContributorsForGetStatus(GetStatusConversationContext context) {
        super();
        this.context = context;
        List<String> expectedContributers = new ArrayList<String>(
                context.getSettings().getCollectionSettings().getGetStatusSettings().getContributorIDs().size());
        for (String contributor : context.getSettings().getCollectionSettings().getGetStatusSettings().getContributorIDs()) {
            expectedContributers.add(contributor);
        }
        selector = new GetStatusContributorSelector(expectedContributers);
    }
    
    @Override
    protected void sendRequest() {
        IdentifyContributorsForGetStatusRequest request = new IdentifyContributorsForGetStatusRequest();
        initializeMessage(request);
        request.setTo(context.getSettings().getCollectionDestination());
        
        context.getMonitor().identifyPillarsRequestSent("Identifying contributors for getting status");
        context.getMessageSender().sendMessage(request);
    }
    
    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getName() {
        return "Identify contributers for Get Status";
    }

    @Override
    public ComponentSelector getSelector() {
        return selector;
    }
    @Override
    public GeneralConversationState getOperationState() {
        return new GettingStatus(context, selector.getSelectedComponents());
    }

}
