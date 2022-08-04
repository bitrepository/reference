/*
 * #%L
 * Bitrepository Monitoring Service
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
package org.bitrepository.monitoringservice.collector;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.monitoringservice.MockAlerter;
import org.bitrepository.monitoringservice.MockGetStatusClient;
import org.bitrepository.monitoringservice.MockStatusStore;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.Duration;

public class StatusCollectorTest extends ExtendedTestCase {
    Settings settings;
    
    private int INTERVAL = 500;
    private int INTERVAL_DELAY = 250;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings("StatusCollectorUnderTest");
    }

    @Test(groups = {"regressiontest"})
    public void testStatusCollector() throws Exception {
        addDescription("Tests the status collector.");
        addStep("Setup", "");
        
        MockAlerter alerter = new MockAlerter();
        MockStatusStore store = new MockStatusStore();
        MockGetStatusClient client = new MockGetStatusClient();
        Duration intervalXmlDur = DatatypeFactory.newInstance().newDuration(INTERVAL);
        settings.getReferenceSettings().getMonitoringServiceSettings().setCollectionInterval(intervalXmlDur);

        addStep("Create the collector", "");
        Assert.assertEquals(store.getCallsForGetStatusMap(), 0);
        Assert.assertEquals(store.getCallsForUpdateReplayCounts(), 0);
        Assert.assertEquals(store.getCallsForUpdateStatus(), 0);
        Assert.assertEquals(client.getCallsToGetStatus(), 0);
        Assert.assertEquals(client.getCallsToShutdown(), 0);
        StatusCollector collector = new StatusCollector(client, settings, store, alerter);
        
        addStep("Start the collector", "It should immediately call the client and store.");
        collector.start();
        synchronized(this) {
            wait(INTERVAL_DELAY);
        }
        Assert.assertEquals(store.getCallsForGetStatusMap(), 0);
        Assert.assertEquals(store.getCallsForUpdateReplayCounts(), 1);
        Assert.assertEquals(store.getCallsForUpdateStatus(), 0);
        Assert.assertEquals(client.getCallsToGetStatus(), 1);
        Assert.assertEquals(client.getCallsToShutdown(), 0);

        addStep("wait 2 * the interval", "It should call the client and store two times more.");        
        synchronized(this) {
            wait(2 * INTERVAL);
        }
        collector.stop();
        
        Assert.assertEquals(store.getCallsForGetStatusMap(), 0);
        Assert.assertEquals(store.getCallsForUpdateReplayCounts(), 3);
        Assert.assertEquals(store.getCallsForUpdateStatus(), 0);
        Assert.assertEquals(client.getCallsToGetStatus(), 3);
        Assert.assertEquals(client.getCallsToShutdown(), 0);
        
        addStep("wait the interval + delay again", "It should not have made any more calls");        
        synchronized(this) {
            wait(INTERVAL + INTERVAL_DELAY);
        }
        
        Assert.assertEquals(store.getCallsForGetStatusMap(), 0);
        Assert.assertEquals(store.getCallsForUpdateReplayCounts(), 3);
        Assert.assertEquals(store.getCallsForUpdateStatus(), 0);
        Assert.assertEquals(client.getCallsToGetStatus(), 3);
        Assert.assertEquals(client.getCallsToShutdown(), 0);
    }
}
