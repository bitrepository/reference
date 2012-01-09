package org.bitrepository.audittrails.store;

import javax.xml.datatype.XMLGregorianCalendar;

import org.bitrepository.bitrepositoryelements.AuditTrailData;
import org.bitrepository.bitrepositoryelements.AuditTrailDataItem;
import org.bitrepository.bitrepositoryelements.ComponentTYPE;

public interface AuditTrailStore {
    public AuditTrailDataItem[] getAuditTrails(XMLGregorianCalendar starttime, XMLGregorianCalendar endtime, String url);
    
    public void addAuditTrails(AuditTrailData newAuditTrails);
}
