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
package org.bitrepository.monitoringservice.webservice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.monitoringservice.MonitoringService;
import org.bitrepository.monitoringservice.MonitoringServiceFactory;
import org.bitrepository.monitoringservice.status.ComponentStatus;

@Path("/MonitoringService")
public class RestMonitoringService {
    
    private MonitoringService service;
    
    public RestMonitoringService() {
        service = MonitoringServiceFactory.getMonitoringService();
    }
    
    @GET
    @Path("/getMonitoringConfiguration/")
    @Produces("application/json")
    public List<WebConfOption> getMonitoringServiceConfiguration() {
        List<WebConfOption> options = new ArrayList<>();
        
        options.add(new WebConfOption("Check interval", TimeUtils.millisecondsToHuman(service.getCollectionInterval())));
        options.add(new WebConfOption("Max retries", Long.toString(service.getMaxRetries())));
        
        return options;
    }
    
    @GET
    @Path("/getComponentStatus/")
    @Produces("application/json")
    public List<WebStatus> getComponentStatus() {
        Map<String, ComponentStatus> statusMap = service.getStatus();
        List<WebStatus> statuses = new ArrayList<>();
        
        for(String component : statusMap.keySet()) {
            statuses.add(new WebStatus(component, statusMap.get(component)));
        }
        return statuses;
    }
    
}
