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

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.client.conversation.AbstractConversation;
import org.bitrepository.client.conversation.ConversationEventMonitor;
import org.bitrepository.client.conversation.ConversationState;
import org.bitrepository.client.conversation.FinishedState;

/**
 * A conversation for GetFileIDs.
 *
 * Logic for behaving sanely in GetFileIDs conversations.
 */
public class SimpleGetFileIDsConversation extends AbstractConversation {
    private final GetFileIDsConversationContext context;
    
    /**
     * Constructor.
     * @param messageSender The instance for sending the messages.
     * @param settings The settings for the GetChecksumsClient.
     * @param url The URL where to upload the results.
     * @param fileIds The IDs for the files to retrieve.
     * @param pillars The pillars to retrieve the checksums from.
     * @param eventHandler The handler of events.
     */
    public SimpleGetFileIDsConversation(GetFileIDsConversationContext context) {
        super(context.getMessageSender(), context.getConversationID(), null, null);
        this.context = context;
        context.setState(new IdentifyPillarsForGetFileIDs(context));
    }

    @Override
    public boolean hasEnded() {
        return context.getState() instanceof FinishedState;
    }
    
    @Override
    public void onMessage(Message message) {
        context.getState().handleMessage(message);
    }

    @Override
    public void startConversation() {
        context.getState().start();
    }
    
    @Override
    public void endConversation() {
        context.setState(new FinishedState(context));
    }
    
    /**
     * Override to use the new context provided monitor.
     * @return The monitor for distributing update information
     */
    public ConversationEventMonitor getMonitor() {
        return context.getMonitor();
    }

    @Override
    public ConversationState getConversationState() {
        // Only used to start conversation, which has been oveloaded. This is because the current parent state isn't of
        // type ConversationState in the AuditTrailCLient.
        return null;
    }
}
