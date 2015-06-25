package org.bitrepository.integrityservice.cache;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDAOFactory;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrityDatabase implements IntegrityModel {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final IntegrityDAO store;
    private final Settings settings;

    public IntegrityDatabase(Settings settings) {
        this.settings = settings;
        this.store = IntegrityDAOFactory.getDAO2Instance(settings);
    }

    @Override
    public void addFileIDs(FileIDsData data, String pillarId, String collectionId) {
        store.updateFileIDs(data, pillarId, collectionId);
    }

    @Override
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId, String collectionId) {
        store.updateChecksums(data, pillarId, collectionId);
    }

    @Override
    public Collection<FileInfo> getFileInfos(String fileId, String collectionId) {
        return store.getFileInfosForFile(fileId, collectionId);
    }
    
    @Override
    public void resetFileCollectionProgress(String collectionId) {
        store.resetFileCollectionProgress(collectionId);
    }
    
    @Override
    public void resetChecksumCollectionProgress(String collectionId) {
        store.resetChecksumCollectionProgress(collectionId);
    }

    @Override
    public long getNumberOfFilesInCollection(String collectionId) {
        return store.getNumberOfFilesInCollection(collectionId);
    }

    @Override
    public long getNumberOfFiles(String pillarId, String collectionId) {
        return store.getNumberOfFilesInCollectionAtPillar(collectionId, pillarId);
    }

    @Override
    public IntegrityIssueIterator getFilesOnPillar(String pillarId, long firstIndex, 
            long maxResults, String collectionId) {
        return store.getAllFileIDsOnPillar(collectionId, pillarId, firstIndex, maxResults);
    }

    @Override
    public IntegrityIssueIterator getMissingFilesAtPillarByIterator(String pillarId, long firstIndex, 
            long maxResults, String collectionId) {
        return store.findMissingFilesAtPillar(collectionId, pillarId, firstIndex, maxResults);
    }
    
    @Override
    public void deleteFileIdEntry(String collectionId, String pillarId, String fileId) {
        store.removeFile(collectionId, pillarId, fileId);
    }
    
    @Override
    public IntegrityIssueIterator findFilesWithMissingChecksum(String collectionId, String pillarId, Date cutoffDate) {
        return store.getFilesWithMissingChecksums(collectionId, pillarId, cutoffDate);
    }

    @Override
    public IntegrityIssueIterator findChecksumsOlderThan(Date date, String pillarID, String collectionId) {
        return store.getFilesWithOutdatedChecksums(collectionId, pillarID, date);
    }
    
    @Override
    public IntegrityIssueIterator findOrphanFiles(String collectionId, String pillarId, Date cutoffDate) {
        return store.getOrphanFilesOnPillar(collectionId, pillarId, cutoffDate);
    }

    @Override
    public IntegrityIssueIterator getFilesWithInconsistentChecksums(String collectionId) {
        return store.findFilesWithChecksumInconsistincies(collectionId);
    }

    @Override
    public Date getDateForNewestFileEntryForCollection(String collectionId) {
        return store.getLatestFileDateInCollection(collectionId);
    }

    @Override
    public Date getDateForNewestFileEntryForPillar(String pillarId, String collectionId) {
        return store.getLatestFileDate(collectionId, pillarId);
    }

    @Override
    public Date getDateForNewestChecksumEntryForPillar(String pillarId, String collectionId) {
        return store.getLatestChecksumDate(collectionId, pillarId);
    }

    @Override
    public Long getCollectionFileSize(String collectionId) {
        return store.getCollectionSize(collectionId);
    }

    @Override
    public List<CollectionStat> getLatestCollectionStat(String collectionID, int count) {
        return store.getLatestCollectionStats(collectionID, count);
    }

    @Override
    public List<PillarCollectionStat> getLatestPillarStats(String collectionID) {
        return store.getLatestPillarStats(collectionID);
    }

    @Override
    public void close() {
        store.close();
    }

    @Override
    public Long getCollectionFileSizeAtPillar(String collectionId, String pillarId) {
        return store.getCollectionSizeAtPillar(collectionId, pillarId);
    }

    @Override
    public void createStatistics(String collectionId, StatisticsCollector statisticsCollector) {
        store.createStatistics(collectionId, statisticsCollector);
    }
}
