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
    private final BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();
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
