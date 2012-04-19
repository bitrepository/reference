/*
 * #%L
 * Bitrepository Modifying Client
 * 
 * $Id: DeleteFileState.java 633 2011-12-14 09:05:28Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-modifying-client/src/main/java/org/bitrepository/modify/deletefile/conversation/DeleteFileState.java $
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

import org.bitrepository.client.conversation.ConversationEventMonitor;
import org.bitrepository.client.conversation.ConversationState;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;
import org.bitrepository.protocol.messagebus.MessageSender;

/**
 * The interface for states of the ReplaceFile communication.
 */
public abstract class ReplaceFileState extends AbstractMessageListener implements ConversationState {
    /** The conversation in the given state.*/
    protected final SimpleReplaceFileConversation conversation;
    /** Handles the mediation of information regarding conversation updates */
    protected final ConversationEventMonitor monitor;
    /** Used for sending messages */
    protected final MessageSender messageSender;
    
    /**
     * Constructor.
     * @param conversation The conversation in the given state.
     */
    protected ReplaceFileState(SimpleReplaceFileConversation conversation) {
        this.conversation = conversation;
        this.monitor = conversation.getMonitor();
        this.messageSender = conversation.messageSender;
    }
    
    /**
     * Mark this conversation as ended, and notifies whoever waits for it to end.
     */
    protected void endConversation() {
        conversation.conversationState = new ReplaceFileFinished(conversation);
    }
}
