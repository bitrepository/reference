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

import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;

/**
 * Interface for the storage of audit trail information for the AuditTrailService.
 */
public interface AuditTrailStore {
    /** 
     * Obtain AuditEventIterator for extracting audit trails from the store.
     * When done with the iterator, the user should ensure that it is closed. 
     * @param fileId [OPTIONAL] The id of the file for restricting the extraction.
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
    public AuditEventIterator getAuditTrailsByIterator(String fileId, String collectionID, String contributorId, 
            Long minSeqNumber, Long maxSeqNumber, String actorName, FileAction operation, Date startDate, 
            Date endDate, String fingerprint, String operationID);
    
    
    /**
     * Method to ensure that a given collection id exists in the database.
     * Inserts the collectionID if it is not present.
     * @param collectionID the ID of the collection
     */
    public void addCollectionID(String collectionID);
    
    /**
     * Method to ensure that a given fileID in a collection exists in the database
     * Inserts the fileID if it is not present
     * @param fileID the ID of the file
     * @param collectionID the ID of the collection
     */
    public void addFileID(String fileID, String collectionID);
    
    /**
     * Method to ensure that a given contributorID exists in the database
     * Inserts the contributorID if it is not present
     * @param contributorID the ID of the contributor
     */
    public void addContributorID(String contributorID);

    /**
     * Method to ensure that a given actor exists in the database
     * Inserts the actor if it is not present
     * @param actorName the name of the actor
     */
    public void addActorName(String actorName);
    
    /**
     * ingest audit trails into the store. 
     * @param newAuditTrails The audit trails to be ingested into the store.
     * @param collectionID The id of the collection, where the audit trail events belong.
     */
    public void addAuditTrailsOld(AuditTrailEvents newAuditTrails, String collectionID);
    public void addAuditTrails(AuditTrailEvents auditTrailsEvents, String collectionID);
    
    /**
     * Retrieves the largest sequence number for a given contributor.
     * 
     * @param contributorId The id of the contributor to retrieve the largest sequence number from.
     * @param collectionId The id of the collection for the sequence number of the contributor.
     * @return The largest sequence number.
     */
    public int largestSequenceNumber(String contributorId, String collectionId);
    
    /**
     * Retrieves the preservation sequence number for the given contributor, which tells how far the preservation
     * of the audit trails has gotten.
     *  
     * @param contributorId The id of the contributor.
     * @param collectionId The id of the collection for the sequence number of the contributor.
     * @return The preservation sequence number for the given contributor.
     */
    public long getPreservationSequenceNumber(String contributorId, String collectionId);
    
    /**
     * Set the preservation sequence number for the given contributor.
     * 
     * @param contributorId The id of the contributor.
     * @param collectionId The id of the collection for the sequence number of the contributor.
     * @param seqNumber The new preservation sequence number for the given contributor.
     */
    public void setPreservationSequenceNumber(String contributorId, String collectionId, long seqNumber);

    /**
     * Check to see if the database knows a contributor
     * 
     *  @param contributorID The ID of the contributor
     *  @param collectionID The ID of the collection;
     *  @return boolean true, if the contributor is known by the database, false otherwise.
     */
    boolean havePreservationKey(String contributorID, String collectionID);
    
    /**
     * Closes the store.
     */
    public void close();
}
