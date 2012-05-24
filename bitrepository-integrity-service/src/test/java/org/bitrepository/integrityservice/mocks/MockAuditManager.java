package org.bitrepository.integrityservice.mocks;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.service.audit.AuditTrailManager;

public class MockAuditManager implements AuditTrailManager {

    private int callsToAddAuditEvent = 0;
    @Override
    public void addAuditEvent(String fileId, String actor, String info, String auditTrail, FileAction operation) {
        callsToAddAuditEvent++;
    }
    public int getCallsToAddAuditEvent() {
        return callsToAddAuditEvent;
    }

    private int callsToGetAudits = 0;
    @Override
    public Collection<AuditTrailEvent> getAudits(String fileId, Long minSeqNumber, Long maxSeqNumber, Date minDate,
            Date maxDate) {
        callsToGetAudits++;
        return new ArrayList<AuditTrailEvent>();
    }
    public int getCallsToGetAudits() {
        return callsToGetAudits;
    }
}
