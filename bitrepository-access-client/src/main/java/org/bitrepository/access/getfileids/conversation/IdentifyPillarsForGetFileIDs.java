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

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.GeneralConversationState;
import org.bitrepository.client.conversation.IdentifyingState;
import org.bitrepository.client.conversation.selector.ComponentSelector;
import org.bitrepository.client.conversation.selector.MultipleComponentSelector;

/**
 * Models the behavior of a GetFileIDs conversation during the identification phase. That is, it begins with the 
 * sending of <code>IdentifyPillarsForGetFileIDsRequest</code> messages and finishes with on the reception of the 
 * <code>IdentifyPillarsForGetFileIDsResponse</code> messages from the responding pillars.
 * 
 * Note that this is only used by the GetFileIDsConversation in the same package, therefore the visibility is package 
 * protected.
 * This is the initial state for the whole GetFileIDs communication.
 */
public class IdentifyPillarsForGetFileIDs extends IdentifyingState {
    private final GetFileIDsConversationContext context;
    private final MultipleComponentSelector selector;

    /**
     * Constructor.
     * @param conversation The conversation where this belongs.
     */
    public IdentifyPillarsForGetFileIDs(GetFileIDsConversationContext context) {
        super();
        this.context = context;
        selector = new GetFileIDsContributorSelector(
                context.getSettings().getCollectionSettings().getClientSettings().getPillarIDs());
    }
    
    
    @Override
    protected void sendRequest() {
        IdentifyPillarsForGetFileIDsRequest msg = new IdentifyPillarsForGetFileIDsRequest();
        initializeMessage(msg);
        msg.setFileIDs(context.getFileIDs());
        msg.setTo(context.getSettings().getCollectionDestination());
        context.getMessageSender().sendMessage(msg);
        context.getMonitor().identifyRequestSent("Identifying contributers for get fileIDs");
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getName() {
        return "Identify pillars for GetFileIDs";
    }

    @Override
    public ComponentSelector getSelector() {
        return selector;
    }

    @Override
    public GeneralConversationState getOperationState() {
        return new GettingFileIDs(context, selector.getSelectedComponents());
    }

}
