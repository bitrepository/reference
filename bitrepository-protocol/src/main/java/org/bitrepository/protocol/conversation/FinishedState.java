/*
 * #%L
 * Bitrepository Protocol
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.protocol.conversation;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;

/**
 * Marker interface to indicate that this is the finished state for a conversation.
 */
public class FinishedState extends AbstractMessageListener implements ConversationState {
    /** Handles the mediation of information regarding conversation updates */
    protected final ConversationEventMonitor monitor;
    
    public FinishedState(ConversationEventMonitor monitor) {
        this.monitor = monitor;
    }

    public void onMessage(Message message) {
        monitor.outOfSequenceMessage("Received " + message.getClass().getName() + 
                " with replyTo " + message.getReplyTo() + " after the conversation has ended.");
    };
    
    @Override
    public void start() {
        //Nothing to do.      
    }

    @Override
    public boolean hasEnded() {
        return true;
    }
}
