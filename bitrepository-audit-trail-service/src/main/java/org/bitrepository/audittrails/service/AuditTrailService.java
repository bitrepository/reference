/*
 * #%L
 * Bitrepository Audit Trail Service
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.audittrails.service;

import java.util.ArrayList;
import java.util.List;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.utils.CalendarUtils;

/**
 * Class to expose the functionality of the AuditTrailService. 
 * Aggregates the needed classes.   
 */
public class AuditTrailService {
    
    List<AuditTrailEvent> dummyEvents;
    
    public AuditTrailService() {
        dummyEvents = new ArrayList<AuditTrailEvent>();
        AuditTrailEvent event = new AuditTrailEvent();
        event.setFileID("foo");
        event.setReportingComponent("pillar1");
        event.setActorOnFile("Benny");
        event.setActionOnFile(FileAction.PUT_FILE);
        event.setActionDateTime(CalendarUtils.getEpoch());
        event.setInfo("Initial file upload");
        event.setAuditTrailInformation("Delivery of foo");
        dummyEvents.add(event);
        event = new AuditTrailEvent();
        event.setFileID("bar");
        event.setReportingComponent("pillar1");
        event.setActorOnFile("Hans");
        event.setActionOnFile(FileAction.PUT_FILE);
        event.setActionDateTime(CalendarUtils.getEpoch());
        event.setInfo("Initial file upload");
        event.setAuditTrailInformation("Delivery of bar");
        dummyEvents.add(event);
        event = new AuditTrailEvent();
        event.setFileID("baz");
        event.setReportingComponent("pillar1");
        event.setActorOnFile("Hans");
        event.setActionOnFile(FileAction.PUT_FILE);
        event.setActionDateTime(CalendarUtils.getEpoch());
        event.setInfo("Initial file upload");
        event.setAuditTrailInformation("Delivery of baz");
        dummyEvents.add(event);
        event = new AuditTrailEvent();
        event.setFileID("foo");
        event.setReportingComponent("pillar1");
        event.setActorOnFile("pillar1");
        event.setActionOnFile(FileAction.CHECKSUM_CALCULATED);
        event.setActionDateTime(CalendarUtils.getEpoch());
        event.setInfo("Scheduled checksum calculation");
        event.setAuditTrailInformation("Delivery of foo");
        dummyEvents.add(event);
    }
    
    /**
     * Retrieve all AuditTrailEvents in store. 
     */
    public List<AuditTrailEvent> getAllAuditTrailEvents() {
        return dummyEvents;
    }
    
    /**
     * Retrieve all AuditTrailEvents matching the criteria from the parameters.
     * All parameters are allowed to be null, meaning that the parameter imposes no restriction on the result
     * @param fromDate Restrict the results to only provide events after this point in time
     * @param toDate Restrict the results to only provide events up till this point in time
     * @param fileID Restrict the results to only be about this fileID
     * @param reportingComponent Restrict the results to only be reported by this component
     * @param Actor Restrict the results to only be events caused by this actor
     * @param Action Restrict the results to only be about this type of action
     */
    public List<AuditTrailEvent> queryAuditTrailEvents(String fromDate, String toDate, String fileID, String reportingComponent,
            String actor, String action) {
        return dummyEvents;
    }
}
