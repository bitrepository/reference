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
package org.bitrepository.modify.deletefile.conversation;

import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.client.conversation.ConversationContext;
import org.bitrepository.client.conversation.GeneralConversationState;
import org.bitrepository.client.conversation.IdentifyingState;
import org.bitrepository.client.conversation.selector.ComponentSelector;

/**
 * The first state of the DeleteFile communication. The identification of the pillars involved.
 */
public class IdentifyPillarsForDeleteFile extends IdentifyingState {
    private final DeleteFileConversationContext context;
       
    /**
     * Constructor.
     * @param conversation The conversation in this given state.
     */
    public IdentifyPillarsForDeleteFile(DeleteFileConversationContext context) {
        this.context = context;
    }

    @Override
    public ComponentSelector getSelector() {
        return context.getSelector();
    }

    @Override
    public GeneralConversationState getOperationState() {
        return new DeletingFile(context, context.getSelector().getSelectedComponents());
    }

    @Override
    protected void sendRequest() {
        IdentifyPillarsForDeleteFileRequest msg = new IdentifyPillarsForDeleteFileRequest();
        initializeMessage(msg);
        msg.setFileID(context.getFileID());
        msg.setTo(context.getSettings().getCollectionDestination());
        context.getMessageSender().sendMessage(msg);
        context.getMonitor().identifyPillarsRequestSent("Identifying contributers for audit trails");
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected String getName() {
        return "Identifying pillars for delete file";
    }

}
