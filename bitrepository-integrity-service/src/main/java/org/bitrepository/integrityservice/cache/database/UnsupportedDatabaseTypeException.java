package org.bitrepository.integrityservice.cache.database;

/**
 * Exception to indicate that a given database type is not supported.  
 */
public class UnsupportedDatabaseTypeException extends RuntimeException {

    public UnsupportedDatabaseTypeException(String message) {
        super(message);
    }
}
