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

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.bitrepository.bitrepositoryelements.AuditTrailEvent;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.database.DatabaseUtils;
import org.bitrepository.common.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Extractor for the audit trail events from the AuditTrailServiceDatabase.
 * 
 * Order of extraction:
 * FileId, ContributorId, SequenceNumber, SeqNumber, ActorName, Operation, OperationDate, AuditTrail, Information
 */
public class AuditDatabaseExtractor {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** Position of the FileId in the extraction.*/
    private static final int POSITION_FILE_GUID = 1;
    /** Position of the ContributorId in the extraction.*/
    private static final int POSITION_CONTRIBUTOR_GUID = 2;
    /** Position of the SequenceNumber in the extraction.*/
    private static final int POSITION_SEQUENCE_NUMBER = 3;
    /** Position of the ActorName in the extraction.*/
    private static final int POSITION_ACTOR_GUID = 4;
    /** Position of the Operation in the extraction.*/
    private static final int POSITION_OPERATION = 5;
    /** Position of the OperationDate in the extraction.*/
    private static final int POSITION_OPERATION_DATE = 6;
    /** Position of the AuditTrail in the extraction.*/
    private static final int POSITION_AUDIT_TRAIL = 7;
    /** Position of the Information in the extraction.*/
    private static final int POSITION_INFORMATION = 8;
    
    /** The model containing the elements for the restriction.*/
    private final ExtractModel model;
    /** The connection to the database.*/
    private final Connection dbConnection;
    
    /**
     * Constructor.
     * @param model The model for the restriction for the extraction from the database.
     * @param dbConnection The connection to the database, where the audit trails are to be extracted.
     */
    public AuditDatabaseExtractor(ExtractModel model, Connection dbConnection) {
        this.model = model;
        this.dbConnection = dbConnection;
    }
    
    /**
     * 
     * @return
     */
    public List<AuditTrailEvent> extractAuditEvents() {
        
        String sql = createSelectString() + " FROM " + AUDITTRAIL_TABLE + createRestriction();
        
        try {
            ResultSet result = null;
            List<AuditTrailEvent> res = new ArrayList<AuditTrailEvent>();
            
            try {
                log.info("Extracting sql '" + sql + "' with arguments '" + Arrays.asList(extractArgumentsFromModel()));
                result = DatabaseUtils.selectObject(dbConnection, sql, extractArgumentsFromModel());
                
                while(result.next()) {
                    res.add(extractEvent(result));
                }
            } finally {
                if(result != null) {
                    result.close();
                }
            }
            
            return res;
        } catch (Exception e) {
            throw new IllegalStateException("Could not retrieve the wanted data from the database.", e);
        }
    }
    
    /**
     * Extracts a single AuditEvent from a single result set.
     * @param resultSet The result set to extract the AuditEvent from.
     * @return The extracted AuditEvent.
     */
    private AuditTrailEvent extractEvent(ResultSet resultSet) throws SQLException {
        AuditTrailEvent event = new AuditTrailEvent();
        
        Long actorGuid = resultSet.getLong(POSITION_ACTOR_GUID);
        String actorName = retrieveActorName(actorGuid);
        
        Long fileGuid = resultSet.getLong(POSITION_FILE_GUID);
        String fileId = retrieveFileId(fileGuid);
        
        Long contributorGuid = resultSet.getLong(POSITION_CONTRIBUTOR_GUID);
        String contributorId = retrieveContributorId(contributorGuid);
        
        event.setActionDateTime(CalendarUtils.getXmlGregorianCalendar(resultSet.getDate(POSITION_OPERATION_DATE)));
        event.setActionOnFile(FileAction.fromValue(resultSet.getString(POSITION_OPERATION)));
        event.setAuditTrailInformation(resultSet.getString(POSITION_AUDIT_TRAIL));
        event.setActorOnFile(actorName);
        event.setFileID(fileId);
        event.setInfo(resultSet.getString(POSITION_INFORMATION));
        event.setReportingComponent(contributorId);
        event.setSequenceNumber(BigInteger.valueOf(resultSet.getLong(POSITION_SEQUENCE_NUMBER)));
        
        return event;
    }
    
    /**
     * @return Creates the SELECT string for the retrieval of the audit events.
     */
    private String createSelectString() {
        StringBuilder res = new StringBuilder();
        
        res.append("SELECT ");
        res.append(AUDITTRAIL_FILE_GUID + ", ");
        res.append(AUDITTRAIL_CONTRIBUTOR_GUID + ", ");
        res.append(AUDITTRAIL_SEQUENCE_NUMBER + ", ");
        res.append(AUDITTRAIL_ACTOR_GUID + ", ");
        res.append(AUDITTRAIL_OPERATION + ", ");
        res.append(AUDITTRAIL_OPERATION_DATE + ", ");
        res.append(AUDITTRAIL_AUDIT + ", ");
        res.append(AUDITTRAIL_INFORMATION + " ");
        
        return res.toString();
    }
    
