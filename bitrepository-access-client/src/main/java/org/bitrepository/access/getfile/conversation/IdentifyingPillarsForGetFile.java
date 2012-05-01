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
package org.bitrepository.access.getfile.conversation;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.GeneralConversationState;
import org.bitrepository.client.conversation.IdentifyingState;
import org.bitrepository.client.conversation.selector.ComponentSelector;


/**
 * Models the functionality for identifying pillars prior to a get file request.
 */
public class IdentifyingPillarsForGetFile extends IdentifyingState {
    private final GetFileConversationContext context;

    /** 
     * The constructor for the indicated conversation.
     * @param context The related context related to the conversation containing information.
     */
    public IdentifyingPillarsForGetFile(GetFileConversationContext context) {
        this.context = context;
    }

    @Override
    public ComponentSelector getSelector() {
        return context.getSelector();
    }

    @Override
    public GeneralConversationState getOperationState() {
        return new GettingFile(context, context.getSelector().getSelectedComponent());
    }

    @Override
    protected void sendRequest() {
        IdentifyPillarsForGetFileRequest msg = new IdentifyPillarsForGetFileRequest();
        initializeMessage(msg);
        msg.setTo(context.getSettings().getCollectionDestination());
        msg.setFileID(context.getFileID());
        context.getMessageSender().sendMessage(msg);
        context.getMonitor().identifyPillarsRequestSent("Identifying contributers for audit trails");
        
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getName() {
        return "Identify pillars for GetFile";
    }

}
