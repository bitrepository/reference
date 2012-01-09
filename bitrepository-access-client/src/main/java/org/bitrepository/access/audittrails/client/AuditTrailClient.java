package org.bitrepository.access.audittrails.client;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.access.audittrails.ComponentDestination;
import org.bitrepository.bitrepositoryelements.AuditTrailDataItem;

/**
 * Handles the retrieval of from contributors and storage of the audit trails into the AuditTrail storage.
 */
public class AuditTrailClient {
    
    public AuditTrailDataItem[] getAuditTrails(ComponentDestination contributorDestination, XMLGregorianCalendar starttime, XMLGregorianCalendar endtime, String url) {
        return null;
    }
    
    public AuditTrailDataItem[] getAuditTrailsFromContributor(ComponentDestination contributorDestination, Integer minSequenceNumber, Integer maxSequenceNumber, String url) {
        return null;
    }
}
