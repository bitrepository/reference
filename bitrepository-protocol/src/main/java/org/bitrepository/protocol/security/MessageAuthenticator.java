package org.bitrepository.protocol.security;

/**
 * Interface for classes to authenticate messages based on a CMS signature. 
 */
public interface MessageAuthenticator {

    /**
     * Method to authenticate a message based on a signature.
     * @param messageData, the data to authenticate
     * @param signatureData, the signature to authenticate the message from
     * @throws MessageAuthenticationException in case authentication fails. 
     */
    public abstract void authenticateMessage(byte[] messageData, byte[] signatureData) throws MessageAuthenticationException;
}
