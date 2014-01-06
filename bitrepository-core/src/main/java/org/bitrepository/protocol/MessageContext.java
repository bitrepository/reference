package org.bitrepository.protocol;

/**
 * Contains information about the message, not contained in the message itself
 */
public class MessageContext {
    private final String certificateSignature;

    public MessageContext(String certificateSignature) {
        this.certificateSignature = certificateSignature;
    }

    public String getCertificateSignature() {
        return certificateSignature;
    }
}
