package org.bitrepository.audittrails;

import org.bitrepository.audittrails.preserver.AuditTrailPreserver;

public class MockAuditPreserver implements AuditTrailPreserver {
    
    private int callsToStart = 0;
    @Override
    public void start() {
        callsToStart++;
    }
    public int getCallsToStart() {
        return callsToStart;
    }
    
    private int callsToPreserveAuditTrailsNow = 0;
    @Override
    public void preserveAuditTrailsNow() {
        callsToPreserveAuditTrailsNow++;
    }
    public int getCallsToPreserveAuditTrailsNow() {
        return callsToPreserveAuditTrailsNow;
    }
    
    private int callsToClose = 0;
    @Override
    public void close() {
        callsToClose++;
    }
    public int getCallsToClose() {
        return callsToClose;
    }
    
}
