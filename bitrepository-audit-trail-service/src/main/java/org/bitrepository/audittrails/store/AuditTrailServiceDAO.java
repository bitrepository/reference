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

import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_NAME;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_ACTOR_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_AUDIT;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_CONTRIBUTOR_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_FILE_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_FINGERPRINT;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_INFORMATION;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_OPERATION;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_OPERATION_DATE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_OPERATION_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_SEQUENCE_NUMBER;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.COLLECTION_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_COLLECTION_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_FILEID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.PRESERVATION_COLLECTION_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.PRESERVATION_CONTRIBUTOR_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.PRESERVATION_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.PRESERVATION_SEQ;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.PRESERVATION_TABLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.CalendarUtils;
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
    
    private String getAddCollectionIDSql() {
        String insertSql = "INSERT INTO " + COLLECTION_TABLE + " ( " + COLLECTION_ID + " ) "
                + " ( SELECT ? FROM " + COLLECTION_TABLE 
                + " WHERE " + COLLECTION_ID + " = ?"
                + " HAVING count(*) = 0 )";
        return insertSql;
    }
    
    @Override
    public void addCollectionID(String collectionID) {       
        DatabaseUtils.executeStatement(dbConnector, getAddCollectionIDSql(), collectionID, collectionID);
    }
    
    private String getAddFileIDSql() {
        String insertSql = "INSERT INTO " + FILE_TABLE + " ( " + FILE_FILEID + ", " + FILE_COLLECTION_KEY + " )"
                + " ( SELECT ?, ("
                    + " SELECT " + COLLECTION_KEY 
                    + " FROM " + COLLECTION_TABLE 
                    + " WHERE " + COLLECTION_ID + " = ?)"
                + " FROM " + FILE_TABLE
                + " JOIN " + COLLECTION_TABLE 
                + " ON " + FILE_TABLE + "." + FILE_COLLECTION_KEY + " = " + COLLECTION_TABLE + "." + COLLECTION_KEY
                + " WHERE " + COLLECTION_ID + " = ?"
                + " AND " + FILE_FILEID + " = ?"
                + " HAVING count(*) = 0 )";
        return insertSql;
    }
    
    @Override
    public void addFileID(String fileID, String collectionID) {
        DatabaseUtils.executeStatement(dbConnector, getAddFileIDSql(), fileID, collectionID, collectionID, fileID);
    }
    
    private String getAddContributorIDSql() {
        String insertSql = "INSERT INTO " + CONTRIBUTOR_TABLE + " ( " + CONTRIBUTOR_ID + " ) "
                + " ( SELECT ? FROM " + CONTRIBUTOR_TABLE 
                + " WHERE " + CONTRIBUTOR_ID + " = ?"
                + " HAVING count(*) = 0 )";
        return insertSql;
    }
    
    @Override
    public void addContributorID(String contributorID) {   
        DatabaseUtils.executeStatement(dbConnector, getAddContributorIDSql(), contributorID, contributorID);
    }
    
    private String getAddActorNameSql() {
        String insertSql = "INSERT INTO " + ACTOR_TABLE + " ( " + ACTOR_NAME + " ) "
                + " ( SELECT ? FROM " + ACTOR_TABLE 
                + " WHERE " + ACTOR_NAME + " = ?"
                + " HAVING count(*) = 0 )";
        return insertSql;
    }
    
    @Override
    public void addActorName(String actorName) {   
        DatabaseUtils.executeStatement(dbConnector, getAddActorNameSql(), actorName, actorName);
    }
    
    public void addAuditTrails(AuditTrailEvents auditTrailEvents, String collectionID) {
        ArgumentValidator.checkNotNull(auditTrailEvents, "AuditTrailEvents auditTrailEvents");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        
        addAuditTrail3(auditTrailEvents, collectionID);
        /*for(AuditTrailEvent event : auditTrailEvents.getAuditTrailEvent()) {
            addAuditTrail2(event, collectionID);
        }*/
    }
        
    private String getAddAuditTrailSql() {
        String insertSql = "INSERT INTO " + AUDITTRAIL_TABLE 
                + " ( " + AUDITTRAIL_SEQUENCE_NUMBER + ", " 
                + AUDITTRAIL_CONTRIBUTOR_KEY + ", " 
                + AUDITTRAIL_FILE_KEY + ", " 
                + AUDITTRAIL_ACTOR_KEY + ", " 
                + AUDITTRAIL_OPERATION + ", " 
                + AUDITTRAIL_OPERATION_DATE + ", " 
                + AUDITTRAIL_AUDIT + ", " 
                + AUDITTRAIL_INFORMATION + ", " 
                + AUDITTRAIL_OPERATION_ID + ", " 
                + AUDITTRAIL_FINGERPRINT + ")"
                + " VALUES ( ?," 
                    + " (SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE
                    + " WHERE " + CONTRIBUTOR_ID + " = ?),"
                    + " (SELECT " + FILE_KEY + " FROM " + FILE_TABLE
                    + " JOIN " + COLLECTION_TABLE 
                    + " ON " + FILE_TABLE + "." + FILE_COLLECTION_KEY + " = " + COLLECTION_TABLE + "." + COLLECTION_KEY  
                    + " WHERE " + COLLECTION_ID + " = ?" 
                    + " AND " + FILE_FILEID + " = ?),"
                    + " (SELECT " + ACTOR_KEY + " FROM " + ACTOR_TABLE
                    + " WHERE " + ACTOR_NAME + " = ?)," 
                    + " ?, ?, ?, ?, ?, ? )";
        
        return insertSql;
    }
    
    private void addAuditTrail(AuditTrailEvent event, String collectionID) {
        addCollectionID(collectionID);
        addActorName(event.getActorOnFile());
        addContributorID(event.getReportingComponent());
        addFileID(event.getFileID(), collectionID);
        String addAuditTrailSql = getAddAuditTrailSql();
        try {
            PreparedStatement ps = null;
            Connection conn = null;
            try {
                conn = dbConnector.getConnection();
                ps = conn.prepareStatement(addAuditTrailSql);
                ps.setLong(1, event.getSequenceNumber().longValue());
                ps.setString(2, event.getReportingComponent());
                ps.setString(3, collectionID);
                ps.setString(4, event.getFileID());
                ps.setString(5, event.getActorOnFile());
                ps.setString(6, event.getActionOnFile().toString());
                ps.setTimestamp(7, new Timestamp(
                        CalendarUtils.convertFromXMLGregorianCalendar(event.getActionDateTime()).getTime()));
                ps.setString(8, event.getAuditTrailInformation());
                ps.setString(9, event.getInfo());
                ps.setString(10, event.getOperationID());
                ps.setString(11, event.getCertificateID());
        
                ps.executeUpdate();
            } finally {
                if(ps != null) {
                    ps.close();
                }
                if(conn != null) {
                    conn.close();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not insert audit trail event for event '" + event + "' with the SQL '"
                    + addAuditTrailSql + "'.", e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Got null input data, not allowed", e);
        }
    }
    
    private void addAuditTrail2(AuditTrailEvent event, String collectionID) {
        String addAuditTrailSql = getAddAuditTrailSql();
        try {
            PreparedStatement addCollectionIDPs = null;
            PreparedStatement addActorNamePs = null;
            PreparedStatement addContributorIDPs = null;
            PreparedStatement addFileIDPs = null;
            PreparedStatement addAuditTrailPs = null;
            Connection conn = null;
            try {
                conn = dbConnector.getConnection();
                conn.setAutoCommit(false);
                addCollectionIDPs = conn.prepareStatement(getAddCollectionIDSql());
                addActorNamePs = conn.prepareStatement(getAddActorNameSql());
                addContributorIDPs = conn.prepareStatement(getAddContributorIDSql());
                addFileIDPs = conn.prepareStatement(getAddFileIDSql());
                addAuditTrailPs = conn.prepareStatement(addAuditTrailSql);
                
                addCollectionIDPs.setString(1, collectionID);
                addCollectionIDPs.setString(2, collectionID);
                
                addActorNamePs.setString(1, event.getActorOnFile());
                addActorNamePs.setString(2, event.getActorOnFile());
                
                addContributorIDPs.setString(1, event.getReportingComponent());
                addContributorIDPs.setString(2, event.getReportingComponent());
                
                addFileIDPs.setString(1, event.getFileID());
                addFileIDPs.setString(2, collectionID);
                addFileIDPs.setString(3, collectionID);
                addFileIDPs.setString(4, event.getFileID());
                
                addAuditTrailPs.setLong(1, event.getSequenceNumber().longValue());
                addAuditTrailPs.setString(2, event.getReportingComponent());
                addAuditTrailPs.setString(3, collectionID);
                addAuditTrailPs.setString(4, event.getFileID());
                addAuditTrailPs.setString(5, event.getActorOnFile());
                addAuditTrailPs.setString(6, event.getActionOnFile().toString());
                addAuditTrailPs.setTimestamp(7, new Timestamp(
                        CalendarUtils.convertFromXMLGregorianCalendar(event.getActionDateTime()).getTime()));
                addAuditTrailPs.setString(8, event.getAuditTrailInformation());
                addAuditTrailPs.setString(9, event.getInfo());
                addAuditTrailPs.setString(10, event.getOperationID());
                addAuditTrailPs.setString(11, event.getCertificateID());
        
                addCollectionIDPs.executeUpdate();
                addActorNamePs.executeUpdate();
                addContributorIDPs.executeUpdate();
                addFileIDPs.executeUpdate();
                addAuditTrailPs.executeUpdate();
            } finally {
                if(addCollectionIDPs != null) {
                    addCollectionIDPs.close();
                }
                if(addActorNamePs != null) {
                    addActorNamePs.close();
                }
                if(addContributorIDPs != null) {
                    addContributorIDPs.close();
                }
                if(addFileIDPs != null) {
                    addFileIDPs.close();
                }
                if(addAuditTrailPs != null) {
                    addAuditTrailPs.close();
                }
                if(conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not insert audit trail event for event '" + event + "' with the SQL '"
                    + addAuditTrailSql + "'.", e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Got null input data, not allowed", e);
        }
    }
    
    private void addAuditTrail3(AuditTrailEvents events, String collectionID) {
        try {
            PreparedStatement addCollectionIDPs = null;
            PreparedStatement addActorNamePs = null;
            PreparedStatement addContributorIDPs = null;
            PreparedStatement addFileIDPs = null;
            PreparedStatement addAuditTrailPs = null;
            Connection conn = null;
            try {
                conn = dbConnector.getConnection();
                conn.setAutoCommit(false);
                addCollectionIDPs = conn.prepareStatement(getAddCollectionIDSql());
                addActorNamePs = conn.prepareStatement(getAddActorNameSql());
                addContributorIDPs = conn.prepareStatement(getAddContributorIDSql());
                addFileIDPs = conn.prepareStatement(getAddFileIDSql());
                addAuditTrailPs = conn.prepareStatement(getAddAuditTrailSql());
                
                for(AuditTrailEvent event : events.getAuditTrailEvent()) {
                    addCollectionIDPs.setString(1, collectionID);
                    addCollectionIDPs.setString(2, collectionID);
                    
                    addActorNamePs.setString(1, event.getActorOnFile());
                    addActorNamePs.setString(2, event.getActorOnFile());
                    
                    addContributorIDPs.setString(1, event.getReportingComponent());
                    addContributorIDPs.setString(2, event.getReportingComponent());
                    
                    addFileIDPs.setString(1, event.getFileID());
                    addFileIDPs.setString(2, collectionID);
                    addFileIDPs.setString(3, collectionID);
                    addFileIDPs.setString(4, event.getFileID());
                    
                    addAuditTrailPs.setLong(1, event.getSequenceNumber().longValue());
                    addAuditTrailPs.setString(2, event.getReportingComponent());
                    addAuditTrailPs.setString(3, collectionID);
                    addAuditTrailPs.setString(4, event.getFileID());
                    addAuditTrailPs.setString(5, event.getActorOnFile());
                    addAuditTrailPs.setString(6, event.getActionOnFile().toString());
                    addAuditTrailPs.setTimestamp(7, new Timestamp(
                            CalendarUtils.convertFromXMLGregorianCalendar(event.getActionDateTime()).getTime()));
                    addAuditTrailPs.setString(8, event.getAuditTrailInformation());
                    addAuditTrailPs.setString(9, event.getInfo());
                    addAuditTrailPs.setString(10, event.getOperationID());
                    addAuditTrailPs.setString(11, event.getCertificateID());
                    
                    addCollectionIDPs.addBatch();
                    addActorNamePs.addBatch();
                    addContributorIDPs.addBatch();
                    addFileIDPs.addBatch();
                    addAuditTrailPs.addBatch();
                }
                
                    addCollectionIDPs.executeBatch();
                    addActorNamePs.executeBatch();
                    addContributorIDPs.executeBatch();
                    addFileIDPs.executeBatch();
                    addAuditTrailPs.executeBatch();
                    conn.commit();
            } finally {
                if(addCollectionIDPs != null) {
                    addCollectionIDPs.close();
                }
                if(addActorNamePs != null) {
                    addActorNamePs.close();
                }
                if(addContributorIDPs != null) {
                    addContributorIDPs.close();
                }
                if(addFileIDPs != null) {
                    addFileIDPs.close();
                }
                if(addAuditTrailPs != null) {
                    addAuditTrailPs.close();
                }
                if(conn != null) {
                    conn.setAutoCommit(true);
                    conn.close();
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to batch insert audit trail events.", e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Got null input data, not allowed", e);
        }
    }
    
    @Override
    public void addAuditTrailsOld(AuditTrailEvents newAuditTrails, String collectionId) {
        ArgumentValidator.checkNotNull(newAuditTrails, "AuditTrailEvents newAuditTrails");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        AuditDatabaseIngestor ingestor = new AuditDatabaseIngestor(dbConnector);
        for(AuditTrailEvent event : newAuditTrails.getAuditTrailEvent()) {
            ingestor.ingestAuditEvents(event, collectionId);
        }
    }
    
    @Override
    public int largestSequenceNumber(String contributorId, String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(contributorId, "String contributorId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        // Indirectly extracts contributor and collection keys, and joins with the file table where the query can be
        // limited by collection
        String sql = "SELECT " + AUDITTRAIL_SEQUENCE_NUMBER + " FROM " + AUDITTRAIL_TABLE 
                + " JOIN " + FILE_TABLE 
                + " ON " + AUDITTRAIL_TABLE + "." + AUDITTRAIL_FILE_KEY + " = " + FILE_TABLE + "." + FILE_KEY
                + " WHERE "  + AUDITTRAIL_TABLE + "." + AUDITTRAIL_CONTRIBUTOR_KEY + " = ("
                    + " SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE 
                    + " WHERE " + CONTRIBUTOR_ID + " = ? )"
                + " AND " + FILE_TABLE + "." + FILE_COLLECTION_KEY + " = (" 
                    + " SELECT " + COLLECTION_KEY + " FROM " + COLLECTION_TABLE 
                    + " WHERE " + COLLECTION_ID + " = ? )"
                + " ORDER BY " + AUDITTRAIL_TABLE + "." + AUDITTRAIL_SEQUENCE_NUMBER + " DESC";
        
        Long seq = DatabaseUtils.selectFirstLongValue(dbConnector, sql, contributorId, collectionId);
        if(seq != null) {
            return seq.intValue();
        }
        return 0;
    }    

    @Override
    public long getPreservationSequenceNumber(String contributorId, String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(contributorId, "String contributorId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        String sql = "SELECT " + PRESERVATION_SEQ + " FROM " + PRESERVATION_TABLE 
                + " WHERE " + PRESERVATION_CONTRIBUTOR_KEY + " = (" 
                    + " SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE 
                    + " WHERE " + CONTRIBUTOR_ID + " = ? )"
                + "AND " + PRESERVATION_COLLECTION_KEY + " = (" 
                    + " SELECT " + COLLECTION_KEY + " FROM " + COLLECTION_TABLE 
                    + " WHERE " + COLLECTION_ID + " = ? )";
        
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
        String sqlUpdate = "UPDATE " + PRESERVATION_TABLE + " SET " + PRESERVATION_SEQ + " = ?"
                + " WHERE " + PRESERVATION_KEY + " = ? ";
        DatabaseUtils.executeStatement(dbConnector, sqlUpdate, seqNumber, preservationKey);
    }
    
    @Override
    public boolean havePreservationKey(String contributorID, String collectionID) {
        String sql = "SELECT " + PRESERVATION_KEY + " FROM " + PRESERVATION_TABLE 
                + " WHERE " + PRESERVATION_CONTRIBUTOR_KEY + " = (" 
                    + " SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE 
                    + " WHERE " + CONTRIBUTOR_ID + " = ? ) "
                + "AND " + PRESERVATION_COLLECTION_KEY + " = ("
                    + " SELECT " + COLLECTION_KEY + " FROM " + COLLECTION_TABLE
                    + " WHERE " + COLLECTION_ID + " = ? )";
        
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
        String sqlRetrieve = "SELECT " + PRESERVATION_KEY + " FROM " + PRESERVATION_TABLE 
                + " WHERE " + PRESERVATION_CONTRIBUTOR_KEY + " = (" 
                    + " SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE 
                    + " WHERE " + CONTRIBUTOR_ID + " = ? ) "
                + "AND " + PRESERVATION_COLLECTION_KEY + " = ("
                    + " SELECT " + COLLECTION_KEY + " FROM " + COLLECTION_TABLE
                    + " WHERE " + COLLECTION_ID + " = ? )";
        Long guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, contributorId, collectionId);
        
        if(guid == null) {
            log.debug("Inserting preservation entry for contributor '" + contributorId + "' and collection '" 
                    + collectionId + "' into the preservation table.");
            String sqlInsert = "INSERT INTO " + PRESERVATION_TABLE + " ( " + PRESERVATION_CONTRIBUTOR_KEY + " , " 
                    + PRESERVATION_COLLECTION_KEY + ") VALUES ( "
                        + "(SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE + " WHERE " 
                        + CONTRIBUTOR_ID + " = ?)"
                    + ", "
                        + "( SELECT " + COLLECTION_KEY + " FROM " + COLLECTION_TABLE + " WHERE " 
                        + COLLECTION_ID + " = ? )"
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
