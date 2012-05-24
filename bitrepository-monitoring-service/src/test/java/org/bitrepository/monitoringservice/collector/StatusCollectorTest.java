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
import org.bitrepository.monitoringservice.collector.StatusCollector;
import org.jaccept.structure.ExtendedTestCase;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

public class StatusCollectorTest extends ExtendedTestCase {
    Settings settings;
    
    @BeforeClass (alwaysRun = true)
    public void setup() {
        settings = TestSettingsProvider.reloadSettings();
    }

    @Test(groups = {"regressiontest"})
    public void testStatusCollector() throws Exception {
        addDescription("Tests the status collector.");
        addStep("Setup", "");
        
        MockAlerter alerter = new MockAlerter();
        MockStatusStore store = new MockStatusStore();
        MockGetStatusClient client = new MockGetStatusClient();
        settings.getReferenceSettings().getMonitoringServiceSettings().setCollectionInterval(100);

        addStep("Create the collector", "");
        StatusCollector collector = new StatusCollector(client, settings, store, alerter);
        
        addStep("Run the collector for 250 millis", "It should call the store and the client 3 times");
        collector.start();
        synchronized(this) {
            wait(250);
        }
        collector.stop();
        
        Assert.assertEquals(store.getCallsForGetStatusMap(), 0);
        Assert.assertEquals(store.getCallsForUpdateReplayCounts(), 3);
        Assert.assertEquals(store.getCallsForUpdateStatus(), 0);
        Assert.assertEquals(client.getCallsToGetStatus(), 3);
        Assert.assertEquals(client.getCallsToShutdown(), 0);
    }
}
