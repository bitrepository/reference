package org.bitrepository.protocol;

/**
 * The interface for sending and receiving messages within a specific conversation.
 *
 * @param <T> The outcome of the conversation.
 */
public interface Conversation<T> extends MessageListener, MessageSender {
    /**
     * Set the mediator that handles this conversation. This method should always be called by the mediator
     * that generates the conversation, immediately after the conversation is generated.
     *
     * Implementations of conversations may use this mediator to end the conversation from within.
     *
     * @param mediator The handler that handles this conversation.
     */
    void setMediator(ConversationMediator mediator);

    /**
     * Get the conversation ID for this conversation.
     *
     * Implementations must ensure that conversation IDs are unique, and that this ID is always used when sending
     * messages.
     *
     * The conversation may have no ID until the first message is sent. In that case, this method returns null. Once
     * a message has been sent, however, this method should never return null, and the conversation ID should never
     * change.
     *
     * @return The conversation ID.
     */
    String getConversationID();

    /**
     * Return whether this conversation has ended.
     * Once this returns true it should never return false again. Conversations cannot be reused.
     *
     * @return Whether this conversation has ended.
     */
    boolean isEnded();

    /**
     * Get the result of this conversation. This will be null, until the conversation has ended,
     * and may be null after the conversation has ended, in case no result can be generated.
     *
     * @return The result of the conversation.
     */
    T getResult();

    /**
     * Block until the conversation is finished.
     *
     * @return The result of the conversation.
     */
    T waitFor();

    /**
     * Block until the conversation is finished, or until timeout has occurred.
     *
     * @param timeout Timeout (in milliseconds)
     *
     * @return The result of the conversation.
     *
     * @throws ConversationTimedOutException On timeout. A partial result MAY be avaiable through
     * {@link #getResult()}
     *
     */
    T waitFor(long timeout) throws ConversationTimedOutException;

}
