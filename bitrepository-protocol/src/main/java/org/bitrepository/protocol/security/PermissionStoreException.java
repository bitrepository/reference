package org.bitrepository.protocol.security;


/**
 * Execption class to indicate a problem with a call to permission store.  
 */
public class PermissionStoreException extends Exception {

    public PermissionStoreException(String message) {
        super(message);
    }
}
