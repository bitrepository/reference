package org.bitrepository.audittrails.store;

import java.util.Set;
import java.util.TreeSet;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.AuditTrailData;
import org.bitrepository.bitrepositoryelements.AuditTrailDataItem;
import org.bitrepository.bitrepositoryelements.ComponentTYPE;

public class SimpleStore implements AuditTrailStore{
    private Set<AuditTrailDataItem> auditTrails = 
            new TreeSet<AuditTrailDataItem>();
    
    @Override
    public AuditTrailDataItem[] getAuditTrails(XMLGregorianCalendar starttime, XMLGregorianCalendar endtime, String url) {
        return (AuditTrailDataItem[])auditTrails.toArray(new AuditTrailDataItem[auditTrails.size()]);
    }

    @Override
    public void addAuditTrails(AuditTrailData newAuditTrails) {
        for (AuditTrailDataItem event : newAuditTrails.getAuditTrailDataItem()) {
            auditTrails.add(event);
        }
    }
}
