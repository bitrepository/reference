package org.bitrepository.access.getaudittrails;

import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.jaccept.TestEventManager;

public class AuditTrailClientTestWrapper implements AuditTrailClient {
    private AuditTrailClient auditTrailClient;
    private TestEventManager testEventManager;


    public AuditTrailClientTestWrapper(AuditTrailClient auditTrailClient,
                                    TestEventManager testEventManager) {
        this.auditTrailClient = auditTrailClient;
        this.testEventManager = testEventManager;
    }
    @Override
    public void getAuditTrails(AuditTrailQuery[] componentQueries, String urlForResult, EventHandler eventHandler,
                               String auditTrailInformation) {
        testEventManager.addStimuli("Calling getAuditTrails(" + componentQueries + ", " + urlForResult + ")");
        auditTrailClient.getAuditTrails(componentQueries, urlForResult, eventHandler, auditTrailInformation);
    }

    @Override
    public void shutdown() {
        // Nothing to do
    }
}
