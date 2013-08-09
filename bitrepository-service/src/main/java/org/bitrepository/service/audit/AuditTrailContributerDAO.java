/*
 * #%L
 * Bitrepository Reference Pillar
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
package org.bitrepository.service.audit;

import static org.bitrepository.service.audit.AuditDatabaseConstants.ACTOR_GUID;
import static org.bitrepository.service.audit.AuditDatabaseConstants.ACTOR_NAME;
import static org.bitrepository.service.audit.AuditDatabaseConstants.ACTOR_TABLE;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_ACTOR_GUID;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_AUDIT;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_FILE_GUID;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_INFORMATION;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_OPERATION;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_OPERATION_DATE;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_SEQUENCE_NUMBER;
import static org.bitrepository.service.audit.AuditDatabaseConstants.AUDITTRAIL_TABLE;
import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_FILEID;
import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_GUID;
import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_TABLE;
import static org.bitrepository.service.audit.AuditDatabaseConstants.FILE_COLLECTIONID;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Access interface for communication with the audit trail database.
 *
 * In the case of 'All-FileIDs', then the 'fileId' is given the string-value 'null'. 
 */
public class AuditTrailContributerDAO implements AuditTrailManager {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The connection to the database.*/
    private DBConnector dbConnector;
    /** The componentID.*/
    private final String componentID;

    /**
     * Constructor.
     * @param settings The settings.
     */
    public AuditTrailContributerDAO(Settings settings, DBConnector dbConnector) {
        ArgumentValidator.checkNotNull(settings, "settings");

        this.componentID = settings.getComponentID();
        this.dbConnector = dbConnector;
        
        getConnection();
        
        AuditTrailContributorDatabaseMigrator migrator = new AuditTrailContributorDatabaseMigrator(dbConnector);
        migrator.migrate();
    }

    /**
     * Retrieve the connection to the database.
     * TODO improve performance (only reconnect every-so-often)... 
     * @return The connection to the database.
     */
    protected Connection getConnection() {
        try {
            Connection dbConnection = dbConnector.getConnection();
            return dbConnection;
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate the database", e);
        }
    }

    @Override
    public void addAuditEvent(String collectionId, String fileId, String actor, String info, String auditTrail, FileAction operation) {
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        ArgumentValidator.checkNotNull(operation, "FileAction operation");
        log.trace("Inserting an audit event for file '" + fileId + "', from actor '" + actor
                + "' performing operation '" + operation + "', with the audit trail information '" + auditTrail + "'");

        long fileGuid;
        if(fileId == null || fileId.isEmpty()) {
            fileGuid = retrieveFileGuid(collectionId, "null");
        } else {
            fileGuid = retrieveFileGuid(collectionId, fileId);
        }
        long actorGuid;
        if(actor == null || actor.isEmpty()) {
            actorGuid = retrieveActorGuid("null");
        } else {
            actorGuid = retrieveActorGuid(actor);
        }

        if(auditTrail == null) {
            auditTrail = "";
        }
        if(info == null) {
            info = "";
        }

        synchronized(this) {
            String insertSql = "INSERT INTO " + AUDITTRAIL_TABLE + " ( " + AUDITTRAIL_FILE_GUID + " , "
                    + AUDITTRAIL_ACTOR_GUID + " , " + AUDITTRAIL_OPERATION + " , " + AUDITTRAIL_OPERATION_DATE + " , "
                    + AUDITTRAIL_AUDIT + " , " + AUDITTRAIL_INFORMATION + " ) VALUES ( ? , ? , ? , ? , ? , ? )";
            DatabaseUtils.executeStatement(dbConnector, insertSql, fileGuid, actorGuid, operation.toString(),
                    new Date(), auditTrail, info);
        }
    }

    @Override
    public AuditTrailDatabaseResults getAudits(String collectionId, String fileId, Long minSeqNumber, 
            Long maxSeqNumber, Date minDate, Date maxDate, Long maxNumberOfResults) {
        return extractEvents(new AuditTrailExtractor(collectionId, fileId, minSeqNumber, maxSeqNumber, minDate, 
                maxDate), maxNumberOfResults);
    }

