package org.bitrepository.protocol.conversation;

import org.bitrepository.bitrepositorymessages.Message;
import org.bitrepository.protocol.messagebus.AbstractMessageListener;

/**
 * Marker interface to indicate that this is the finished state for a conversation.
 */
public class FinishedState extends AbstractMessageListener implements ConversationState {
    /** Handles the mediation of information regarding conversation updates */
    protected final ConversationEventMonitor monitor;
    
    public FinishedState(ConversationEventMonitor monitor) {
        this.monitor = monitor;
    }

    public void onMessage(Message message) {
        monitor.outOfSequenceMessage("Received " + message.getClass().getName() + 
                " with replyTo " + message.getReplyTo() + " after the conversation has ended.");
    };
    
    @Override
    public void start() {
        //Nothing to do.      
    }

    @Override
    public boolean hasEnded() {
        return true;
    }
}
