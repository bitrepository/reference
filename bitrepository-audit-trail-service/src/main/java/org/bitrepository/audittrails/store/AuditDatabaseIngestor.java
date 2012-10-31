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
 *  AUDITTRAIL_ACTOR_GUID 
 *  AUDITTRAIL_FILE_GUID 
 *  AUDITTRAIL_CONTRIBUTOR_GUID
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
    public void ingestAuditEvents(AuditTrailEvent event) {
        ArgumentValidator.checkNotNull(event, "AuditTrailEvent event");
        
        String sqlInsert = "INSERT INTO " + AUDITTRAIL_TABLE + " ( " + createIngestElementString(event) + " ) VALUES ( " 
                + createIngestArgumentString(event) + " )";
        DatabaseUtils.executeStatement(dbConnector, sqlInsert, extractArgumentsFromEvent(event));
    }

    /**
     * @param event The audit trail event to ingeste into the database.
     * @return Creates the set of elements to be ingested into the database.
     */
    private String createIngestElementString(AuditTrailEvent event) {
        StringBuilder res = new StringBuilder();
        
        addElement(res, event.getActorOnFile(), AUDITTRAIL_ACTOR_GUID);
        addElement(res, event.getFileID(), AUDITTRAIL_FILE_GUID);
        addElement(res, event.getReportingComponent(), AUDITTRAIL_CONTRIBUTOR_GUID);
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
     * @return The list of elements in the model which are not null.
     */
    private Object[] extractArgumentsFromEvent(AuditTrailEvent event) {
        List<Object> res = new ArrayList<Object>();

        if(event.getActorOnFile() != null) {
            Long actorGuid = retrieveActorGuid(event.getActorOnFile());
            res.add(actorGuid);
        }
        
        if(event.getFileID() != null) {
            Long fileGuid = retrieveFileGuid(event.getFileID());
            res.add(fileGuid);
        }
        
        if(event.getReportingComponent() != null) {
            Long contributorGuid = retrieveContributorGuid(event.getReportingComponent());
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
     * Retrieve the guid for a given contributor. If the contributor does not exist within the contributor table, 
     * then it is created.
     * 
     * @param contributorId The name of the actor.
     * @return The guid of the actor with the given name.
     */
    private long retrieveContributorGuid(String contributorId) {
        String sqlRetrieve = "SELECT " + CONTRIBUTOR_GUID + " FROM " + CONTRIBUTOR_TABLE + " WHERE " 
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
     * Retrieve the guid for a given file. If the file does not exist within the file table, then it is created.
     * 
     * @param fileId The id of the file.
     * @return The guid of the file with the given id.
     */
    private long retrieveFileGuid(String fileId) {
        ArgumentValidator.checkNotNull(fileId, "fileId");
        String sqlRetrieve = "SELECT " + FILE_GUID + " FROM " + FILE_TABLE + " WHERE " + FILE_FILEID + " = ?";
        Long guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, fileId);
        
        if(guid == null) {
            log.debug("Inserting file '" + fileId + "' into the file table.");
            String sqlInsert = "INSERT INTO " + FILE_TABLE + " ( " + FILE_FILEID + " ) VALUES ( ? )";
            DatabaseUtils.executeStatement(dbConnector, sqlInsert, fileId);
            
            guid = DatabaseUtils.selectLongValue(dbConnector, sqlRetrieve, fileId);
        }
        
        return guid;
    }
    
    /**
     * Retrieve the guid for a given actor. If the actor does not exist within the actor, then it is created.
     * 
     * @param actorName The name of the actor.
     * @return The guid of the actor with the given name.
     */
    private long retrieveActorGuid(String actorName) {
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
}
