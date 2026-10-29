/*
 * #%L
 * Bitrepository Monitoring Service
 * %%
 * Copyright (C) 2010 - 2026 Royal Danish Library and The State Archives, Denmark
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
package org.bitrepository.monitoringservice.webservice;

import org.bitrepository.TestGroups;
import org.bitrepository.monitoringservice.MonitoringService;
import org.bitrepository.monitoringservice.MonitoringServiceFactory;
import org.bitrepository.monitoringservice.status.ComponentStatus;
import org.bitrepository.monitoringservice.status.ComponentStatusCode;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class RestMonitoringServiceTest {
    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getComponentStatusReturnsAWebStatusForEachComponentTest() {
        ComponentStatus unresponsiveStatus = new ComponentStatus();
        unresponsiveStatus.markAsUnresponsive();
        Map<String, ComponentStatus> statusMap = new LinkedHashMap<>();
        statusMap.put("pillar1", new ComponentStatus());
        statusMap.put("pillar2", unresponsiveStatus);
        MonitoringService monitoringService = mock(MonitoringService.class);
        when(monitoringService.getStatus()).thenReturn(statusMap);

        List<WebStatus> statuses;
        try (MockedStatic<MonitoringServiceFactory> factory = mockStatic(MonitoringServiceFactory.class)) {
            factory.when(MonitoringServiceFactory::getMonitoringService).thenReturn(monitoringService);
            statuses = new RestMonitoringService().getComponentStatus();
        }

        assertEquals(2, statuses.size());
        assertEquals("pillar1", statuses.get(0).getComponentID());
        assertEquals(ComponentStatusCode.UNKNOWN.toString(), statuses.get(0).getStatus());
        assertEquals("N/A", statuses.get(0).getTimeStamp());
        assertEquals("pillar2", statuses.get(1).getComponentID());
        assertEquals(ComponentStatusCode.UNRESPONSIVE.toString(), statuses.get(1).getStatus());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void getComponentStatusWithoutComponentsTest() {
        MonitoringService monitoringService = mock(MonitoringService.class);
        when(monitoringService.getStatus()).thenReturn(Map.of());

        List<WebStatus> statuses;
        try (MockedStatic<MonitoringServiceFactory> factory = mockStatic(MonitoringServiceFactory.class)) {
            factory.when(MonitoringServiceFactory::getMonitoringService).thenReturn(monitoringService);
            statuses = new RestMonitoringService().getComponentStatus();
        }

        assertEquals(List.of(), statuses);
    }
}
