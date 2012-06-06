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
package org.bitrepository.audittrails;

import org.bitrepository.audittrails.collector.AuditTrailCollector;
import org.bitrepository.audittrails.service.AuditTrailService;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.service.contributor.ContributorMediator;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class AuditTrailServiceTest extends ExtendedTestCase {
    /** The settings for the tests. Should be instantiated in the setup.*/
    Settings settings;
    
    @BeforeClass (alwaysRun = true)
    public void setup() throws Exception {
        settings = TestSettingsProvider.reloadSettings();
    }
    
    @Test(groups = {"regressiontest"})
    public void auditTrailServiceTest() throws Exception {
        addDescription("Test the Audit Trail Service");
        addStep("Setup variables.", "");
        
        MockAuditStore store = new MockAuditStore();
        MockAuditClient client = new MockAuditClient();
        AuditTrailCollector collector = new AuditTrailCollector(settings, client, store);
        ContributorMediator mediator = new MockContributorMediator();
        
        addStep("Instantiate the service.", "Should work.");
        AuditTrailService service = new AuditTrailService(store, collector, mediator);
        service.start();
        
        addStep("Try to collect audit trails.", "Should make a call to the client.");
        Assert.assertEquals(client.getCallsToGetAuditTrails(), 0);
        service.collectAuditTrails();
        Assert.assertEquals(client.getCallsToGetAuditTrails(), 1);
        
        addStep("Retrieve audit trails with and without an action", "Should work.");
        Assert.assertEquals(store.getCallsToAddAuditTrails(), 0);
        Assert.assertEquals(store.getCallsToGetAuditTrails(), 0);        
        service.queryAuditTrailEvents(null, null, null, null, null, null);
        Assert.assertEquals(store.getCallsToGetAuditTrails(), 1);        
        service.queryAuditTrailEvents(null, null, null, null, null, FileAction.FAILURE.name());
        Assert.assertEquals(store.getCallsToGetAuditTrails(), 2);        
        
        addStep("Shutdown", "");
        service.shutdown();
    }

    private class MockContributorMediator implements ContributorMediator {
        @Override
        public void start() { }
        @Override
        public void close() { }
    }
}
