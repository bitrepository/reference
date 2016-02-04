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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.cache.CollectionStat;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.PillarCollectionStat;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Common parts of the implementation of the access to the integrity db. 
 * Database specific backends are abstracted out in concrete classes.  
 */
public abstract class IntegrityDAO {
	private Logger log = LoggerFactory.getLogger(getClass());
	
    /** The connector to the database.*/
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
     * Method to ensure that pillars found in RepositorySettings is present in the database 
     */
    protected abstract void initializePillars();
    
    /**
     * Method to ensure that collections found in RepositorySettings is present in the database 
     */
    protected abstract void initializeCollections();
    
    /**
     * Get all known collectionIDs from the database
     * @return a list of all known collectionIDs from the database
     */
    public List<String> getCollections() {
        String sql = "SELECT collectionID FROM collections";
        return DatabaseUtils.selectStringList(dbConnector, sql, new Object[0]);
    }
    
    /**
     * Get all known pillarIDs from the database
     * @return a list of all known pillarIDs from the database
     */
    public List<String> getAllPillars() {
        String sql = "SELECT pillarID FROM pillar";
        return DatabaseUtils.selectStringList(dbConnector, sql, new Object[0]);
    }
    
    /**
     * Update the database with a batch of fileIDs data from a pillar for a given collection. 
     * If the fileIDs is not already present in the database a new record will be created
     * @param data The FileIDsData to update the database with
     * @param pillarID The ID of the pillar to update the with the FileIDsData
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
     * @param data The list of ChecksumDataForChecksumSpecTYPE to update the database with
     * @param pillarID The ID of the pillar to update with the data
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
     * Get the date of latest file known on the given pillar in the given collection.
     * @param collectionID The ID of the collection
     * @param pillarID  The ID of the pillar
     * @return The date for the latest file in the collection on the pillar
     */
    public Date getLatestFileDate(String collectionID, String pillarID) {
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        
        String retrieveSql = "SELECT latest_file_timestamp FROM collection_progress" 
        		+ " WHERE collectionID = ? "
        		+ " AND pillarID = ?";
        
        Long time = DatabaseUtils.selectFirstLongValue(dbConnector, retrieveSql, collectionID, pillarID);
        return (time == null ? null : new Date(time));
    }
    
    /**
     * Get the date of the latest file in the given collection
     * @param collectionID The ID of the collection
     * @return The date of the latest file in the collection. 
     */
    public Date getLatestFileDateInCollection(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        
        String retrieveSql = "SELECT MAX(latest_file_timestamp) FROM collection_progress"
                + " WHERE collectionID = ?";
        
        Long time = DatabaseUtils.selectFirstLongValue(dbConnector, retrieveSql, collectionID);
        return (time == null ? null : new Date(time));
    }
    
    /**
     * Get the date of latest known checksum on the given pillar in the given collection.
     * @param collectionID The ID of the collection
     * @param pillarID  The ID of the pillar
     * @return The date for the latest checksum in the collection on the pillar
     */
    public Date getLatestChecksumDate(String collectionID, String pillarID) {
    	ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        
        String retrieveSql = "SELECT latest_checksum_timestamp FROM collection_progress" 
        		+ " WHERE collectionID = ? "
        		+ " AND pillarID = ?";
        Long time = DatabaseUtils.selectFirstLongValue(dbConnector, retrieveSql, collectionID, pillarID);
        return (time == null ? null : new Date(time));
    }
    
    /**
     * Reset the file collection progress for a given collection
     * @param collectionID The ID of the collection to reset file collection progress for
     */
    public void resetFileCollectionProgress(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        String resetSql = "UPDATE collection_progress"
                + " SET latest_file_timestamp = NULL"
                + " WHERE collectionID = ?";
        
        DatabaseUtils.executeStatement(dbConnector, resetSql, collectionID);
    }
    
    /**
     * Reset the checksum collection progress for a given collection
     * @param collectionID The ID of the collection to reset checksum collection progress for
     */
    public void resetChecksumCollectionProgress(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        String resetSql = "UPDATE collection_progress"
                + " SET latest_checksum_timestamp = NULL"
                + " WHERE collectionID = ?";
        
        DatabaseUtils.executeStatement(dbConnector, resetSql, collectionID);
    }
    
    /**
     * Get fileIDs for those files which have outdated checksums
     * @param collectionID The ID of the collection to get fileIDs from
     * @param pillarID The ID of the pillar to get fileIDs from
     * @param maxDate The date prior to which checksums are considered outdated
     * @return an Iterator of the fileIDs for those files which have outdated checksums
     */
    public IntegrityIssueIterator getFilesWithOutdatedChecksums(String collectionID, String pillarID, Date maxDate) {
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNull(maxDate, "Date maxDate");
        
        String retrieveSql = "SELECT fileID from fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?"
                + " AND checksum_timestamp < ?";
        
        return makeIntegrityIssueIterator(retrieveSql, collectionID, pillarID, maxDate.getTime());
    }
    
