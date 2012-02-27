package org.bitrepository.protocol.security;

/**
 * Exception class to indicate that authorization of an operation has failed.  
 */
public class OperationAuthorizationException extends Exception {

    public OperationAuthorizationException(String message) {
        super(message);
    }
    
    public OperationAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
