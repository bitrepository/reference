/*
 * #%L
 * Bitrepository Protocol
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
package org.bitrepository.client.conversation;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Abstract super class for conversations.
 * This super class will handle sending all messages with the correct
 * conversation id, and simply log messages received. Overriding implementations should override the behaviour for
 * receiving specific messages.
 */
public class StateBasedConversation implements Conversation {
    private Logger log = LoggerFactory.getLogger(getClass());
    private long startTime;
    private ConversationContext context;

    /**
     * Initialize a conversation on the given message bus.
     *
     * @param context The cointext to use for this conversation.
     */
    public StateBasedConversation(ConversationContext context) {
        this.startTime = System.currentTimeMillis();
        this.context = context;
    }

    @Override
    public synchronized void onMessage(Message message) {
        context.getState().handleMessage(message);
    }

    @Override
    public String getConversationID() {
        return context.getConversationID();
    }
    
    @Override
    public long getStartTime() {
        return startTime;
    }

    @Override
    public synchronized void startConversation() {
        context.getState().start();
    }

    @Override
    public void endConversation() {
        context.setState(new FinishedState(context));
    }

    @Override
    public synchronized void failConversation(OperationFailedEvent failedEvent) {
        getMonitor().operationFailed(failedEvent);
        endConversation();
    }

    /**
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
