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
package org.bitrepository.audittrails.store;

import java.sql.SQLException;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseManager;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Audit trail storages backed by a database for preserving
 */
public class AuditTrailServiceDAO implements AuditTrailStore {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The connection to the database.*/
    private DBConnector dbConnector;
    
    /** 
     * Constructor.
     * @param databaseManager The database manager
     */
    public AuditTrailServiceDAO(DatabaseManager databaseManager) {
        dbConnector = databaseManager.getConnector();
    }
    
    @Override
    public AuditEventIterator getAuditTrailsByIterator(String fileId, String collectionID, String contributorId, 
            Long minSeqNumber, Long maxSeqNumber, String actorName, FileAction operation, Date startDate, 
            Date endDate, String fingerprint, String operationID) {
        ExtractModel model = new ExtractModel();
        model.setFileId(fileId);
        model.setCollectionId(collectionID);
        model.setContributorId(contributorId);
        model.setMinSeqNumber(minSeqNumber);
        model.setMaxSeqNumber(maxSeqNumber);
        model.setActorName(actorName);
        model.setOperation(operation);
        model.setStartDate(startDate);
        model.setEndDate(endDate);
        model.setFingerprint(fingerprint);
        model.setOperationID(operationID);

        AuditDatabaseExtractor extractor = new AuditDatabaseExtractor(model, dbConnector);
        return extractor.extractAuditEventsByIterator();
    }
    
    @Override
    public void addAuditTrails(AuditTrailEvents auditTrailEvents, String collectionID, String contributorID) {
        ArgumentValidator.checkNotNull(auditTrailEvents, "AuditTrailEvents auditTrailEvents");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(contributorID, "String contributorID");
        
        AuditTrailAdder adder = new AuditTrailAdder(dbConnector, collectionID, contributorID);
        adder.addAuditTrails(auditTrailEvents);
    }
    
    @Override
    public long largestSequenceNumber(String contributorID, String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(contributorID, "String contributorId");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionId");
        
        String sql = "SELECT latest_sequence_number FROM collection_progress"
                + " WHERE collectionID = ?"
                + " AND contributorID = ?";
        
        Long seq = DatabaseUtils.selectFirstLongValue(dbConnector, sql, collectionID, contributorID);
        return (seq != null ? seq.longValue() : 0L);
    }

    @Override
    public long getPreservationSequenceNumber(String contributorId, String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(contributorId, "String contributorId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        String sql = "SELECT preserved_seq_number FROM preservation" 
                + " WHERE contributor_key = (" 
                    + " SELECT contributor_key FROM contributor" 
                    + " WHERE contributor_id = ? )"
                + "AND collection_key = (" 
                    + " SELECT collection_key FROM collection" 
                    + " WHERE collectionid = ? )";
        
        Long seq = DatabaseUtils.selectLongValue(dbConnector, sql, contributorId, collectionId);
        if(seq != null) {
            return seq.intValue();
        }
        return 0;
    }

    @Override
    public void setPreservationSequenceNumber(String contributorId, String collectionId, long seqNumber) {
        ArgumentValidator.checkNotNullOrEmpty(contributorId, "String contributorId");
        ArgumentValidator.checkNotNegative(seqNumber, "int seqNumber");
        long preservationKey = retrievePreservationKey(contributorId, collectionId);
        log.debug("Updating preservation sequence number for contributor: " + contributorId 
                + " in collection: " + collectionId + " to seq: " + seqNumber);
        
        String sqlUpdate = "UPDATE preservation SET preserved_seq_number = ?"
                + " WHERE preservation_key = ?";
        DatabaseUtils.executeStatement(dbConnector, sqlUpdate, seqNumber, preservationKey);
    }
    
    @Override
    public boolean havePreservationKey(String contributorID, String collectionID) {
        String sql = "SELECT preservation_key FROM preservation" 
                + " WHERE contributor_key = (" 
                    + " SELECT contributor_key FROM contributor" 
                    + " WHERE contributor_id = ? ) "
                + "AND collection_key = ("
                    + " SELECT collection_key FROM collection"
                    + " WHERE collectionid = ? )";
        
        Long preservationKey = DatabaseUtils.selectLongValue(dbConnector, sql, contributorID, collectionID);
        if(preservationKey == null) {
            return false;    
        } else {
            return true;
        }   
    }
    
    /**
     * Retrieves the key of the preservation table entry for the given collection and contributor.
     * 
     * @param contributorId The contributor of the preservation table entry.
     * @param collectionId The collection of the preservation table entry.
     * @return The key of the entry in the preservation table.
     */
    private Long retrievePreservationKey(String contributorId, String collectionId) {
        String sqlRetrieve = "SELECT preservation_key FROM preservation" 
                + " WHERE contributor_key = (" 
                    + " SELECT contributor_key FROM contributor" 
                    + " WHERE contributor_id = ? )"
                + " AND collection_key = ("
                    + " SELECT collection_key FROM collection"
                    + " WHERE collectionid = ? )";
        Long guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, contributorId, collectionId);
        
        if(guid == null) {
            log.debug("Inserting preservation entry for contributor '" + contributorId + "' and collection '" 
                    + collectionId + "' into the preservation table.");
            String sqlInsert = "INSERT INTO preservation ( contributor_key, collection_key)"
                    + " VALUES ( "
                        + "(SELECT contributor_key FROM contributor"
                        + " WHERE contributor_id = ?)"
                    + ", "
                        + "( SELECT collection_key FROM collection"
                        + " WHERE collectionid" + " = ? )"
                    + ")";
            DatabaseUtils.executeStatement(dbConnector, sqlInsert, contributorId, collectionId);
            
            guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, contributorId, collectionId);
        }
        
        if(guid == null) {
            throw new IllegalStateException("PreservationKey cannot be obtained for contributor: " + contributorId +
                    " in collection: " + collectionId);
        }
        
        return guid;
    }

    @Override
    public void close() {
        try {
            dbConnector.getConnection().close();
        } catch (SQLException e) {
            log.warn("Cannot close the database properly.", e);
        }
    }

}
