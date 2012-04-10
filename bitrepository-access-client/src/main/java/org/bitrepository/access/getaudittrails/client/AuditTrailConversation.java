/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getaudittrails.client;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FinishedState;

/**
 * Mostly just a event mediator.
 */
public class AuditTrailConversation extends AbstractConversation {
    private final AuditTrailConversationContext context;
    
    public AuditTrailConversation (AuditTrailConversationContext context) {
        super();
        this.context = context;
        context.setState(new IdentifyingAuditTrailContributers(context));
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

    @Override
    public boolean hasEnded() {
        return context.getState() instanceof FinishedState;
    }
}
