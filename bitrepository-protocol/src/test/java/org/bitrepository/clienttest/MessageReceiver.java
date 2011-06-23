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
package org.bitrepository.clienttest;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;

import org.bitrepository.bitrepositorymessages.Alarm;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetAuditTrailsRequest;
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
import org.bitrepository.protocol.MessageListener;
import org.jaccept.TestEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

/**
 * May be added as listener to the message queue where it will store all received messages for later reading. 
 * 
 * This makes it possible to add the asynchronize reception of messages to the otherwise sequential test cases. Eg.
 * <pre>
 * {@code}
 * public void test() {
 *      //Setup
 *      MessageReceiver messageReceiver = new MessageReceiver();
 *      messagebus.addListener("", messageReceiver.getMessageListener());
 *      
 *      //Test step
 *      sendMessageXXX(messageToSend);
 *      receivedMessage = messageReceiver.waitForMessage(messageToSend.getClass(), 3, TimeUnit.SECONDS);
 *      assertEquals(messageToSend, receivedMessage);      
 *  }
 * }
 * </pre>  
 */
public class MessageReceiver {
    private final String name;
    //ToDo convert to TestLogger
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final MessageModel messageModel = new MessageModel();
    private final MessageListener messageListener;
    private final TestEventManager testEventManager;
    
    public MessageReceiver(String name, TestEventManager testEventManager) {
        super();
        this.name = name;
        this.testEventManager = testEventManager;
        messageListener = new TestMessageHandler(name + "Listener", messageModel);
    }

    public MessageListener getMessageListener() {
        return messageListener;
    }
    
    /**
     * Corresponds to the {@link #waitForMessage(Class, long, TimeUnit)} method with a default timeout of 1 second.
     */
    public <T> T waitForMessage(Class<T> messageType) {
        return waitForMessage(messageType, 3, TimeUnit.SECONDS);
    }

    /**
     * Waits for a message of the specified type to be received on the test message queue, when the message is 
     * received (or if one is already present) the message is returned.
     * 
     * @param messageType The type of message to wait for.
     * @param timeout The amount of time to wait for a message of this type.
     * @param unit The unit of time for the timeout value
     * @return The received message or null if no message was received.
     * @throws InterruptedException 
     */
    public <T> T waitForMessage(Class<T> messageType, long timeout, TimeUnit unit) {
        long startWait = System.currentTimeMillis();
        T message;  
        try {
            message = messageModel.getMessageQueue(messageType).poll(timeout, unit);
        } catch (InterruptedException e) {
            throw new RuntimeException(e); // Should never happen
        }
        long waitTime = System.currentTimeMillis()-startWait;
        if (message != null) {
            log.debug("Received message in (" + waitTime + " ms): " + message);
        } else {
            log.info("Wait for " + messageType.getName() + " message timed out (" + waitTime + " ms).");
            Assert.fail("Wait for " + messageType.getName() + " message timed out (" + waitTime + " ms).");
        }
        return message;
    }

    private class MessageModel {
        private Map<Class<?>, BlockingQueue<?>> messageMap = new HashMap<Class<?>, BlockingQueue<?>>();

        private <T> void addMessage(T message) {
            testEventManager.addResult(name + " received message: " + message);
            @SuppressWarnings("unchecked")
            BlockingQueue<T> queue = (BlockingQueue<T>)getMessageQueue(message.getClass());
            queue.add(message);
        }

        private synchronized <T> BlockingQueue<T> getMessageQueue(Class<T> messageType) {
            if (!messageMap.containsKey(messageType)) {
                messageMap.put(messageType, new LinkedBlockingQueue<T>());
            }

            @SuppressWarnings("unchecked")
            BlockingQueue<T> queue = (BlockingQueue<T>)messageMap.get(messageType);
            return queue;
        }
    }
    
    @Override
    public String toString() {
        return "MessageReceiver [name=" + name + "]";
    }

    // ToDo: Should the exception lister be part of this class?
    public class TestMessageHandler implements MessageListener, ExceptionListener {
        private final String listenerName;
        private MessageModel messageModel;

        public TestMessageHandler(String listenerName, MessageModel messageModel) {
            this.listenerName = listenerName;
            this.messageModel = messageModel;
        }

        @Override
        public String toString() {
            return "TestMessageHandler [listenerName=" + listenerName + "]";
        }
        
        @Override
        public void onMessage(Alarm message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetAuditTrailsRequest message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetAuditTrailsProgressResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetAuditTrailsFinalResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetChecksumsFinalResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetChecksumsRequest message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetChecksumsProgressResponse message) {
            messageModel.addMessage(message);            
        }
        @Override
        public void onMessage(GetFileFinalResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetFileIDsFinalResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetFileIDsRequest message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetFileIDsProgressResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetFileRequest message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetFileProgressResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetStatusRequest message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetStatusProgressResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(GetStatusFinalResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(IdentifyPillarsForGetChecksumsResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(IdentifyPillarsForGetChecksumsRequest message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(IdentifyPillarsForGetFileIDsResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(IdentifyPillarsForGetFileIDsRequest message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(IdentifyPillarsForGetFileResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(IdentifyPillarsForGetFileRequest message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(IdentifyPillarsForPutFileResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(IdentifyPillarsForPutFileRequest message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(PutFileFinalResponse message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(PutFileRequest message) {
            messageModel.addMessage(message);
        }
        @Override
        public void onMessage(PutFileProgressResponse message) {
            messageModel.addMessage(message);
        }   

        @Override
        public void onException(JMSException e) {
            e.printStackTrace();
        }
    }
}
