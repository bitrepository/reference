package org.bitrepository.protocol.security;

/**
 * Exception class to indicate that authentication of a message has failed.  
 */
public class MessageAuthenticationException extends Exception {
    
    /** 
     * Constructor for MessageAuthenticationException
     * @param message, the message describing the reason for the exception
     */
    public MessageAuthenticationException(String message) {
        super(message);
    }
    
    /**
     * Constructor for MessageAuthenticationException
     * @param message, the message describing the reason for the exception
     * @param e, the exception that caused the creation of MessageAuthenticationException
     */
    public MessageAuthenticationException(String message, Throwable e) {
        super(message, e);
    }
}
