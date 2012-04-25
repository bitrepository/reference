package org.bitrepository.audittrails.collector;

import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.client.eventhandler.DefaultEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AuditCollectorTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings();
    }

    @Test(groups = {"regressiontest"})
    public void AuditCollectorIntervalTest() throws Exception {
        addDescription("Test that the collector calls the AuditClient at the correct intervals.");
        addStep("Setup varables", "Should be OK.");
        settings.getReferenceSettings().getAuditTrailServiceSettings().setCollectAuditInterval(950);
        settings.getReferenceSettings().getAuditTrailServiceSettings().setTimerTaskCheckInterval(500L);
        
        MockAuditClient client = new MockAuditClient();
        MockAuditStore store = new MockAuditStore();
        AuditTrailCollector collector = new AuditTrailCollector(settings, client, store);
        
        synchronized(this) {
            this.wait(2100);
        }
        collector.close();
        
        Assert.assertEquals(client.getCallsToGetAuditTrails(), 2);
        Assert.assertEquals(store.getCallsToLargestSequenceNumber(), client.getCallsToGetAuditTrails() 
                * settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors().size(),
                "There should be one call for largest sequence number for each contributor for each call to the client.");
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);
        Assert.assertEquals(store.getCallsToGetAuditTrails(), 0);
    }
    
    @Test(groups = {"regressiontest"})
    public void AuditCollectorEventHandlerTest() throws Exception {
        addDescription("Test the eventHandler for the AuditTrailCollector.");
        addStep("Setup varables", "Should be OK.");
        String info = "INFO";
        String pillarID = "test-pillar";
        
        MockAuditClient client = new MockAuditClient();
        MockAuditStore store = new MockAuditStore();
        AuditTrailCollector collector = new AuditTrailCollector(settings, client, store);
        
        addStep("Run the 'collector'.", "Validator that the collector calls the client.");
        collector.collectNewestAudits();
        
        Assert.assertEquals(client.getCallsToGetAuditTrails(), 1);
        Assert.assertEquals(store.getCallsToLargestSequenceNumber(), client.getCallsToGetAuditTrails() 
                * settings.getReferenceSettings().getAuditTrailServiceSettings().getContributors().size(),
                "There should be one call for largest sequence number for each contributor for each call to the client.");
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);
        Assert.assertEquals(store.getCallsToGetAuditTrails(), 0);
        
        addStep("Extract the eventhandler and give it different kinds of events to handle.", 
                "In the case of a AuditTrailResult it should make a call for the store.");
        EventHandler eventHandler = client.getLatestEventHandler();

        eventHandler.handleEvent(new DefaultEvent(OperationEventType.COMPONENT_FAILED, info));
        eventHandler.handleEvent(new DefaultEvent(OperationEventType.FAILED, info));
        eventHandler.handleEvent(new DefaultEvent(OperationEventType.IDENTIFY_TIMEOUT, info));
        eventHandler.handleEvent(new DefaultEvent(OperationEventType.NO_COMPONENT_FOUND, info));        
        eventHandler.handleEvent(new DefaultEvent(OperationEventType.COMPLETE, info));

        Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);
        AuditTrailResult result = new AuditTrailResult(info, pillarID, new ResultingAuditTrails());
        eventHandler.handleEvent(result);
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 1);
    }    
}
