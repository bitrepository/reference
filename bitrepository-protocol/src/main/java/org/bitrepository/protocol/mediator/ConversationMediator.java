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
package org.bitrepository.protocol.mediator;

import org.bitrepository.protocol.conversation.Conversation;
import org.bitrepository.protocol.messagebus.MessageListener;

/**
 * The interface for keeping track of conversations.
 *
 * Implementations must listen for messages and delegate them to the correct started conversation, until conversations
 * are ended.
 *
 * @param <T> The type of conversation to have.
 */
public interface ConversationMediator<T extends Conversation> extends MessageListener {
    /**
     * Start a conversation of type T and begin delegating messages to this conversation when received.
     *
     * @param The new conversation.
     */
    void addConversation(T conversation);

    /**
     * Consider a conversation as ended and stop delegating messages for it.
     *
     * @param conversation The conversation to end.
     */
    void endConversation(T conversation);
}
