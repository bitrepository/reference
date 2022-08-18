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

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.integrityservice.cache.CollectionStat;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.PillarCollectionMetric;
import org.bitrepository.integrityservice.cache.PillarCollectionStat;
import org.bitrepository.integrityservice.checking.MaxChecksumAgeProvider;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.integrityservice.workflow.step.HandleObsoleteChecksumsStep;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.bitrepository.settings.referencesettings.ObsoleteChecksumSettings;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.*;

/**
 * Common parts of the implementation of the access to the integrity db.
 * Database specific backends are abstracted out in concrete classes.
 */
public abstract class IntegrityDAO {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * The connector to the database.
     */
    protected final DBConnector dbConnector;

    public IntegrityDAO(DBConnector dbConnector) {
        this.dbConnector = dbConnector;
        initializePillars();
        initializeCollections();
    }

    /**
     * Destroys the DB connector.
     */
    public void close() {
        dbConnector.destroy();
    }

    /**
     * Method to ensure that pillars found in RepositorySettings are present in the database
     */
    protected abstract void initializePillars();

    /**
     * Method to ensure that collections found in RepositorySettings are present in the database
     */
    protected abstract void initializeCollections();

    /**
     * Get all known collectionIDs from the database
     *
     * @return a list of all known collectionIDs from the database
     */
    public List<String> getCollections() {
        String sql = "SELECT collectionID FROM collections";
        return DatabaseUtils.selectStringList(dbConnector, sql);
    }

    /**
     * Get all known pillarIDs from the database
     *
     * @return a list of all known pillarIDs from the database
     */
    public List<String> getAllPillars() {
        String sql = "SELECT pillarID FROM pillar";
        return DatabaseUtils.selectStringList(dbConnector, sql);
    }

    /**
     * Update the database with a batch of fileIDs data from a pillar for a given collection.
     * If the fileIDs is not already present in the database a new record will be created
     *
     * @param data         The FileIDsData to update the database with
     * @param pillarID     The ID of the pillar to update the with the FileIDsData
     * @param collectionID The ID of the collection to update with the FileIDsData
     */
    public void updateFileIDs(FileIDsData data, String pillarID, String collectionID) {
        ArgumentValidator.checkNotNull(data, "FileIDsData data");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        log.trace("Updating the file ids '" + data + "' for pillar '" + pillarID + "'");

        FileUpdater fu = new FileUpdater(pillarID, dbConnector.getConnection(), collectionID);
        fu.updateFiles(data.getFileIDsDataItems());
    }

