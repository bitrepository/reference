package org.bitrepository.pillar.cache.database;

import static org.bitrepository.pillar.cache.database.DatabaseConstants.CHECKSUM_TABLE;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_FILE_ID;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_CHECKSUM;
import static org.bitrepository.pillar.cache.database.DatabaseConstants.CS_DATE;

import java.util.Date;

import org.bitrepository.common.database.DBConnector;
import org.bitrepository.common.database.DatabaseUtils;

/**
 * Ingests data to the checksum database. And also deals with the deletion of entries.
 */
public class ChecksumIngestor {
    /** The connector for the database.*/
    private final DBConnector connector;
    
    /**
     * Constructor.
     * @param connector The connector for the database.
     */
    public ChecksumIngestor(DBConnector connector) {
        this.connector = connector;
    }
    
    /**
     * Inserts a new entry into the database.
     * @param fileId The id of the file for the entry.
     * @param checksum The checksum of the file.
     * @param date The calculation timestamp for the file.
     */
    public void insertNewEntry(String fileId, String checksum, Date date) {
        String sql = "INSERT INTO " + CHECKSUM_TABLE + " ( " + CS_FILE_ID + " , " + CS_CHECKSUM + " , " + CS_DATE 
                + " ) VALUES ( ? , ? , ? )";
        DatabaseUtils.executeStatement(connector, sql, fileId, checksum, date);
    }
    
    /**
     * Updates an existing entry in the database.
     * @param fileId The id of the file to update.
     * @param checksum The new checksum for the file.
     * @param date The new date for the calculation of the checksum of the file.
     */
    public void updateEntry(String fileId, String checksum, Date date) {
        String sql = "UPDATE " + CHECKSUM_TABLE + " SET " + CS_CHECKSUM + " = ? , " + CS_DATE + " = ? WHERE " 
                + CS_FILE_ID + " = ?";
        DatabaseUtils.executeStatement(connector, sql, checksum, date, fileId);
    }
    
    /**
     * Removes an entry from the database.
     * @param fileId The id of the file whose entry should be removed.
     */
    public void removeEntry(String fileId) {
        String sql = "DELETE FROM " + CHECKSUM_TABLE + " WHERE " + CS_FILE_ID + " = ?";
        DatabaseUtils.executeStatement(connector, sql, fileId);
    }
}