    /**
     * Extracts the largest sequence number from the database. 
     * @return The largest sequence number. If no entry exists, then zero is returned.
     */
    public Long extractLargestSequenceNumber() {
        String sql = "SELECT " + AUDITTRAIL_SEQUENCE_NUMBER + " FROM " + AUDITTRAIL_TABLE + " ORDER BY "
                + AUDITTRAIL_SEQUENCE_NUMBER + " DESC";

        try {
            PreparedStatement ps = null;
            ResultSet res = null;
            Connection conn = null;
            try {
                conn = getConnection();
                ps = DatabaseUtils.createPreparedStatement(conn, sql, new Object[0]);
                res = ps.executeQuery();
                if(!res.next()) {
                    return 0L;
                }
                return res.getLong(1);
            } finally {
                if(res != null) {
                    res.close();
                }
                if(ps != null) {
                    ps.close();
                }
                if(conn != null) {
                    conn.close();
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not use SQL query '" + sql
                    + "' for retrieving the largest sequence number", e);

        }
    }

    /**
     * Extracts the the audit trail information based on the given sql query and arguments.
     * @param extractor The entity for extracting the audit trails.
     * @param maxNumberOfResults The maximum number of results.
     * @return The extracted audit trails.
     */
    private AuditTrailDatabaseResults extractEvents(AuditTrailExtractor extractor, Long maxNumberOfResults) {

        final int sequencePosition = 1;
        final int fileGuidPosition = 2;
        final int actorPosition = 3;
        final int actionDatePosition = 4;
        final int operationPosition = 5;
        final int auditTrailInformationPosition = 6;
        final int infoPosition = 7;
        
        String sql = "SELECT " + AUDITTRAIL_SEQUENCE_NUMBER + ", " + AUDITTRAIL_FILE_GUID + " , "
                + AUDITTRAIL_ACTOR_GUID + " , " + AUDITTRAIL_OPERATION_DATE + " , " + AUDITTRAIL_OPERATION + " , "
                + AUDITTRAIL_AUDIT + " , " + AUDITTRAIL_INFORMATION + " FROM " + AUDITTRAIL_TABLE + " "
                + extractor.createRestriction();

        AuditTrailDatabaseResults res = new AuditTrailDatabaseResults();
        try {
            PreparedStatement ps = null;
            ResultSet results = null;
            Connection conn = null;
            try {
                conn = getConnection();
                ps = DatabaseUtils.createPreparedStatement(conn, sql, extractor.getArguments());
                results = ps.executeQuery();
                
                int count = 0;
                while(results.next() && (maxNumberOfResults == null || count < maxNumberOfResults)) {
                    count++;
                    AuditTrailEvent event = new AuditTrailEvent();

                    event.setSequenceNumber(BigInteger.valueOf(results.getLong(sequencePosition)));
                    event.setFileID(retrieveFileId(results.getLong(fileGuidPosition)));
                    event.setActorOnFile(retrieveActorName(results.getLong(actorPosition)));
                    event.setActionDateTime(CalendarUtils.getFromMillis(results.getTimestamp(actionDatePosition).getTime()));
                    event.setActionOnFile(FileAction.fromValue(results.getString(operationPosition)));
                    event.setAuditTrailInformation(results.getString(auditTrailInformationPosition));
                    event.setInfo(results.getString(infoPosition));
                    event.setReportingComponent(componentID);
                    res.addAuditTrailEvent(event);
                }
                
                if(maxNumberOfResults != null && count >= maxNumberOfResults) {
                    log.debug("More than the maximum {} results found.", maxNumberOfResults);
                    res.reportMoreResultsFound();
                }
            } finally {
                if(res != null) {
                    results.close();
                }
                if(ps != null) {
                    ps.close();
                }
                if(conn != null) {
                    conn.close();
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not extract the audit trails events.", e);
        }

        log.debug("Extracted audit trail events: {}", res);
        return res;
    }

    /**
     * Retrieve the guid for a given file id. If the file id does not exist within the table, then it is created.
     *
     * @param collectionId The collection id for the file.
     * @param fileId The id of the file to retrieve. 
     * @return The guid for the given file id.
     */
    private synchronized long retrieveFileGuid(String collectionId, String fileId) {
        String sqlRetrieve = "SELECT " + FILE_GUID + " FROM " + FILE_TABLE + " WHERE " + FILE_FILEID + " = ? AND " 
                + FILE_COLLECTIONID + " = ?";

        Long guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, fileId, collectionId);

        if(guid == null) {
            log.debug("Inserting fileid '" + fileId + "' into the file table.");
            String sqlInsert = "INSERT INTO " + FILE_TABLE + " ( " + FILE_FILEID + " , " + FILE_COLLECTIONID 
                    + " ) VALUES ( ? , ? )";
            DatabaseUtils.executeStatement(dbConnector, sqlInsert, fileId, collectionId);

            guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, fileId, collectionId);
        }

        return guid;
    }

    /**
     * Retrieves a id of an file based on the guid.
     * @param fileGuid The guid for the file table entry.
     * @return The id of the file corresponding to the guid.
     */
    private String retrieveFileId(long fileGuid) {
        String sqlRetrieve = "SELECT " + FILE_FILEID + " FROM " + FILE_TABLE + " WHERE " + FILE_GUID + " = ?";

        return DatabaseUtils.selectStringValue(dbConnector, sqlRetrieve, fileGuid);
    }

    /**
     * Retrieve the guid for a given actor. If the actor does not exist within the actor, then it is created.
     *
     * @param actorName The name of the actor.
     * @return The guid of the actor with the given name.
     */
    private synchronized long retrieveActorGuid(String actorName) {
        String sqlRetrieve = "SELECT " + ACTOR_GUID + " FROM " + ACTOR_TABLE + " WHERE " + ACTOR_NAME + " = ?";

        Long guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, actorName);

        if(guid == null) {
            log.debug("Inserting actor '" + actorName + "' into the actor table.");
            String sqlInsert = "INSERT INTO " + ACTOR_TABLE + " ( " + ACTOR_NAME + " ) VALUES ( ? )";
            DatabaseUtils.executeStatement(dbConnector, sqlInsert, actorName);

            guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, actorName);
        }

