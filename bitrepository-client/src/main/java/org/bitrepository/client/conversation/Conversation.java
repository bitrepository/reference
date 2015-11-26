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

import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.protocol.messagebus.MessageListener;

/**
 * A conversation models the messaging based workflow used to accomplish a operation called on a client interface.
 * 
 * The interface primarily consists of sending and receiving message functionality.
 * 
 * <h3>Implementation considerations</h3>
 * 
 * The following concerns needs to be addressed by classes implementing this interface.
 * 
 * <h4>Threadsafe</h4>
 * Note that conversation needs to be thread safe as multiple messages and timer event may occur in parallel. 
 * 
 * <h4>Timeouts</h4>
 * The conversation implementation should include timeouts for the different conversation phases.
 * 
 * <h4>Error handling</h4>
 * As conversation will normally be called in either a asynchronously manor using a callback listener or synchronous, 
 * the error handling needs to behave differently in the two cases. This means a error handling block will typically 
 * contain a <code>if (callbackListener == null) { ... } else {...}</code> block.
 */
public interface Conversation extends MessageListener {
    /**
     * Get the conversation ID for this conversation.
     *
     * Implementations must ensure that conversation IDs are unique, and that this ID is always used when sending
     * messages.
     *
     * @return The conversation ID.
     */
    String getConversationID();
    
    /**
     * Returns the start time for the conversation. Main use is to timeout conversations, which have lasted too long.
     * @return The start time of the conversation as set by the System.currentTimeMillis().
     */
    long getStartTime();
    
    /**
     * Starts the conversation.
     */
    void startConversation();
    
    /**
     * Ends the conversation. This means setting any relevant conversation state.
     */
    void endConversation();

    /**
     * Return whether this conversation has ended.
     * Once this returns true it should never return false again. Conversations cannot be reused.
     *
     * @return Whether this conversation has ended.
     */
    boolean hasEnded();
    
    /**
     * Can be used to fail an conversation. This might be caused by a timeout or other event.
     * @param operationFailedEvent Contains information regarding the cause for the failure. 
     */
    void failConversation(OperationFailedEvent operationFailedEvent);
}
