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

import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.protocol.conversation.AbstractConversation;
import org.bitrepository.protocol.conversation.ConversationState;
import org.bitrepository.protocol.conversation.FlowController;
import org.bitrepository.protocol.exceptions.OperationFailedException;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.testng.annotations.Test;

/**
 * Test the general ConversationMediator functionality.
 */
@Test
public abstract class ConversationMediatorTest {
    protected Settings settings = TestSettingsProvider.getSettings(); 

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
            super(messageSender, conversationID, null, new FlowController(settings, false));
        }

        @Override
        public boolean hasEnded() {
            return hasEnded;
        }

        @Override
        public void startConversation() throws OperationFailedException {
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
        public void sendMessage(Alarm content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetChecksumsFinalResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetChecksumsRequest content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetChecksumsProgressResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetFileFinalResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetFileIDsFinalResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetFileIDsRequest content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetFileIDsProgressResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetFileRequest content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetFileProgressResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetStatusRequest content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetStatusProgressResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(GetStatusFinalResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(IdentifyPillarsForGetChecksumsResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(IdentifyPillarsForGetChecksumsRequest content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(IdentifyPillarsForGetFileIDsRequest content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(IdentifyPillarsForGetFileIDsResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(IdentifyPillarsForGetFileRequest content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(IdentifyPillarsForGetFileResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(IdentifyPillarsForPutFileResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(IdentifyPillarsForPutFileRequest content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(PutFileFinalResponse content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(PutFileRequest content) { 
            messageSender.sendMessage(content); 
        }
        @Override
        public void sendMessage(PutFileProgressResponse content) { 
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
    }
}
