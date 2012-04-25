package org.bitrepository.audittrails.collector;

import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.client.eventhandler.EventHandler;

public class MockAuditClient implements AuditTrailClient {
    
    @Override
    public void shutdown() {}
    
    private EventHandler latestEventHandler = null;
    public EventHandler getLatestEventHandler() {
        return latestEventHandler;
    }
    
    private int callsToGetAuditTrails = 0;
    @Override
    public void getAuditTrails(AuditTrailQuery[] componentQueries, String fileID, String urlForResult,
            EventHandler eventHandler, String auditTrailInformation) {
        latestEventHandler = eventHandler;
        callsToGetAuditTrails++;
    }
    public int getCallsToGetAuditTrails() {
        return callsToGetAuditTrails;
    }
    
}
