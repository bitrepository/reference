package org.bitrepository.pillar;

import java.util.ArrayList;
import java.util.Collection;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;

public class MockAuditManager implements AuditTrailManager {

    private int callsForAuditEvent = 0;
    @Override
    public void addAuditEvent(String fileId, String actor, String info, String auditTrail, FileAction operation) {
        callsForAuditEvent++;
    }
    public int getCallsForAuditEvent() {
        return callsForAuditEvent;
    }
    public void resetCallsForAuditEvent() {
        callsForAuditEvent = 0;
    }

    private int callsForGetAudits = 0;
    @Override
    public Collection<AuditTrailEvent> getAudits(String fileId, Long sequenceNumber) {
        callsForGetAudits++;
        return new ArrayList<AuditTrailEvent>();
    }
    public int getCallsForGetAudits() {
        return callsForGetAudits;
    }
    public void resetCallsForGetAudits() {
        callsForGetAudits = 0;
    }
}
