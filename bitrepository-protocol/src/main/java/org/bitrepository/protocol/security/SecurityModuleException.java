package org.bitrepository.protocol.security;

/**
 * Exception to indicate a serious problem in the security package.  
 */
public class SecurityModuleException extends RuntimeException {

    /**
     * Constructor. 
     * @param message, the textual description of the cause.
     * @param cause, the reason for throwing the exception. 
     */
    public SecurityModuleException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructor. 
     * @param message, the textual description of the cause.
     */
    public SecurityModuleException(String message) {
        super(message);
    }

}