    /**
     * Get the fileIDs of the files on a given pillar in a given collection which is missing their checksum. 
     * A checksum is considered missing if it's entry in the database is either NULL or the checksum have
     * not been seen after a certain cutoff date. 
     * @param collectionID The ID of the collection to look for missing checksums
     * @param pillarID The ID of the pillar on which to look for missing checksums
     * @param cutoffDate The date after which the checksum should have been seen to not be considered missing
     * @return an Iterator of the fileIDs of the files on a given pillar in a given collection which is missing their checksum.
     */
    public IntegrityIssueIterator getFilesWithMissingChecksums(String collectionID, String pillarID, Date cutoffDate) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        
        String retrieveSql = "SELECT fileID from fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?"
                + " AND (checksum is NULL"
                + " OR last_seen_getchecksums < ?)";
        
        return makeIntegrityIssueIterator(retrieveSql, collectionID, pillarID, cutoffDate.getTime());
    }
    
    /**
     * Get the fileIDs of files that are no longer on the given pillar in the given collection
     * @param collectionID The ID of the collection to look for orphan files
     * @param pillarID The ID of the pillar to look for orphan files
     * @param cutoffDate The date that a file should have been seen to not be considered orphan
     * @return an Iterator of the fileIDs of files that are no longer on the given pillar in the given collection
     */
    public IntegrityIssueIterator getOrphanFilesOnPillar(String collectionID, String pillarID, Date cutoffDate) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNull(cutoffDate, "Date cutoffDate");
        
        String findOrphansSql = "SELECT fileID from fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?"
                + " AND last_seen_getfileids < ?";
        
        return makeIntegrityIssueIterator(findOrphansSql, collectionID, pillarID, cutoffDate.getTime());
    }
    
    /**
     * Remove the file entry for a given pillar in a given collection from the database
     * @param collectionID The ID of the collection
     * @param pillarID The ID of the pillar
     * @param fileID The ID of the file
     */
    public void removeFile(String collectionID, String pillarID, String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        String removeSql = "DELETE FROM fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?"
                + " AND fileID = ?";
        
        DatabaseUtils.executeStatement(dbConnector, removeSql, collectionID, pillarID, fileID);
    }
    
    /**
     * Method that should deliver the database specific SQL for finding missing files at a pillar
     * @return the database specific SQL for finding missing files at a pillar
     */
    protected abstract String getFindMissingFilesAtPillarSql();
    
    /**
     * Method to find files in a given collection missing on a given pillar
     * @param collectionID The ID of the collection
     * @param pillarID The ID of the pillar
     * @param firstIndex start the iterator at this index, or 0 if null
     * @param maxResults maxResults
     * @return Iterator with the fileIDs that could not be found on the pillar
     */
    public IntegrityIssueIterator findMissingFilesAtPillar(String collectionID, String pillarID,
            Long firstIndex, Long maxResults) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        
        long first;
        if(firstIndex == null) {
            first = 0;
        } else {
            first = firstIndex;
        }
        
        String findMissingFilesSql = getFindMissingFilesAtPillarSql();
        return makeIntegrityIssueIterator(findMissingFilesSql, collectionID, collectionID, pillarID,
                first, maxResults);
    }
    
    /**
     * Method to find the files in a collection where the pillars does not agree upon the checksum
     * @param collectionID The ID of the collection
     * @return Iterator with the fileIDs that have checksum inconsistencies
     */
    public IntegrityIssueIterator findFilesWithChecksumInconsistincies(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
                
        String findInconsistentChecksumsSql = "SELECT fileID FROM ("
                + " SELECT fileID, count(distinct(checksum)) as checksums FROM fileinfo"
                + " WHERE collectionID = ?"
                + " GROUP BY fileID) as subselect"
                + " WHERE checksums > 1";
        
        return makeIntegrityIssueIterator(findInconsistentChecksumsSql, collectionID);
    }
    
    /**
     * Method that should deliver the database specific SQL for all files at a pillar
     * @return the database specific SQL for all files at a pillar
     */
    protected abstract String getAllFileIDsSql();
    
    /**
     * Get the files present on a pillar in a given collection
     * @param collectionID The ID of the collection
     * @param pillarID The ID of the pillar
     * @param firstIndex start the iterator at this index. If null, start at 0
     * @param maxResults the maximum number of results
     * @return The iterator with fileIDs present on the pillar in the given collection.
     */
    public IntegrityIssueIterator getAllFileIDsOnPillar(String collectionID, String pillarID,
            Long firstIndex, Long maxResults) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        long first;
        if(firstIndex == null) {
            first = 0;
        } else {
            first = firstIndex;
        }
        String getAllFileIDsSql = getAllFileIDsSql();
        return makeIntegrityIssueIterator(getAllFileIDsSql, collectionID, pillarID, first, maxResults);
        
    }
    
    /**
     * Get the list of FileInfo's for a given file in a given collection
     * @param fileID The ID of the file
     * @param collectionID The ID of the collection
     * @return The list of FileInfo objects 
     */
    public List<FileInfo> getFileInfosForFile(String fileID, String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        
        List<FileInfo> res = new ArrayList<FileInfo>();
        String getFileInfoSql = "SELECT pillarID, filesize, checksum, file_timestamp,"
                + " checksum_timestamp, last_seen_getfileids, last_seen_getchecksums FROM fileinfo"
                + " WHERE collectionID = ?"
                + " AND fileID = ?";
        
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, getFileInfoSql, collectionID, fileID)) {
            try (ResultSet dbResult = ps.executeQuery()) {
                while(dbResult.next()) {
                    Date lastFileCheck = new Date(dbResult.getLong("file_timestamp"));
                    String checksum = dbResult.getString("checksum");
                    Date lastChecksumCheck = new Date(dbResult.getLong("checksum_timestamp"));
                    Long fileSize = dbResult.getLong("fileSize");
                    String pillarID = dbResult.getString("pillarID");
                    Date lastSeenGetFileIDs = new Date(dbResult.getLong("last_seen_getfileids"));
                    Date lastSeenGetChecksums = new Date(dbResult.getLong("last_seen_getchecksums"));
                    
                    FileInfo f = new FileInfo(fileID, CalendarUtils.getXmlGregorianCalendar(lastFileCheck), checksum,
                            fileSize, CalendarUtils.getXmlGregorianCalendar(lastChecksumCheck), pillarID);
                    f.setLastSeenGetFileIDs(lastSeenGetFileIDs);
                    f.setLastSeenGetChecksums(lastSeenGetChecksums);
                    res.add(f);
                }
            } 
        } catch (SQLException e) {
            throw new IllegalStateException("Could not retrieve the FileInfo for '" + fileID + "' with the SQL '"
                    + getFileInfoSql + "'.", e);
        }
        return res;
    }
    
    /**
     * Method to create a new set of statistics entries.
     * @param collectionID The ID of the collection
     * @param statisticsCollector The statisticsCollector object containing the data to create
     * the statistics on
     */
    public void createStatistics(String collectionID, StatisticsCollector statisticsCollector) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        StatisticsCreator sc = new StatisticsCreator(dbConnector.getConnection(), collectionID);
        sc.createStatistics(statisticsCollector);
    }
    
    /**
     * Get the size of a given collection
     * @param collectionID The ID of the collection
     * @return The size of the collection 
     */
    public long getCollectionSize(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        
        String getCollectionSizeSql = "SELECT SUM(filesize) FROM "
                        + "(SELECT distinct(fileID), filesize from fileinfo"
                            + " WHERE collectionID = ?) AS subselect";
        Long size = DatabaseUtils.selectFirstLongValue(dbConnector, getCollectionSizeSql, collectionID);
        return (size == null ? 0 : size);
    }
    
    /**
     * Get the size of a collection on a given pillar
     * @param collectionID The ID of the collection
     * @param pillarID The ID of the pillar
     * @return The size of the collection on the pillar 
     */
    public long getCollectionSizeAtPillar(String collectionID, String pillarID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");

        String getCollectionSizeAtPillarSql = "SELECT SUM(filesize) FROM fileinfo"
                        + " WHERE collectionID = ?"
                        + " AND pillarID = ?";
        Long size = DatabaseUtils.selectFirstLongValue(dbConnector, getCollectionSizeAtPillarSql, 
                collectionID, pillarID);
               
        return (size == null ? 0 : size);
    }
    
    /**
     * Get the number of files in a given collection
     * @param collectionID The ID of the collection
     * @return The number of files in the collection 
     */
    public Long getNumberOfFilesInCollection(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        
        String getNumberOfFilesSql = "SELECT COUNT(DISTINCT(fileid)) FROM fileinfo"
                        + " WHERE collectionID = ?";
        
        return DatabaseUtils.selectFirstLongValue(dbConnector, getNumberOfFilesSql, collectionID);
    }
    
    /**
     * Get the number of files in a given collection
     * @param collectionID The ID of the collection
     * @param pillarID The ID of the pillar
     * @return The number of files in the collection at the given pillar 
     */
    public Long getNumberOfFilesInCollectionAtPillar(String collectionID, String pillarID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(pillarID, "String pillarID");
        
        String getNumberOfFilesSql = "SELECT COUNT(fileid) FROM fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?";
        
        return DatabaseUtils.selectFirstLongValue(dbConnector, getNumberOfFilesSql, collectionID, pillarID);

    }
    
    /**
     * Get the latest pillar statistics for a given collection
     * @param collectionID The ID of the collection
     * @return A list of the latest PillarCollectionStat for the given collection 
     */
    public List<PillarCollectionStat> getLatestPillarStats(String collectionID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        List<PillarCollectionStat> stats = new ArrayList<>();
        
        String latestPillarStatsSql = "SELECT pillarID, file_count, file_size, missing_files_count,"
                + " checksum_errors_count, missing_checksums_count, obsolete_checksums_count"
                + " FROM pillarstats"
                + " WHERE stat_key = ("
                    + " SELECT MAX(stat_key) FROM stats"
                    + " WHERE collectionID = ?)";
                
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, latestPillarStatsSql, collectionID)) {
               try (ResultSet dbResult = ps.executeQuery()) {
                   while(dbResult.next()) {
                       String pillarID = dbResult.getString("pillarID");
                       Long fileCount = dbResult.getLong("file_count");
                       Long dataSize = dbResult.getLong("file_size");
                       Long missingFiles = dbResult.getLong("missing_files_count");
                       Long checksumErrors = dbResult.getLong("checksum_errors_count");
                       Long missingChecksums = dbResult.getLong("missing_checksums_count");
                       Long obsoleteChecksums = dbResult.getLong("obsolete_checksums_count");
                       Date statsTime = null;
                       Date updateTime = null;
                       
                       
                       PillarCollectionStat p = new PillarCollectionStat(pillarID, collectionID, fileCount, dataSize, missingFiles, 
                               checksumErrors, missingChecksums, obsoleteChecksums, statsTime, updateTime);
                       stats.add(p);
                   }
               } 
           } catch (SQLException e) {
               throw new IllegalStateException("Could not retrieve the latest PillarCollectionStat's for '" + collectionID + "' " +
                       "with the SQL '" + latestPillarStatsSql + "'.", e);
           }        
        
        return stats;
    }
    
    /**
     * Method that should deliver the database specific SQL for getting the latest N collection statistics
     * @return the database specific SQL for getting the latest N collection statistics
     */
    protected abstract String getLatestCollectionStatsSql();
    
    /**
     *  Method to get the latest N collection statistics for a given collection
     *  @param collectionID The ID of the collection to get statistics for
     *  @param count The maximum number of statistics (N)
     *  @return The list of CollectionStat's
     */
    public List<CollectionStat> getLatestCollectionStats(String collectionID, int count) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        List<CollectionStat> stats = new ArrayList<>();

        String latestCollectionStatSql = getLatestCollectionStatsSql();
        
        try (Connection conn = dbConnector.getConnection();
                PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, latestCollectionStatSql, collectionID, count)) {
               try (ResultSet dbResult = ps.executeQuery()) {
                   while(dbResult.next()) {
                       Long fileCount = dbResult.getLong("file_count");
                       Long dataSize = dbResult.getLong("file_size");
                       Long checksumErrors = dbResult.getLong("checksum_errors_count");
                       Date latestFile = dbResult.getTimestamp("latest_file_date");
                       Date statsTime = dbResult.getTimestamp("stat_time");
                       Date updateTime = dbResult.getTimestamp("last_update");
                       
                       CollectionStat stat = new CollectionStat(collectionID, fileCount, dataSize, checksumErrors, 
                               latestFile, statsTime, updateTime);
                       stats.add(stat);
                   }
               } 
           } catch (SQLException e) {
               throw new IllegalStateException("Could not retrieve the latest PillarStat's for '" + collectionID + "' " +
                       "with the SQL '" + latestCollectionStatSql + "' with arguments '"
                               + Arrays.asList(collectionID, count) + "'.", e);
           }
           java.util.Collections.reverse(stats);
           return stats;
    }
    
    /**
     *  Method to obtain the earliest date that a file has on any pillar in the specific collection
     *  @param collectionID The ID of the collection
     *  @param fileID The ID of the file
     *  @return the earliest date that a file has on any pillar in the specific collection
     */
    public Date getEarliestFileDate(String collectionID, String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(collectionID, "String collectionID");
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        String getEarliestFileDateSql = "SELECT MIN(file_timestamp) FROM fileinfo"
                + " WHERE collectionID = ?"
                + " AND fileID = ?";
        long time = DatabaseUtils.selectFirstLongValue(dbConnector, getEarliestFileDateSql, collectionID, fileID);
        return new Date(time);
    }
    
    private IntegrityIssueIterator makeIntegrityIssueIterator(String query, Object... args) {
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = dbConnector.getConnection();
            ps = DatabaseUtils.createPreparedStatement(conn, query, args);
            return new IntegrityIssueIterator(ps);
        } catch (Exception e) {
            throw new IllegalStateException("Failed to create IntegrityIssueIterator for query '" 
                    + query + "' with arguments" + Arrays.asList(args), e);
        }
    }
}
