package org.bitrepository.protocol.security;

/**
 *  Exception class to indicate that a certificate has been used by someone who should not.  
 */
public class CertificateUseException extends Exception {
    
    /** 
     * Constructor for MessageAuthenticationException
     * @param message, the message describing the reason for the exception
     */
    public CertificateUseException(String message) {
        super(message);
    }
    
    /** 
     * Constructor for MessageAuthenticationException
     * @param message, the message describing the reason for the exception
     * @param cause, the cause for throwing the exception
     */
    public CertificateUseException(String message, Throwable cause) {
        super(message, cause);
    }
}
