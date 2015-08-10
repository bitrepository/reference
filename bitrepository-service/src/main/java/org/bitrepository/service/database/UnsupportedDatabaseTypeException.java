package org.bitrepository.service.database;

/**
 * Exception to indicate that a given database type is not supported.  
 */
@SuppressWarnings("serial")
public class UnsupportedDatabaseTypeException extends RuntimeException {

    public UnsupportedDatabaseTypeException(String message) {
        super(message);
    }
}
