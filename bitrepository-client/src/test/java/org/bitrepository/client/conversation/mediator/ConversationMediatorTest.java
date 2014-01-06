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

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.client.conversation.StateBasedConversation;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.testng.annotations.Test;

/**
 * Test the general ConversationMediator functionality.
 */
@Test
public abstract class ConversationMediatorTest {
    protected Settings settings = TestSettingsProvider.getSettings(getClass().getSimpleName());
    protected SecurityManager securityManager = new DummySecurityManager();

    /**
     * Validates the core mediator functionality of delegating messages from the message bus to the relevant 
     * conversation.
     */
    @Test (groups = {"testfirst"})
    public void messagedelegationTest() {
        ConversationMediator mediator = createMediator(settings);

        mediator.addConversation(new ConversationStub());
    }

    abstract ConversationMediator createMediator(Settings settings);

    @SuppressWarnings("unused")
    private class ConversationStub extends StateBasedConversation {
        private boolean hasStarted = false;
        private boolean hasFailed = false;
        private boolean hasEnded = false;
        private Object result = null;

        public ConversationStub() {
            super(null);
        }

        @Override
        public boolean hasEnded() {
            return hasEnded;
        }

        @Override
        public void startConversation() {
            hasStarted = true;
        }

        @Override
        public void endConversation() {}
        @Override
        public void onMessage(Message message, MessageContext messageContext) {}
    }
}
