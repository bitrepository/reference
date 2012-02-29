package org.bitrepository.protocol.security;

/**
 * Exception class to indicate that signing of a message has failed. 
 */
public class MessageSigningException extends Exception {

    /**
     * Constructor for MessageSigningException
     * @param message the message describing what caused the exception
     * @param cause the throwable object that caused the exception 
     */
    public MessageSigningException(String message, Throwable cause) {
        super(message, cause);
    }
}
