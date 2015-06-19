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
package org.bitrepository.integrityservice.cache.database;

/**
 * Constants for the tables and columns in the database.
 */
public final class DatabaseConstants {
    
    /** Private constructor to prevent instantiation of this utility class.*/
    private DatabaseConstants() { }
    
    // THE FILE INFO TABLE.
    /** The name of the File Info table.*/
    public static final String FILE_INFO_TABLE = "fileinfo";
    /** The key of an entry in the File Info table. Primary automatically generated key. A long/bigint value. */
    public static final String FI_KEY = "fileinfo_key";
    /** The key of the file for an entry in the File Info table. A long/bigint value.*/
    public static final String FI_FILE_KEY = "file_key";
    /** The key of the pillar for an entry in the File Info table. A long/bigint value.*/
    public static final String FI_PILLAR_KEY = "pillar_key";
    /** The checksum value of an entry in the File Info table. A String value, which may be null.*/
    public static final  String FI_CHECKSUM = "checksum";
    /** The timestamp for the file creation or latest modification. A date/timestamp value, may be null.*/
    public static final  String FI_LAST_FILE_UPDATE = "last_file_update";
    /** The timestamp for the latest checksum calculation. A date/timestamp value, may be null.*/
    public static final  String FI_LAST_CHECKSUM_UPDATE = "last_checksum_update";
    /** The state of the file. An integer value for the FileState enumerator. @see FileState.*/
    public static final  String FI_FILE_STATE = "file_state";
    /** The state of the checksum. An integer value for the ChecksumState enumerator. @see ChecksumState.*/
    public static final  String FI_CHECKSUM_STATE = "checksum_state";
    /** The size of the file. An integer value, may be null */
    public static final String FI_FILE_SIZE = "file_size";
    
    // THE FILES TABLE.
    /** The name of the Files table.*/
    public static final  String FILES_TABLE = "files";
    /** The key of an entry in the files table. Primary automatically generated key. A long/bigint value.*/
    public static final  String FILES_KEY = "file_key";
    /** The id of the file. String value.*/
    public static final  String FILES_ID = "file_id";
    /** The date, when the file has been entered into the database. A date/timestamp value.*/
    public static final  String FILES_CREATION_DATE = "creation_date";
    
    // THE PILLAR TABLE.
    /** The name of the pillar table.*/
    public static final  String PILLAR_TABLE = "pillar";
    /** The key of an entry in the pillar table. Primary automatically generated key. A long/bigint value.*/
    public static final  String PILLAR_KEY = "pillar_key";
    /** The id of the pillar. String value.*/
    public static final  String PILLAR_ID = "pillarID";
    
    // THE COLLECTIONS TABLE
    /** The name of the collections table. */
    public static final String COLLECTIONS_TABLE = "collections";
    /** The key of an entry in the collections table. Primary automatically generated key. A long/bigint value */
    public static final String COLLECTION_KEY = "collection_key";
    /** The id of the collection. String value. */
    public static final String COLLECTION_ID = "collectionID";
    
    // THE STATS TABLE
    /** The name of the statistics table. A long/bigint value. */
    public static final String STATS_TABLE = "stats";
    /** The key for an entry in the statistics table. Primary automatically generated key. A long/bigint value */
    public static final String STATS_KEY = "stat_key";
    /** The time that the statistics were created. A date/timestamp*/
    public static final String STATS_TIME = "stat_time";
    /** The time the statistics were last updated. A date/timestamp, may be null*/
    public static final String STATS_LAST_UPDATE = "last_update";
    /** The key for the collection that the statistics belong to.  A long/bigint value.*/
    public static final String STATS_COLLECTION_KEY = "collection_key";
    
    // THE COLLETION STATISTICS TABLE
    /** The name of the collection statistics table */
    public static final String COLLECTION_STATS_TABLE = "collectionstats";
    /** The key for an entry in the collection statistics table. Primary automatically generated key. A long/bigint value */
    public static final String CS_KEY = "collectionstat_key";
    /** Foreign key for referring collection statistics entry to statistics entry. A long/bigint value */
    public static final String CS_STAT_KEY = "stat_key";
    /** The value of the total number of files in the collection. A long/bigint value. */
    public static final String CS_FILECOUNT = "file_count";
    /** The value of the summed size of the files in the collection. A long/bigint value. */
    public static final String CS_FILESIZE = "file_size";
    /** The number of checksum errors in the collection. A long/bigint value. */
    public static final String CS_CHECKSUM_ERRORS = "checksum_errors_count";
    
    // THE PILLAR STATISTICS TABLE
    /** The name of the pillar statistics table */
    public static final String PILLAR_STATS_TABLE = "pillarstats";
    /** The key for an entry in the pillar statistics table. Primary automatically generated key. A long/bigint value */
    public static final String PS_KEY = "pillarstat_key";
    /** Foreign key for referring pillar statistics entry to statistics entry. A long/bigint value */
    public static final String PS_STAT_KEY = "stat_key"; 
    /** Foreign key for referring pillar statistics entry to the pillar. A long/bigint value */
    public static final String PS_PILLAR_KEY = "pillar_key";
    /** The value of the total number of files on the pillar in the collection. A long/bigint value. */
    public static final String PS_FILE_COUNT = "file_count";
    /** The value of the summed size of the files on the pillar in the collection. A long/bigint value. */
    public static final String PS_FILE_SIZE = "file_size";
    /** The number of files missing on the pillar. A long/bigint value. */
    public static final String PS_MISSING_FILES_COUNT = "missing_files_count";
    /** The number of files with checksum errors on the pillar in the collection. A long/bigint value. */
    public static final String PS_CHECKSUM_ERRORS = "checksum_errors_count";
    
    /** The name of the version table entry for the database.*/
    public final static String DATABASE_VERSION_ENTRY = "integritydb";
    /** The name of the version entry for the fileinfo table.*/
    public final static String FILEINFO_TABLE_VERSION_ENTRY = "fileinfo";
    
}

