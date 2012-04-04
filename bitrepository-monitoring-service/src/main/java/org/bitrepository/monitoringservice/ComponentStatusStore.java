package org.bitrepository.monitoringservice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.bitrepository.bitrepositoryelements.ResultingStatus;

public class ComponentStatusStore {

    ConcurrentMap<String, ResultingStatus> statusMap;
    
    public ComponentStatusStore() {
        statusMap = new ConcurrentHashMap<String, ResultingStatus>();
    }
    
    public synchronized void updateStatus(String componentID, ResultingStatus status) {
        if(statusMap.containsKey(componentID)) {
            statusMap.replace(componentID, status);
        } else {
            statusMap.put(componentID, status);            
        }

    }
    
    public synchronized Map<String, ResultingStatus> getStatusMap() {
        return statusMap;
    }
}
