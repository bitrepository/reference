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
package org.bitrepository.monitoringservice.status;

import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.bitrepositoryelements.StatusCode;
import org.bitrepository.bitrepositoryelements.StatusInfo;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.settings.TestSettingsProvider;
import org.bitrepository.common.utils.CalendarUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import static org.bitrepository.protocol.utils.AllureTestUtils.addDescription;
import static org.bitrepository.protocol.utils.AllureTestUtils.addStep;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ComponentStatusStoreTest {
    Settings settings;
    
    @BeforeAll
    public void setup() {
        settings = TestSettingsProvider.reloadSettings("ComponentStatusStoreUnderTest");
    }

    @Test
    @Tag("regressiontest")
    public void testComponentStatus() throws Exception {
        addDescription("Tests the compontent status");
        addStep("Setup", "");
        String componentId = "componentId";
        Set<String> contributors = new HashSet<>();
        contributors.add(componentId);
        ComponentStatusStore store = new ComponentStatusStore(contributors);
        
        addStep("Validate the initial content", "Should be one component with a 'new and empty' component status.");
        Map<String, ComponentStatus> statuses = store.getStatusMap();
        Assertions.assertEquals(1, statuses.size());
        
        ComponentStatus newStatus = new ComponentStatus();
        Assertions.assertNotNull(statuses.get(componentId));
        Assertions.assertEquals(statuses.get(componentId).getInfo(), newStatus.getInfo());
        Assertions.assertEquals(statuses.get(componentId).getNumberOfMissingReplies(), newStatus.getNumberOfMissingReplies());
        Assertions.assertEquals(statuses.get(componentId).getLastReplyDate(), newStatus.getLastReplyDate());
        Assertions.assertEquals(statuses.get(componentId).getStatus(), newStatus.getStatus());

        addStep("Update the replay counts and validate ", "it should increases the 'number of missing replies' by 1");
        store.updateReplyCounts();
        statuses = store.getStatusMap();
        Assertions.assertEquals(1, statuses.size());
        Assertions.assertNotNull(statuses.get(componentId));
        Assertions.assertEquals(statuses.get(componentId).getInfo(), newStatus.getInfo());
        Assertions.assertEquals(1, statuses.get(componentId).getNumberOfMissingReplies());
        Assertions.assertEquals(statuses.get(componentId).getLastReplyDate(), newStatus.getLastReplyDate());
        Assertions.assertEquals(statuses.get(componentId).getStatus(), newStatus.getStatus());
        
        addStep("Test what happens when an invalid component id attempted to be updated.", "Should not affect content.");
        store.updateStatus("BAD-COMPONENT-ID", null);
        statuses = store.getStatusMap();
        Assertions.assertEquals(1, statuses.size());
        Assertions.assertNotNull(statuses.get(componentId));
        Assertions.assertEquals(statuses.get(componentId).getInfo(), newStatus.getInfo());
        Assertions.assertEquals(1, statuses.get(componentId).getNumberOfMissingReplies());
        Assertions.assertEquals(statuses.get(componentId).getLastReplyDate(), newStatus.getLastReplyDate());
        Assertions.assertEquals(statuses.get(componentId).getStatus(), newStatus.getStatus());
        
        addStep("Try giving it a positive status", "Should be inserted into the store.");
        ResultingStatus resStatus = createPositiveStatus();
        store.updateStatus(componentId, resStatus);
        statuses = store.getStatusMap();
        Assertions.assertEquals(1, statuses.size());
        Assertions.assertNotNull(statuses.get(componentId));
        Assertions.assertEquals(statuses.get(componentId).getInfo(), resStatus.getStatusInfo().getStatusText());
        Assertions.assertEquals(0, statuses.get(componentId).getNumberOfMissingReplies());
        Assertions.assertEquals(statuses.get(componentId).getLastReplyDate(), resStatus.getStatusTimestamp());
        Assertions.assertEquals(statuses.get(componentId).getStatus().value(), resStatus.getStatusInfo().getStatusCode().name());
    }
    
    private ResultingStatus createPositiveStatus() {
        ResultingStatus res = new ResultingStatus();
        StatusInfo si = new StatusInfo();
        si.setStatusCode(StatusCode.OK);
        si.setStatusText("createPositiveStatus");
        res.setStatusInfo(si);
        res.setStatusTimestamp(CalendarUtils.getNow());
        return res;
    }
}
