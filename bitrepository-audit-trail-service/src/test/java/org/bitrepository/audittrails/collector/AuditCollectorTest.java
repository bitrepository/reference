/*
 * #%L
 * Bitrepository Audit Trail Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as 
 * published by the Free Software Foundation, either version 2.1 of the 
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public 
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-2.1.html>.
 * #L%
 */
package org.bitrepository.audittrails.collector;

import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.audittrails.MockAuditClient;
import org.bitrepository.audittrails.MockAuditStore;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.DefaultEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
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
        settings = TestSettingsProvider.reloadSettings("AuditCollectorUnderTest");
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
                * settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs().size(),
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
                * settings.getCollectionSettings().getGetAuditTrailSettings().getContributorIDs().size(),
                "There should be one call for largest sequence number for each contributor for each call to the client.");
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);
        Assert.assertEquals(store.getCallsToGetAuditTrails(), 0);
        
        addStep("Extract the eventhandler and give it different kinds of events to handle.", 
                "In the case of a AuditTrailResult it should make a call for the store.");
        EventHandler eventHandler = client.getLatestEventHandler();

        eventHandler.handleEvent(new ContributorFailedEvent("ContributorID", ResponseCode.REQUEST_NOT_SUPPORTED));
        eventHandler.handleEvent(new OperationFailedEvent(info, null));
        DefaultEvent identificationTimeoutEvent = new DefaultEvent();
        identificationTimeoutEvent.setType(OperationEventType.IDENTIFY_TIMEOUT);
        eventHandler.handleEvent(identificationTimeoutEvent);
        eventHandler.handleEvent(new CompleteEvent(null));

        Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);
        AuditTrailResult result = new AuditTrailResult(pillarID, new ResultingAuditTrails());
        eventHandler.handleEvent(result);
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 1);
    }    
}
