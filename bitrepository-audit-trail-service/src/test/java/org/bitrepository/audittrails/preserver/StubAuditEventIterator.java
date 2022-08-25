package org.bitrepository.audittrails.preserver;

import org.bitrepository.audittrails.store.AuditEventIterator;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.utils.CalendarUtils;

import java.math.BigInteger;

/**
 * AuditEventIterator that will only return an event on first iteration.
 */
public class StubAuditEventIterator extends AuditEventIterator {
    boolean called = false;
    public StubAuditEventIterator() {
        super(null);
    }

    @Override
    public AuditTrailEvent getNextAuditTrailEvent() {
        String pillarID = "pillarID";
        String actor = "actor";

        if(called) {
            return null;
        } else {
            called = true;
            AuditTrailEvent e1 = new AuditTrailEvent();
            e1.setActionDateTime(CalendarUtils.getNow());
            e1.setActionOnFile(FileAction.FAILURE);
            e1.setActorOnFile(actor);
            e1.setSequenceNumber(BigInteger.ONE);
            e1.setReportingComponent(pillarID);
            return e1;
        }
    }
}