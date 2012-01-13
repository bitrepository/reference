package org.bitrepository.audittrails.store;

import java.util.Set;
import java.util.TreeSet;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;

public class SimpleStore implements AuditTrailStore{
    private Set<AuditTrailEvent> auditTrails = 
            new TreeSet<AuditTrailEvent>();
    
    @Override
    public AuditTrailEvent[] getAuditTrails(XMLGregorianCalendar starttime, XMLGregorianCalendar endtime, String url) {
        return (AuditTrailEvent[])auditTrails.toArray(new AuditTrailEvent[auditTrails.size()]);
    }

    @Override
    public void addAuditTrails(AuditTrailEvents newAuditTrails) {
        for (AuditTrailEvent event : newAuditTrails.getAuditTrailEvent()) {
            auditTrails.add(event);
        }
    }
}
