package org.bitrepository.protocol.security;

import org.bouncycastle.cms.SignerId;

/**
 * Class to authorize an operation based on the certificate which has signed the operation request.  
 */
public interface OperationAuthorizor {

    /**
     * Authorize an operation based on its signature
     * @param String operationType, the type of operation that should be authorized.
     * @param byte[] signature, the signature that belongs to the request. 
     * @throws OperationAuthorizationException if the authorization fails.  
     */
    public abstract void authorizeOperation(String operationType, SignerId signer) throws OperationAuthorizationException;

}
