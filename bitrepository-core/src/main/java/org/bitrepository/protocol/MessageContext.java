package org.bitrepository.protocol;

/**
 * Contains information about the message, not contained in the message itself.
 */
public class MessageContext {
    private final String certificateFingerprint;

    public MessageContext(String certificateFingerprint) {
        this.certificateFingerprint = certificateFingerprint;
    }

    public String getCertificateFingerprint() {
        return certificateFingerprint;
    }
}
