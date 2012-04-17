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

import static org.mockito.Mockito.mock;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.mediator.ConversationMediator;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.bitrepository.protocol.security.DummySecurityManager;
import org.bitrepository.protocol.security.SecurityManager;
import org.testng.annotations.Test;

/**
 * Test the general ConversationMediator functionality.
 */
@Test
public abstract class ConversationMediatorTest {
    protected Settings settings = TestSettingsProvider.getSettings(); 
    protected SecurityManager securityManager = new DummySecurityManager();

    /**
     * Validates the core mediator functionality of delegating messages from the message bus to the relevant 
     * conversation.
     */
    @Test (groups = {"testfirst"})
    public void messagedelegationTest() {
        MessageBus messagebus = new MessageBusMock();
        ConversationMediator mediator = createMediator(settings);

        mediator.addConversation(new ConversationStub(messagebus, "testConversation"));
    }

    abstract ConversationMediator createMediator(Settings settings);

    private class ConversationStub extends AbstractConversation {
        private boolean hasStarted = false;
        private boolean hasFailed = false;
        private boolean hasEnded = false;
        private Object result = null;

        public ConversationStub(MessageSender messageSender, String conversationID) {
            super(messageSender, conversationID, null, new FlowController(settings));
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
        public void endConversation() {
        }

        @Override
        public ConversationState getConversationState() {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private class MessageBusMock implements MessageBus {        
        private MessageSender messageSender = mock(MessageSender.class);
        private MessageListener listener;

        @Override
        public void sendMessage(Message content) { 
            messageSender.sendMessage(content); 
        }
        
        @Override
        public void addListener(String destinationId, MessageListener listener) {
            this.listener = listener;
        }
        @Override
        public void removeListener(String destinationId, MessageListener listener) {
            listener = null;
        }
        
        @Override 
        public void close() {
            // Empty
        }
    }
}
