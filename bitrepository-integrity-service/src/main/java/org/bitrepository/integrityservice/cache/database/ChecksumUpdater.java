package org.bitrepository.integrityservice.cache.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle the update of fileIDs information in the integrity database. 
 * Does this by batching of sql statements for performance.  
 * 
 *  Implementation detail: Postgres will first feature 'upsert' functionality in version 9.5. 
 *  This means that we currently can't use the functionality, and is forced
 *  to use the two call way. I.e. the conditional update, the conditional insert 
 *  
 *  When updating checksums, if a file was not known by the database it is inserted and the 
 *  timestamp for the file is set to the checksum timestamp. 
 *  This should be no problem since 1) it will get overwritten the next time the files is reported
 *  and 2) the pillar calculated a checksum meaning that at that time the pillar must have had the 
 *  file.  
 */
public class ChecksumUpdater {

    /**
     * SQL for conditional insert of the fileID in the files table.
     * The insert is only performed when the (file_id, collection_key) 
     * tuple is not already found in the database  
     */
    private final String insertFileInfoWithChecksumSql = "INSERT INTO fileinfo ("
            + " collectionID, pillarID, fileID, file_timestamp, last_seen_getfileids,"
            + " checksum, checksum_timestamp, last_seen_getchecksums)"
            + " (SELECT collectionID, ?, ?, ?, CURRENT_TIMESTAMP, ?, ?, CURRENT_TIMESTAMP FROM collections"
                + " WHERE collectionID = ?"
                + " AND NOT EXISTS ("
                    + " SELECT * FROM fileinfo "
                    + " WHERE fileID = ?"
                    + " AND collectionID = ?"
                    + " AND pillarID = ?))";

    private final String updateChecksumSql = "UPDATE fileinfo "
            + "	SET checksum = ?,"
            + " checksum_timestamp = ?,"
            + " last_seen_getchecksums = CURRENT_TIMESTAMP"
            + " WHERE fileID = ?"
            + "	AND collectionID = ?"
            + " AND pillarID = ?";	

    private final String insertLatestChecksumTime = "INSERT INTO collection_progress "
            + "(collectionID, pillarID, latest_checksum_timestamp)"
            + " ( SELECT collectionID, ?, ? FROM collections"
                + " WHERE collectionID = ?"
                + " AND NOT EXISTS ("
                    + " SELECT * FROM collection_progress"
                    + " WHERE collectionID = ?"
                    + " AND pillarID = ?))";

    private final String updateLatestChecksumTime = "UPDATE collection_progress"
            + " SET latest_checksum_timestamp = ? "
            + " WHERE collectionID = ?"
            + " AND pillarID = ?";

    private Logger log = LoggerFactory.getLogger(getClass());

    private final String collectionID;
    private final String pillar;
    private final Connection conn;
    private PreparedStatement insertFileInfoPS;
    private PreparedStatement updateChecksumPS;
    private PreparedStatement insertLatestChecksumTimePS;
    private PreparedStatement updateLatestChecksumTimePS;

    public ChecksumUpdater(String pillar, Connection dbConnection, String collectionID) {
        this.collectionID = collectionID;
        this.pillar = pillar;
        conn = dbConnection;
    }

    private void init() throws SQLException {
        conn.setAutoCommit(false);
        insertFileInfoPS = conn.prepareStatement(insertFileInfoWithChecksumSql);
        updateChecksumPS = conn.prepareStatement(updateChecksumSql);
        insertLatestChecksumTimePS = conn.prepareStatement(insertLatestChecksumTime);
        updateLatestChecksumTimePS = conn.prepareStatement(updateLatestChecksumTime);
    }

    /**
     * Method to handle the actual update.  
     * @param data The date to update the database with
     */
    public void updateChecksums(List<ChecksumDataForChecksumSpecTYPE> data) {
        ArgumentValidator.checkNotNull(data, "data");
        try {
            init();
            log.debug("Initialized checksumUpdater");
            try {
                Date maxDate = new Date(0);
                for(ChecksumDataForChecksumSpecTYPE csData : data) {
                    updateChecksum(csData);
                    addFileInfoWithChecksum(csData);
                    maxDate = TimeUtils.getMaxDate(maxDate, 
                            CalendarUtils.convertFromXMLGregorianCalendar(csData.getCalculationTimestamp()));                	
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

    private void addFileInfoWithChecksum(ChecksumDataForChecksumSpecTYPE item) throws SQLException {
        Timestamp calculationTime = new Timestamp(
                CalendarUtils.convertFromXMLGregorianCalendar(item.getCalculationTimestamp()).getTime());

        insertFileInfoPS.setString(1, pillar);
        insertFileInfoPS.setString(2, item.getFileID());
        insertFileInfoPS.setTimestamp(3, calculationTime);
        insertFileInfoPS.setString(4, Base16Utils.decodeBase16(item.getChecksumValue()));
        insertFileInfoPS.setTimestamp(5, calculationTime);
        insertFileInfoPS.setString(6, collectionID);
        insertFileInfoPS.setString(7, item.getFileID());
        insertFileInfoPS.setString(8, collectionID);
        insertFileInfoPS.setString(9, pillar);
        insertFileInfoPS.addBatch();
    }

    private void updateChecksum(ChecksumDataForChecksumSpecTYPE item) throws SQLException {
        Timestamp calculationTime = new Timestamp(
                CalendarUtils.convertFromXMLGregorianCalendar(item.getCalculationTimestamp()).getTime());

        updateChecksumPS.setString(1, Base16Utils.decodeBase16(item.getChecksumValue()));
        updateChecksumPS.setTimestamp(2, calculationTime);
        updateChecksumPS.setString(3, item.getFileID());
        updateChecksumPS.setString(4, collectionID);
        updateChecksumPS.setString(5, pillar);
        updateChecksumPS.addBatch();
    }

    private void updateMaxTime(Date maxDate) throws SQLException {
        updateLatestChecksumTimePS.setTimestamp(1, new Timestamp(maxDate.getTime()));
        updateLatestChecksumTimePS.setString(2, collectionID);
        updateLatestChecksumTimePS.setString(3, pillar);

        insertLatestChecksumTimePS.setString(1, pillar);
        insertLatestChecksumTimePS.setTimestamp(2, new Timestamp(maxDate.getTime()));
        insertLatestChecksumTimePS.setString(3, collectionID);
        insertLatestChecksumTimePS.setString(4, collectionID);
        insertLatestChecksumTimePS.setString(5, pillar);
    }

    private void execute() throws SQLException {
        updateChecksumPS.executeBatch();
        insertFileInfoPS.executeBatch();
        updateLatestChecksumTimePS.execute();
        insertLatestChecksumTimePS.execute();
        conn.commit();
    }

    private void close() throws SQLException {
        if(updateChecksumPS != null) {
            updateChecksumPS.close();
        }
        if(insertFileInfoPS != null) {
            insertFileInfoPS.close();
        }
        if(conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }
}