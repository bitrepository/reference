package org.bitrepository.pillar.audit;

/**
 * Container for the constants for the audit trail database.
 * All the names of the tables and the fields in these tables.
 */
public final class AuditDatabaseConstants {
    
    /** Private constructor to prevent instantiation of this constants class.*/
    private AuditDatabaseConstants() { }
    
    /** The name of the audit trail table.*/
    public final static String AUDITTRAIL_TABLE = "audittrail";
    /** The name of the sequence number field in the audit trail table. */
    public final static String AUDITTRAIL_SEQUENCE_NUMBER = "sequence_number";
    /** The name of the file guid field in the audit trail table. */
    public final static String AUDITTRAIL_FILE_GUID = "file_guid";
    /** The name of the actor guid field in the audit trail table. */
    public final static String AUDITTRAIL_ACTOR_GUID = "actor_guid";
    /** The name of the operation field in the audit trail table. */
    public final static String AUDITTRAIL_OPERATION = "operation";
    /** The name of the operation date field in the audit trail table. */
    public final static String AUDITTRAIL_OPERATION_DATE = "operation_date";
    /** The name of the information field in the audit trail table. */
    public final static String AUDITTRAIL_INFORMATION = "information";
    /** The name of the audit field in the audit trail table. */
    public final static String AUDITTRAIL_AUDIT = "audit";    
    
    /** The name of the file table.*/
    public final static String FILE_TABLE = "file";
    /** The name of the file guid field in the file table.*/
    public final static String FILE_GUID = "file_guid";
    /** The name of the fileid field in the file table.*/
    public final static String FILE_FILEID = "fileid";
    
    /** The name of the actor table.*/
    public final static String ACTOR_TABLE = "actor";
    /** The name of the actor guid field in the actor table.*/
    public final static String ACTOR_GUID = "actor_guid";
    /** The name of the actor name field in the actor table.*/
    public final static String ACTOR_NAME = "actor_name";
}
