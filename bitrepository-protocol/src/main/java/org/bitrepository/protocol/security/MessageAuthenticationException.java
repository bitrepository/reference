package org.bitrepository.protocol.security;

/**
 * Exception class to indicate that authentication of a message has failed.  
 */
public class MessageAuthenticationException extends Exception {

    public MessageAuthenticationException(String message) {
        super(message);
    }
}
