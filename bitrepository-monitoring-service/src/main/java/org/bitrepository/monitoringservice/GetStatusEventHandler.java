package org.bitrepository.monitoringservice;

import org.bitrepository.access.getstatus.conversation.StatusCompleteContributorEvent;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GetStatusEventHandler implements EventHandler {

    private final ComponentStatusStore statusStore;
    private final Logger log = LoggerFactory.getLogger(GetStatusEventHandler.class);

    public GetStatusEventHandler(ComponentStatusStore statusStore) {
        this.statusStore = statusStore;
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        log.debug("Got event: " + event);
        
        switch(event.getType()) {
        case IDENTIFY_REQUEST_SENT:
            break;
        case COMPONENT_IDENTIFIED:
            break;
        case IDENTIFICATION_COMPLETE:
            break;
        case REQUEST_SENT:
            break;
        case PROGRESS:
            break;
        case COMPONENT_COMPLETE:
            StatusCompleteContributorEvent statusEvent = (StatusCompleteContributorEvent) event; 
            statusStore.updateStatus(statusEvent.getID(), statusEvent.getStatus());
            break;
        case COMPLETE:
            break;
        case COMPONENT_FAILED:
            break;
        case FAILED:
            break;
        case NO_COMPONENT_FOUND:
            break;
        case IDENTIFY_TIMEOUT: 
            break;
        case WARNING:
            break;
        }  
        
    }

}
