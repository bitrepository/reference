/*
 * #%L
 * Bitrepository Integrity Service
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

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.FileInfosDataItem;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle the update of FileInfo information in the integrity database. 
 * Does this by batching of sql statements for performance.  
 * 
 *  Implementation detail: Postgres will first feature 'upsert' functionality in version 9.5. 
 *  This means that we currently can't use the functionality, and is forced
 *  to use the two call way. I.e. the conditional update, the conditional insert 
 *  
 */
public class FileInfoUpdater {

    /**
     * SQL for conditional insert of the fileID in the files table.
     * The insert is only performed when the (file_id, collection_key) 
     * tuple is not already found in the database  
     */
    private final String insertFileInfoSql = "INSERT INTO fileinfo ("
            + " collectionID, pillarID, fileID, file_timestamp, last_seen_getfileids,"
            + " checksum, checksum_timestamp, last_seen_getchecksums)"
            + " (SELECT collectionID, ?, ?, ?, ?, ?, ?, ? FROM collections"
                + " WHERE collectionID = ?"
                + " AND NOT EXISTS ("
                    + " SELECT * FROM fileinfo "
                    + " WHERE fileID = ?"
                    + " AND collectionID = ?"
                    + " AND pillarID = ?))";

    private final String updateFileInfoSql = "UPDATE fileinfo "
            + "	SET checksum = ?,"
            + " file_timestamp = ?,"
            + " last_seen_getfileids = ?,"
            + " checksum_timestamp = ?,"
            + " last_seen_getchecksums = ?"
            + " WHERE fileID = ?"
            + "	AND collectionID = ?"
            + " AND pillarID = ?";	

    private final String insertLatestFileInfoTime = "INSERT INTO collection_progress "
            + "(collectionID, pillarID, latest_file_timestamp, latest_checksum_timestamp)"
            + " ( SELECT collectionID, ?, ?, ? FROM collections"
                + " WHERE collectionID = ?"
                + " AND NOT EXISTS ("
                    + " SELECT * FROM collection_progress"
                    + " WHERE collectionID = ?"
                    + " AND pillarID = ?))";

    private final String updateLatestFileInfoTime = "UPDATE collection_progress"
            + " SET latest_file_timestamp = ?, latest_checksum_timestamp = ? "
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

    public FileInfoUpdater(String pillar, Connection dbConnection, String collectionID) {
        this.collectionID = collectionID;
        this.pillar = pillar;
        conn = dbConnection;
    }

    private void init() throws SQLException {
        conn.setAutoCommit(false);
        insertFileInfoPS = conn.prepareStatement(insertFileInfoSql);
        updateChecksumPS = conn.prepareStatement(updateFileInfoSql);
        insertLatestChecksumTimePS = conn.prepareStatement(insertLatestFileInfoTime);
        updateLatestChecksumTimePS = conn.prepareStatement(updateLatestFileInfoTime);
    }

    /**
     * Method to handle the actual update.  
     * @param data The date to update the database with
     */
    public void updateFileInfos(List<FileInfosDataItem> data) {
        ArgumentValidator.checkNotNull(data, "data");
        try {
            init();
            log.debug("Initialized fileInfoUpdater");
            try {
                Date maxFileDate = new Date(0);
                Date maxChecksumDate = new Date(0);
                for(FileInfosDataItem infoData : data) {
                    updateFileInfo(infoData);
                    addFileInfo(infoData);
                    maxChecksumDate = TimeUtils.getMaxDate(maxFileDate, 
                            CalendarUtils.convertFromXMLGregorianCalendar(infoData.getLastModificationTime()));
                    maxChecksumDate = TimeUtils.getMaxDate(maxChecksumDate, 
                            CalendarUtils.convertFromXMLGregorianCalendar(infoData.getCalculationTimestamp()));                	
                }
                updateMaxTimes(maxFileDate, maxChecksumDate);
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

    private void addFileInfo(FileInfosDataItem item) throws SQLException {
        long fileTime = CalendarUtils.convertFromXMLGregorianCalendar(item.getLastModificationTime()).getTime();
        long checksumTime = CalendarUtils.convertFromXMLGregorianCalendar(item.getCalculationTimestamp()).getTime();
        
        Date now = new Date();
        insertFileInfoPS.setString(1, pillar);
        insertFileInfoPS.setString(2, item.getFileID());
        insertFileInfoPS.setLong(3, fileTime);
        insertFileInfoPS.setLong(4, now.getTime());
        insertFileInfoPS.setString(5, Base16Utils.decodeBase16(item.getChecksumValue()));
        insertFileInfoPS.setLong(6, checksumTime);
        insertFileInfoPS.setLong(7, now.getTime());
        insertFileInfoPS.setString(8, collectionID);
        insertFileInfoPS.setString(9, item.getFileID());
        insertFileInfoPS.setString(10, collectionID);
        insertFileInfoPS.setString(11, pillar);
        insertFileInfoPS.addBatch();
    }

    private void updateFileInfo(FileInfosDataItem item) throws SQLException {
        long fileTime = CalendarUtils.convertFromXMLGregorianCalendar(item.getLastModificationTime()).getTime();
        long calculationTime = CalendarUtils.convertFromXMLGregorianCalendar(item.getCalculationTimestamp()).getTime();

        Date now = new Date();
        updateChecksumPS.setString(1, Base16Utils.decodeBase16(item.getChecksumValue()));
        updateChecksumPS.setLong(2, fileTime);
        updateChecksumPS.setLong(3, now.getTime());
        updateChecksumPS.setLong(4, calculationTime);
        updateChecksumPS.setLong(5, now.getTime());
        updateChecksumPS.setString(6, item.getFileID());
        updateChecksumPS.setString(7, collectionID);
        updateChecksumPS.setString(8, pillar);
        updateChecksumPS.addBatch();
    }

    private void updateMaxTimes(Date maxFileDate, Date maxChecksumDate) throws SQLException {
        updateLatestChecksumTimePS.setLong(1, maxFileDate.getTime());
        updateLatestChecksumTimePS.setLong(2, maxChecksumDate.getTime());
        updateLatestChecksumTimePS.setString(3, collectionID);
        updateLatestChecksumTimePS.setString(4, pillar);

        insertLatestChecksumTimePS.setString(1, pillar);
        insertLatestChecksumTimePS.setLong(2, maxFileDate.getTime());
        insertLatestChecksumTimePS.setLong(3, maxChecksumDate.getTime());
        insertLatestChecksumTimePS.setString(4, collectionID);
        insertLatestChecksumTimePS.setString(5, collectionID);
        insertLatestChecksumTimePS.setString(6, pillar);
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
