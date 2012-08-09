/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.client.conversation.AbstractConversation;
import org.bitrepository.client.conversation.ConversationEventMonitor;
import org.bitrepository.client.conversation.ConversationState;
import org.bitrepository.client.conversation.FinishedState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A conversation for PutFile.
 * Logic for behaving sanely in PutFile conversations.
 *
 * TODO move all the message generations into separate methods, or use the auto-generated constructors, which Mikis
 * has talked about.
 */
public class SimplePutFileConversation extends AbstractConversation {
    private final PutFileConversationContext context;
    
    /**
     * Constructor.
     * Initializes the conversation.
     * @param context The conversation's context.
     */
    public SimplePutFileConversation(PutFileConversationContext context) {
        super(context.getMessageSender(), context.getConversationID(), null);
        this.context = context;
        context.setState(new IdentifyPillarsForPutFile(context));
    }


    @Override
    public void onMessage(Message message) {
        context.getState().handleMessage(message);
    }

    @Override
    public ConversationState getConversationState() {
        // Only used to start conversation, which has been oveloaded. This is because the current parent state isn't of
        // type ConversationState in the AuditTrailCLient.
        return null;
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
    public boolean hasEnded() {
        return context.getState() instanceof FinishedState;
    }
    
}
