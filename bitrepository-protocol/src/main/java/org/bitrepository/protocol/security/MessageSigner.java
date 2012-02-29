package org.bitrepository.protocol.security;

import java.security.KeyStore.PrivateKeyEntry;

/**
 * Interface for classes using CMS to sign messages. 
 */
public interface MessageSigner {

    /**
     * Setter method for setting the PrivateKeyEntry needed by implementers to sign messages. 
     * @param privateKeyEntry the PrivateKeyEntry used for signing messages. 
     */
    public abstract void setPrivateKeyEntry(PrivateKeyEntry privateKeyEntry);
    
    /**
     * Method to sign a message.
     * @param The messages in byte raw byte form
     * @return The raw signature. 
     * @throws MessageSigningException if the signing fails. 
     */
    public abstract byte[] signMessage(byte[] messageData) throws MessageSigningException;
}
