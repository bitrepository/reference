package org.bitrepository.protocol;

/**
 * The interface for keeping track of conversations.
 *
 * Implementations must listen for messages and delegate them to the correct started conversation, until conversations
 * are ended.
 *
 * @param <T> The type of conversation to have.
 */
public interface ConversationMediator<T extends Conversation> extends MessageListener {
    /**
     * Start a conversation of type T and begin delegating messages to this conversation when received.
     *
     * @return The started conversation.
     */
    T startConversation();

    /**
     * Consider a conversation as ended and stop delegating messages for it.
     *
     * @param conversation The conversation to end.
     */
    void endConversation(T conversation);
}
