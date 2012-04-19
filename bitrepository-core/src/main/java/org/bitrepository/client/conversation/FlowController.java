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

import java.math.BigInteger;

import org.bitrepository.common.settings.Settings;

/**
 * Encapsulates the logic for doing flow related functionality in a conversation, like blocking and timeouts.
 */
public class FlowController {
    private final BigInteger identifyTimeout;
    private final BigInteger operationTimeout;
    private Conversation conversation;

    /**
     * 
     * @param settings The configuration to use
     * @param waitForCompletion Should the controller block until the operation has completed.
     */
    public FlowController(Settings settings) {
        super();
        this.identifyTimeout = settings.getCollectionSettings().getClientSettings().getIdentificationTimeout();
        this.operationTimeout = settings.getCollectionSettings().getClientSettings().getOperationTimeout();
    }

    /**
     * @param conversation The conversation to use for context.
     */
    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    /**
     * Notifies whoever waits for this conversation to end.
     */
    public void unblock() {  
        synchronized (conversation) {
            conversation.notifyAll();
        }
    }
    
    /**
     * @return The identifyTimeout.
     */
    public BigInteger getIdentifyTimeout() {
        return identifyTimeout;
    }
    
    /**
     * @return The operationTimeout.
     */
    public BigInteger getOperationTimeout() {
        return operationTimeout;
    }
}
