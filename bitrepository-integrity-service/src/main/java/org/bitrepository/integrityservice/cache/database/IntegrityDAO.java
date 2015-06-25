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
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityservice.cache.CollectionStat;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.PillarCollectionStat;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class IntegrityDAO {
	private Logger log = LoggerFactory.getLogger(getClass());
	
    /** The connector to the database.*/
    protected final DBConnector dbConnector;
    protected final Settings settings;
    
    public IntegrityDAO(DBConnector dbConnector, Settings settings) {
    	this.dbConnector = dbConnector;
    	this.settings = settings;
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
    
    public List<String> getCollections() {
        String sql = "SELECT collectionID FROM collections";
        return DatabaseUtils.selectStringList(dbConnector, sql, new Object[0]);
    }
    
    public List<String> getAllPillars() {
        String sql = "SELECT pillarID FROM pillar";
        return DatabaseUtils.selectStringList(dbConnector, sql, new Object[0]);
    }
    
    public void updateFileIDs(FileIDsData data, String pillarId, String collectionId) {
        ArgumentValidator.checkNotNull(data, "FileIDsData data");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        log.trace("Updating the file ids '" + data + "' for pillar '" + pillarId + "'");
        
        FileUpdater fu = new FileUpdater(pillarId, dbConnector.getConnection(), collectionId);
        fu.updateFiles(data.getFileIDsDataItems());
    }
    
    public void updateChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId, String collectionId) {
    	ArgumentValidator.checkNotNull(data, "List<ChecksumDataForChecksumSpecTYPE> data");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        ChecksumUpdater cu = new ChecksumUpdater(pillarId, dbConnector.getConnection(), collectionId);
        cu.updateChecksums(data);
    }

    public Date getLatestFileDate(String collectionId, String pillarId) {
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        String retrieveSql = "SELECT latest_file_timestamp FROM collection_progress" 
        		+ " WHERE collectionID = ? "
        		+ " AND pillarID = ?";
        
        return DatabaseUtils.selectFirstDateValue(dbConnector, retrieveSql, collectionId, pillarId);
    }
    
    public Date getLatestFileDateInCollection(String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        String retrieveSql = "SELECT MAX(latest_file_timestamp) FROM collection_progress"
                + " WHERE collectionID = ?";
        
        return DatabaseUtils.selectFirstDateValue(dbConnector, retrieveSql, collectionId);
    }
    
    public Date getLatestChecksumDate(String collectionId, String pillarId) {
    	ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        String retrieveSql = "SELECT latest_checksum_timestamp FROM collection_progress" 
        		+ " WHERE collectionID = ? "
        		+ " AND pillarID = ?";
        
        return DatabaseUtils.selectFirstDateValue(dbConnector, retrieveSql, collectionId, pillarId);
    }
    
    public void resetFileCollectionProgress(String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        String resetSql = "UPDATE collection_progress"
                + " SET latest_file_timestamp = NULL"
                + " WHERE collectionID = ?";
        
        DatabaseUtils.executeStatement(dbConnector, resetSql, collectionId);
    }
    
    public void resetChecksumCollectionProgress(String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        String resetSql = "UPDATE collection_progress"
                + " SET latest_checksum_timestamp = NULL"
                + " WHERE collectionID = ?";
        
        DatabaseUtils.executeStatement(dbConnector, resetSql, collectionId);
    }
    
    public IntegrityIssueIterator getFilesWithOutdatedChecksums(String collectionId, String pillarId, Date maxDate) {
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        ArgumentValidator.checkNotNull(maxDate, "Date maxDate");
        
        String retrieveSql = "SELECT fileID from fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?"
                + " AND checksum_timestamp < ?";
        
        return makeIntegrityIssueIterator(retrieveSql, collectionId, pillarId, maxDate);
    }
    
    public IntegrityIssueIterator getFilesWithMissingChecksums(String collectionId, String pillarId, Date cutoffDate) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        
        String retrieveSql = "SELECT fileID from fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?"
                + " AND (checksum is NULL"
                + " OR last_seen_getchecksums < ?)";
        
        return makeIntegrityIssueIterator(retrieveSql, collectionId, pillarId, cutoffDate);
    }
    
    public IntegrityIssueIterator getOrphanFilesOnPillar(String collectionId, String pillarId, Date cutoffDate) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNull(cutoffDate, "Date cutoffDate");
        
        String findOrphansSql = "SELECT fileID from fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?"
                + " AND last_seen_getfileids < ?";
        
        return makeIntegrityIssueIterator(findOrphansSql, collectionId, pillarId, cutoffDate);
    }
    
    public void removeFile(String collectionId, String pillarId, String fileId) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");

        String removeSql = "DELETE FROM fileinfo"
                + " WHERE collectionId = ?"
                + " AND pillarId = ?"
                + " AND fileId = ?";
        
        DatabaseUtils.executeStatement(dbConnector, removeSql, collectionId, pillarId, fileId);
    }
    
    protected abstract String getFindMissingFilesAtPillarSql();
    
    public IntegrityIssueIterator findMissingFilesAtPillar(String collectionId, String pillarId, 
            Long firstIndex, Long maxResults) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        
        long first;
        if(firstIndex == null) {
            first = 0;
        } else {
            first = firstIndex;
        }
        
        String findMissingFilesSql = getFindMissingFilesAtPillarSql();
        return makeIntegrityIssueIterator(findMissingFilesSql, collectionId, collectionId, pillarId,
                first, maxResults);
    }
    
    public IntegrityIssueIterator findFilesWithChecksumInconsistincies(String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
                
        String findInconsistentChecksumsSql = "SELECT fileID FROM ("
                + " SELECT fileID, count(distinct(checksum)) as checksums FROM fileinfo"
                + " WHERE collectionID = ?"
                + " GROUP BY fileID) as subselect"
                + " WHERE checksums > 1";
        
        return makeIntegrityIssueIterator(findInconsistentChecksumsSql, collectionId);
    }
    
    protected abstract String getAllFileIDsSql();
    
    public IntegrityIssueIterator getAllFileIDsOnPillar(String collectionId, String pillarId, 
            Long firstIndex, Long maxResults) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        long first;
        if(firstIndex == null) {
            first = 0;
        } else {
            first = firstIndex;
        }
        String getAllFileIDsSql = getAllFileIDsSql();
        return makeIntegrityIssueIterator(getAllFileIDsSql, collectionId, pillarId, first, maxResults);
        
    }
    
    public List<FileInfo> getFileInfosForFile(String fileId, String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        List<FileInfo> res = new ArrayList<FileInfo>();
        String getFileInfoSql = "SELECT pillarID, filesize, checksum, file_timestamp,"
                + " checksum_timestamp, last_seen_getfileids, last_seen_getchecksums FROM fileinfo"
                + " WHERE collectionID = ?"
                + " AND fileID = ?";
        
        try (Connection conn = dbConnector.getConnection();
             PreparedStatement ps = DatabaseUtils.createPreparedStatement(conn, getFileInfoSql, collectionId, fileId)) {
            try (ResultSet dbResult = ps.executeQuery()) {
                while(dbResult.next()) {
                    Date lastFileCheck = dbResult.getTimestamp("file_timestamp");
                    String checksum = dbResult.getString("checksum");
                    Date lastChecksumCheck = dbResult.getTimestamp("checksum_timestamp");
                    Long fileSize = dbResult.getLong("fileSize");
                    String pillarId = dbResult.getString("pillarID");
                    Date lastSeenGetFileIDs = dbResult.getTimestamp("last_seen_getfileids");
                    Date lastSeenGetChecksums = dbResult.getTimestamp("last_seen_getchecksums");
                    
                    FileInfo f = new FileInfo(fileId, CalendarUtils.getXmlGregorianCalendar(lastFileCheck), checksum, 
                            fileSize, CalendarUtils.getXmlGregorianCalendar(lastChecksumCheck), pillarId);
                    f.setLastSeenGetFileIDs(lastSeenGetFileIDs);
                    f.setLastSeenGetChecksums(lastSeenGetChecksums);
                    res.add(f);
                }
            } 
        } catch (SQLException e) {
            throw new IllegalStateException("Could not retrieve the FileInfo for '" + fileId + "' with the SQL '"
                    + getFileInfoSql + "'.", e);
        }
        return res;
    }
    
    public void createStatistics(String collectionId, StatisticsCollector statisticsCollector) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        StatisticsCreator sc = new StatisticsCreator(dbConnector.getConnection(), collectionId);
        sc.createStatistics(statisticsCollector);
    }
    
    public Long getCollectionSize(String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        String getCollectionSizeSql = "SELECT SUM(filesize) FROM "
                        + "(SELECT distinct(fileID), filesize from fileinfo"
                            + " WHERE collectionID = ?) AS subselect";
        Long size = DatabaseUtils.selectFirstLongValue(dbConnector, getCollectionSizeSql, collectionId);
        return (size == null ? 0 : size);
    }
    
    public Long getCollectionSizeAtPillar(String collectionId, String pillarId) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");

        String getCollectionSizeAtPillarSql = "SELECT SUM(filesize) FROM fileinfo"
                        + " WHERE collectionID = ?"
                        + " AND pillarID = ?";
        Long size = DatabaseUtils.selectFirstLongValue(dbConnector, getCollectionSizeAtPillarSql, 
                collectionId, pillarId);
               
        return (size == null ? 0 : size);
    }
    
    public Long getNumberOfFilesInCollection(String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        String getNumberOfFilesSql = "SELECT COUNT(DISTINCT(fileid)) FROM fileinfo"
                        + " WHERE collectionID = ?";
        
        return DatabaseUtils.selectFirstLongValue(dbConnector, getNumberOfFilesSql, collectionId);
    }
    
    public Long getNumberOfFilesInCollectionAtPillar(String collectionId, String pillarId) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        
        String getNumberOfFilesSql = "SELECT COUNT(fileid) FROM fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?";
        
        return DatabaseUtils.selectFirstLongValue(dbConnector, getNumberOfFilesSql, collectionId, pillarId);

    }
    
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
    
    protected abstract String getLatestCollectionStatsSql();
    
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
