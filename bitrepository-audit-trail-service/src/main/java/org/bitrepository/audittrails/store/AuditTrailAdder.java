/*
 * #%L
 * Bitrepository Audit Trail Service
 * %%
 * Copyright (C) 2010 - 2015 The State and University Library, The Royal Library and The State Archives, Denmark
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.database.DBConnector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuditTrailAdder {

    /**
     * SQL for conditionally adding an collectionID to the database.
     * Only adds the collectionID if it does not exist. 
     */
    private final String addCollectionIDSql = "INSERT INTO collection ( collectionid )"
            + " ( SELECT ? FROM collection" 
            + " WHERE collectionid = ?"
            + " HAVING count(*) = 0 )";
    
    /**
     * SQL for conditionally adding an actor to the database
     * Only adds an actor if it does not already exist.
     */
    private final String addActorSql = "INSERT INTO actor ( actor_name )"
            + " ( SELECT ? FROM actor" 
            + " WHERE actor_name = ?"
            + " HAVING count(*) = 0 )";
    
    /**
     * SQL for conditionally adding a contributor to the database
     * Only adds the contributor if it does not already exist. 
     */
    private final String addContributorSql = "INSERT INTO contributor ( contributor_id )"
            + " ( SELECT ? FROM contributor" 
            + " WHERE contributor_id = ?"
            + " HAVING count(*) = 0 )";
    
    /**
     * SQL for conditionally adding a fileID to the database. 
     * Only adds the file if it does not already exist. 
     */
    private final String addFileIDSql = "INSERT INTO file ( fileid, collection_key )"
            + " ( SELECT ?, ("
                + " SELECT collection_key" 
                + " FROM collection" 
                + " WHERE collectionid = ?)"
            + " FROM file"
            + " JOIN collection" 
            + " ON file.collection_key = collection.collection_key"
            + " WHERE collectionid = ?"
            + " AND fileid = ?"
            + " HAVING count(*) = 0 )";
    
    /**
     * Sql for adding audit trail an audit trail event 
     */
    private final String addAuditTrailSql = "INSERT INTO audittrail" 
            + " ( sequence_number, "
            + "contributor_key, " 
            + "file_key, " 
            + "actor_key, " 
            + "operation, " 
            + "operation_date, " 
            + "audit, " 
            + "information, " 
            + "operationID, " 
            + "fingerprint)"
            + " VALUES ( ?," 
                + " (SELECT contributor_key FROM contributor"
                + " WHERE contributor_id = ?),"
                + " (SELECT file_key FROM file"
                + " JOIN collection" 
                + " ON file.collection_key = collection.collection_key"  
                + " WHERE collectionid = ?" 
                + " AND fileid = ?),"
                + " (SELECT actor_key FROM actor"
                + " WHERE actor_name = ?)," 
                + " ?, ?, ?, ?, ?, ? )";
    
    private final String addLatestSequencesSql = "INSERT INTO collection_progress "
            + "(collectionID, contributorID, latest_sequence_number)"
            + " ( SELECT collectionID, ?, ? FROM collection"
                + " WHERE collectionID = ?"
                + " AND NOT EXISTS ( SELECT * FROM collection_progress"
                    + " WHERE collectionID = ?"
                    + " AND contributorID = ?))";

    private final String updateLatestSequenceSql = "UPDATE collection_progress"
            + " SET latest_sequence_number = ? "
            + " WHERE collectionID = ?"
            + " AND contributorID = ?";
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private final String collectionID;
    private final String contributorID;
    
    private final Connection conn;
    private PreparedStatement addCollectionIDPs;
    private PreparedStatement addActorNamePs;
    private PreparedStatement addContributorIDPs;
    private PreparedStatement addFileIDPs;
    private PreparedStatement addAuditTrailPs;
    private PreparedStatement addLatestSeqPs;
    private PreparedStatement updateLatestSeqPs;
    
    public AuditTrailAdder(DBConnector connector, String collectionID, String contributorID) {
        this.conn = connector.getConnection();
        this.collectionID = collectionID;
        this.contributorID = contributorID;
    }
    
    private void init() throws SQLException {
        conn.setAutoCommit(false);
        addCollectionIDPs = conn.prepareStatement(addCollectionIDSql);
        addActorNamePs = conn.prepareStatement(addActorSql);
        addContributorIDPs = conn.prepareStatement(addContributorSql);
        addFileIDPs = conn.prepareStatement(addFileIDSql);
        addAuditTrailPs = conn.prepareStatement(addAuditTrailSql);
        updateLatestSeqPs = conn.prepareStatement(updateLatestSequenceSql);
        addLatestSeqPs = conn.prepareStatement(addLatestSequencesSql);
    }
    
    /**
     * Method to handle the actual addition of audit trails
     * @param events the audit trail events
     */
    public void addAuditTrails(AuditTrailEvents events) {
        try {
            init();
            log.debug("Initialized AuditTrailAdder");
            Long latestSeq = 0L;
            addCollectionID(collectionID);
            addContributor(contributorID);
            for(AuditTrailEvent event : events.getAuditTrailEvent()) {
                addActor(event);
                addFileID(event);
                addAuditTrail(event);
                latestSeq = Math.max(event.getSequenceNumber().longValue(), latestSeq);
            }
            updateMaxSeq(latestSeq);
            log.debug("Done building audit trail batch insert");
            execute();
            log.debug("Done executing audit trail batch insert");
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to batch insert audit trail events.", e);
        } catch (NullPointerException e) {
            throw new IllegalStateException("Got null input data, not allowed", e);
        } finally {
            close();
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
    
    private void addContributor(String contributor) throws SQLException {
        addContributorIDPs.setString(1, contributor);
        addContributorIDPs.setString(2, contributor);
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
    
    private void updateMaxSeq(Long seq) throws SQLException {
        updateLatestSeqPs.setLong(1, seq);
        updateLatestSeqPs.setString(2, collectionID);
        updateLatestSeqPs.setString(3, contributorID);
        
        addLatestSeqPs.setString(1, contributorID);
        addLatestSeqPs.setLong(2, seq);
        addLatestSeqPs.setString(3, collectionID);
        addLatestSeqPs.setString(4, collectionID);
        addLatestSeqPs.setString(5, contributorID);
    }
    
    private void execute() throws SQLException {
        try {
            addCollectionIDPs.execute();
            addActorNamePs.executeBatch();
            addContributorIDPs.execute();
            addFileIDPs.executeBatch();
            addAuditTrailPs.executeBatch();
            updateLatestSeqPs.execute();
            addLatestSeqPs.execute();
            conn.commit();
        } catch (SQLException e) {
            conn.rollback();
            throw new SQLException("Rolled back transaction due to failure", e);
        }
    }
    
    private void close() {
        try {
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
            if(updateLatestSeqPs != null) {
                updateLatestSeqPs.close();
            }
            if(addLatestSeqPs != null) {
                addLatestSeqPs.close();
            }
            if(conn != null) {
                conn.setAutoCommit(true);
                conn.close();
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Failed clean up prepared statements and/or dbconnection.", e);
        }
    }
}
