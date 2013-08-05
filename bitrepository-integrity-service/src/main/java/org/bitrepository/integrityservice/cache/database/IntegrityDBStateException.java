package org.bitrepository.integrityservice.cache.database;

/**
 * Exception class to indicate that the integritydb has an unexpected state 
 */
public class IntegrityDBStateException extends IllegalStateException {
    
    public IntegrityDBStateException(String message) {
        super(message);
    }

}
