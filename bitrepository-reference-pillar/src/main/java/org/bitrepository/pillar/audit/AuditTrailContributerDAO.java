package org.bitrepository.pillar.audit;

import java.io.File;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.database.DatabaseUtils;
import org.bitrepository.common.database.DerbyDBConnector;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.AuditTrailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.ACTOR_GUID;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.ACTOR_NAME;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.ACTOR_TABLE;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.AUDITTRAIL_AUDIT;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.AUDITTRAIL_ACTOR_GUID;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.AUDITTRAIL_FILE_GUID;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.AUDITTRAIL_INFORMATION;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.AUDITTRAIL_OPERATION;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.AUDITTRAIL_OPERATION_DATE;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.AUDITTRAIL_SEQUENCE_NUMBER;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.AUDITTRAIL_TABLE;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.FILE_TABLE;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.FILE_GUID;
import static org.bitrepository.pillar.audit.AuditDatabaseConstants.FILE_FILEID;

/**
 * Access interface for communication with the audit trail database.
 */
public class AuditTrailContributerDAO implements AuditTrailManager {
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
    public AuditTrailContributerDAO(Settings settings) {
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
        File sqlDatabaseFile = new File("src/main/resources/auditContributerDB.sql");
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
                    settings.getReferenceSettings().getPillarSettings().getAuditContributerDatabaseUrl());
            return dbConnection;
        } catch (Exception e) {
            throw new IllegalStateException("Could not instantiate the database with the url '"
                    + settings.getReferenceSettings().getPillarSettings().getAuditContributerDatabaseUrl() + "'", e);
        }
    }
    
    @Override
    public void addAuditEvent(String fileId, String actor, String info, String auditTrail, FileAction operation) {
        log.info("Inserting an audit event  for file '" + fileId + "', from actor '" + actor 
                + "' performing operation '" + operation + "', with the audit trail information '" + auditTrail + "'");
        long fileGuid = retrieveFileGuid(fileId);
        long actorGuid = retrieveActorGuid(actor);
        
        String insertSql = "INSERT INTO " + AUDITTRAIL_TABLE + " ( " + AUDITTRAIL_FILE_GUID + " , " 
                + AUDITTRAIL_ACTOR_GUID + " , " + AUDITTRAIL_OPERATION + " , " + AUDITTRAIL_OPERATION_DATE + " , "
                + AUDITTRAIL_AUDIT + " , " + AUDITTRAIL_INFORMATION + " ) VALUES ( ? , ? , ? , ? , ? , ? )";
        DatabaseUtils.executeStatement(getConnection(), insertSql, fileGuid, actorGuid, operation.toString(), 
                new Date(), auditTrail, info);
    }
    
    @Override
    public Collection<AuditTrailEvent> getAudits(String fileId, Long sequenceNumber) {
        if(fileId == null && sequenceNumber == null) {
            return extractEvents("", new Object[0]);
        }
        if(fileId == null && sequenceNumber != null) {
            return extractEvents("WHERE " + AUDITTRAIL_SEQUENCE_NUMBER + " >= ? ", sequenceNumber);
        }
        Long fileGuid = retrieveFileGuid(fileId);
        if(fileId != null && sequenceNumber == null) {
            return extractEvents("WHERE " + AUDITTRAIL_FILE_GUID + " = ? ", fileGuid);
        } 
        
        return extractEvents("WHERE " + AUDITTRAIL_SEQUENCE_NUMBER + " >= ? AND " + AUDITTRAIL_FILE_GUID + " = ? ", 
                sequenceNumber, fileGuid);
    }
    
    /**
     * Extracts the largest sequence number from the database. 
     * @return The largest sequence number.
     */
    public Long extractLargestSequenceNumber() {
        String sql = "SELECT " + AUDITTRAIL_SEQUENCE_NUMBER + " FROM " + AUDITTRAIL_TABLE + " ORDER BY " 
                + AUDITTRAIL_SEQUENCE_NUMBER + " DESC";
        
        try {
            ResultSet res = DatabaseUtils.selectObject(getConnection(), sql, new Object[0]);
            
            try {
                if(!res.next()) {
                    return 0L;
                }
                return res.getLong(1);
            } finally {
                res.close();
            }
        } catch (Exception e) {
            throw new IllegalStateException("Could not use SQL query '" + sql 
                    + "' for retrieving the largest sequence number", e);
        }
    }
    
    /**
     * Extracts the the audit trail information based on the given sql query and arguments.
     * @param restriction The restriction to the query.
     * @param args The arguments.
     * @return The extracted 
     */
    private Collection<AuditTrailEvent> extractEvents(String restriction, Object ... args) {
        
        final int sequencePosition = 1;
        final int fileGuidPosition = 2;
        final int actorPosition = 3;
        final int actionDatePosition = 4;
        final int operationPosition = 5;
        final int auditTrailInformationPosition = 6;
        final int infoPosition = 7;
        
        String sql = "SELECT " + AUDITTRAIL_SEQUENCE_NUMBER + ", " + AUDITTRAIL_FILE_GUID + " , " 
                + AUDITTRAIL_ACTOR_GUID + " , " + AUDITTRAIL_OPERATION_DATE + " , " + AUDITTRAIL_OPERATION + " , " 
                + AUDITTRAIL_AUDIT + " , " + AUDITTRAIL_INFORMATION + " FROM " + AUDITTRAIL_TABLE + " " + restriction;
        
        List<AuditTrailEvent> res = new ArrayList<AuditTrailEvent>();
        try {
            ResultSet results = null;
            try {
                results = DatabaseUtils.selectObject(getConnection(), sql, args);
                
                while(results.next()) {
                    AuditTrailEvent event = new AuditTrailEvent();
                    
                    event.setSequenceNumber(BigInteger.valueOf(results.getLong(sequencePosition)));
                    event.setFileID(retrieveFileId(results.getLong(fileGuidPosition))); 
                    event.setActorOnFile(retrieveActorName(results.getLong(actorPosition)));
                    event.setActionDateTime(CalendarUtils.getXmlGregorianCalendar(results.getDate(actionDatePosition)));
                    event.setActionOnFile(FileAction.fromValue(results.getString(operationPosition)));
                    event.setAuditTrailInformation(results.getString(auditTrailInformationPosition));
                    event.setInfo(results.getString(infoPosition));
                    event.setReportingComponent(settings.getReferenceSettings().getPillarSettings().getPillarID());
                    res.add(event);
                }
            } finally {
                if(results != null) {
                    results.close();
                }
            }
        } catch (Exception e) {
            throw new IllegalStateException("Issue regarding", e);
        }
        
        return res;
    }
    
    /**
     * Retrieve the guid for a given file id. If the file id does not exist within the table, then it is created.
     * 
     * @param fileId The id of the file to retrieve. 
     * @return The guid for the given file id.
     */
    private long retrieveFileGuid(String fileId) {
        String sqlRetrieve = "SELECT " + FILE_GUID + " FROM " + FILE_TABLE + " WHERE " + FILE_FILEID + " = ?";
        
        Long guid = DatabaseUtils.selectLongValue(getConnection(), sqlRetrieve, fileId);
        
        if(guid == null) {
            log.debug("Inserting fileid '" + fileId + "' into the file table.");
            String sqlInsert = "INSERT INTO " + FILE_TABLE + " ( " + FILE_FILEID + " ) VALUES ( ? )";
            DatabaseUtils.executeStatement(getConnection(), sqlInsert, fileId);
            
            guid = DatabaseUtils.selectLongValue(getConnection(), sqlRetrieve, fileId);
        }
        
        return guid;
    }
    
    /**
     * Retrieves a id of an file based on the guid. 
     * @param actorGuid The id of the file.
     * @return The id of the file corresponding to the guid.
     */
    private String retrieveFileId(long fileGuid) {
        String sqlRetrieve = "SELECT " + FILE_FILEID + " FROM " + FILE_TABLE + " WHERE " + FILE_GUID + " = ?";
        
        return DatabaseUtils.selectStringValue(getConnection(), sqlRetrieve, fileGuid);        
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
    
    /**
     * Retrieves a name of an actor based on the guid. 
     * @param actorGuid The guid of the actor.
     * @return The name of the actor corresponding to guid.
     */
    private String retrieveActorName(long actorGuid) {
        String sqlRetrieve = "SELECT " + ACTOR_NAME + " FROM " + ACTOR_TABLE + " WHERE " + ACTOR_GUID + " = ?";
        
        return DatabaseUtils.selectStringValue(getConnection(), sqlRetrieve, actorGuid);        
    }
}
