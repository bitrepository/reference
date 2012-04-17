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

import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.exceptions.UnableToFinishException;

/**
 * Marker interface to indicate that this is the finished state for a conversation.
 */
public class FinishedState extends GeneralConversationState {
    protected final ConversationContext context;
    
    public FinishedState(ConversationContext context) {
        this.context = context;
    }

    @Override
    protected ConversationContext getContext() {
        return context;
    }

    @Override
    protected GeneralConversationState getNextState() throws UnableToFinishException {
        return this;
    }

    @Override
    protected long getTimeout() {
        return context.getSettings().getReferenceSettings().getClientSettings().getConversationTimeout().longValue();
    }

    @Override
    protected String getName() {
        return "Finished";
    }

    @Override
    public void sendRequest() {
        // Nothing to do.
    }

    @Override
    protected void processMessage(MessageResponse message) {
        context.getMonitor().outOfSequenceMessage("Received " + message.getClass().getName() +
                " with replyTo " + message.getReplyTo() + " after the conversation has ended.");
    }

    /**
     * Never occures, this is the final state
     * @return
     */
    @Override
    protected GeneralConversationState handleStateTimeout() {
        // What to do?
        return this;
    }
}
