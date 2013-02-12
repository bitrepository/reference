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
import org.bitrepository.bitrepositoryelements.ResultingAuditTrails;
import org.bitrepository.client.eventhandler.CompleteEvent;
import org.bitrepository.client.eventhandler.EventHandler;
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
        settings.getRepositorySettings().getGetAuditTrailSettings().getContributorIDs().clear();
        settings.getRepositorySettings().getGetAuditTrailSettings().getContributorIDs().add("Contributor1");
        settings.getReferenceSettings().getAuditTrailServiceSettings().setCollectAuditInterval(500);
        settings.getReferenceSettings().getAuditTrailServiceSettings().setTimerTaskCheckInterval(100L);
        
        MockAuditClient client = new MockAuditClient();
        MockAuditStore store = new MockAuditStore();
        AuditTrailCollector collector = new AuditTrailCollector(settings, client, store);
        Assert.assertEquals(client.getCallsToGetAuditTrails(), 0);
        Thread.sleep(1000);
        EventHandler eventHandler = client.getLatestEventHandler();
        eventHandler.handleEvent(new AuditTrailResult("Contributor1", new ResultingAuditTrails(), false));
        eventHandler.handleEvent(new CompleteEvent(null));
        Assert.assertEquals(client.getCallsToGetAuditTrails(), 1);
        Thread.sleep(1000);
        eventHandler = client.getLatestEventHandler();
        eventHandler.handleEvent(new AuditTrailResult("Contributor1", new ResultingAuditTrails(), false));
        eventHandler.handleEvent(new CompleteEvent(null));
        Assert.assertEquals(client.getCallsToGetAuditTrails(), 2);
        collector.close();
    }
}
