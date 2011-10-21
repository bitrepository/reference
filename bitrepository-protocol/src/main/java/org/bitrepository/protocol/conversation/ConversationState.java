package org.bitrepository.protocol.conversation;

/**
 * Models a specific sate of a conversation.
 */
public interface ConversationState {

    /**
     * Starts this state.
     */
    void start();
    
    /**
     * Indicates whether this is a finish state
     * @return
     */
    boolean hasEnded();
}
