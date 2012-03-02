package org.bitrepository.protocol.security;

/**
 * Exception class to indicate that no permissions has been registered for the given operation type.  
 */
public class UnregisteredPermissionException extends Exception {

    /**
     * Constructor for UnregisteredPermissionException
     * @param message, description of why the exception was created 
     */
    public UnregisteredPermissionException(String message) {
        super(message);
    }
    
    /**
     * Constructor for UnregisteredPermissionException
     * @param message, description of why the exception was created
     * @param cause, the throwable that caused the exception to be created. 
     */    
    public UnregisteredPermissionException(String message, Throwable cause) {
        super(message, cause);
    }
}
