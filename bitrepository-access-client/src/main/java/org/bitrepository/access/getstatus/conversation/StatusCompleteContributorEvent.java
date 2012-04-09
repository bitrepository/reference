package org.bitrepository.access.getstatus.conversation;

import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.protocol.eventhandler.PillarOperationEvent;

public class StatusCompleteContributorEvent extends PillarOperationEvent {

    private final ResultingStatus status;
    
    public StatusCompleteContributorEvent(String info, String contributor, ResultingStatus status) {
        super(OperationEventType.COMPONENT_COMPLETE, info, contributor);
        this.status = status;
    }
    
    public ResultingStatus getStatus() {
        return status;
    }
    

}