    /**
     * Create the restriction part of the SQL statement for extracting the requested data from the database.
     * @return The restriction, or empty string if no restrictions.
     */
    private String createRestriction() {
        StringBuilder res = new StringBuilder();
        
        if(model.getFileId() != null) {
            nextArgument(res);
            res.append(AUDITTRAIL_FILE_GUID + " = ( SELECT " + FILE_GUID + " FROM " + FILE_TABLE + " WHERE " 
                    + FILE_FILEID + " = ? )");
        }
        
        if(model.getContributorId() != null) {
            nextArgument(res);
            res.append(AUDITTRAIL_CONTRIBUTOR_GUID + " = ( SELECT " + CONTRIBUTOR_GUID + " FROM " + CONTRIBUTOR_TABLE 
                    + " WHERE " + CONTRIBUTOR_ID + " = ? )");
        }
        
        if(model.getMinSeqNumber() != null) {
            nextArgument(res);
            res.append(AUDITTRAIL_SEQUENCE_NUMBER + " >= ?");
        }
        
        if(model.getMaxSeqNumber() != null) {
            nextArgument(res);
            res.append(AUDITTRAIL_SEQUENCE_NUMBER + " <= ?");
        }
        
        if(model.getActorName() != null) {
            nextArgument(res);
            res.append(AUDITTRAIL_ACTOR_GUID + " = ( SELECT " + ACTOR_GUID + " FROM " + ACTOR_TABLE + " WHERE " 
                    + ACTOR_NAME + " = ? )");
        }
        
        if(model.getOperation() != null) {
            nextArgument(res);
            res.append(AUDITTRAIL_OPERATION + " = ?");
        }
        
        if(model.getStartDate() != null) {
            nextArgument(res);
            res.append(AUDITTRAIL_OPERATION_DATE + " >= ?");
        }
        
        if(model.getEndDate() != null) {
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
            res.append(" WHERE ");
        }            
    }
    
    /**
     * @return The list of elements in the model which are not null.
     */
    private Object[] extractArgumentsFromModel() {
        List<Object> res = new ArrayList<Object>();
        
        if(model.getFileId() != null) {
            res.add(model.getFileId());
        }
        
        if(model.getContributorId() != null) {
            res.add(model.getContributorId());
        }
        
        if(model.getMinSeqNumber() != null) {
            res.add(model.getMinSeqNumber());
        }
        
        if(model.getMaxSeqNumber() != null) {
            res.add(model.getMaxSeqNumber());
        }
        
        if(model.getActorName() != null) {
            res.add(model.getActorName());
        }
        
        if(model.getOperation() != null) {
            res.add(model.getOperation().toString());
        }
        
        if(model.getStartDate() != null) {
            res.add(model.getStartDate());
        }
        
        if(model.getEndDate() != null) {
            res.add(model.getEndDate());
        }
        
        return res.toArray();
    }
    
    /**
     * Retrieves a id of a contributor based on the guid. 
     * @param contributorGuid The guid of the contributor.
     * @return The id of the contributor corresponding to guid.
     */
    private String retrieveContributorId(long contributorGuid) {
        String sqlRetrieve = "SELECT " + CONTRIBUTOR_ID + " FROM " + CONTRIBUTOR_TABLE + " WHERE " + CONTRIBUTOR_GUID 
                + " = ?";
        
        return DatabaseUtils.selectStringValue(dbConnection, sqlRetrieve, contributorGuid);        
    }
    
    /**
     * Retrieves a id of a file based on the guid. 
     * @param fileGuid The guid of the file.
     * @return The id of the file corresponding to guid.
     */
    private String retrieveFileId(long fileGuid) {
        String sqlRetrieve = "SELECT " + FILE_FILEID + " FROM " + FILE_TABLE + " WHERE " + FILE_GUID + " = ?";
        
        return DatabaseUtils.selectStringValue(dbConnection, sqlRetrieve, fileGuid);        
    }
    
    /**
     * Retrieves a name of an actor based on the guid. 
     * @param actorGuid The guid of the actor.
     * @return The name of the actor corresponding to guid.
     */
    private String retrieveActorName(long actorGuid) {
        String sqlRetrieve = "SELECT " + ACTOR_NAME + " FROM " + ACTOR_TABLE + " WHERE " + ACTOR_GUID + " = ?";
        
        return DatabaseUtils.selectStringValue(dbConnection, sqlRetrieve, actorGuid);        
    }
}
