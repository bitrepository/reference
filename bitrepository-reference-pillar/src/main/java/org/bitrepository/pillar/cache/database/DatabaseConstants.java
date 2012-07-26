package org.bitrepository.pillar.cache.database;

/**
 * Container for the constants for the checksum database.
 */
public class DatabaseConstants {
    /** Private constructor to prevent instantiation of this constant container.*/
    private DatabaseConstants() {}
    
    /** The table with the checksum entries.*/
    public static final String CHECKSUM_TABLE = "checksums";
    /** The column for the id of file.*/
    public static final String CS_FILE_ID = "fileid";
    /** The column for the checksum of the file.*/
    public static final String CS_CHECKSUM = "checksum";
    /** The column for the date for the calculation of the checksum.*/
    public static final String CS_DATE = "calculationdate";
    
    /** The column for the guid of the entry.*/
    public static final String CS_GUID = "guid";
    
}
