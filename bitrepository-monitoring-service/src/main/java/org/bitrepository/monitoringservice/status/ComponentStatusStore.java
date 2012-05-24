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

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ComponentStatusStore {
    private Logger log = LoggerFactory.getLogger(getClass());
    private final ConcurrentMap<String, ComponentStatus> statusMap;
    
    public ComponentStatusStore(List<String> components) {
        statusMap = new ConcurrentHashMap<String, ComponentStatus>();
        for(String component : components) {
            statusMap.put(component, new ComponentStatus());
        }
    }
    
    public synchronized void updateStatus(String componentID, ResultingStatus status) {
        if(statusMap.containsKey(componentID)) {
            statusMap.get(componentID).updateStatus(status);
        } else {
            log.error("Got status from an unexpected component");
        }
    }
    
    public synchronized void updateReplyCounts() {
        for(String ID : statusMap.keySet()) {
            statusMap.get(ID).updateReplys();
        }
    }
    
    public synchronized Map<String, ComponentStatus> getStatusMap() {
        return statusMap;
    }
}
