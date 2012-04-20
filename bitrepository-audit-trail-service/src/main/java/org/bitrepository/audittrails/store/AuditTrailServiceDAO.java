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

import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_GUID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_NAME;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_ACTOR_GUID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_AUDIT;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_CONTRIBUTOR_GUID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_FILE_GUID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_INFORMATION;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_OPERATION;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_OPERATION_DATE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_SEQUENCE_NUMBER;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.AUDITTRAIL_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_GUID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_FILEID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_GUID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_TABLE;

import java.io.File;
import java.sql.Connection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.database.DatabaseUtils;
import org.bitrepository.common.database.DerbyDBConnector;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
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
    /** The settings.*/
    private final Settings settings;
    
    /** 
     * Constructor.
     * @param settings The settings.
     */
    public AuditTrailServiceDAO(Settings settings) {
        ArgumentValidator.checkNotNull(settings, "settings");
        
        this.settings = settings;
        
        // TODO make a better instantiation, which is not depending on Derby.
        dbConnector = new DerbyDBConnector();
        
        try {
            getConnection();
        } catch (IllegalStateException e) {
            log.warn("No existing database.", e);
            initDatabaseConnection();
            getConnection();
        }
    }
    
    /**
     * Retrieve the access to the database. If it cannot be done, then it is automatically attempted to instantiate 
     * the database based on the SQL script.
     * @return The connection to the database.
     */
    private void initDatabaseConnection() {
        log.info("Trying to instantiate the database.");
        // TODO handle this!
        //        InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(
        //                "src/main/resources/integrityDB.sql");
        File sqlDatabaseFile = new File("src/main/resources/auditServiceDB.sql");
        dbConnector.createDatabase(sqlDatabaseFile);
    }
    
    /**
     * Retrieve the connection to the database.
     * TODO improve performance (only reconnect every-so-often)... 
     * @return The connection to the database.
     */
    protected Connection getConnection() {
        try { 
            Connection dbConnection = dbConnector.getEmbeddedDBConnection(
                    settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditContributerDatabaseUrl());
            return dbConnection;
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate the database with the url '"
                    + settings.getReferenceSettings().getAuditTrailServiceSettings().getAuditContributerDatabaseUrl() + "'", e);
        }
    }
    
    @Override
    public List<AuditTrailEvent> getAuditTrails(String fileId, String contributorId, Long minSeqNumber, 
            Long maxSeqNumber, String actorName, FileAction operation, Date startDate, Date endDate) {
        ExtractModel model = new ExtractModel();
        model.setFileId(fileId);
        model.setContributorId(contributorId);
        model.setMinSeqNumber(minSeqNumber);
        model.setMaxSeqNumber(maxSeqNumber);
        model.setActorName(actorName);
        model.setOperation(operation);
        model.setStartDate(startDate);
        model.setEndDate(endDate);

        AuditDatabaseExtractor extractor = new AuditDatabaseExtractor(model, getConnection());
        return extractor.extractAuditEvents();
    }
    
    @Override
    public void addAuditTrails(AuditTrailEvents newAuditTrails) {
        ArgumentValidator.checkNotNull(newAuditTrails, "AuditTrailEvents newAuditTrails");
        
        for(AuditTrailEvent event : newAuditTrails.getAuditTrailEvent()) {
            insertAuditTrailEvent(event);
        }
    }
    
    @Override
    public int largestSequenceNumber(String contributorId) {
        String sql = "SELECT " + AUDITTRAIL_SEQUENCE_NUMBER + " FROM " + AUDITTRAIL_TABLE + " WHERE " 
                + AUDITTRAIL_CONTRIBUTOR_GUID + " = ( SELECT " + CONTRIBUTOR_GUID + " FROM " + CONTRIBUTOR_TABLE 
                + " WHERE " + CONTRIBUTOR_ID + " = ? ) ORDER BY " + AUDITTRAIL_SEQUENCE_NUMBER + " DESC";
        
        Long seq = DatabaseUtils.selectFirstLongValue(getConnection(), sql, contributorId);
        if(seq != null) {
            return seq.intValue();
        }
        return 0;
    }
    
    /**
     * Inserts a single audit trail event into the database.
     * @param event The event to be inserted into the database.
     */
    private void insertAuditTrailEvent(AuditTrailEvent event) {
        // retrieve the different guids
        long actorGuid = retrieveActorGuid(event.getActorOnFile());
        long fileGuid = retrieveFileGuid(event.getFileID());
        long contributorGuid = retrieveContributorGuid(event.getReportingComponent());
        
        String sqlInsert = "INSERT INTO " + AUDITTRAIL_TABLE + " ( " + AUDITTRAIL_ACTOR_GUID + " , "
                + AUDITTRAIL_FILE_GUID + " , " + AUDITTRAIL_CONTRIBUTOR_GUID + " , " + AUDITTRAIL_AUDIT + " , "
                + AUDITTRAIL_INFORMATION + " , " + AUDITTRAIL_OPERATION + " , " + AUDITTRAIL_OPERATION_DATE + " , "
                + AUDITTRAIL_SEQUENCE_NUMBER + " ) VALUES ( ? , ? , ? , ? , ? , ? , ? , ? )";
        
        DatabaseUtils.executeStatement(getConnection(), sqlInsert, actorGuid, fileGuid, contributorGuid,
                event.getAuditTrailInformation(), event.getInfo(), event.getActionOnFile().toString(),
                CalendarUtils.convertFromXMLGregorianCalendar(event.getActionDateTime()), 
                event.getSequenceNumber().longValue());
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
        
        Long guid = DatabaseUtils.selectLongValue(getConnection(), sqlRetrieve, contributorId);
        
        if(guid == null) {
            log.debug("Inserting contributor '" + contributorId + "' into the contributor table.");
            String sqlInsert = "INSERT INTO " + CONTRIBUTOR_TABLE + " ( " + CONTRIBUTOR_ID + " ) VALUES ( ? )";
            DatabaseUtils.executeStatement(getConnection(), sqlInsert, contributorId);
            
            guid = DatabaseUtils.selectLongValue(getConnection(), sqlRetrieve, contributorId);
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
        String sqlRetrieve = "SELECT " + FILE_GUID + " FROM " + FILE_TABLE + " WHERE " + FILE_FILEID + " = ?";
        Long guid = DatabaseUtils.selectLongValue(getConnection(), sqlRetrieve, fileId);
        
        if(guid == null) {
            log.debug("Inserting file '" + fileId + "' into the file table.");
            String sqlInsert = "INSERT INTO " + FILE_TABLE + " ( " + FILE_FILEID + " ) VALUES ( ? )";
            DatabaseUtils.executeStatement(getConnection(), sqlInsert, fileId);
            
            guid = DatabaseUtils.selectLongValue(getConnection(), sqlRetrieve, fileId);
        }
        
        return guid;
    }
    
    /**
     * Retrieve the guid for a given actor. If the actor does not exist within the actor, then it is created.
     * 
     * @param actor The name of the actor.
     * @return The guid of the actor with the given name.
     */
    private long retrieveActorGuid(String actorName) {
        String sqlRetrieve = "SELECT " + ACTOR_GUID + " FROM " + ACTOR_TABLE + " WHERE " + ACTOR_NAME + " = ?";
        
        Long guid = DatabaseUtils.selectLongValue(getConnection(), sqlRetrieve, actorName);
        
        if(guid == null) {
            log.debug("Inserting actor '" + actorName + "' into the actor table.");
            String sqlInsert = "INSERT INTO " + ACTOR_TABLE + " ( " + ACTOR_NAME + " ) VALUES ( ? )";
            DatabaseUtils.executeStatement(getConnection(), sqlInsert, actorName);
            
            guid = DatabaseUtils.selectLongValue(getConnection(), sqlRetrieve, actorName);
        }
        
        return guid;
    }    
}
