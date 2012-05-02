/*
 * #%L
 * Bitrepository Modifying Client
 * 
 * $Id: SimpleDeleteFileConversation.java 633 2011-12-14 09:05:28Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/main/java/org/bitrepository/modify/deletefile/conversation/SimpleDeleteFileConversation.java $
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
package org.bitrepository.modify.replacefile.conversation;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.client.conversation.AbstractConversation;
import org.bitrepository.client.conversation.ConversationState;
import org.bitrepository.client.conversation.FinishedState;

/**
 * A conversation for the ReplaceFile operation.
 * Logic for behaving sanely in ReplaceFile conversations.
 */
public class SimpleReplaceFileConversation extends AbstractConversation {
    private final ReplaceFileConversationContext context;

    /**
     * Constructor.
     * Initializes all the variables for the conversation.
     * 
     * @param context The conversation's context.
     */
    public SimpleReplaceFileConversation(ReplaceFileConversationContext context) {
        super(context.getMessageSender(), context.getConversationID(), null, null);
        this.context = context;
        context.setState(new IdentifyPillarsForReplaceFile(context));
    }
    
    @Override
    public void endConversation() {
        context.setState(new FinishedState(context));       
    }

    @Override
    public void onMessage(Message message) {
        context.getState().handleMessage(message);
    }
    
    @Override
    public boolean hasEnded() {
        return context.getState() instanceof FinishedState;
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

}
