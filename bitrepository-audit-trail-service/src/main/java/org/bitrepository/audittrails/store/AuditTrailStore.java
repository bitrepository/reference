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

import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;

/**
 * Interface for the storage of audit trail information for the AuditTrailService.
 */
public interface AuditTrailStore {
    /** 
     * Obtain AuditEventIterator for extracting audit trails from the store.
     * When done with the iterator, the user should ensure that it is closed. 
     * @param fileID [OPTIONAL] The id of the file for restricting the extraction.
     * @param collectionID [OPTIONAL] The id of the collection from which to retrieve audit trails. 
     * @param contributorId [OPTIONAL] The id of the contributor for restricting the extraction.
     * @param minSeqNumber [OPTIONAL] The minimum sequence number for restricting the extraction.
     * @param maxSeqNumber [OPTIONAL] The maximum sequence number for restricting the extraction.
     * @param actorName [OPTIONAL] The name of the actor for restricting the extraction.
     * @param operation [OPTIONAL] The FileAction operation for restricting the extraction.
     * @param startDate [OPTIONAL] The earliest date for the audits for restricting the extraction.
     * @param endDate [OPTIONAL] The latest date for the audits for restricting the extraction.
     * @param fingerprint [OPTIONAL] The fingerprint of the certificate for the audits
     * @param operationID [OPTIONAL] The ID of the operation (conversationID) for the audits
     * @return The requested audit trails from the store.
     */
    AuditEventIterator getAuditTrailsByIterator(String fileID, String collectionID, String contributorId,
                                                Long minSeqNumber, Long maxSeqNumber, String actorName, FileAction operation, Date startDate,
                                                Date endDate, String fingerprint, String operationID);
    
    /**
     * ingest audit trails into the store. 
     * @param auditTrailsEvents The audit trails to be ingested into the store.
     * @param collectionID The id of the collection, where the audit trail events belong.
     * @param contributorID The id of the contributor, that the audit trail event belongs to.
     */
    void addAuditTrails(AuditTrailEvents auditTrailsEvents, String collectionID, String contributorID);
    
    /**
     * Retrieves the largest sequence number for a given contributor.
     * 
     * @param contributorId The id of the contributor to retrieve the largest sequence number from.
     * @param collectionID The id of the collection for the sequence number of the contributor.
     * @return The largest sequence number.
     */
    long largestSequenceNumber(String contributorId, String collectionID);
    
    /**
     * Retrieves the preservation sequence number for the given contributor, which tells how far the preservation
     * of the audit trails has gotten.
     *  
     * @param contributorId The id of the contributor.
     * @param collectionID The id of the collection for the sequence number of the contributor.
     * @return The preservation sequence number for the given contributor.
     */
    long getPreservationSequenceNumber(String contributorId, String collectionID);
    
    /**
     * Set the preservation sequence number for the given contributor.
     * 
     * @param contributorId The id of the contributor.
     * @param collectionID The id of the collection for the sequence number of the contributor.
     * @param seqNumber The new preservation sequence number for the given contributor.
     */
    void setPreservationSequenceNumber(String contributorId, String collectionID, long seqNumber);

    /**
     * Check to see if the database knows a contributor
     * 
     *  @param contributorID The ID of the contributor
     *  @param collectionID The ID of the collection;
     *  @return boolean true, if the contributor is known by the database, false otherwise.
     */
    boolean havePreservationKey(String contributorID, String collectionID);
    
    /**
     * Get the list of known audittrail contributors. I.e. those contributors which have delivered
     * audit trails for the database. 
     * 
     * @return List containing the IDs of the contributors that the database have audittrails from.
     */
    List<String> getKnownContributors();
    /**
     * Closes the store.
     */
    void close();
}
