package org.bitrepository.protocol.security;

/**
 * Interface for classes to authenticate messages based on a CMS signature. 
 */
public interface MessageAuthenticator {

    public abstract void authenticateMessage(byte[] messageData, byte[] signatureData);
}
