package org.bitrepository.integrityclient;

/**
 * Exception thrown on trouble retrieving integrity information.
 */
public class IntegrityInformationRetrievalException extends RuntimeException {
    /**
     * Create exception for integrity information retrieval error.
     * @param message What went wrong.
     */
    public IntegrityInformationRetrievalException(String message) {
        super(message);
    }

    /**
     * Create exception for integrity information retrieval error.
     * @param message What went wrong.
     * @param cause Exception that caused this exception.
     */
    public IntegrityInformationRetrievalException(String message, Throwable cause) {
        super(message, cause);
    }
}
