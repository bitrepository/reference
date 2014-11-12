package org.bitrepository.integrityservice.tools;

/**
 * Exception class to indicate an invalid method.  
 */
@SuppressWarnings("serial")
public class InvalidMethodException extends IllegalStateException {

    public InvalidMethodException(String message) {
        super(message);
    }
    
}
