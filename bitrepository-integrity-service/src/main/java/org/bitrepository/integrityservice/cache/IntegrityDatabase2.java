package org.bitrepository.integrityservice.cache;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO2;
import org.bitrepository.integrityservice.cache.database.IntegrityDAOFactory;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntegrityDatabase2 implements IntegrityModel {
    private Logger log = LoggerFactory.getLogger(this.getClass());

    private final IntegrityDAO2 store;
    private final Settings settings;

    public IntegrityDatabase2(Settings settings) {
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
    public Collection<String> getAllFileIDs(String collectionId) {
        // TODO Auto-generated method stub
        return null;
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
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public long getNumberOfMissingFiles(String pillarId, String collectionId) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IntegrityIssueIterator getMissingFilesAtPillarByIterator(String pillarId, long firstIndex, 
            long maxResults, String collectionId) {
        return store.findMissingFilesAtPillar(collectionId, pillarId, firstIndex, maxResults);
    }

    @Override
    public long getNumberOfChecksumErrors(String pillarId, String collectionId) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public IntegrityIssueIterator getFilesWithChecksumErrorsAtPillar(String pillarId, long firstIndex, 
            long maxResults, String collectionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void setFileMissing(String fileId, Collection<String> pillarIds, String collectionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setChecksumError(String fileId, Collection<String> pillarIds, String collectionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setChecksumAgreement(String fileId, Collection<String> pillarIds, String collectionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteFileIdEntry(String fileId, String collectionId) {
        // TODO Auto-generated method stub

    }
    
    @Override
    public void deleteFileIdEntry(String collectionId, String pillarId, String fileId) {
        store.removeFile(collectionId, pillarId, fileId);
    }

    @Override
    public IntegrityIssueIterator findFilesWithMissingChecksum(String collectionId) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public IntegrityIssueIterator findFilesWithMissingChecksum(String collectionId, String pillarId) {
        return store.getFilesWithMissingChecksums(collectionId, pillarId);
    }

    @Override
    public void setFilesWithUnknownChecksumToMissing(String collectionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public IntegrityIssueIterator findChecksumsOlderThan(Date date, String pillarID, String collectionId) {
        return store.getFilesWithOutdatedChecksums(collectionId, pillarID, date);
    }

    @Override
    public IntegrityIssueIterator findMissingFiles(String collectionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IntegrityIssueIterator findOrphanFiles(String collectionID) {
        // TODO Auto-generated method stub
        return null;
    }
    
    @Override
    public IntegrityIssueIterator findOrphanFiles(String collectionId, String pillarId, Date cutoffDate) {
        return store.getOrphanFilesOnPillar(collectionId, pillarId, cutoffDate);
    }

    @Override
    public List<String> getPillarsMissingFile(String fileId, String collectionId) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public IntegrityIssueIterator getFilesWithInconsistentChecksums(String collectionId) {
        return store.findFilesWithChecksumInconsistincies(collectionId);
    }

    @Override
    public void setFilesWithConsistentChecksumToValid(String collectionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setExistingFilesToPreviouslySeenFileState(String collectionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setExistingChecksumsToPreviouslySeen(String collectionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPreviouslySeenChecksumsToMissing(String collectionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setOldUnknownFilesToMissing(String collectionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPreviouslySeenFilesToMissing(String collectionId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPreviouslySeenFilesToExisting(String collectionId, String pillarId) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setPreviouslySeenChecksumsToUnknown(String collectionId, String pillarId) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean hasFile(String fileId, String collectionId) {
        // TODO Auto-generated method stub
        return false;
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
    public void setChecksumTimestampsToEpocForCollection(String collectionID) {
        // TODO Auto-generated method stub

    }

    @Override
    public Long getCollectionFileSize(String collectionId) {
        return store.getCollectionSize(collectionId);
    }

    @Override
    public Long getPillarDataSize(String pillarID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<CollectionStat> getLatestCollectionStat(String collectionID, int count) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<PillarCollectionStat> getLatestPillarStats(String collectionID) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void makeStatisticsForCollection(String collectionID) {
        // TODO Auto-generated method stub

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
