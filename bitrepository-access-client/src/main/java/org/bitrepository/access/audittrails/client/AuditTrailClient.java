package org.bitrepository.access.audittrails.client;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.access.audittrails.ComponentDestination;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;

/**
 * Handles the retrieval of from contributors and storage of the audit trails into the AuditTrail storage.
 */
public class AuditTrailClient {
    
    public AuditTrailEvent[] getAuditTrails(ComponentDestination contributorDestination, XMLGregorianCalendar starttime, XMLGregorianCalendar endtime, String url) {
        return null;
    }
    
    public AuditTrailEvent[] getAuditTrailsFromContributor(ComponentDestination contributorDestination, Integer minSequenceNumber, Integer maxSequenceNumber, String url) {
        return null;
    }
}
