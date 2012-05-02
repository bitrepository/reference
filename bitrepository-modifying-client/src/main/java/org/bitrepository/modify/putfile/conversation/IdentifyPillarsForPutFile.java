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

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.FinishedState;
import org.bitrepository.client.conversation.GeneralConversationState;
import org.bitrepository.client.conversation.IdentifyingState;
import org.bitrepository.client.conversation.selector.ComponentSelector;
import org.bitrepository.client.conversation.selector.MultipleComponentSelector;

/**
 * The first state of the PutFile communication. The identification of the pillars involved.
 */
public class IdentifyPillarsForPutFile extends IdentifyingState {
    private final PutFileConversationContext context;
    private final MultipleComponentSelector selector;

    /**
     * Constructor.
     * @param conversation The conversation in this given state.
     */
    public IdentifyPillarsForPutFile(PutFileConversationContext context) {
        this.context = context;
        selector = new PutFilePillarSelector(context.getSettings().getCollectionSettings().getClientSettings().getPillarIDs());
    
    }

    @Override
    public ComponentSelector getSelector() {
        return selector;
    }

    @Override
    public GeneralConversationState getOperationState() {
        if(selector.getOutstandingComponents().isEmpty()) {
            return new PuttingFile(context, selector.getSelectedComponents());
        } else {
            context.getMonitor().operationFailed("Failed to put file, the following pillars didn't respond: " + 
                    selector.getOutstandingComponents());
            return new FinishedState(context);
        }
    }

    @Override
    protected void sendRequest() {
        IdentifyPillarsForPutFileRequest msg = new IdentifyPillarsForPutFileRequest();
        initializeMessage(msg);
        msg.setFileID(context.getFileID());
        msg.setFileSize(context.getFileSize());
        msg.setTo(context.getSettings().getCollectionDestination());
        context.getMessageSender().sendMessage(msg);
        context.getMonitor().identifyPillarsRequestSent("Identifying pillars for put file");
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getName() {
        return "Identifying pillars for put file";
    }

}
