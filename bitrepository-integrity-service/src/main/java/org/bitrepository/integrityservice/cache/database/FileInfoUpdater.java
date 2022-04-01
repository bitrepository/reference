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

import org.bitrepository.bitrepositoryelements.FileInfosDataItem;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Types;
import java.util.Date;
import java.util.List;

/**
 * Class to handle the update of FileInfo information in the integrity database.
 * Does this by batching of sql statements for performance.
 * <p>
 * Implementation detail: Postgres will first feature 'upsert' functionality in version 9.5.
 * This means that we currently can't use the functionality, and is forced
 * to use the two call way. I.e. the conditional update, the conditional insert
 */
public class FileInfoUpdater {

    /**
     * SQL for conditional insert of the fileID in the files table.
     * The insert is only performed when the (file_id, collection_key)
     * tuple is not already found in the database
     */
    private final String insertFileInfoSql = "INSERT INTO fileinfo ("
            + " collectionID, pillarID, fileID, filesize, file_timestamp, last_seen_getfileids,"
            + " checksum, checksum_timestamp, last_seen_getchecksums)"
            + " (SELECT collectionID, ?, ?, ?, ?, ?, ?, ?, ? FROM collections"
            + " WHERE collectionID = ?"
            + " AND NOT EXISTS ("
            + " SELECT * FROM fileinfo "
            + " WHERE fileID = ?"
            + " AND collectionID = ?"
            + " AND pillarID = ?))";

    private final String updateFileInfoSql = "UPDATE fileinfo "
            + "	SET filesize = ?,"
            + " checksum = ?,"
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

    private final Logger log = LoggerFactory.getLogger(getClass());

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
     *
     * @param data The date to update the database with
     */
    public void updateFileInfos(List<FileInfosDataItem> data) {
        ArgumentValidator.checkNotNull(data, "data");
        try {
            init();
            log.debug("Initialized fileInfoUpdater");
            try {
                Date maxFileDate = null;
                Date maxChecksumDate = null;
                for (FileInfosDataItem infoData : data) {
                    updateFileInfo(infoData);
                    addFileInfo(infoData);
                    maxFileDate = getNewestDate(maxFileDate, infoData.getLastModificationTime());
                    maxChecksumDate = getNewestDate(maxChecksumDate, infoData.getCalculationTimestamp());
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
        Long checksumTime;
        if (item.getCalculationTimestamp() == null) {
            checksumTime = null;
        } else {
            checksumTime = CalendarUtils.convertFromXMLGregorianCalendar(item.getCalculationTimestamp()).getTime();
        }

        Date now = new Date();
        insertFileInfoPS.setString(1, pillar);
        insertFileInfoPS.setString(2, item.getFileID());
        if (item.getFileSize() == null) {
            insertFileInfoPS.setNull(3, Types.BIGINT);
        } else {
            insertFileInfoPS.setLong(3, item.getFileSize().longValue());
        }
        insertFileInfoPS.setLong(4, fileTime);
        insertFileInfoPS.setLong(5, now.getTime());
        insertFileInfoPS.setString(6, Base16Utils.decodeBase16(item.getChecksumValue()));

        if (checksumTime == null) {
            insertFileInfoPS.setNull(7, Types.BIGINT);
        } else {
            insertFileInfoPS.setLong(7, checksumTime);
        }

        insertFileInfoPS.setLong(8, now.getTime());
        insertFileInfoPS.setString(9, collectionID);
        insertFileInfoPS.setString(10, item.getFileID());
        insertFileInfoPS.setString(11, collectionID);
        insertFileInfoPS.setString(12, pillar);
        insertFileInfoPS.addBatch();
    }

    private void updateFileInfo(FileInfosDataItem item) throws SQLException {
        long fileTime = CalendarUtils.convertFromXMLGregorianCalendar(item.getLastModificationTime()).getTime();
        Long calculationTime;
        if (item.getCalculationTimestamp() == null) {
            calculationTime = null;
        } else {
            calculationTime = CalendarUtils.convertFromXMLGregorianCalendar(item.getCalculationTimestamp()).getTime();
        }

        Date now = new Date();
        if (item.getFileSize() == null) {
            updateChecksumPS.setNull(1, Types.BIGINT);
        } else {
            updateChecksumPS.setLong(1, item.getFileSize().longValue());
        }
        updateChecksumPS.setString(2, Base16Utils.decodeBase16(item.getChecksumValue()));
        updateChecksumPS.setLong(3, fileTime);
        updateChecksumPS.setLong(4, now.getTime());
        if (calculationTime == null) {
            updateChecksumPS.setNull(5, Types.BIGINT);
        } else {
            updateChecksumPS.setLong(5, calculationTime);
        }
        updateChecksumPS.setLong(6, now.getTime());
        updateChecksumPS.setString(7, item.getFileID());
        updateChecksumPS.setString(8, collectionID);
        updateChecksumPS.setString(9, pillar);
        updateChecksumPS.addBatch();
    }

    private void updateMaxTimes(Date maxFileDate, Date maxChecksumDate) throws SQLException {
        if (maxFileDate == null) {
            updateLatestChecksumTimePS.setNull(1, Types.BIGINT);
        } else {
            updateLatestChecksumTimePS.setLong(1, maxFileDate.getTime());
        }
        if (maxChecksumDate == null) {
            updateLatestChecksumTimePS.setNull(2, Types.BIGINT);
        } else {
            updateLatestChecksumTimePS.setLong(2, maxChecksumDate.getTime());
        }
        updateLatestChecksumTimePS.setString(3, collectionID);
        updateLatestChecksumTimePS.setString(4, pillar);


        insertLatestChecksumTimePS.setString(1, pillar);
        if (maxFileDate == null) {
            insertLatestChecksumTimePS.setNull(2, Types.BIGINT);
        } else {
            insertLatestChecksumTimePS.setLong(2, maxFileDate.getTime());
        }
        if (maxChecksumDate == null) {
            insertLatestChecksumTimePS.setNull(3, Types.BIGINT);
        } else {
            insertLatestChecksumTimePS.setLong(3, maxChecksumDate.getTime());
        }
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
        if (updateChecksumPS != null) {
            updateChecksumPS.close();
        }
        if (insertFileInfoPS != null) {
            insertFileInfoPS.close();
        }
        if (conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    private Date getNewestDate(Date currentDate, XMLGregorianCalendar timestamp) {
        if (currentDate == null && timestamp == null) {
            return null;
        }

        if (timestamp == null) {
            return currentDate;
        }

        Date newDate = CalendarUtils.convertFromXMLGregorianCalendar(timestamp);

        if (currentDate == null) {
            return newDate;
        }

        return TimeUtils.getMaxDate(currentDate, newDate);
    }
}