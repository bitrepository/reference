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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.common.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditTrailAdder {

    /**
     * SQL for conditionally adding an collectionID to the database.
     * Only adds the collectionID if it does not exist. 
     */
    private final String addCollectionIDSql = "INSERT INTO " + COLLECTION_TABLE + " ( " + COLLECTION_ID + " ) "
            + " ( SELECT ? FROM " + COLLECTION_TABLE 
            + " WHERE " + COLLECTION_ID + " = ?"
            + " HAVING count(*) = 0 )";
    
    /**
     * SQL for conditionally adding an actor to the database
     * Only adds an actor if it does not already exist.
     */
    private final String addActorSql = "INSERT INTO " + ACTOR_TABLE + " ( " + ACTOR_NAME + " ) "
            + " ( SELECT ? FROM " + ACTOR_TABLE 
            + " WHERE " + ACTOR_NAME + " = ?"
            + " HAVING count(*) = 0 )";
    
    /**
     * SQL for conditionally adding a contributor to the database
     * Only adds the contributor if it does not already exist. 
     */
    private final String addContributorSql = "INSERT INTO " + CONTRIBUTOR_TABLE + " ( " + CONTRIBUTOR_ID + " ) "
            + " ( SELECT ? FROM " + CONTRIBUTOR_TABLE 
            + " WHERE " + CONTRIBUTOR_ID + " = ?"
            + " HAVING count(*) = 0 )";
    
    /**
     * SQL for conditionally adding a fileID to the database. 
     * Only adds the file if it does not already exist. 
     */
    private final String addFileIDSql = "INSERT INTO " + FILE_TABLE + " ( " + FILE_FILEID + ", " + FILE_COLLECTION_KEY + " )"
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
    
    /**
     * Sql for adding audit trail an audit trail event 
     */
    private final String addAuditTrailSql = "INSERT INTO " + AUDITTRAIL_TABLE 
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
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private final String collectionID;
    
    private final Connection conn;
    private PreparedStatement addCollectionIDPs;
    private PreparedStatement addActorNamePs;
    private PreparedStatement addContributorIDPs;
    private PreparedStatement addFileIDPs;
    private PreparedStatement addAuditTrailPs;
    
    public AuditTrailAdder(Connection dbConnection, String collectionID) {
        this.conn = dbConnection;
        this.collectionID = collectionID;
    }
    
    private void init() throws SQLException {
        conn.setAutoCommit(false);
        addCollectionIDPs = conn.prepareStatement(addCollectionIDSql);
        addActorNamePs = conn.prepareStatement(addActorSql);
        addContributorIDPs = conn.prepareStatement(addContributorSql);
        addFileIDPs = conn.prepareStatement(addFileIDSql);
        addAuditTrailPs = conn.prepareStatement(addAuditTrailSql);
    }
    
    /**
     * Method to handle the actual addition of audit trails 
     */
    public void addAuditTrails(AuditTrailEvents events) {
        try {
            init();
            log.debug("Initialized AuditTrailAdder");
            try {
                addCollectionID(collectionID);
                for(AuditTrailEvent event : events.getAuditTrailEvent()) {
                    addActor(event);
                    addContributor(event);
                    addFileID(event);
                    addAuditTrail(event);
                }
                log.debug("Done building audit trail batch insert");
                execute();
                log.debug("Done executing audit trail batch insert");
            } finally {
                close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to batch insert audit trail events.", e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Got null input data, not allowed", e);
        }
    }
    
    private void addCollectionID(String collectionID) throws SQLException {
        addCollectionIDPs.setString(1, collectionID);
        addCollectionIDPs.setString(2, collectionID);
    }
    
    private void addActor(AuditTrailEvent event) throws SQLException {
        addActorNamePs.setString(1, event.getActorOnFile());
        addActorNamePs.setString(2, event.getActorOnFile());
        addActorNamePs.addBatch();
    }
    
    private void addContributor(AuditTrailEvent event) throws SQLException {
        addContributorIDPs.setString(1, event.getReportingComponent());
        addContributorIDPs.setString(2, event.getReportingComponent());
        addContributorIDPs.addBatch();
    }
    
    private void addFileID(AuditTrailEvent event) throws SQLException {
        addFileIDPs.setString(1, event.getFileID());
        addFileIDPs.setString(2, collectionID);
        addFileIDPs.setString(3, collectionID);
        addFileIDPs.setString(4, event.getFileID());
        addFileIDPs.addBatch();       
    }
    
    private void addAuditTrail(AuditTrailEvent event) throws SQLException {
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
        addAuditTrailPs.addBatch();
    }
    
    private void execute() throws SQLException {
        addCollectionIDPs.execute();
        addActorNamePs.executeBatch();
        addContributorIDPs.executeBatch();
        addFileIDPs.executeBatch();
        addAuditTrailPs.executeBatch();
        conn.commit();
    }
    
    private void close() throws SQLException {
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
}
