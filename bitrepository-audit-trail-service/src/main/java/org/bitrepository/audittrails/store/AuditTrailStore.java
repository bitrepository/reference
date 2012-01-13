package org.bitrepository.audittrails.store;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;

public interface AuditTrailStore {
    public AuditTrailEvent[] getAuditTrails(XMLGregorianCalendar starttime, XMLGregorianCalendar endtime, String url);
    
    public void addAuditTrails(AuditTrailEvents newAuditTrails);
}
