/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.audit;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.AuditTrailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Method for the ReferencePillar to keep track of audits.
 */
public class MemorybasedAuditTrailManager implements AuditTrailManager {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The map to keep the audit trails.*/
    private List<AuditTrailEvent> auditTrails = new ArrayList<AuditTrailEvent>();

    /** Constructor.*/
    public MemorybasedAuditTrailManager() {}

    /**
     * Inserts a audit at a given time. 
     * TODO this method is currently only for tests.
     * @param msg The audit to insert.
     */
    public synchronized void insertAudit(AuditTrailEvent event) {
        ArgumentValidator.checkNotNull(event, "AuditTrailEvent event");
        log.debug("Adding event: {}", event);
        
        auditTrails.add(event);
    }

    @Override
    public synchronized Collection<AuditTrailEvent> getAudits(String fileId, Long sequence) {
        if(fileId == null && sequence == null) {
            return auditTrails.subList(0, auditTrails.size()-1);
        }
        
        List<AuditTrailEvent> res = new ArrayList<AuditTrailEvent>();
        for(AuditTrailEvent event : auditTrails) {
            if(fileId != null && !event.getFileID().equals(fileId)) {
                continue;
            }
            
            if(sequence != null && event.getSequenceNumber().longValue() < sequence) {
                continue;
            }
            
            res.add(event);
        }
        
        return res;
    }

    @Override
    public synchronized void addAuditEvent(String fileId, String actor, String info, String auditTrail, FileAction operation) {
        AuditTrailEvent event = new AuditTrailEvent();
        event.setActionDateTime(CalendarUtils.getNow());
        event.setActionOnFile(operation);
        event.setActorOnFile(actor);
        event.setFileID(fileId);
        event.setInfo(info);
        event.setReportingComponent("Mock ReferencePillar");
        event.setSequenceNumber(BigInteger.valueOf((long) auditTrails.size()));
        event.setAuditTrailInformation(auditTrail);
        
        log.debug("Adding event: {}", event);
        auditTrails.add(event);
    }
}
