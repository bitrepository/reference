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
package org.bitrepository.audittrails.store;

import java.util.Collection;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;

/**
 * Interface for the storage of audit trail information for the AuditTrailService.
 */
public interface AuditTrailStore {
    /** 
     * Extract audit trails from the store.
     * @param fileId [OPTIONAL] The id of the file for restricting the extraction.
     * @param contributorId [OPTIONAL] The id of the contributor for restricting the extraction.
     * @param minSeqNumber [OPTIONAL] The minimum sequence number for restricting the extraction.
     * @param maxSeqNumber [OPTIONAL] The maximum sequence number for restricting the extraction.
     * @param actorName [OPTIONAL] The name of the actor for restricting the extraction.
     * @param operation [OPTIONAL] The FileAction operation for restricting the extraction.
     * @param startDate [OPTIONAL] The earliest date for the audits for restricting the extraction.
     * @param endDate [OPTIONAL] The latest date for the audits for restricting the extraction.
     * @return The requested audit trails from the store.
     */
    public Collection<AuditTrailEvent> getAuditTrails(String fileId, String contributorId, Long minSeqNumber, Long maxSeqNumber, 
            String actorName, FileAction operation, Date startDate, Date endDate, Integer maxResults);
    
    /**
     * ingest audit trails into the store. 
     * @param newAuditTrails The audit trails to be ingested into the store.
     */
    public void addAuditTrails(AuditTrailEvents newAuditTrails);
    
    /**
     * Retrieves the largest sequence number for a given contributor.
     * 
     * @param contributorId The id of the contributor to retrieve the largest sequence number from.
     * @return The largest sequence number.
     */
    public int largestSequenceNumber(String contributorId);
    
    /**
     * Retrieves the preservation sequence number for the given contributor, which tells how far the preservation
     * of the audit trails has gotten.
     *  
     * @param contributorId The id of the contributor.
     * @return The preservation sequence number for the given contributor.
     */
    public long getPreservationSequenceNumber(String contributorId);
    
    /**
     * Set the preservation sequence number for the given contributor.
     * 
     * @param contributorId The id of the contributor.
     * @param seqNumber The new preservation sequence number for the given contributor.
     */
    public void setPreservationSequenceNumber(String contributorId, long seqNumber);

    /**
     * Closes the store.
     */
    public void close();
}
