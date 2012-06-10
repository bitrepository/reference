package org.bitrepository.audittrails.preserver;

/**
 * Interface for the preservation of audit trails.
 * This will automatically preserve the audit trails with a given interval.
 */
public interface AuditTrailPreserver {
    /**
     * Start the preservation of audit trails.
     */
    void start();
    
    /**
     * Method for performing the preservation of the unpreserved audit trails, around the scheduled preservation.
     */
    void preserveAuditTrailsNow();
    
    /**
     * Stop the preservation of audit trails.
     */
    void close();
}
