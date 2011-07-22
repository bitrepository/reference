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

import org.bitrepository.protocol.bitrepositorycollection.ClientSettings;
import org.bitrepository.protocol.bitrepositorycollection.MutableClientSettings;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.testng.annotations.Test;

/**
 * Test the general ConversationMediator functionality.
 */
@Test
public abstract class ConversationMediatorTest {
    protected MutableClientSettings settings = new MutableClientSettings(); 
    protected MessageBus messagebus;
    protected String listenerDestination;
    
    /**
     * Validates the core mediator functionality of delegating messages from the message bus to the relevant 
     * conversation.
     */
    @Test (groups = {"testfirst"})
    public void messagedelegationTest() {
        //  ConversationMediator mediator = createMediator(settings, messagebus, listenerDestination);
    }
    
    abstract ConversationMediator<?> createMediator(
            ClientSettings settings, 
            MessageBus messagebus,
            String listenerDestination);
}
