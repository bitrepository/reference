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

import java.util.ArrayList;
import java.util.List;
import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.bitrepository.audittrails.store.AuditDatabaseConstants.*;

/**
 * Handles the ingestion of the Audit Events into the database.
 * 
 * Ingested in the following order:
 *  AUDITTRAIL_ACTOR_KEY 
 *  AUDITTRAIL_FILE_KEY 
 *  AUDITTRAIL_CONTRIBUTOR_KEY
 *  AUDITTRAIL_AUDIT 
 *  AUDITTRAIL_INFORMATION 
 *  AUDITTRAIL_OPERATION 
 *  AUDITTRAIL_OPERATION_DATE 
 *  AUDITTRAIL_SEQUENCE_NUMBER 
 */
public class AuditDatabaseIngestor {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The connector to the database.*/
    private final DBConnector dbConnector;
    
    /**
     * Constructor.
     * @param dbConnector The connector to the database, where the audit trails are to be ingested.
     */
    public AuditDatabaseIngestor(DBConnector dbConnector) {
        ArgumentValidator.checkNotNull(dbConnector, "DBConnector dbConnector");
        this.dbConnector = dbConnector;
    }
    
    /**
     * Ingests the given audit trails into the database.
     * @param event The auditTrail event to ingest into the database.
     */
    public void ingestAuditEvents(AuditTrailEvent event, String collectionId) {
        ArgumentValidator.checkNotNull(event, "AuditTrailEvent event");
        
        String sqlInsert = "INSERT INTO " + AUDITTRAIL_TABLE + " ( " + createIngestElementString(event) + " ) VALUES ( " 
                + createIngestArgumentString(event) + " )";
        DatabaseUtils.executeStatement(dbConnector, sqlInsert, extractArgumentsFromEvent(event, collectionId));
    }

    /**
     * @param event The audit trail event to ingeste into the database.
     * @return Creates the set of elements to be ingested into the database.
     */
    private String createIngestElementString(AuditTrailEvent event) {
        StringBuilder res = new StringBuilder();
        
        addElement(res, event.getActorOnFile(), AUDITTRAIL_ACTOR_KEY);
        addElement(res, event.getFileID(), AUDITTRAIL_FILE_KEY);
        addElement(res, event.getReportingComponent(), AUDITTRAIL_CONTRIBUTOR_KEY);
        addElement(res, event.getAuditTrailInformation(), AUDITTRAIL_AUDIT);
        addElement(res, event.getInfo(), AUDITTRAIL_INFORMATION);
        addElement(res, event.getActionOnFile(), AUDITTRAIL_OPERATION);
        addElement(res, event.getActionDateTime(), AUDITTRAIL_OPERATION_DATE);
        addElement(res, event.getSequenceNumber(), AUDITTRAIL_SEQUENCE_NUMBER);
        
        return res.toString();
    }
    
    /**
     * Adds the field for a given element to the string builder if the element is not null.
     * @param res The StringBuilder where the restrictions are combined.
     * @param element The element to be ingested. Is validated whether it is null.
     * @param name The name of the field in the database corresponding to the element. 
     */
    private void addElement(StringBuilder res, Object element, String name) {
        if(element == null) {
            return;
        }
        
        if(res.length() == 0) {
            res.append(" ");
        } else {
            res.append(" , ");
        }
        
        res.append(name);
    }
    
    /**
     * @param event The audit trail event to ingest into the database.
     * @return The string for the arguments for the elements of the event to be ingested into the database.
     */
    private String createIngestArgumentString(AuditTrailEvent event) {
        StringBuilder res = new StringBuilder();
        
        addArgument(res, event.getActorOnFile());
        addArgument(res, event.getFileID());
        addArgument(res, event.getReportingComponent());
        addArgument(res, event.getAuditTrailInformation());
        addArgument(res, event.getInfo());
        addArgument(res, event.getActionOnFile());
        addArgument(res, event.getActionDateTime());
        addArgument(res, event.getSequenceNumber());
        
        return res.toString();
    }
    
