/*
 * #%L
 * Bitrepository Integrity Client
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;

/**
 * Store of cached integrity information.
 */
public interface IntegrityModel {
    /**
     * Add file ID data to cache.
     * @param data The received data.
     * @param pillarId The id of the pillar the received data comes from.
     * @param collectionId The id of the collection the received data belongs to.
     */
    void addFileIDs(FileIDsData data, String pillarId, String collectionId);

    /**
     * Add checksum data to cache.
     * @param data The received data.
     * @param pillarId The id of the pillar the received data comes from.
     * @param collectionId The id of the collection the received data belongs to.
     */
    void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, String pillarId, String collectionId);

    /**
     * Retrieves the information of a given file id for all pillars.
     * @param fileId The id of the file. 
     * @param collectionId The id of the collection the file belongs to
     * @return The collection of information about this file.
     */
    Collection<FileInfo> getFileInfos(String fileId, String collectionId);
    
    /**
     * Reset the status of fileID collection
     * @param collectionId The collection to reset the fileID collection status of
     */
    void resetFileCollectionProgress(String collectionId);
    
    /**
     * Reset the status of the checksum collection
     * @param collectionId The collection to reset the checksum collection status of
     */
    void resetChecksumCollectionProgress(String collectionId);
    
    /**
     * Retrieves the number of files in a collection
     * @param collectionId The ID of the collection
     * @return The number of files in the collection
     */
    long getNumberOfFilesInCollection(String collectionId);
    
    /**
     * Retrieves the number of files on a pillar in a collection
     * @param pillarId The pillar.
     * @param collectionId The ID of the collection to get the number of files from
     * @return Retrieves the number of files in the state 'EXISTING' or 'PREVIOUSLY_SEEN' at a given pillar.
     */
    long getNumberOfFiles(String pillarId, String collectionId);
    
    /**
     * Get an IntegrityIssueIterator for the files for a given pillar, restricted by min and max ids.
     * @param pillarId The id of the pillar.
     * @param firstIndex The index of the first result.
     * @param maxResults The maximum number of results.
     * @param collectionId The ID of the collection to get files from
     * @return The list of file ids for the pillar, between min and max.
     */
    IntegrityIssueIterator getFilesOnPillar(String pillarId, long firstIndex, long maxResults, String collectionId);
    
    /**
     * An IntegrityIssueIterator for missing files for a given pillar, restricted by min and max ids.
     * @param pillarId The id of the pillar.
     * @param firstIndex The first index to get results from.
     * @param maxResults The maximum number of results.
     * @param collectionId The ID of the collection to get missing files from
     * @return The IntegrityIssueIterator for missing file ids for the pillar, between min and max.
     */
    IntegrityIssueIterator getMissingFilesAtPillarByIterator(String pillarId, long firstIndex, long maxResults, 
            String collectionId);

    /**
     * Removes the record of the given fileId for the given pillar
     * @param collectionId The id of the collection the file belongs to.
     * @param pillarId The id of the pillar that the record is about
     * @param fileId The id of the file to be removed from cache.
     */
    void deleteFileIdEntry(String collectionId, String pillarId, String fileId);
    
    /**
     * Locates the files which exists but are missing their checksum on a given pillar.
     * Checksums are considered missing if they are not present (NULL in the database), or the checksum
     * entry have not been updated since a given date. 
     * @param collectionId The collection in which to look for missing checksums
     * @param pillarId The pillar on which to look for missing checksums
     * @param cutoffDate The latest date that the checksums should have been seen to not be considered missing
     * @return The IntegrityIssueIterator of file ids for the files which exists but are missing their checksum at any pillar.
     */
    IntegrityIssueIterator findFilesWithMissingChecksum(String collectionId, String pillarId, Date cutoffDate);
    
    /**
     * Locates the id of all the files which are older than a given date.
     * @param date The date for the checksum to be older than.
     * @param pillarID The pillar to get checksums from
     * @param collectionId The collection to where the files belong
     * @return The IntegrityIssueIterator of ids for the files which have an old checksum.
     */
    IntegrityIssueIterator findChecksumsOlderThan(Date date, String pillarID, String collectionId);
    
    /**
     * Finds orphan files on a pillar in a given collection, i.e. files that no longer exists on the pillar
     * @param collectionId, The ID of the collection in which to find orphan files.
     * @param pillarId The ID of the pillar on which to look for orphan files.
     * @param cutoffDate The date after which the file need to have been updated to not be considered orphan
     * @return The list of orphan files   
     */
    IntegrityIssueIterator findOrphanFiles(String collectionID, String pillarId, Date cutoffDate);
    
    /**
     * Retrieves the list of file ids for the files, where the pillars does not agree about the checksums.
     * @param The ID of the collection in which to get files from.
     * @return The IntegrityIssueIterator of file ids for the files with inconsistent checksums.
     */
    IntegrityIssueIterator getFilesWithInconsistentChecksums(String collectionId);
      
    /**
     * Retrieves the date for the latest file entry for a given collection.
     * E.g. the date for the latest file which has been positively identified as existing in the collection.  
     * @param collectionId The ID of the collection to look in
     * @return The requested date.
     */
     Date getDateForNewestFileEntryForCollection(String collectionId);
    
    /**
     * Retrieves the date for the latest file entry for a given pillar.
     * E.g. the date for the latest file which has been positively identified as existing on the given pillar.  
     * @param pillarId The pillar whose latest file entry is requested.
     * @param collectionId The ID of the collection to look in
     * @return The requested date.
     */
    Date getDateForNewestFileEntryForPillar(String pillarId, String collectionId);
    
    /**
     * Retrieves the date for the latest checksum entry for a given pillar.
     * E.g. the date for the latest checksum which has been positively identified as valid on the given pillar.  
     * @param pillarId The pillar whose latest checksum entry is requested.
     * @param collectionId The ID of the collection to look in
     * @return The requested date.
     */
    Date getDateForNewestChecksumEntryForPillar(String pillarId, String collectionId);
    
    /**
     * Retrieves the accumulated size of the files in the given collection
     * @param collectionId The ID of the collection
     * @return The accumulated size of the files in the collection.
     */
    Long getCollectionFileSize(String collectionId);
    
    /**
     * Retrieves the accumulated size of the files in the given collection
     * @param collectionId The ID of the collection
     * @return The accumulated size of the files in the collection.
     */
    Long getCollectionFileSizeAtPillar(String collectionId, String pillarId);
    
    /**
     *  Retrieves the latest collection statistics for the given collection
     *  @param collectionID The ID of the collection
     *  @return {@link CollectionStat} The latest collection statistics object for the collection
     */
    List<CollectionStat> getLatestCollectionStat(String collectionID, int count);
    
    /**
     * Retrieves the lastest statistics for the set of pillars in the given collection
     * @param collectionID The ID of the collection
     * @return {@link PillarCollectionStat} The latest pillar statistics for the pillars in the collection 
     */
    List<PillarCollectionStat> getLatestPillarStats(String collectionID);
    
    /**
     * Shutdown the model. This will typically consist of closing DB connections.
     */
    void close();

    /**
     * Create the statistics entries in the database based on the information from the StatisticsCollector
     * @param statisticsCollector the collection with information needed to create the statistics entries. 
     */
    void createStatistics(String collectionId,  StatisticsCollector statisticsCollector);
    
    /**
     * Get the earliest date a specific file on any pillar in a given collection. 
     * @param collectionId The ID of the collection
     * @param fileID The ID of the file
     */
    Date getEarlistFileDate(String collectionId, String fileID);
}
