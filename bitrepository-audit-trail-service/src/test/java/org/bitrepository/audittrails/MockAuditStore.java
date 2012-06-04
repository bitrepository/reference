/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;

import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;

public class MockAuditStore implements AuditTrailStore {
    
    private int callsToGetAuditTrails = 0;
    @Override
    public Collection<AuditTrailEvent> getAuditTrails(String fileId, String contributorId, Long minSeqNumber,
            Long maxSeqNumber, String actorName, FileAction operation, Date startDate, Date endDate) {
        callsToGetAuditTrails++;
        return new ArrayList<AuditTrailEvent>();
    }
    public int getCallsToGetAuditTrails() {
        return callsToGetAuditTrails;
    }
    
    private int callsToAddAuditTrails = 0;
    @Override
    public void addAuditTrails(AuditTrailEvents newAuditTrails) {
        callsToAddAuditTrails++;
    }
    public int getCallsToAddAuditTrails() {
        return callsToAddAuditTrails;
    }
    
    private int callsToLargestSequenceNumber = 0;
    private int largestSequenceNumber = 0;
    @Override
    public int largestSequenceNumber(String contributorId) {
        callsToLargestSequenceNumber++;
        return largestSequenceNumber;
    }
    public void setLargestSequenceNumber(int seq) {
        largestSequenceNumber = seq;
    }
    public int getCallsToLargestSequenceNumber() {
        return callsToLargestSequenceNumber;
    }
    
    @Override
    public void close() {
        callsToGetAuditTrails = 0;
        callsToAddAuditTrails = 0;
        callsToLargestSequenceNumber = 0;
        largestSequenceNumber = 0;
    }
}
