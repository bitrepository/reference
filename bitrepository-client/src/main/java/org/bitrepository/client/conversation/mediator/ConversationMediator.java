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
package org.bitrepository.client.conversation.mediator;

import org.bitrepository.client.conversation.Conversation;
import org.bitrepository.protocol.messagebus.MessageListener;

/**
 * Keeps track of conversations.
 *
 * Must listen delegate messages to the correct conversations.
 */
public interface ConversationMediator extends MessageListener {
    /**
     * Will begin listning for messages on the message bus.
     */
    void start();

    /**
     * Will stop this <code>ConversationMediator</code> from listening to the message bus.
     */
    void shutdown();

    /**
     * Start a conversation of type T and begin delegating messages to this conversation when received.
     *
     * @param conversation The new conversation.
     */
    void addConversation(Conversation conversation);
}
