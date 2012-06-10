package org.bitrepository.audittrails.preserver;

import java.util.HashMap;
import java.util.Map;

import org.bitrepository.audittrails.MockAuditStore;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.Test;

public class AuditPreservationEventHandlerTest extends ExtendedTestCase {
    String PILLARID = "pillarId";
    
    @Test(groups = {"regressiontest"})
    public void auditPreservationEventHandlerTest() throws Exception {
        addDescription("Test the handling of the audit trail event handler.");
        addStep("Setup", "");
        Map<String, Long> map = new HashMap<String, Long>();
        map.put(PILLARID, 1L);
        MockAuditStore store = new MockAuditStore();
        
        AuditPreservationEventHandler eventHandler = new AuditPreservationEventHandler(map, store);
        Assert.assertEquals(store.getCallsToSetPreservationSequenceNumber(), 0);
        
        addStep("Test the handling of a non-complete event.", "Should not make call");
        eventHandler.handleEvent(new ContributorEvent(OperationEventType.NO_COMPONENT_FOUND, "info", PILLARID, "conversationID"));
        Assert.assertEquals(store.getCallsToSetPreservationSequenceNumber(), 0);

        addStep("Test the handling of a complete event.", "Should make call");
        eventHandler.handleEvent(new ContributorEvent(OperationEventType.COMPLETE, "info", PILLARID, "conversationID"));
        Assert.assertEquals(store.getCallsToSetPreservationSequenceNumber(), 1);
        
        addStep("Test the handling of another complete event.", "Should not make another call");
        eventHandler.handleEvent(new ContributorEvent(OperationEventType.COMPLETE, "info", PILLARID, "conversationID"));
        Assert.assertEquals(store.getCallsToSetPreservationSequenceNumber(), 1);
    }
}