    /**
     * Update the database with a batch of checksum data from a pillar for a given collection.
     *
     * @param data         The list of ChecksumDataForChecksumSpecTYPE to update the database with
     * @param pillarID     The ID of the pillar to update with the data
     * @param collectionID The ID of the collection to update with the data
     */
    public void updateChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarID, String collectionID) {
        ArgumentValidator.checkNotNull(data, "List<ChecksumDataForChecksumSpecTYPE> data");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");

        ChecksumUpdater cu = new ChecksumUpdater(pillarID, dbConnector.getConnection(), collectionID);
        cu.updateChecksums(data);
    }

    /**
     * Get the date of the latest file known on the given pillar in the given collection.
     *
     * @param collectionID The ID of the collection
     * @param pillarID     The ID of the pillar
     * @return The date for the latest file in the collection on the pillar
     */
    public Date getLatestFileDate(String collectionID, String pillarID) {
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");

        String retrieveSql = "SELECT latest_file_timestamp FROM collection_progress" + " WHERE collectionID = ? " + " AND pillarID = ?";

        Long time = DatabaseUtils.selectFirstLongValue(dbConnector, retrieveSql, collectionID, pillarID);
        return (time == null ? null : new Date(time));
    }

    /**
     * Get the date of the latest file in the given collection
     *
     * @param collectionID The ID of the collection
     * @return The date of the latest file in the collection.
     */
    public Date getLatestFileDateInCollection(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");

        String retrieveSql = "SELECT MAX(latest_file_timestamp) FROM collection_progress" + " WHERE collectionID = ?";

        Long time = DatabaseUtils.selectFirstLongValue(dbConnector, retrieveSql, collectionID);
        return (time == null ? null : new Date(time));
    }

    /**
     * Get the date of the latest known checksum on the given pillar in the given collection.
     *
     * @param collectionID The ID of the collection
     * @param pillarID     The ID of the pillar
     * @return The date for the latest checksum in the collection on the pillar
     */
    public Date getLatestChecksumDate(String collectionID, String pillarID) {
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");

        String retrieveSql = "SELECT latest_checksum_timestamp FROM collection_progress" + " WHERE collectionID = ? " + " AND pillarID = ?";
        Long time = DatabaseUtils.selectFirstLongValue(dbConnector, retrieveSql, collectionID, pillarID);
        return (time == null ? null : new Date(time));
    }

    /**
     * Reset the file collection progress for a given collection
     *
     * @param collectionID The ID of the collection to reset file collection progress for
     */
    public void resetFileCollectionProgress(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        String resetSql = "UPDATE collection_progress" + " SET latest_file_timestamp = NULL" + " WHERE collectionID = ?";

        DatabaseUtils.executeStatement(dbConnector, resetSql, collectionID);
    }

    /**
     * Reset the checksum collection progress for a given collection
     *
     * @param collectionID The ID of the collection to reset checksum collection progress for
     */
    public void resetChecksumCollectionProgress(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        String resetSql = "UPDATE collection_progress" + " SET latest_checksum_timestamp = NULL" + " WHERE collectionID = ?";

        DatabaseUtils.executeStatement(dbConnector, resetSql, collectionID);
    }

    /**
     * Get fileIDs for those files which have outdated checksums
     *
     * @param collectionID The ID of the collection to get fileIDs from
     * @param pillarID     The ID of the pillar to get fileIDs from
     * @param maxDate      The date prior to which checksums are considered outdated
     * @return an Iterator of the fileIDs for those files which have outdated checksums
     */
    public IntegrityIssueIterator getFilesWithOutdatedChecksums(String collectionID, String pillarID, Date maxDate) {
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNull(maxDate, "Date maxDate");

        String retrieveSql =
                "SELECT fileID from fileinfo" + " WHERE collectionID = ?" + " AND pillarID = ?" + " AND checksum_timestamp < ?";

        return makeIntegrityIssueIterator(retrieveSql, collectionID, pillarID, maxDate.getTime());
    }

    /**
     * Get the fileIDs of the files on a given pillar in a given collection which is missing their checksum.
     * A checksum is considered missing if it's entry in the database is either NULL or the checksum have
     * not been seen after a certain cutoff date.
     *
     * @param collectionID The ID of the collection to look for missing checksums
     * @param pillarID     The ID of the pillar on which to look for missing checksums
     * @param cutoffDate   The date after which the checksum should have been seen to not be considered missing
     * @return an Iterator of the fileIDs of the files on a given pillar in a given collection which is missing their checksum.
     */
    public IntegrityIssueIterator getFilesWithMissingChecksums(String collectionID, String pillarID, Date cutoffDate) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");

        String retrieveSql = "SELECT fileID from fileinfo" + " WHERE collectionID = ?" + " AND pillarID = ?" + " AND (checksum is NULL" +
                " OR last_seen_getchecksums < ?)";

        return makeIntegrityIssueIterator(retrieveSql, collectionID, pillarID, cutoffDate.getTime());
    }

    /**
     * Get the fileIDs of files that are no longer on the given pillar in the given collection
     *
     * @param collectionID The ID of the collection to look for orphan files
     * @param pillarID     The ID of the pillar to look for orphan files
     * @param cutoffDate   The date that a file should have been seen to not be considered orphan
     * @return an Iterator of the fileIDs of files that are no longer on the given pillar in the given collection
     */
    public IntegrityIssueIterator getOrphanFilesOnPillar(String collectionID, String pillarID, Date cutoffDate) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNull(cutoffDate, "Date cutoffDate");

        String findOrphansSql =
                "SELECT fileID from fileinfo" + " WHERE collectionID = ?" + " AND pillarID = ?" + " AND last_seen_getfileids < ?";

        return makeIntegrityIssueIterator(findOrphansSql, collectionID, pillarID, cutoffDate.getTime());
    }

    /**
     * Remove the file entry for a given pillar in a given collection from the database
     *
     * @param collectionID The ID of the collection
     * @param pillarID     The ID of the pillar
     * @param fileID       The ID of the file
     */
    public void removeFile(String collectionID, String pillarID, String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        String removeSql = "DELETE FROM fileinfo" + " WHERE collectionID = ?" + " AND pillarID = ?" + " AND fileID = ?";

        DatabaseUtils.executeStatement(dbConnector, removeSql, collectionID, pillarID, fileID);
    }

    /**
     * Method that should deliver the database specific SQL for finding files with less than N copies
     *
     * @return the database specific SQL for finding files with less than N copies
     */
    protected abstract String getFindFilesWithMissingCopiesSql();

    /**
     * Method to find files in a given collection missing on a given pillar
     *
     * @param collectionID   The ID of the collection
     * @param expectedCopies The number of copies that should be present
     * @param firstIndex     start the iterator at this index, or 0 if null
     * @param maxResults     maxResults
     * @return Iterator with the fileIDs that could not be found on the pillar
     */
    public IntegrityIssueIterator findFilesWithMissingCopies(String collectionID, int expectedCopies, Long firstIndex, Long maxResults) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");

        long first = firstIndex == null ? 0 : firstIndex;
        long maxRes = maxResults == null ? Long.MAX_VALUE : maxResults;

        String findFileSql = getFindFilesWithMissingCopiesSql();
        return makeIntegrityIssueIterator(findFileSql, collectionID, expectedCopies, first, maxRes);
    }

    /**
     * Method to find the files in a collection where the pillars do not agree upon the checksum
     *
     * @param collectionID The ID of the collection
     * @return Iterator with the fileIDs that have checksum inconsistencies
     */
    public IntegrityIssueIterator findFilesWithChecksumInconsistencies(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");

        String findInconsistentChecksumsSql =
                "SELECT fileID FROM (" + " SELECT fileID, count(distinct(checksum)) as checksums FROM fileinfo" +
                        " WHERE collectionID = ?" + " GROUP BY fileID) as subselect" + " WHERE checksums > 1";

        return makeIntegrityIssueIterator(findInconsistentChecksumsSql, collectionID);
    }

    /**
     * Method that should deliver the database specific SQL for all files at a pillar
     *
     * @return the database specific SQL for all files at a pillar
     */
    protected abstract String getAllFileIDsSql();

    /**
     * Get the files present on a pillar in a given collection
     *
     * @param collectionID The ID of the collection
     * @param pillarID     The ID of the pillar
     * @param firstIndex   start the iterator at this index. If null, start at 0
     * @param maxResults   the maximum number of results
     * @return The iterator with fileIDs present on the pillar in the given collection.
     */
    public IntegrityIssueIterator getAllFileIDsOnPillar(String collectionID, String pillarID, Long firstIndex, Long maxResults) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        long first;
        first = Objects.requireNonNullElse(firstIndex, 0L);
        String getAllFileIDsSql = getAllFileIDsSql();
        return makeIntegrityIssueIterator(getAllFileIDsSql, collectionID, pillarID, first, maxResults);
    }

    /**
     * Method that should deliver the database specific SQL for retrieving the FileID at
     * a specific index.
     *
     * @return The database specific SQL for delivering the FileID at a specific index.
     */
    protected abstract String getFileIdAtIndexSql();

    /**
     * Gets the FileID at a given index.
     *
     * @param collectionID The ID of the collection.
     * @param index        The iterator index for the entry.
     * @return The FileID.
     */
    public String getFileIdAtIndex(String collectionID, Long index) {
        String getSql = getFileIdAtIndexSql();
        return DatabaseUtils.selectFirstStringValue(dbConnector, getSql, collectionID, index, 1);
    }

    /**
     * Get the list of FileInfo's for a given file in a given collection
     *
     * @param fileID       The ID of the file
     * @param collectionID The ID of the collection
     * @return The list of FileInfo objects
     */
    public List<FileInfo> getFileInfosForFile(String fileID, String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");

        List<FileInfo> res = new ArrayList<>();
        String getFileInfoSql = "SELECT pillarID, filesize, checksum, file_timestamp," +
                " checksum_timestamp, last_seen_getfileids, last_seen_getchecksums FROM fileinfo" + " WHERE collectionID = ?" +
                " AND fileID = ?";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, getFileInfoSql, collectionID, fileID)) {
            try (ResultSet dbResult = ps.executeQuery()) {
                while (dbResult.next()) {
                    Date lastFileCheck = new Date(dbResult.getLong("file_timestamp"));
                    String checksum = dbResult.getString("checksum");
                    Date lastChecksumCheck = new Date(dbResult.getLong("checksum_timestamp"));
                    Long fileSize = dbResult.getLong("fileSize");
                    String pillarID = dbResult.getString("pillarID");
                    Date lastSeenGetFileIDs = new Date(dbResult.getLong("last_seen_getfileids"));
                    Date lastSeenGetChecksums = new Date(dbResult.getLong("last_seen_getchecksums"));

                    FileInfo f = new FileInfo(fileID, CalendarUtils.getXmlGregorianCalendar(lastFileCheck), checksum, fileSize,
                            CalendarUtils.getXmlGregorianCalendar(lastChecksumCheck), pillarID);
                    f.setLastSeenGetFileIDs(lastSeenGetFileIDs);
                    f.setLastSeenGetChecksums(lastSeenGetChecksums);
                    res.add(f);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException("Could not retrieve the FileInfo for '" + fileID + "' with the SQL '" + getFileInfoSql + "'.",
                    e);
        }
        return res;
    }

    /**
     * Method to create a new set of statistics entries.
     *
     * @param collectionID        The ID of the collection
     * @param statisticsCollector The statisticsCollector object containing the data to create
     *                            the statistics on
     */
    public void createStatistics(String collectionID, StatisticsCollector statisticsCollector) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        StatisticsCreator sc = new StatisticsCreator(dbConnector.getConnection(), collectionID);
        sc.createStatistics(statisticsCollector);
    }


    /**
     * Method to retrieves the metrics for the pillars in the given collection
     * I.e. the summed filesize and file count per pillar
     *
     * @param collectionID The ID of the collection to get metrics for
     * @return A mapping between pillars in the collection and the PillarCollectionMetric,
     * the returned map is empty if nothing is found for the collection. Never null.
     */
    public Map<String, PillarCollectionMetric> getPillarCollectionMetrics(String collectionID) {
        Map<String, PillarCollectionMetric> metrics = new HashMap<>();
        String selectSql =
                "SELECT pillarID, COUNT(fileID) as filecount, SUM(filesize) as sizesum," +
                        "   MIN(checksum_timestamp) as oldest_checksum_timestamp" +
                        " FROM fileinfo" +
                        " WHERE collectionID = ?" +
                        " GROUP BY pillarID";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, selectSql, collectionID);
             ResultSet dbResult = ps.executeQuery()) {
            while (dbResult.next()) {
                String pillarID = dbResult.getString("pillarID");
                long fileCount = dbResult.getLong("filecount");
                // In case SUM(filesize) returned null, dbResult.getLong() will return 0, which is the sum we want
                long fileSize = dbResult.getLong("sizesum");
                long oldestChecksumTimestampMillis = dbResult.getLong("oldest_checksum_timestamp");
                Instant oldestChecksumTimestamp =
                        dbResult.wasNull() ? null : Instant.ofEpochMilli(oldestChecksumTimestampMillis);
                PillarCollectionMetric metric = new PillarCollectionMetric(fileSize, fileCount, oldestChecksumTimestamp);
                metrics.put(pillarID, metric);
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve PillarCollectionMetrics for collection '" + collectionID + "' with the SQL '" + selectSql + "'.",
                    e);
        }

        return metrics;
    }

    /**
     * Get the size of a given collection
     *
     * @param collectionID The ID of the collection
     * @return The size of the collection
     */
    public long getCollectionSize(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");

        String getCollectionSizeSql =
                "SELECT SUM(filesize) FROM " + "(SELECT distinct(fileID), filesize from fileinfo" + " WHERE collectionID = ?) AS subselect";
        Long size = DatabaseUtils.selectFirstLongValue(dbConnector, getCollectionSizeSql, collectionID);
        return (size == null ? 0 : size);
    }

    /**
     * Get the number of files in a given collection
     *
     * @param collectionID The ID of the collection
     * @return The number of files in the collection
     */
    public Long getNumberOfFilesInCollection(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");

        String getNumberOfFilesSql = "SELECT COUNT(DISTINCT(fileid)) FROM fileinfo" + " WHERE collectionID = ?";

        return DatabaseUtils.selectFirstLongValue(dbConnector, getNumberOfFilesSql, collectionID);
    }

    /**
     * Get the latest pillar statistics for a given collection
     *
     * @param collectionID The ID of the collection
     * @return A list of the latest PillarCollectionStat for the given collection
     */
    public List<PillarCollectionStat> getLatestPillarStats(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        List<PillarCollectionStat> stats = new ArrayList<>();

        String latestPillarStatsSql = "SELECT pillarID, file_count, file_size, missing_files_count," +
                "   checksum_errors_count, missing_checksums_count, obsolete_checksums_count," +
                "   oldest_checksum_timestamp" +
                " FROM pillarstats" +
                " WHERE stat_key = (" +
                "   SELECT MAX(stat_key)" +
                "   FROM stats" +
                "   WHERE collectionID = ?)";

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, latestPillarStatsSql, collectionID)) {
            try (ResultSet dbResult = ps.executeQuery()) {
                while (dbResult.next()) {
                    String pillarID = dbResult.getString("pillarID");
                    Long fileCount = dbResult.getLong("file_count");
                    Long dataSize = dbResult.getLong("file_size");
                    Long missingFiles = dbResult.getLong("missing_files_count");
                    Long checksumErrors = dbResult.getLong("checksum_errors_count");
                    Long missingChecksums = dbResult.getLong("missing_checksums_count");
                    Long obsoleteChecksums = dbResult.getLong("obsolete_checksums_count");
                    Date statsTime = null;
                    Date updateTime = null;
                    String pillarHostname = Objects.requireNonNullElse(SettingsUtils.getPillarName(pillarID), "N/A");
                    String pillarType = (SettingsUtils.getPillarType(pillarID) != null) ?
                            Objects.requireNonNull(SettingsUtils.getPillarType(pillarID)).value() : "Unknown";
                    String maxAgeForChecksums = getMaxAgeForChecksums(pillarID);
                    Instant oldestChecksumTimestamp = getOldestChecksumTimestamp(dbResult);
                    PillarCollectionStat p = new PillarCollectionStat(pillarID, collectionID,
                            pillarName, pillarType, fileCount, dataSize,
                            missingFiles, checksumErrors, missingChecksums, obsoleteChecksums,
                            maxAgeForChecksums, oldestChecksumTimestamp, statsTime, updateTime);
                    stats.add(p);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve the latest PillarCollectionStat's for '" + collectionID + "' " + "with the SQL '" +
                            latestPillarStatsSql + "'.", e);
        }

        return stats;
    }

    @NotNull
    private String getMaxAgeForChecksums(String pillarID) {
        ObsoleteChecksumSettings obsoleteChecksumSettings =
                SettingsUtils.getIntegrityServiceSettings().getObsoleteChecksumSettings();
        MaxChecksumAgeProvider maxChecksumAgeProvider =
                new MaxChecksumAgeProvider(HandleObsoleteChecksumsStep.DEFAULT_MAX_CHECKSUM_AGE,
                        obsoleteChecksumSettings);
        long maxAge = maxChecksumAgeProvider.getMaxChecksumAge(pillarID);
        return maxAge == 0 ? "unlimited" : TimeUtils.millisecondsToHuman(maxAge);
    }

    private Instant getOldestChecksumTimestamp(ResultSet dbResult) throws SQLException {
        long oldestChecksumTimestamp = dbResult.getLong("oldest_checksum_timestamp");
        return dbResult.wasNull() ? null : Instant.ofEpochMilli(oldestChecksumTimestamp);
    }

    /**
     * Method that should deliver the database specific SQL for getting the latest N collection statistics
     *
     * @return the database specific SQL for getting the latest N collection statistics
     */
    protected abstract String getLatestCollectionStatsSql();

    /**
     * Method to get the latest N collection statistics for a given collection
     *
     * @param collectionID The ID of the collection to get statistics for
     * @param count        The maximum number of statistics (N)
     * @return The list of CollectionStat's
     */
    public List<CollectionStat> getLatestCollectionStats(String collectionID, int count) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        List<CollectionStat> stats = new ArrayList<>();

        String latestCollectionStatSql = getLatestCollectionStatsSql();

        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, latestCollectionStatSql, collectionID, count)) {
            try (ResultSet dbResult = ps.executeQuery()) {
                while (dbResult.next()) {
                    Long fileCount = dbResult.getLong("file_count");
                    Long dataSize = dbResult.getLong("file_size");
                    Long checksumErrors = dbResult.getLong("checksum_errors_count");
                    Date latestFile = new Date(dbResult.getLong("latest_file_date"));
                    Date statsTime = new Date(dbResult.getLong("stat_time"));
                    Date updateTime = new Date(dbResult.getLong("last_update"));

                    CollectionStat stat = new CollectionStat(collectionID, fileCount, dataSize, checksumErrors, latestFile, statsTime,
                            updateTime);
                    stats.add(stat);
                }
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Could not retrieve the latest PillarStat's for '" + collectionID + "' " + "with the SQL '" + latestCollectionStatSql +
                            "' with arguments '" + Arrays.asList(collectionID, count) + "'.", e);
        }
        java.util.Collections.reverse(stats);
        return stats;
    }

    /**
     * Method to obtain the earliest date that a file has on any pillar in the specific collection
     *
     * @param collectionID The ID of the collection
     * @param fileID       The ID of the file
     * @return the earliest date that a file has on any pillar in the specific collection
     */
    public Date getEarliestFileDate(String collectionID, String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        String getEarliestFileDateSql = "SELECT MIN(file_timestamp) FROM fileinfo" + " WHERE collectionID = ?" + " AND fileID = ?";
        long time = Optional.ofNullable(DatabaseUtils.selectFirstLongValue(dbConnector, getEarliestFileDateSql, collectionID, fileID))
                .orElse(0L);
        return new Date(time);
    }

    private IntegrityIssueIterator makeIntegrityIssueIterator(String query, Object... args) {
        PreparedStatement ps;
        Connection conn;
        try {
            conn = dbConnector.getConnection();
            ps = DatabaseUtils.createPreparedStatement(conn, query, args);
            return new IntegrityIssueIterator(ps);
        } catch (Exception e) {
            throw new IllegalStateException(
                    "Failed to create IntegrityIssueIterator for query '" + query + "' with arguments" + Arrays.asList(args), e);
        }
    }
}
