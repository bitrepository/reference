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
package org.bitrepository.protocol.bus;

import jakarta.jms.ExceptionListener;
import jakarta.jms.JMSException;
import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.messagebus.MessageListener;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * May be added as listener to the message queue where it will store all received messages for later reading. 
 * 
 * This makes it possible to add the asynchronize reception of messages to the otherwise sequential test cases. I.E.
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
public class MessageReceiver implements AutoCloseable {
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private final MessageModel messageModel = new MessageModel();
    private final String destination;
    private final MessageListener messageListener;

    private final ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

    @Override
    public void close() {
        executorService.close();
    }

    /**
     * @param destination The destination to use for the receiver. Primarily used for logging purposes.
     */
    public MessageReceiver(String destination) {
        this.destination = destination;

        messageListener = new TestMessageHandler();
    }

    public String getDestination() {
        return destination;
    }

    /** Can be used to ignore messages from irrelevant components */
    public void setFromFilter(Collection<String> filter) {
        messageModel.pillarFilter = filter;
    }

    public MessageListener getMessageListener() {
        return messageListener;
    }

    /**
     * Corresponds to the {@link #waitForMessage(Class, long, TimeUnit, String)} method with a default timeout of 10 second.
     */
    public <T extends Message> T waitForMessage(Class<T> messageType) {
        return waitForMessage(messageType, 10, TimeUnit.SECONDS, null);
    }

    /**
     * Corresponds to the {@link #waitForMessage(Class, long, TimeUnit, String)} method with a default timeout of 10 second.
     */
    public <T extends Message> T waitForMessage(Class<T> messageType, String correlationId) {
        return waitForMessage(messageType, 10, TimeUnit.SECONDS, correlationId);
    }

    public <T extends Message> T waitForMessage(Class<T> messageType,
                                                long timeout,
                                                TimeUnit unit) {
        return waitForMessage(messageType, timeout, unit, null);
    }

    /**
     * Waits for a message of the specified type to be received on the test message queue, when the message is
     * received (or if one is already present) the message is returned.
     *
     * @param messageType   The type of message to wait for.
     * @param timeout       The amount of time to wait for a message of this type.
     * @param unit          The unit of time for the timeout value
     * @param correlationID if not null, will wait for messages with this exact correlation ID, ignoring other messages
     * @return The received message or null if no message was received.
     */
    public <T extends Message> T waitForMessage(Class<T> messageType,
                                                long timeout,
                                                TimeUnit unit,
                                                final String correlationID) {

        try {
            Callable<T> objectCallable = () -> waitForMessageLoop(messageType, correlationID);
            return executorService.invokeAny(List.of(objectCallable),
                                             timeout,
                                             unit);
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            return Assertions.fail("Wait for "
                                   + messageType.getSimpleName()
                                   + " message timed out");
        }
    }


    private <T extends Message> T waitForMessageLoop(Class<T> messageType,
                                                     final String correlationID) {

        while (true) {
            T message;
            try {
                BlockingQueue<T> messageQueue = messageModel.getMessageQueue(messageType);
                message = messageQueue.poll();

                if (message == null) {
                    Thread.sleep(1000);
                    continue;
                }
                if (correlationID == null || Objects.equals(message.getCorrelationID(),
                                                            correlationID)) {
                    return message;
                } else {
                    messageQueue.put(message);
                }
            } catch (InterruptedException e) {
                return null;
            }
        }
    }


    /**
     * Verifies no message of the given type is received.
     *
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
            Assertions.fail("Received unexpected message " + message);
        }
    }

    /**
     * Verifies that no messages remain in any queues.
     */
    public void checkNoMessagesRemain() {
        StringBuilder outstandingMessages = new StringBuilder();
        for (BlockingQueue<?> messageQueue : messageModel.getMessageQueues()) {
            if (!messageQueue.isEmpty()) {
                while (!messageQueue.isEmpty()) {
                    outstandingMessages.append("\n").append(messageQueue.poll());
                }
            }
            if (!outstandingMessages.isEmpty()) {
                String info = "The following messages haven't been handled by the testcase: " + outstandingMessages;
                log.warn(info);
            }
        }
    }

    /**
     * Clears the message in the queue.
     */
    public void clearMessages() {
        for (BlockingQueue<?> messageQueue : messageModel.getMessageQueues()) {
            if (!messageQueue.isEmpty()) {
                messageQueue.clear();
            }
        }
    }

    private static class MessageModel {
        private final Map<Class<?>, BlockingQueue<?>> messageMap = new HashMap<>();
        private Collection<String> pillarFilter;

        private <T> void addMessage(T message) {
            if (pillarFilter != null && !pillarFilter.contains(((Message) message).getFrom())) {
                return;
            }
            @SuppressWarnings("unchecked")
            BlockingQueue<T> queue = (BlockingQueue<T>) getMessageQueue(message.getClass());
            queue.add(message);
        }

        private synchronized <T> BlockingQueue<T> getMessageQueue(Class<T> messageType) {
            if (!messageMap.containsKey(messageType)) {
                messageMap.put(messageType, new LinkedBlockingQueue<>());
            }

            @SuppressWarnings("unchecked")
            BlockingQueue<T> queue = (BlockingQueue<T>) messageMap.get(messageType);
            return queue;
        }

        private synchronized Collection<BlockingQueue<?>> getMessageQueues() {
            return messageMap.values();
        }
    }
    
    @Override
    public String toString() {
        return "MessageReceiver(" + destination + ")";
    }

    public class TestMessageHandler implements MessageListener, ExceptionListener {

        @Override
        public String toString() {
            return "MessageReceiverListener(" + destination + ")";
        }

        public void onMessage(Message message, MessageContext messageContext) {
            messageModel.addMessage(message);
        }

        @Override
        public void onException(JMSException e) {
            log.error("Received error in MessageReceiver", e);
        }
    }
}
