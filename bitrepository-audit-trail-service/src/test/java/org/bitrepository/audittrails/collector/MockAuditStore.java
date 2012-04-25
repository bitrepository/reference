package org.bitrepository.audittrails.collector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;

public class MockAuditStore implements AuditTrailStore {
    
    private int callsToGetAuditTrails = 0;
    @Override
    public Collection<AuditTrailEvent> getAuditTrails(String fileId, String contributorId, Long minSeqNumber,
            Long maxSeqNumber, String actorName, FileAction operation, Date startDate, Date endDate) {
        callsToGetAuditTrails++;
        return new ArrayList<AuditTrailEvent>();
    }
    public int getCallsToGetAuditTrails() {
        return callsToGetAuditTrails;
    }
    
    private int callsToAddAuditTrails = 0;
    @Override
    public void addAuditTrails(AuditTrailEvents newAuditTrails) {
        callsToAddAuditTrails++;
    }
    public int getCallsToAddAuditTrails() {
        return callsToAddAuditTrails;
    }
    
    private int callsToLargestSequenceNumber = 0;
    private int largestSequenceNumber = 0;
    @Override
    public int largestSequenceNumber(String contributorId) {
        callsToLargestSequenceNumber++;
        return largestSequenceNumber;
    }
    public void setLargestSequenceNumber(int seq) {
        largestSequenceNumber = seq;
    }
    public int getCallsToLargestSequenceNumber() {
        return callsToLargestSequenceNumber;
    }
    
    @Override
    public void close() {
        callsToGetAuditTrails = 0;
        callsToAddAuditTrails = 0;
        callsToLargestSequenceNumber = 0;
        largestSequenceNumber = 0;
    }
}
