package org.bitrepository.protocol.security;

/**
 * Exception class to indicate that signing of a message has failed. 
 */
public class MessageSigningException extends Exception {

    public MessageSigningException(String message) {
        super(message);
    }
}
