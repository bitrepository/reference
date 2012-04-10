package org.bitrepository.access.getaudittrails.client;

import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.protocol.eventhandler.PillarOperationEvent;

public class AuditTrailResult extends PillarOperationEvent {
    private final AuditTrailEvents auditTrailEvents;

    public AuditTrailResult(String info, String pillarID, AuditTrailEvents auditTrailEvents, String conversationID) {
        super(OperationEventType.COMPONENT_COMPLETE, info, pillarID, conversationID);
        this.auditTrailEvents = auditTrailEvents;
    }

    public AuditTrailEvents getAuditTrailEvents() {
        return auditTrailEvents;
    }

    @Override
    public String additionalInfo() {
        return super.additionalInfo() + ", " +"auditTrailEvents=" + auditTrailEvents;
    }
}
