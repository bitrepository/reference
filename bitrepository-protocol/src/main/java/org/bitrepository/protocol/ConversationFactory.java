package org.bitrepository.protocol;

/**
 * Creator of conversations.
 */
public interface ConversationFactory<T extends Conversation> {
    /**
     * Create a conversation of type T.
     *
     * @return A new conversation.
     */
    T createConversation();
}
