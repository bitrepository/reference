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
    /** The name of the information field in the audit trail table. */
    public final static String AUDITTRAIL_OPERATIONID = "operationID";
    /** The name of the information field in the audit trail table. */
    public final static String AUDITTRAIL_FINGERPRINT = "fingerprint";
    /** The name of the audit field in the audit trail table. */
    public final static String AUDITTRAIL_AUDIT = "audit";    
    
    /** The name of the file table.*/
    public final static String FILE_TABLE = "file";
    /** The name of the file guid field in the file table.*/
    public final static String FILE_GUID = "file_guid";
    /** The name of the fileid field in the file table.*/
    public final static String FILE_FILEID = "fileid";
    /** The name of the collectionid field in the file table.*/
    public final static String FILE_COLLECTIONID = "collectionid";
    
    /** The name of the actor table.*/
    public final static String ACTOR_TABLE = "actor";
    /** The name of the actor guid field in the actor table.*/
    public final static String ACTOR_GUID = "actor_guid";
    /** The name of the actor name field in the actor table.*/
    public final static String ACTOR_NAME = "actor_name";
    
    /** The name of the version table entry for the database.*/
    public final static String DATABASE_VERSION_ENTRY = "auditcontributordb";
}
