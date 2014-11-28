package org.bitrepository.integrityservice.cache.database;

import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.COLLECTION_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_CREATION_DATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_ID;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILES_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FILE_INFO_TABLE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_CHECKSUM_STATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_SIZE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_FILE_STATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_LAST_CHECKSUM_UPDATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_LAST_FILE_UPDATE;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.FI_PILLAR_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_ID;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_KEY;
import static org.bitrepository.integrityservice.cache.database.DatabaseConstants.PILLAR_TABLE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle the update of fileIDs information in the integrity database. 
 * Does this by batching of sql statements for performance.  
 */
public class FileUpdater {

    /**
     * SQL for conditional insert of the fileID in the files table.
     * The insert is only performed when the (file_id, collection_key) 
     * tuple is not already found in the database  
     */
    private final String insertFileSql = "INSERT INTO " + FILES_TABLE + " ( "
            + FILES_ID + ", " + FILES_CREATION_DATE + ", " + COLLECTION_KEY + " )"
            + " (SELECT ?, ?, ?"
            + " FROM " + FILES_TABLE
            + " WHERE " + COLLECTION_KEY + " = ?"
            + " AND " + FILES_ID + " = ?"
            + " HAVING count(*) = 0 )";
    
    /**
     * SQL for conditional insert of the file information in the fileinfo table.
     * The insert is only performed when no fileinfo for the given pillar exists in the table  
     */
    private final String insertFileInfoSql = "INSERT INTO " + FILE_INFO_TABLE
            + " ( " + FI_FILE_KEY + ", " + FI_PILLAR_KEY + ", " + FI_CHECKSUM_STATE + ", " 
            + FI_LAST_CHECKSUM_UPDATE + ", " + FI_FILE_STATE + ", " + FI_LAST_FILE_UPDATE + " )" 
            + " (SELECT " 
                + "(SELECT " + FILES_KEY 
                + " FROM " + FILES_TABLE 
                + " WHERE " + FILES_ID + " = ?"
                + " AND " + COLLECTION_KEY + " = ? ), "
                + "(SELECT " + PILLAR_KEY 
                + " FROM " + PILLAR_TABLE 
                + " WHERE " + PILLAR_ID + " = ? )," 
                + " ?, ?, ?, ?"
            + " FROM " + FILE_INFO_TABLE
            + " JOIN " + FILES_TABLE
            + " ON " + FILE_INFO_TABLE + "." + FI_FILE_KEY + " = " + FILES_TABLE + "." + FILES_KEY
            + " JOIN " + PILLAR_TABLE
            + " ON " + FILE_INFO_TABLE + "." + FI_PILLAR_KEY + " = " + PILLAR_TABLE + "." + PILLAR_KEY
            + " WHERE " + FILES_ID + " = ?"
            + " AND " + COLLECTION_KEY + " = ?"
            + " AND " + PILLAR_ID + " = ?"
            + " HAVING count(*) = 0 )";
    
    /**
     * Update statements for filesize and file state fields.  
     */
    private final String updateFileInfoSql = "UPDATE " + FILE_INFO_TABLE 
            + " SET " + FI_FILE_SIZE + " = ?, "
                      + FI_FILE_STATE + " = ?"
            + " WHERE " + FI_FILE_KEY + " = ("
                + " SELECT " + FILES_KEY 
                + " FROM " + FILES_TABLE
                + " WHERE " + FILES_ID + " = ?"
                + " AND " + COLLECTION_KEY + " = ?)"
            + " AND " + FI_PILLAR_KEY + " = ("
                + " SELECT " + PILLAR_KEY
                + " FROM " + PILLAR_TABLE
                + " WHERE " + PILLAR_ID + " = ?)";
    
    /**
     * Conditional update statement for last file update and checksum state fields.
     * The condition is that the updated information needs to be newer than that already 
     * registered in the database. 
     */
    private final String updateFileExistanceSql = "UPDATE " + FILE_INFO_TABLE 
            + " SET " + FI_LAST_FILE_UPDATE + " = ?, "
                      + FI_CHECKSUM_STATE + " = ? "
            + " WHERE " + FI_FILE_KEY + " = ("
                + " SELECT " + FILES_KEY 
                + " FROM " + FILES_TABLE
                + " WHERE " + FILES_ID + " = ?"
                + " AND " + COLLECTION_KEY + " = ?)"
            + " AND " + FI_PILLAR_KEY + " = ("
                + " SELECT " + PILLAR_KEY
                + " FROM " + PILLAR_TABLE
                + " WHERE " + PILLAR_ID + " = ?)"
            + " AND " + FI_LAST_FILE_UPDATE + " < ?";

    private Logger log = LoggerFactory.getLogger(getClass());
    