    /**
     * Adds a question mark for a given element to the string builder if the element is not null.
     * @param res The StringBuilder where the restrictions are combined.
     * @param element The element to be ingested. Is validated whether it is null.
     */
    private void addArgument(StringBuilder res, Object element) {
        if(element == null) {
            return;
        }
        
        if(res.length() == 0) {
            res.append(" ? ");
        } else {
            res.append(", ? ");
        }
    }

    
    /**
     * Extracts the arguments from the event.
     * @param event The event for the audit trail.
     * @param collectionId The id of the collection, where the audit event has taken place.
     * @return The list of elements in the model which are not null.
     */
    private Object[] extractArgumentsFromEvent(AuditTrailEvent event, String collectionId) {
        List<Object> res = new ArrayList<Object>();

        if(event.getActorOnFile() != null) {
            Long actorGuid = retrieveActorKey(event.getActorOnFile());
            res.add(actorGuid);
        }
        
        if(event.getFileID() != null) {
            Long fileGuid = retrieveFileKey(event.getFileID(), collectionId);
            res.add(fileGuid);
        }
        
        if(event.getReportingComponent() != null) {
            Long contributorGuid = retrieveContributorKey(event.getReportingComponent());
            res.add(contributorGuid);
        }
        
        if(event.getAuditTrailInformation() != null) {
            res.add(event.getAuditTrailInformation());
        }
        
        if(event.getInfo() != null) {
            res.add(event.getInfo());
        }
        
        if(event.getActionOnFile() != null) {
            res.add(event.getActionOnFile().toString());
        }
        
        if(event.getActionDateTime() != null) {
            res.add(CalendarUtils.convertFromXMLGregorianCalendar(event.getActionDateTime()));
        }
        
        if(event.getSequenceNumber() != null) {
            res.add(event.getSequenceNumber().longValue());
        }
        
        return res.toArray();
    }
    
    /**
     * Retrieve the key for a given contributor. If the contributor does not exist within the contributor table, 
     * then it is created.
     * 
     * @param contributorId The name of the contributor.
     * @return The key for the contributor with the given id.
     */
    private long retrieveContributorKey(String contributorId) {
        String sqlRetrieve = "SELECT " + CONTRIBUTOR_KEY + " FROM " + CONTRIBUTOR_TABLE + " WHERE " 
                + CONTRIBUTOR_ID + " = ?";
        
        Long guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, contributorId);
        
        if(guid == null) {
            log.debug("Inserting contributor '" + contributorId + "' into the contributor table.");
            String sqlInsert = "INSERT INTO " + CONTRIBUTOR_TABLE + " ( " + CONTRIBUTOR_ID + " ) VALUES ( ? )";
            DatabaseUtils.executeStatement(dbConnector, sqlInsert, contributorId);
            
            guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, contributorId);
        }
        
        return guid;
    }
    
    /**
     * Retrieve the key for a given file. If the file does not exist within the file table, then it is created.
     * 
     * @param fileId The id of the file.
     * @param collectionId The id of the collection for the 
     * @return The key of the file with the given id.
     */
    private synchronized long retrieveFileKey(String fileId, String collectionId) {
        ArgumentValidator.checkNotNull(fileId, "fileId");
        Long collectionKey = retrieveCollectionKey(collectionId);
        
        String sqlRetrieve = "SELECT " + FILE_KEY + " FROM " + FILE_TABLE + " WHERE " + FILE_FILEID + " = ? AND "
                + FILE_COLLECTION_KEY + " = ?";
        Long guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, fileId, collectionKey);
        
        if(guid == null) {
            log.debug("Inserting file '" + fileId + "' into the file table.");
            String sqlInsert = "INSERT INTO " + FILE_TABLE + " ( " + FILE_FILEID + " , " + FILE_COLLECTION_KEY 
                    + " ) VALUES ( ? , ? )";
            DatabaseUtils.executeStatement(dbConnector, sqlInsert, fileId, collectionKey);
            
            guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, fileId, collectionKey);
        }
        
        return guid;
    }

    /**
     * Retrieve the key for a given collection. 
     * If the collection does not exist within the collection table, then it is created.
     * 
     * @param collectionId The id of the collection.
     * @return The key for the collection with the given id.
     */
    private synchronized long retrieveCollectionKey(String collectionId) {
        ArgumentValidator.checkNotNull(collectionId, "String collectionId");
        
        String sqlRetrieve = "SELECT " + COLLECTION_KEY + " FROM " + COLLECTION_TABLE + " WHERE " + COLLECTION_ID 
                + " = ?";
        Long guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, collectionId);
        
        if(guid == null) {
            log.debug("Inserting collection '" + collectionId + "' into the collection table.");
            String sqlInsert = "INSERT INTO " + COLLECTION_TABLE + " ( " + COLLECTION_ID + " ) VALUES ( ? )";
            DatabaseUtils.executeStatement(dbConnector, sqlInsert, collectionId);
            
            guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, collectionId);
        }
        
        return guid;
    }
    
    /**
     * Retrieve the key for a given actor. If the actor does not exist within the actor, then it is created.
     * 
     * @param actorName The name of the actor.
     * @return The key for the actor with the given name.
     */
    private synchronized long retrieveActorKey(String actorName) {
        String sqlRetrieve = "SELECT " + ACTOR_KEY + " FROM " + ACTOR_TABLE + " WHERE " + ACTOR_NAME + " = ?";
        
        Long guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, actorName);
        
        if(guid == null) {
            log.debug("Inserting actor '" + actorName + "' into the actor table.");
            String sqlInsert = "INSERT INTO " + ACTOR_TABLE + " ( " + ACTOR_NAME + " ) VALUES ( ? )";
            DatabaseUtils.executeStatement(dbConnector, sqlInsert, actorName);
            
            guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, actorName);
        }
        
        return guid;
    }    
}
