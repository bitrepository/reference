/*
 * #%L
 * Bitrepository Access
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.access;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.messagebus.MessageSender;
import org.jaccept.TestEventManager;

public class MessageSenderStub implements MessageSender {

    /** The <code>TestEventManager</code> used to manage the event for the associated test. */
    private final TestEventManager testEventManager;
    /** The queue used to store the received messages. */
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<>();
    /** The default time to wait for messages */
    private static final long DEFAULT_WAIT_SECONDS = 10;  
    
    public MessageSenderStub(TestEventManager testEventManager) {
        this.testEventManager = testEventManager;
    }
    
    @Override
    public void sendMessage(Message content) {
       testEventManager.addStimuli("Sent message: " + content);
       messageQueue.add(content);
    }
    
    public void clearMessages() {
        messageQueue.clear();
    }
    
    /**
     * Wait for an event for the DEFAULT_WAIT_SECONDS amount of time.
     * @return The next event if any, else null 
     */
    public Message waitForMessage() throws InterruptedException {
        return waitForMessage(DEFAULT_WAIT_SECONDS, TimeUnit.SECONDS);
    }

    public Message waitForMessage(long timeout, TimeUnit unit) throws InterruptedException {
        return messageQueue.poll(timeout, unit);
    }
}
