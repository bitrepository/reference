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
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.service.database.DBConnector;
import org.bitrepository.service.database.DatabaseUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrityDAO2 {
	private Logger log = LoggerFactory.getLogger(getClass());
	
    /** The connector to the database.*/
    protected final DBConnector dbConnector;
    protected final Settings settings;
    
    public IntegrityDAO2(DBConnector dbConnector, Settings settings) {
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
    private synchronized void initializePillars() {
    	List<String> pillars = SettingsUtils.getAllPillarIDs();
    	for(String pillar : pillars) {
            String sql = "INSERT INTO pillar (pillarID)"
                    + " (SELECT ?"
            			+ " WHERE NOT EXISTS ("
            				+ "	SELECT pillarID FROM pillar"
            				+ " WHERE pillarID = ?))";
            DatabaseUtils.executeStatement(dbConnector, sql, pillar, pillar);
    	}
    }
    
    /**
     * Method to ensure that collections found in RepositorySettings is present in the database 
     */
    private synchronized void initializeCollections() {
    	List<String> collections = SettingsUtils.getAllCollectionsIDs();
    	for(String collection : collections) {
            String sql = "INSERT INTO collections (collectionID)"
                    + " (SELECT ?"
            			+ " WHERE NOT EXISTS ("
            				+ "	SELECT collectionID FROM collections"
            				+ " WHERE collectionID = ?))";
            DatabaseUtils.executeStatement(dbConnector, sql, collection, collection);
    	}
    }
    
    public void updateFileIDs(FileIDsData data, String pillarId, String collectionId) {
        ArgumentValidator.checkNotNull(data, "FileIDsData data");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        log.trace("Updating the file ids '" + data + "' for pillar '" + pillarId + "'");
        
        FileUpdater2 fu = new FileUpdater2(pillarId, dbConnector.getConnection(), collectionId);
        fu.updateFiles(data.getFileIDsDataItems());
    }
    
    public void updateChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId, String collectionId) {
    	ArgumentValidator.checkNotNull(data, "List<ChecksumDataForChecksumSpecTYPE> data");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        ChecksumUpdater2 cu = new ChecksumUpdater2(pillarId, dbConnector.getConnection(), collectionId);
        cu.updateChecksums(data);
    }

    public Date getLatestFileDate(String collectionId, String pillarId) {
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        /* Consider getting this from a small helper table */
        String retrieveSql = "SELECT latest_file_timestamp FROM collection_progress" 
        		+ " WHERE collectionID = ? "
        		+ " AND pillarID = ?";
        
        return DatabaseUtils.selectFirstDateValue(dbConnector, retrieveSql, collectionId, pillarId);
    }
    
    public Date getLatestChecksumDate(String collectionId, String pillarId) {
    	ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        
        /* Consider getting this from a small helper table */
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
    
    public IntegrityIssueIterator getFilesWithMissingChecksums(String collectionId, String pillarId) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        
        String retrieveSql = "SELECT fileID from fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?"
                + " AND checksum is NULL";
        
        return makeIntegrityIssueIterator(retrieveSql, collectionId, pillarId);
    }
    
    public IntegrityIssueIterator getOrphanFilesOnPillar(String collectionId, String pillarId, Date cutoffDate) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
        ArgumentValidator.checkNotNullOrEmpty(pillarId, "String pillarId");
        ArgumentValidator.checkNotNull(cutoffDate, "Date cutoffDate");
        
        String findOrphansSql = "SELECT fileID from fileinfo"
                + " WHERE collectionID = ?"
                + " AND pillarID = ?"
                + " AND last_seen_getfileids < ?"
                + " AND last_seen_getchecksums < ?";
        
        return makeIntegrityIssueIterator(findOrphansSql, collectionId, pillarId, cutoffDate, cutoffDate);
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
        
        String findMissingFilesSql = "SELECT DISTINCT(fileID) FROM fileinfo"
                + " WHERE collectionID = ?"
                + " EXCEPT SELECT fileID FROM fileinfo"
                    + " WHERE collectionID = ?"
                    + " AND pillarID = ?"
                + " ORDER BY fileid"
                + " OFFSET ?"
                + " LIMIT ?";
        
        return makeIntegrityIssueIterator(findMissingFilesSql, collectionId, collectionId, pillarId,
                first, maxResults);
    }
    
    public IntegrityIssueIterator findFilesWithChecksumInconsistincies(String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(collectionId, "String collectionId");
                
        String findInconsistentChecksumsSql = "SELECT fileID FROM ("
                + " SELECT fileID, count(distinct(checksum)) as checksums FROM fileinfo"
                + " WHERE collectionID = ?"
                + " GROUP BY fileID) as subselect"
                //+ " GROUP BY fileID, collectionID) as subselect"
                + " WHERE checksums > 1";
        
        return makeIntegrityIssueIterator(findInconsistentChecksumsSql, collectionId);
    }
    
    public List<FileInfo> getFileInfosForFile(String fileId, String collectionId) {
        ArgumentValidator.checkNotNullOrEmpty(fileId, "String fileId");
        ArgumentValidator.checkNotNullOrEmpty(fileId, "String collectionId");
        
        List<FileInfo> res = new ArrayList<FileInfo>();
        String getFileInfoSql = "SELECT pillarID, filesize, checksum, file_timestamp, checksum_timestamp FROM fileinfo"
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
                    
                    FileInfo f = new FileInfo(fileId, CalendarUtils.getXmlGregorianCalendar(lastFileCheck), checksum, 
                            fileSize, CalendarUtils.getXmlGregorianCalendar(lastChecksumCheck), pillarId,
                            null, null);
                    res.add(f);
                }
            } 
        } catch (SQLException e) {
            throw new IllegalStateException("Could not retrieve the FileInfo for '" + fileId + "' with the SQL '"
                    + getFileInfoSql + "'.", e);
        }
        return res;
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
