package org.bitrepository.audittrails.store;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.AuditTrailData;

public class SimpleStore {
    private AuditTrailData auditTrails = new AuditTrailData();
    
    public AuditTrailData getAuditTrails(XMLGregorianCalendar starttime, XMLGregorianCalendar endtime, String url) {
        return auditTrails;
    }
    
    public void addAuditTrails(AuditTrailData newAuditTrails) {
        
    }
}
