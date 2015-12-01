/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.service.audit;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.utils.CalendarUtils;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * MockAuditManager does stuff.
 */
public class MockAuditManager implements AuditTrailManager {

    List<AuditTrailEvent> events = new ArrayList<AuditTrailEvent>();
    
    private int callsForAuditEvent = 0;
    @Override
    public void addAuditEvent(String collectionID, String fileID, String actor, String info, String auditTrail,
                              FileAction operation, String operationID, String certificateID) {
        callsForAuditEvent++;
        AuditTrailEvent audit = new AuditTrailEvent();
        audit.setActionDateTime(CalendarUtils.getNow());
        audit.setActionOnFile(operation);
        audit.setActorOnFile(actor);
        audit.setAuditTrailInformation(auditTrail);
        audit.setFileID(fileID);
        audit.setInfo(info);
        audit.setReportingComponent("MOCK-AUDIT-MANAGER");
        audit.setSequenceNumber(BigInteger.valueOf(events.size() + 1));
        audit.setOperationID(operationID);
        audit.setCertificateID(certificateID);
        events.add(audit);
    }
    public int getCallsForAuditEvent() {
        return callsForAuditEvent;
    }
    public void resetCallsForAuditEvent() {
        callsForAuditEvent = 0;
    }

    private int callsForGetAudits = 0;
    @Override
    public AuditTrailDatabaseResults getAudits(String collectionID, String fileID, Long minSeqNumber, Long maxSeqNumber, Date minDate,
            Date maxDate, Long maxNumberOfResults) {
        callsForGetAudits++;
        AuditTrailDatabaseResults res = new AuditTrailDatabaseResults();
        for(AuditTrailEvent event : events) {
            if(fileID != null && !event.getFileID().equals(fileID)) {
                continue;
            }
            if(minSeqNumber != null && event.getSequenceNumber().longValue() < minSeqNumber) {
                continue;
            }
            if(maxSeqNumber != null && event.getSequenceNumber().longValue() > maxSeqNumber) {
                continue;
            }
            if(minDate != null && CalendarUtils.convertFromXMLGregorianCalendar(
                    event.getActionDateTime()).getTime() < minDate.getTime()) {
                continue;
            }
            if(maxDate != null && CalendarUtils.convertFromXMLGregorianCalendar(
                    event.getActionDateTime()).getTime() > maxDate.getTime()) {
                continue;
            }
            if(maxNumberOfResults != null && res.getAuditTrailEvents().getAuditTrailEvent().size() >= maxNumberOfResults) {
                res.reportMoreResultsFound();
                continue;
            }
            res.addAuditTrailEvent(event);
        }
        return res;
    }
    public int getCallsForGetAudits() {
        return callsForGetAudits;
    }
    public void resetCallsForGetAudits() {
        callsForGetAudits = 0;
    }
}
