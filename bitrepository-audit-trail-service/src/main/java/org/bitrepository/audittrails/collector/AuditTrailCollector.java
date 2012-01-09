package org.bitrepository.audittrails.collector;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.AuditTrailDataItem;
/**
 * Manages the retrieval of of AuditTrails from contributors
 */
public class AuditTrailCollector {
    private Map<String,Contributor> contributors = new HashMap<String,Contributor>();
    
    
    public List<AuditTrailDataItem> collectNewAuditTrails() {
        return null;
    }
    
    private class Contributor {
        private String id;
        private int lastestSequenceNumber = 0; 
    }
}
