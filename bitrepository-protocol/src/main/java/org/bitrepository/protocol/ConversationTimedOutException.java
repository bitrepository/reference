package org.bitrepository.protocol;

/**
 * Exception thrown when a conversation timed out.
 */
public class ConversationTimedOutException extends Exception {
    /**
     * Initiate a ConversationTimedOutException.
     *
     * @param message Description of problem.
     */
    public ConversationTimedOutException(String message) {
        super(message);
    }

    /**
     * Initiate a ConversationTimedOutException.
     *
     * @param message Description of problem.
     * @param cause The exception that caused this exception.
     */
    public ConversationTimedOutException(String message, Throwable cause) {
        super(message, cause);
    }
}
