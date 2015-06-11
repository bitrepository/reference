package org.bitrepository.integrityservice.cache.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle the update of fileIDs information in the integrity database. 
 * Does this by batching of sql statements for performance.  
 * 
 *  Implementation detail: Postgres will first feature 'upsert' functionality in version 9.5. 
 *  This means that we currently can't use the functionality, and is forced
 *  to use the two call way. I.e. the conditional update, the conditional insert 
 */
public class FileUpdater2 {

    /**
     * SQL for conditional insert of the fileID in the files table.
     * The insert is only performed when the (file_id, collection_key) 
     * tuple is not already found in the database  
     */
    private final String insertFileInfoSql = "INSERT INTO fileinfo ("
    		+ " fileID, collectionID, pillarID, filesize, file_timestamp, last_seen_getfileids)"
    		+ " (SELECT ?, ?, ?, ?, ?, NOW()"
    		+ " WHERE NOT EXISTS ("
    			+ " SELECT * FROM fileinfo "
    			+ " WHERE fileID = ?"
    			+ " AND collectionID = ?"
    			+ " AND pillarID = ?))";
    
    private final String updateFileInfoSql = "UPDATE fileinfo "
    		+ "	SET filesize = ?,"
    		+ " file_timestamp = ?,"
    		+ " last_seen_getfileids = NOW()"
    		+ " WHERE fileID = ?"
    		+ "	AND collectionID = ?"
    		+ " AND pillarID = ?";	
    
    private final String insertLatestFileTime = "INSERT INTO collection_progress "
            + "(collectionID, pillarID, latest_file_timestamp)"
            + " ( SELECT ?, ?, ? "
                + " WHERE NOT EXISTS ( SELECT * FROM collection_progress"
                    + " WHERE collectionID = ?"
                    + " AND pillarID = ?))";

    private final String updateLatestFileTime = "UPDATE collection_progress"
            + " SET latest_file_timestamp = ? "
            + " WHERE collectionID = ?"
            + " AND pillarID = ?";

    private Logger log = LoggerFactory.getLogger(getClass());
    
    private final String collectionID;
    private final String pillar;
    private final Connection conn;
    private PreparedStatement insertFileInfoPS;
    private PreparedStatement updateFileInfoPS;
    private PreparedStatement insertLatestFileTimePS;
    private PreparedStatement updateLatestFileTimePS;
    
    public FileUpdater2(String pillar, Connection dbConnection, String collectionID) {
        this.collectionID = collectionID;
        this.pillar = pillar;
        conn = dbConnection;
    }
    
    private void init() throws SQLException {
        conn.setAutoCommit(false);
        insertFileInfoPS = conn.prepareStatement(insertFileInfoSql);
        updateFileInfoPS = conn.prepareStatement(updateFileInfoSql);
        insertLatestFileTimePS = conn.prepareStatement(insertLatestFileTime);
        updateLatestFileTimePS = conn.prepareStatement(updateLatestFileTime);
    }
    
    /**
     * Method to handle the actual update.  
     */
    public void updateFiles(FileIDsDataItems dataItems) {
        try {
            init();
            log.debug("Initialized fileUpdater");
            try {
                Date maxDate = new Date(0);
                for(FileIDsDataItem item : dataItems.getFileIDsDataItem()) {
                	updateFileInfo(item);
                	addFileInfo(item);
                	maxDate = getMaxDate(maxDate, 
                	        CalendarUtils.convertFromXMLGregorianCalendar(item.getLastModificationTime()));
                }
                updateMaxTime(maxDate);
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
    
    private void addFileInfo(FileIDsDataItem item) throws SQLException {
    	insertFileInfoPS.setString(1, item.getFileID());
    	insertFileInfoPS.setString(2, collectionID);
    	insertFileInfoPS.setString(3, pillar);
        if(item.getFileSize() == null) {
        	insertFileInfoPS.setNull(4, Types.BIGINT);
        } else {
        	insertFileInfoPS.setLong(4, item.getFileSize().longValue());
        }
        Timestamp ts = new Timestamp(
                CalendarUtils.convertFromXMLGregorianCalendar(item.getLastModificationTime()).getTime());
        insertFileInfoPS.setTimestamp(5, ts);
        insertFileInfoPS.setString(6, item.getFileID());
    	insertFileInfoPS.setString(7, collectionID);
    	insertFileInfoPS.setString(8, pillar);
    	insertFileInfoPS.addBatch();
    }
    
    private void updateFileInfo(FileIDsDataItem item) throws SQLException {
        if(item.getFileSize() == null) {
            updateFileInfoPS.setNull(1, Types.BIGINT);
        } else {
            updateFileInfoPS.setLong(1, item.getFileSize().longValue());
        }
        Timestamp ts = new Timestamp(
                CalendarUtils.convertFromXMLGregorianCalendar(item.getLastModificationTime()).getTime());
        updateFileInfoPS.setTimestamp(2, ts);
        updateFileInfoPS.setString(3, item.getFileID());
        updateFileInfoPS.setString(4, collectionID);
        updateFileInfoPS.setString(5, pillar);
        updateFileInfoPS.addBatch();
    }
    
    private void updateMaxTime(Date maxDate) throws SQLException {
        updateLatestFileTimePS.setTimestamp(1, new Timestamp(maxDate.getTime()));
        updateLatestFileTimePS.setString(2, collectionID);
        updateLatestFileTimePS.setString(3, pillar);
        
        insertLatestFileTimePS.setString(1, collectionID);
        insertLatestFileTimePS.setString(2, pillar);
        insertLatestFileTimePS.setTimestamp(3, new Timestamp(maxDate.getTime()));
        insertLatestFileTimePS.setString(4, collectionID);
        insertLatestFileTimePS.setString(5, pillar);
    }
    
    private void execute() throws SQLException {
        updateFileInfoPS.executeBatch();
        insertFileInfoPS.executeBatch();
        updateLatestFileTimePS.execute();
        insertLatestFileTimePS.execute();
        conn.commit();
    }
    
    private void close() throws SQLException {
        if(updateFileInfoPS != null) {
            updateFileInfoPS.close();
        }
        if(insertFileInfoPS != null) {
        	insertFileInfoPS.close();
        }
        if(conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
    
    private Date getMaxDate(Date currentMax, Date itemDate) {
        if(itemDate.after(currentMax)) {
            return itemDate;
        } else {
            return currentMax;
        }
    }
}
