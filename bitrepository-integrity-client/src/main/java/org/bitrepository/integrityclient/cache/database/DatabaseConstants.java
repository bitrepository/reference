/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.integrityclient.cache.database;

/**
 * Constants for the tables and columns in the database.
 */
public class DatabaseConstants {
    
    /** Private constructor to prevent instantiation of this utility class.*/
    private DatabaseConstants() { }
    
    // THE FILE INFO TABLE.
    /** The name of the File Info table.*/
    final static String FILE_INFO_TABLE = "fileinfo";
    /** The guid of an entry in the File Info table. Primary automatically generated key. A long/bigint value. */
    final static String FI_GUID = "guid";
    /** The guid of the file for an entry in the File Info table. A long/bigint value.*/
    final static String FI_FILE_GUID = "file_guid";
    /** The guid of the pillar for an entry in the File Info table. A long/bigint value.*/
    final static String FI_PILLAR_GUID = "pillar_guid";
    /** The guid of the checksum specification for an entry in the File Info table. A long/bigint value, may be null.
     *  This should always by the checksum specification of the latest checksum calculated.*/
    final static String FI_CHECKSUM_GUID = "checksum_guid";
    /** The checksum value of an entry in the File Info table. A String value, which may be null.*/
    final static String FI_CHECKSUM = "checksum";
    /** The timestamp for the file creation or latest modification. A date/timestamp value, may be null.*/
    final static String FI_LAST_FILE_UPDATE = "last_file_update";
    /** The timestamp for the latest checksum calculation. A date/timestamp value, may be null.*/
    final static String FI_LAST_CHECKSUM_UPDATE = "last_checksum_update";
    /** The state of the file. An integer value for the FileState enumerator. @see FileState.*/
    final static String FI_FILE_STATE = "file_state";
    /** The state of the checksum. An integer value for the ChecksumState enumerator. @see ChecksumState.*/
    final static String FI_CHECKSUM_STATE = "checksum_state";
    
    // THE FILES TABLE.
    /** The name of the Files table.*/
    final static String FILES_TABLE = "files";
    /** The guid of an entry in the files table. Primary automatically generated key. A long/bigint value.*/
    final static String FILES_GUID = "file_guid";
    /** The id of the file. String value.*/
    final static String FILES_ID = "file_id";
    /** The date, when the file has been entered into the database. A date/timestamp value.*/
    final static String FILES_CREATION_DATE = "creation_date";
    
    // THE PILLAR TABLE.
    /** The name of the pillar table.*/
    final static String PILLAR_TABLE = "pillar";
    /** The guid of an entry in the pillar table. Primary automatically generated key. A long/bigint value.*/
    final static String PILLAR_GUID = "pillar_guid";
    /** The id of the pillar. String value.*/
    final static String PILLAR_ID = "pillar_id";
    /** The guid of the checksum specification corresponding to a checksum pillar. A long/bigint value, may be null.*/
    final static String PILLAR_CHECKSUM_SPEC_GUID = "checksum_spec_guid";
    /** The timestamp for the latest file list update for the pillar entry. A date/timestamp value, may be null.*/
    final static String PILLAR_LAST_FILE_UPDATE = "last_file_update";
    /** The timestamp for the latest checksum update for the pillar entry. A date/timestamp value, may be null.*/
    final static String PILLAR_LAST_CHECKSUM_UPDATE = "last_checksum_update";
    
    // THE CHECKSUMSPEC TABLE
    /** The name of the checksum specification table.*/
    final static String CHECKSUM_TABLE = "checksumspec";
    /** The guid of an entry in the checksum table. Primary automatically generated key. A long/bigint value.*/
    final static String CHECKSUM_GUID = "checksum_guid";
    /** The name of the algorithm for the checksum specification. A string value.*/
    final static String CHECKSUM_ALGORITHM = "checksum_algorithm";
    /** The salt of the checksum specification. A string value, may be null.*/
    final static String CHECKSUM_SALT = "checksum_salt";
}
