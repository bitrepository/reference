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
package org.bitrepository.integrityservice.cache;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileInfosDataItem;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.database.IntegrityDAO;
import org.bitrepository.integrityservice.cache.database.IntegrityDAOFactory;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class IntegrityDatabase implements IntegrityModel {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final IntegrityDAO store;

    public IntegrityDatabase(Settings settings) {
        IntegrityDAOFactory daoFactory = new IntegrityDAOFactory();
        this.store = daoFactory.getIntegrityDAOInstance(
                settings.getReferenceSettings().getIntegrityServiceSettings().getIntegrityDatabase());
    }

    @Override
    public void addFileIDs(FileIDsData data, String pillarID, String collectionID) {
        store.updateFileIDs(data, pillarID, collectionID);
    }

    @Override
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarID, String collectionID) {
        store.updateChecksums(data, pillarID, collectionID);
    }

    @Override
    public void addFileInfos(List<FileInfosDataItem> data, String pillarID, String collectionID) {
        store.updateFileInfos(data, pillarID, collectionID);
    }

    @Override
    public Collection<FileInfo> getFileInfos(String fileID, String collectionID) {
        return store.getFileInfosForFile(fileID, collectionID);
    }

    @Override
    public void resetFileCollectionProgress(String collectionID) {
        store.resetFileCollectionProgress(collectionID);
    }

    @Override
    public void resetChecksumCollectionProgress(String collectionID) {
        store.resetChecksumCollectionProgress(collectionID);
    }

    @Override
    public void resetFileInfoCollectionProgress(String collectionID) {
        store.resetFileInfoCollectionProgress(collectionID);
    }

    @Override
    public long getNumberOfFilesInCollection(String collectionID) {
        return store.getNumberOfFilesInCollection(collectionID);
    }

    @Override
    public IntegrityIssueIterator getFilesOnPillar(String pillarID, long firstIndex,
                                                   long maxResults, String collectionID) {
        return store.getAllFileIDsOnPillar(collectionID, pillarID, firstIndex, maxResults);
    }

    @Override
    public IntegrityIssueIterator findFilesWithMissingCopies(String collectionID, int expectedCopies,
                                                             Long firstIndex, Long maxResults) {
        return store.findFilesWithMissingCopies(collectionID, expectedCopies, firstIndex, maxResults);
    }

    @Override
    public void deleteFileIdEntry(String collectionID, String pillarID, String fileID) {
        store.removeFile(collectionID, pillarID, fileID);
    }

    @Override
    public IntegrityIssueIterator findFilesWithMissingChecksum(String collectionID, String pillarID, Date cutoffDate) {
        return store.getFilesWithMissingChecksums(collectionID, pillarID, cutoffDate);
    }

    @Override
    public IntegrityIssueIterator findChecksumsOlderThan(Date date, String pillarID, String collectionID) {
        return store.getFilesWithOutdatedChecksums(collectionID, pillarID, date);
    }

    @Override
    public IntegrityIssueIterator findOrphanFiles(String collectionID, String pillarID, Date cutoffDate) {
        return store.getOrphanFilesOnPillar(collectionID, pillarID, cutoffDate);
    }

    @Override
    public IntegrityIssueIterator getFilesWithInconsistentChecksums(String collectionID) {
        return store.findFilesWithChecksumInconsistencies(collectionID);
    }

    @Override
    public Date getDateForNewestFileEntryForCollection(String collectionID) {
        return store.getLatestFileDateInCollection(collectionID);
    }

    @Override
    public Date getDateForNewestFileEntryForPillar(String pillarID, String collectionID) {
        return store.getLatestFileDate(collectionID, pillarID);
    }

    @Override
    public Date getDateForNewestChecksumEntryForPillar(String pillarID, String collectionID) {
        return store.getLatestChecksumDate(collectionID, pillarID);
    }

    @Override
    public Long getCollectionFileSize(String collectionID) {
        return store.getCollectionSize(collectionID);
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
    public void createStatistics(String collectionID, StatisticsCollector statisticsCollector) {
        store.createStatistics(collectionID, statisticsCollector);
    }

    @Override
    public Date getEarliestFileDate(String collectionID, String fileID) {
        return store.getEarliestFileDate(collectionID, fileID);
    }

    @Override
    public String getFileIDAtPosition(String collectionID, Long position) {
        return store.getFileIdAtIndex(collectionID, position);
    }

    @Override
    public Map<String, PillarCollectionMetric> getPillarCollectionMetrics(String collectionID) {
        return store.getPillarCollectionMetrics(collectionID);
    }
}
