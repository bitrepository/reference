package org.bitrepository.access.getaudittrails;

import org.bitrepository.access.getaudittrails.client.AuditTrailClient;
import org.bitrepository.bitrepositoryelements.FileIDs;
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
    public void getAuditTrails(AuditTrailQuery[] componentQueries, FileIDs fileIDs, String urlForResult, EventHandler eventHandler,
                               String auditTrailInformation) {
        testEventManager.addStimuli(
                "Calling getAuditTrails(" + componentQueries + ", " + fileIDs + ", " + urlForResult + ")");
        auditTrailClient.getAuditTrails(componentQueries, fileIDs, urlForResult, eventHandler, auditTrailInformation);
    }

    @Override
    public void shutdown() {
        // Nothing to do
    }
}