    private final List<String> pillars;
    private final Long collectionKey;
    private final String pillar;
    private final Connection conn;
    private PreparedStatement filePS;
    private PreparedStatement fileInfoPS;
    private PreparedStatement updateFileInfoPS;
    private PreparedStatement updateFileExistancePS;
    
    public FileUpdater(List<String> pillars, Connection dbConnection, Long collectionKey,
            String pillar) {
        this.pillars = pillars;
        this.collectionKey = collectionKey;
        this.pillar = pillar;
        conn = dbConnection;
    }
    
    private void init() throws SQLException {
        conn.setAutoCommit(false);
        filePS = conn.prepareStatement(insertFileSql);
        fileInfoPS = conn.prepareStatement(insertFileInfoSql);
        updateFileInfoPS = conn.prepareStatement(updateFileInfoSql);
        updateFileExistancePS = conn.prepareStatement(updateFileExistanceSql);
    }
    
    /**
     * Method to handle the actual update.  
     */
    public void updateFiles(FileIDsDataItems dataItems) {
        try {
            init();
            log.debug("Initialized fileUpdater");
            try {
                for(FileIDsDataItem item : dataItems.getFileIDsDataItem()) {
                    addFile(item);
                    addFileInfo(item);
                    addFileInfoUpdate(item);
                    updateFileExistance(item);
                }
                log.debug("Done building file update batch");
                execute();
                log.debug("Done executing file update batch");
            } finally {
                close();
            }
        } catch (SQLException e) {
            log.error("Failed to update files", e);
        }
        
    } 
    
    private void addFile(FileIDsDataItem item) throws SQLException {
        filePS.setString(1, item.getFileID());
        Timestamp ts = new Timestamp(
                CalendarUtils.convertFromXMLGregorianCalendar(item.getLastModificationTime()).getTime());
        filePS.setTimestamp(2, ts);
        filePS.setLong(3, collectionKey);
        filePS.setLong(4, collectionKey);
        filePS.setString(5, item.getFileID());
        filePS.addBatch();
    }
    
    private void addFileInfo(FileIDsDataItem item) throws SQLException {
        Date epoch = new Date(0);
        for(String pillar : pillars) {
            fileInfoPS.setString(1, item.getFileID());
            fileInfoPS.setLong(2, collectionKey);
            fileInfoPS.setString(3, pillar);
            fileInfoPS.setInt(4, ChecksumState.MISSING.ordinal());
            fileInfoPS.setTimestamp(5, new Timestamp(epoch.getTime()));
            fileInfoPS.setInt(6, FileState.UNKNOWN.ordinal());
            fileInfoPS.setTimestamp(7, new Timestamp(epoch.getTime()));
            fileInfoPS.setString(8, item.getFileID());
            fileInfoPS.setLong(9, collectionKey);
            fileInfoPS.setString(10, pillar);
            fileInfoPS.addBatch();
        }
    }
    
    private void addFileInfoUpdate(FileIDsDataItem item) throws SQLException {
        if(item.getFileSize() == null) {
            updateFileInfoPS.setNull(1, Types.BIGINT);
        } else {
            updateFileInfoPS.setLong(1, item.getFileSize().longValue());
        }
        updateFileInfoPS.setInt(2, FileState.EXISTING.ordinal());
        updateFileInfoPS.setString(3, item.getFileID());
        updateFileInfoPS.setLong(4, collectionKey);
        updateFileInfoPS.setString(5, pillar);
        updateFileInfoPS.addBatch();
    }
    
    private void updateFileExistance(FileIDsDataItem item) throws SQLException {
        Timestamp ts = new Timestamp(
                CalendarUtils.convertFromXMLGregorianCalendar(item.getLastModificationTime()).getTime());
        updateFileExistancePS.setTimestamp(1, ts);
        updateFileExistancePS.setInt(2, ChecksumState.UNKNOWN.ordinal());
        updateFileExistancePS.setString(3, item.getFileID());
        updateFileExistancePS.setLong(4, collectionKey);
        updateFileExistancePS.setString(5, pillar);
        updateFileExistancePS.setTimestamp(6, ts);
        updateFileExistancePS.addBatch();
    }
    
    private void execute() throws SQLException {
        filePS.executeBatch();
        fileInfoPS.executeBatch();
        updateFileInfoPS.executeBatch();
        updateFileExistancePS.executeBatch();
        conn.commit();
    }
    
    private void close() throws SQLException {
        if(filePS != null) {
            filePS.close();
        }
        if(fileInfoPS != null) {
            fileInfoPS.close();
        }
        if(updateFileInfoPS != null) {
            updateFileInfoPS.close();
        }
        if(updateFileExistancePS != null) {
            updateFileExistancePS.close();
        }
        if(conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}
