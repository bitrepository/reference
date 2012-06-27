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
package org.bitrepository.client;

import org.bitrepository.bitrepositorymessages.*;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.jaccept.TestEventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.Assert;

import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

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
     * Corresponds to the {@link #waitForMessage(Class, long, TimeUnit)} method with a default timeout of 5 second.
     */
    public <T> T waitForMessage(Class<T> messageType) {
        return waitForMessage(messageType, 10, TimeUnit.SECONDS);
    }

    /**
     * Waits for a message of the specified type to be received on the test message queue, when the message is 
     * received (or if one is already present) the message is returned.
     * 
     * @param messageType The type of message to wait for.
     * @param timeout The amount of time to wait for a message of this type.
     * @param unit The unit of time for the timeout value
     * @return The received message or null if no message was received.
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
            log.info("Wait for " + messageType.getSimpleName() + " message timed out (" + waitTime + " ms).");
            Assert.fail("Wait for " + messageType.getSimpleName() + " message timed out (" + waitTime + " ms).");
        }
        return message;
    }

   /**
    * Verifies no message of the given type is received.
    * @param messageType The type of message to wait for.
            */
    public <T> void checkNoMessageIsReceived(Class<T> messageType) {
        T message;
        try {
            message = messageModel.getMessageQueue(messageType).poll(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e); // Should never happen
        }
        if (message != null) {
            Assert.fail("Received unexpected message " + message);
        }
    }

    private class MessageModel {
        private Map<Class<?>, BlockingQueue<?>> messageMap = new HashMap<Class<?>, BlockingQueue<?>>();

        private <T> void addMessage(T message) {
            if(testEventManager != null) {
                testEventManager.addResult(name + " received message: " + message);
            }
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

        public void onMessage(Message message) {
            messageModel.addMessage(message);
        }

        @Override
        public void onException(JMSException e) {
            e.printStackTrace();
        }
    }
}
