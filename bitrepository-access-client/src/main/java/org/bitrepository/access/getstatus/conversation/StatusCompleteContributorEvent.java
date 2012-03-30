package org.bitrepository.access.getstatus.conversation;

import org.bitrepository.protocol.eventhandler.PillarOperationEvent;

public class StatusCompleteContributorEvent extends PillarOperationEvent {

    public StatusCompleteContributorEvent(String info, String contributor) {
        super(OperationEventType.COMPONENT_COMPLETE, info, contributor);
        
    }

}
