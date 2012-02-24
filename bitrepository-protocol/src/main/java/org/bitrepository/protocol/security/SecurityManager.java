package org.bitrepository.protocol.security;


import org.bitrepository.settings.collectionsettings.CollectionSettings;

/**
 * Class to handle:
 * - loading of certificates
 * - setup of SSLContext
 * - Authentication of signatures
 * - Signature generation 
 * - Authorization of operations
 */
public class SecurityManager {
    private final String privateKeyFile;
    private final CollectionSettings collectionSettings;
    private final MessageAuthenticator authenticator;
    private final MessageSigner signer;
    private final OperationAuthorizor authorizer;
    
    /**
     * Constructor for the class. 
     */
    public SecurityManager(CollectionSettings collectionSettings, String privateKeyFile, MessageAuthenticator authenticator,
            MessageSigner signer, OperationAuthorizor authorizer) {
        this.privateKeyFile = privateKeyFile;
        this.collectionSettings = collectionSettings;
        this.authenticator = authenticator;
        this.signer = signer;
        this.authorizer = authorizer;
    }
    
    /**
     * Method to authenticate a message. 
     * @param String message, the message that needs to be authenticated. 
     * @param String signature, the signature belonging to the message. 
     * @throws MessageAuthenticationException in case of failure.
     */
    public void authenticateMessage(String message, String signature) throws MessageAuthenticationException {
        if(collectionSettings.getProtocolSettings().isRequireMessageAuthentication()) {
            // call authenticator to authenticate message
        }
    }
    
    /**
     * Method to sign a message
     * @param String message, the message to sign
     * @return String the signature for the message, or null if authentication is disabled. 
     * @throws MessageSigningException if signing of the message fails.   
     */
    public String signMessage(String message) throws MessageSigningException {
        if(collectionSettings.getProtocolSettings().isRequireMessageAuthentication()) {
            // call signer create a signature based on the message
            return "";
        } else { 
            return null;
        }
    }
    
    /**
     * Method to authorize an operation 
     * @param String operationType, the type of operation that is to be authorized. 
     * @param String messageData, the data of the message request. 
     * @param String signature, the signature belonging to the message request. 
     * @throws OperationAuthorizationException in case of failure. 
     */
    public void authorizeOperation(String operationType, String messageData, String signature) 
            throws OperationAuthorizationException {
        if(collectionSettings.getProtocolSettings().isRequireOperationAuthorization()) {
            // call authorizer to authorize operation
        }
        
    }
}
