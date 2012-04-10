/*
 * #%L
 * Bitrepository Access
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
package org.bitrepository.access.getstatus.conversation;

import java.util.Collection;
import java.util.UUID;

import org.bitrepository.access.getstatus.selector.ContributorSelectorForGetStatus;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageSender;

public class SimpleGetStatusConversation extends AbstractConversation {

    /** The sender to use for dispatching messages */
    final MessageSender messageSender; 
    /** The configuration specific to the BitRepositoryCollection related to this conversion. */
    final Settings settings;
    
    /** Selects a pillar based on responses. */
    final ContributorSelectorForGetStatus selector;
    /** The conversation state (State pattern) */
    GetStatusState conversationState;
    
    public SimpleGetStatusConversation(MessageSender messageSender, Settings settings, Collection<String> contributors, 
            EventHandler eventHandler, FlowController flowController) {
        super(messageSender, UUID.randomUUID().toString(), eventHandler, flowController);
        this.messageSender = messageSender;
        this.settings = settings;     
        selector = new ContributorSelectorForGetStatus(contributors);
        conversationState = new IdentifyingContributorsForGetStatus(this);
    }

    @Override
    public synchronized void onMessage(GetStatusFinalResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(GetStatusProgressResponse message) {
        conversationState.onMessage(message);
    }

    @Override
    public synchronized void onMessage(IdentifyContributorsForGetStatusResponse message) {
        conversationState.onMessage(message);
    }
    
    @Override
    public void endConversation() {
        conversationState.endConversation();       
    }

    @Override
    public boolean hasEnded() {
        return conversationState.hasEnded();
    }

    @Override
    public ConversationState getConversationState() {
        return conversationState;
    }

}
