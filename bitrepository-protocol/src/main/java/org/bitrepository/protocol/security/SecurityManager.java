package org.bitrepository.protocol.security;

public interface SecurityManager {
    /**
     * Method to authenticate a message. 
     * @param message, the message that needs to be authenticated. 
     * @param signature, the signature belonging to the message. 
     * @throws MessageAuthenticationException in case of failure.
     */
    public void authenticateMessage(String message, String signature) throws MessageAuthenticationException;
    
    /**
     * Method to sign a message
     * @param message, the message to sign
     * @return String the signature for the message, or null if authentication is disabled. 
     * @throws MessageSigningException if signing of the message fails.   
     */
    public String signMessage(String message) throws MessageSigningException;
    
    /**
     * Method to authorize an operation 
     * @param operationType, the type of operation that is to be authorized. 
     * @param messageData, the data of the message request. 
     * @param signature, the signature belonging to the message request. 
     * @throws OperationAuthorizationException in case of failure. 
     */
    public void authorizeOperation(String operationType, String messageData, String signature) 
            throws OperationAuthorizationException;
}