        return guid;
    }

    /**
     * Retrieves a name of an actor based on the guid. 
     * @param actorGuid The guid of the actor.
     * @return The name of the actor corresponding to guid.
     */
    private String retrieveActorName(long actorGuid) {
        String sqlRetrieve = "SELECT " + ACTOR_NAME + " FROM " + ACTOR_TABLE + " WHERE " + ACTOR_GUID + " = ?";

        return DatabaseUtils.selectStringValue(dbConnector, sqlRetrieve, actorGuid);
    }

    /**
     * Class for encapsulating the request for extracting
     */
    private class AuditTrailExtractor {
        /** The file id limitation for the request. */
        private Long fileGuid;
        /** The minimum sequence number limitation for the request.*/
        private Long minSeqNumber;
        /** The maximum sequence number limitation for the request.*/
        private Long maxSeqNumber;
        /** The minimum date limitation for the request.*/
        private Date minDate;
        /** The maxmimum date limitation for the request.*/
        private Date maxDate;

        /**
         * Constructor.
         * @param collectionId The id of the collection.
         * @param fileId The file id limitation for the request.
         * @param minSeqNumber The minimum sequence number limitation for the request.
         * @param maxSeqNumber The maximum sequence number limitation for the request.
         * @param minDate The minimum date limitation for the request.
         * @param maxDate The maximum date limitation for the request.
         */
        public AuditTrailExtractor(String collectionId, String fileId, Long minSeqNumber, Long maxSeqNumber, Date minDate,
                                   Date maxDate) {
            if(fileId == null) {
                this.fileGuid = null;
            } else {
                this.fileGuid = retrieveFileGuid(collectionId, fileId);
            }
            this.minSeqNumber = minSeqNumber;
            this.maxSeqNumber = maxSeqNumber;
            this.minDate = minDate;
            this.maxDate = maxDate;
        }

        /**
         * @return The restriction for the request.
         */
        public String createRestriction() {
            // Handle the case with no restrictions.
            if(fileGuid == null && minSeqNumber == null && maxSeqNumber == null && minDate == null && maxDate == null) {
                return "";
            }

            StringBuilder res = new StringBuilder();

            if(fileGuid != null) {
                nextArgument(res);
                res.append(AUDITTRAIL_FILE_GUID + " = ?");
            }

            if(minSeqNumber != null) {
                nextArgument(res);
                res.append(AUDITTRAIL_SEQUENCE_NUMBER + " >= ?");
            }

            if(maxSeqNumber != null) {
                nextArgument(res);
                res.append(AUDITTRAIL_SEQUENCE_NUMBER + " <= ?");
            }

            if(minDate != null) {
                nextArgument(res);
                res.append(AUDITTRAIL_OPERATION_DATE + " >= ?");
            }

            if(maxDate != null) {
                nextArgument(res);
                res.append(AUDITTRAIL_OPERATION_DATE + " <= ?");
            }

            return res.toString();
        }

        /**
         * Adds either ' AND ' or 'WHERE ' depending on whether it is the first restriction.
         * @param res The StringBuilder where the restrictions are combined.
         */
        private void nextArgument(StringBuilder res) {
            if(res.length() > 0) {
                res.append(" AND ");
            } else {
                res.append("WHERE ");
            }
        }

        /**
         * @return The arguments for the SQL statement.
         */
        public Object[] getArguments() {
            List<Object> res = new ArrayList<Object>();
            if(fileGuid != null) {
                res.add(fileGuid);
            }
            if(minSeqNumber != null) {
                res.add(minSeqNumber);
            }
            if(maxSeqNumber != null) {
                res.add(maxSeqNumber);
            }
            if(minDate != null) {
                res.add(minDate);
            }
            if(maxDate != null) {
                res.add(maxDate);
            }

            return res.toArray();
        }
    }
}
