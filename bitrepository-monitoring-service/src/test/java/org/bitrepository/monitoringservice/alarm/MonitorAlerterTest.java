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
package org.bitrepository.monitoringservice.alarm;

import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.bitrepositoryelements.StatusCode;
import org.bitrepository.bitrepositoryelements.StatusInfo;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.monitoringservice.MockStatusStore;
import org.bitrepository.monitoringservice.status.ComponentStatus;
import org.bitrepository.monitoringservice.status.ComponentStatusCode;
import org.bitrepository.protocol.IntegrationTest;
import org.bitrepository.service.contributor.ContributorContext;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class MonitorAlerterTest extends IntegrationTest {
    
    static int callsForError = 0;
    
    @Test(groups = {"regressiontest"})
    public void testMonitorAlerter() throws Exception {
        addDescription("Tests the " + BasicMonitoringServiceAlerter.class.getName());
        addStep("Setup", "");
        String componentID = "TestMonitorService";
        componentSettings.getReferenceSettings().getMonitoringServiceSettings().setMaxRetries(BigInteger.ONE);
        ContributorContext context = new ContributorContext(messageBus, componentSettings);
        
        AlerterStatusStore store = new AlerterStatusStore();
        
        addStep("Create the alerter, but ignore the part of actually sending the alarms. Just log it.", "");
        callsForError = 0;
        BasicMonitoringServiceAlerter alerter = new BasicMonitoringServiceAlerter(context, store) {
            // Workaround to avoid testing that it actually sends the alarm through the messagebus.
            @Override
            public void error(Alarm alarm) {
                callsForError++;
            }
        };
        
        Assert.assertEquals(store.getCallsForGetStatusMap(), 0);
        
        addStep("Check statuses with an empty map.", "Should only make a call for GetStatusMap");
        store.statuses = new HashMap<String, ComponentStatus>();
        alerter.checkStatuses();
        Assert.assertEquals(store.getCallsForGetStatusMap(), 1);
        Assert.assertEquals(callsForError, 0);
        
        addStep("Check the status when a positive entry exists.", "Should make another call for the GetStatusMap");
        ComponentStatus cs = new ComponentStatus();
        cs.updateStatus(createPositiveStatus());
        store.statuses.put(componentID, cs);
        alerter.checkStatuses();
        Assert.assertEquals(store.getCallsForGetStatusMap(), 2);
        Assert.assertEquals(callsForError, 0);
        
        addStep("Check the status when a negative entry exists.", 
                "Should send an alarm and make another call for the GetStatusMap");
        cs.updateReplys();
        store.statuses.put(componentID, cs);
        alerter.checkStatuses();
        Assert.assertEquals(store.getCallsForGetStatusMap(), 3);
        Assert.assertEquals(callsForError, 1);
        
        Assert.assertEquals(cs.getStatus(), ComponentStatusCode.UNRESPONSIVE);
    }
    
    private ResultingStatus createPositiveStatus() {
        ResultingStatus res = new ResultingStatus();
        StatusInfo si = new StatusInfo();
        si.setStatusCode(StatusCode.OK);
        si.setStatusText("createPositiveStatus");
        res.setStatusInfo(si);
        res.setStatusTimestamp(CalendarUtils.getEpoch());
        return res;
    }

    @Override
    protected String getComponentID() {
        return "MonitorAlerterUnderTest";
    }

    class AlerterStatusStore extends MockStatusStore {
        public Map<String, ComponentStatus> statuses = new HashMap<String, ComponentStatus>();
        @Override
        public Map<String, ComponentStatus> getStatusMap() {
            super.getStatusMap();
            return statuses;
        }
    }
}
