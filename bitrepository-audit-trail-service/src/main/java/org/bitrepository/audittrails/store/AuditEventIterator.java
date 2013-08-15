package org.bitrepository.audittrails.store;

import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_NAME;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.ACTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_ID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.CONTRIBUTOR_TABLE;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_FILEID;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_KEY;
import static org.bitrepository.audittrails.store.AuditDatabaseConstants.FILE_TABLE;

import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;

/**
 * Class to iterate over the set of AuditTrailEvents produced by a resultset.  
 */
public class AuditEventIterator {

    /** Position of the FileId in the extraction.*/
    private static final int POSITION_FILE_KEY = 1;
    /** Position of the ContributorId in the extraction.*/
    private static final int POSITION_CONTRIBUTOR_KEY = 2;
    /** Position of the SequenceNumber in the extraction.*/
    private static final int POSITION_SEQUENCE_NUMBER = 3;
    /** Position of the ActorName in the extraction.*/
    private static final int POSITION_ACTOR_KEY = 4;
    /** Position of the Operation in the extraction.*/
    private static final int POSITION_OPERATION = 5;
    /** Position of the OperationDate in the extraction.*/
    private static final int POSITION_OPERATION_DATE = 6;
    /** Position of the AuditTrail in the extraction.*/
    private static final int POSITION_AUDIT_TRAIL = 7;
    /** Position of the Information in the extraction.*/
    private static final int POSITION_INFORMATION = 8;
    
    private ResultSet auditResultSet = null;
    private final PreparedStatement ps;
    private final DBConnector dbConnector;
    
    /**
     * Constructor
     * @param auditResultSet The ResultSet of audit trails from the database
     * @param dbConnector The database connection, for looking up foreign keys in the auditResultSet 
     */
    public AuditEventIterator(PreparedStatement ps, DBConnector dbConnector) {
        this.ps = ps;
        this.dbConnector = dbConnector;
    }
    
    /**
     * Method to explicitly close the ResultSet in the AuditEventIterator 
     * @throws SQLException in case of a sql error
     */
    public void close() throws SQLException {
        if(auditResultSet != null) {
            if(!auditResultSet.isClosed()) {
                auditResultSet.close();
            }    
        }
        if(ps != null) {
            ps.close();
        }
    }
    
    /**
     * Method to return the next AuditTrailEvent in the ResultSet
     * When no more AuditTrailEvents are available, null is returned and the internal ResultSet closed. 
     * @return The next AuditTrailEvent available in the ResultSet, or null if no more events are available. 
     * @throws SQLException In case of a sql error. 
     */
    public AuditTrailEvent getNextAuditTrailEvent() {
        try {
            AuditTrailEvent event = null;
            try {
                if(auditResultSet == null) {
                    auditResultSet = ps.executeQuery();
                }
                if(!ps.isClosed() && !auditResultSet.isClosed()) {
                    if(auditResultSet.next()) {
                         event = new AuditTrailEvent();
                        
                        Long actorKey = auditResultSet.getLong(POSITION_ACTOR_KEY);
                        String actorName = retrieveActorName(actorKey);
                        
                        Long fileKey = auditResultSet.getLong(POSITION_FILE_KEY);
                        String fileId = retrieveFileId(fileKey);
                        
                        Long contributorKey = auditResultSet.getLong(POSITION_CONTRIBUTOR_KEY);
                        String contributorId = retrieveContributorId(contributorKey);
                        
                        event.setActionDateTime(CalendarUtils.getFromMillis(auditResultSet.getTimestamp(POSITION_OPERATION_DATE).getTime()));
                        event.setActionOnFile(FileAction.fromValue(auditResultSet.getString(POSITION_OPERATION)));
                        event.setAuditTrailInformation(auditResultSet.getString(POSITION_AUDIT_TRAIL));
                        event.setActorOnFile(actorName);
                        event.setFileID(fileId);
                        event.setInfo(auditResultSet.getString(POSITION_INFORMATION));
                        event.setReportingComponent(contributorId);
                        event.setSequenceNumber(BigInteger.valueOf(auditResultSet.getLong(POSITION_SEQUENCE_NUMBER)));
                    } else {
                        close();
                    }
                }
            } finally {
                close();
            }
    
            return event;
        } catch (Exception e) {
            throw new IllegalStateException("Could not extract the wanted audittrails", e);
        }

    }
    
    /**
     * Retrieves a id of a contributor based on the guid. 
     * @param contributorGuid The guid of the contributor.
     * @return The id of the contributor corresponding to guid.
     */
    private String retrieveContributorId(long contributorGuid) {
        String sqlRetrieve = "SELECT " + CONTRIBUTOR_ID + " FROM " + CONTRIBUTOR_TABLE + " WHERE " + CONTRIBUTOR_KEY 
                + " = ?";
        
        return DatabaseUtils.selectStringValue(dbConnector, sqlRetrieve, contributorGuid);        
    }
    
    /**
     * Retrieves a id of a file based on the guid. 
     * @param fileGuid The guid of the file.
     * @return The id of the file corresponding to guid.
     */
    private String retrieveFileId(long fileGuid) {
        String sqlRetrieve = "SELECT " + FILE_FILEID + " FROM " + FILE_TABLE + " WHERE " + FILE_KEY + " = ?";
        
        return DatabaseUtils.selectStringValue(dbConnector, sqlRetrieve, fileGuid);        
    }
    
    /**
     * Retrieves a name of an actor based on the guid. 
     * @param actorGuid The guid of the actor.
     * @return The name of the actor corresponding to guid.
     */
    private String retrieveActorName(long actorGuid) {
        String sqlRetrieve = "SELECT " + ACTOR_NAME + " FROM " + ACTOR_TABLE + " WHERE " + ACTOR_KEY + " = ?";
        
        return DatabaseUtils.selectStringValue(dbConnector, sqlRetrieve, actorGuid);        
    }
}
