package org.bitrepository.protocol.security;

import java.security.KeyStore.PrivateKeyEntry;

/**
 * Interface for classes using CMS to sign messages. 
 */
public interface MessageSigner {

    public abstract void setPrivateKeyEntry(PrivateKeyEntry privateKeyEntry);
    
    /**
     * Method to sign a message.
     * @param byte[] The messages in byte raw byte form
     * @return byte[] The raw signature. 
     * @throws MessageSigningException if the signing fails. 
     */
    public abstract byte[] signMessage(byte[] messageData) throws MessageSigningException;
}
