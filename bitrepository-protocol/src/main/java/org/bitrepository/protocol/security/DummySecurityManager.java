package org.bitrepository.protocol.security;

/**
 * Class containing empty / safe implementation of the SecurityManager interface.
 * It is intented to be used in tests, or where the functionality of a real SecurityManager implementation is not
 * needed. 
 */
public class DummySecurityManager implements SecurityManager {

    @Override
    public void authenticateMessage(String message, String signature) throws MessageAuthenticationException {
        // Safe empty implementation
    }

    @Override
    public String signMessage(String message) throws MessageSigningException {
        // Safe empty implementation
        return null;
    }

    @Override
    public void authorizeOperation(String operationType, String messageData, String signature) 
            throws OperationAuthorizationException {
        // Safe empty implementation
    }

}
