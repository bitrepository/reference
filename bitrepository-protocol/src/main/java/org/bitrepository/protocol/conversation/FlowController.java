package org.bitrepository.protocol.conversation;

import java.math.BigInteger;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.protocol.exceptions.ConversationTimedOutException;

/**
 * Encapsulates the logic for doing flow related functionality in a conversation, like blocking and timeouts.
 */
public class FlowController {
    private final boolean waitForCompletion;
    private final BigInteger identifyTimeout;
    private final BigInteger operationTimeout;
    private final BigInteger conversationTimeout;
    private Conversation conversation;


    public FlowController(Settings settings, boolean waitForCompletion) {
        super();
        this.waitForCompletion = waitForCompletion;
        this.identifyTimeout = settings.getCollectionSettings().getClientSettings().getIdentificationTimeout();
        this.operationTimeout = settings.getCollectionSettings().getClientSettings().getOperationTimeout();
        this.conversationTimeout = settings.getReferenceSettings().getClientSettings().getConversationTimeout();
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }


    public boolean waitForCompletion() throws ConversationTimedOutException {
        if (waitForCompletion) {
            waitFor(conversationTimeout.longValue());
        }
        return waitForCompletion;
    }

    /**
     * Will be removed shortly as the blocking functionality is removed.
     * @param timeout
     * @throws ConversationTimedOutException
     */
    private void waitFor(long timeout) throws ConversationTimedOutException {
        long startTime = System.currentTimeMillis();
        synchronized (conversation) {
            while (!conversation.hasEnded() && startTime + timeout > System.currentTimeMillis()) {
                try {
                    conversation.wait(timeout);
                } catch (InterruptedException e) {
                    // ignore
                }
            }
        }
        if (!conversation.hasEnded()) {
            throw new ConversationTimedOutException("Conversation timed out");
        }
    }

    /**
     * Notifies whoever waits for this conversation to end.
     */
    public void unblock() {  
        synchronized (conversation) {
            conversation.notifyAll();
        }
    }
}
