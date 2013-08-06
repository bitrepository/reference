package org.bitrepository.integrityservice.tools;

/**
 * Exception class to indicate unknown collection 
 */
public class UnknownCollectionException extends IllegalStateException {
    
    public UnknownCollectionException(String message) {
        super(message);
    }
}
