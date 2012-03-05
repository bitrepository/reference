package org.bitrepository.protocol.security;

/**
 * Exception class to indicate that authorization of an operation has failed.  
 */
public class OperationAuthorizationException extends Exception {

    /**
     * Constructor for OperationAuthorizationException
     * @param message, description of why the exception was created 
     */
    public OperationAuthorizationException(String message) {
        super(message);
    }
    
    /**
     * Constructor for OperationAuthorizationException
     * @param message, description of why the exception was created
     * @param cause, the throwable that caused the exception to be created. 
     */    
    public OperationAuthorizationException(String message, Throwable cause) {
        super(message, cause);
    }
}
