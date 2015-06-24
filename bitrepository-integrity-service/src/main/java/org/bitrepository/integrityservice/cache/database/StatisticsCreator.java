package org.bitrepository.integrityservice.cache.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.CollectionStat;
import org.bitrepository.integrityservice.cache.PillarCollectionStat;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to handle the creation of a new statistics entry.
 * The entries covers the entry point (stat) and detailed statistics for the collection and each of its pillars. 
 */
public class StatisticsCreator {

    private final String insertStatisticEntrySql = "INSERT INTO stats (stat_time, last_update, collectionID)"
            + " VALUES (?, ?, ?)";
    
    private final String insertCollectionStatEntrySql = "INSERT INTO collectionstats"
            + " (stat_key, file_count, file_size, checksum_errors_count, latest_file_date)"
            + " (SELECT MAX(stat_key), ?, ?, ?, ? FROM stats WHERE collectionID = ?)";
    
    private final String insertPillarStatEntrySql = "INSERT INTO pillarstats"
            + " (stat_key, pillarID, file_count, file_size, missing_files_count, "
                + "checksum_errors_count, missing_checksums_count, obsolete_checksums_count)"
            + " (SELECT MAX(stat_key), ?, ?, ?, ?, ?, ?, ? FROM stats WHERE collectionID = ?)";
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    private final String collectionID;
    private final Connection conn;
    private PreparedStatement insertStatisticsEntryPS;
    private PreparedStatement insertCollectionStatPS;
    private PreparedStatement insertPillarStatPS;
    
    public StatisticsCreator(Connection dbConnection, String collectionID) {
        this.collectionID = collectionID;
        conn = dbConnection;
    }
    
    private void init() throws SQLException {
        conn.setAutoCommit(false);
        insertStatisticsEntryPS = conn.prepareStatement(insertStatisticEntrySql);
        insertCollectionStatPS = conn.prepareStatement(insertCollectionStatEntrySql);
        insertPillarStatPS = conn.prepareStatement(insertPillarStatEntrySql);
    }
    
    /**
     * Method to handle the actual update.  
     */
    public void createStatistics(StatisticsCollector statisticsCollector) {
        try {
            init();
            log.debug("Initialized statisticsCreator");
            try {
                Date statisticsTime = statisticsCollector.getCollectionStat().getStatsTime();
                Date now = new Date();
                insertStatisticsEntryPS.setTimestamp(1, new Timestamp(statisticsTime.getTime()));
                insertStatisticsEntryPS.setTimestamp(2, new Timestamp(now.getTime()));
                insertStatisticsEntryPS.setString(3, collectionID);
                
                addCollectionStatistics(statisticsCollector.getCollectionStat());
                List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
                for(String pillar : pillars) {
                    addPillarStat(statisticsCollector.getPillarCollectionStat(pillar));
                }
                log.debug("Done building statistics batch");
                execute();
                log.debug("Done executing statistics batch");
            } finally {
                close();
            }
        } catch (SQLException e) {
            log.error("Failed to update files", e);
        }
    } 
    
    private void addCollectionStatistics(CollectionStat cs) throws SQLException {
        insertCollectionStatPS.setLong(1, cs.getFileCount());
        insertCollectionStatPS.setLong(2, cs.getDataSize());
        insertCollectionStatPS.setLong(3, cs.getChecksumErrors());
        insertCollectionStatPS.setTimestamp(4, new Timestamp(cs.getLatestFileTime().getTime()));
        insertCollectionStatPS.setString(5, cs.getCollectionID());
    }
    
    private void addPillarStat(PillarCollectionStat ps) throws SQLException {
        insertPillarStatPS.setString(1, ps.getPillarID());
        insertPillarStatPS.setLong(2, ps.getFileCount());
        insertPillarStatPS.setLong(3, ps.getDataSize());
        insertPillarStatPS.setLong(4, ps.getMissingFiles());
        insertPillarStatPS.setLong(5, ps.getChecksumErrors());
        insertPillarStatPS.setLong(6, ps.getMissingChecksums());
        insertPillarStatPS.setLong(7, ps.getObsoleteChecksums());
        insertPillarStatPS.setString(8, ps.getCollectionID());
        insertPillarStatPS.addBatch();
    }
    
    
    
    private void execute() throws SQLException {
        insertStatisticsEntryPS.execute();
        insertCollectionStatPS.execute();
        insertPillarStatPS.executeBatch();
        conn.commit();
    }
    
    private void close() throws SQLException {
        if(insertStatisticsEntryPS != null) {
        	insertStatisticsEntryPS.close();
        }
        if(insertCollectionStatPS != null) {
            insertCollectionStatPS.close();
        }
        if(insertPillarStatPS != null) {
            insertPillarStatPS.close();
        }
        if(conn != null) {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

}
