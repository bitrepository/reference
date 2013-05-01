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
     * Retrieves all the file ids in the collection.
     * @param collectiondId The ID of the collection from which to get all fileIDs
     * @return The collection of file ids.
     */
    Collection<String> getAllFileIDs(String collectionId);
    
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
     * @return Retrieves the number of files in the state 'EXISTING' at a given pillar.
     */
    long getNumberOfFiles(String pillarId, String collectionId);
    
    /**
     * A list of files for a given pillar, restricted by min and max ids.
     * @param pillarId The id of the pillar.
     * @param minId The minimum result number.
     * @param maxId The maximum result number.
     * @param collectionId The ID of the collection to get files from
     * @return The list of file ids for the pillar, between min and max.
     */
    List<String> getFilesOnPillar(String pillarId, long minId, long maxId, String collectionId);
    
    /**
     * @param pillarId The pillar.
     * @param collectionId The collection to look for missing files in
     * @return Retrieves the number of files in the state 'MISSING' at a given pillar.
     */
    long getNumberOfMissingFiles(String pillarId, String collectionId);
    
    /**
     * A list of missing files for a given pillar, restricted by min and max ids.
     * @param pillarId The id of the pillar.
     * @param minId The minimum result number.
     * @param maxId The maximum result number.
     * @param collectionId The ID of the collection to get missing files from
     * @return The list of missing file ids for the pillar, between min and max.
     */
    List<String> getMissingFilesAtPillar(String pillarId, long minId, long maxId, String collectionId);
    
    /**
     * @param pillarId The pillar.
     * @param collectionId The ID of the collection to look for checksum errors in
     * @return The number of files with checksum state 'ERROR' for the given pillar.
     */
    long getNumberOfChecksumErrors(String pillarId, String collectionId);
    
    
    /**
     * A list of files with checksum error for a given pillar, restricted by min and max ids.
     * @param pillarId The id of the pillar.
     * @param minId The minimum result number.
     * @param maxId The maximum result number.
     * @param collectionId The ID of the collection to get checksum errors from
     * @return The list of ids for the files with checksum errors for the pillar, between min and max.
     */
    List<String> getFilesWithChecksumErrorsAtPillar(String pillarId, long minId, long maxId, String collectionId);

    /**
     * Sets the file to be missing at the given pillars.
     * @param fileId The id of the file, which is missing at some pillars.
     * @param pillarIds The ids of the pillars, where the file is missing.
     * @param collectionId The ID of the collection where the file belongs
     */
    void setFileMissing(String fileId, Collection<String> pillarIds, String collectionId);
    
    /**
     * Sets the checksum state of the file to be erroneous at the given pillars.
     * @param fileId The id of the file, which has erroneous checksum at some pillars.
     * @param pillarIds The ids of the pillars, where the file has a erroneous checksum.
     * @param collectionId The ID of the collection where the file belongs
     */
    void setChecksumError(String fileId, Collection<String> pillarIds, String collectionId);
    
    /**
     * Sets the checksum state of the file to be valid at the given pillars.
     * @param fileId The id of the file, which has valid checksum at some pillars.
     * @param pillarIds The ids of the pillars, where the file has a valid checksum.
     * @param collectionId The ID of the collection that the file belongs to
     */
    void setChecksumAgreement(String fileId, Collection<String> pillarIds, String collectionId);
    
    /**
     * Removes a given file id from the cache.
     * @param fileId The id of the file to be removed from cache.
     * @param collectionId The id of the collection the file belongs to.
     */
    void deleteFileIdEntry(String fileId, String collectionId);
    
    /**
     * Locates the files which exists but are missing their checksum at any pillar.
     * @param collectionId The collection in which to look for missing checksums
     * @return The list of file ids for the files which exists but are missing their checksum at any pillar.
     */
    List<String> findMissingChecksums(String collectionId);
    
    /**
     * Locates the id of all the files which are older than a given date.
     * @param date The date for the checksum to be old than.
     * @param pillarID The pillar to get checksums from
     * @param collectionId The collection to where the files belong
     * @return The list of ids for the files which have an old checksum.
     */
    Collection<String> findChecksumsOlderThan(Date date, String pillarID, String collectionId);
    
    /**
     * Locates the files which are missing at any pillar.
     * @param collectionId The ID of the collection in which to search for missing files
     * @return The list of file ids for the files which are missing at any pillar.
     */
    List<String> findMissingFiles(String collectionId);
    
    /**
     * Checks whether a given file is missing and returns the list of pillars, where it is missing.
     * @param fileId The id of the file to check whether it is missing.
     * @param collectiondId The ID of the collection where the file belongs
     * @return The list of pillars where it is missing (empty list, if not missing at any pillar).
     */
    List<String> getPillarsMissingFile(String fileId, String collectionId);
    
    /**
     * Retrieves the list of file ids for the files, where the pillars does not agree about the checksums.
     * @param The ID of the collection in which to get files from.
     * @return The list of file ids for the files with distinct checksums.
     */
    List<String> getFilesWithInconsistentChecksums(String collectionId);
    
    /**
     * Set the checksum state of a file to 'valid' its entries if the different checksums are unanimous.
     * @param collectionId The ID of the collection in which to change the state
     */
    void setFilesWithConsistentChecksumToValid(String collectionId);
    
    /**
     * Set the file state of all files to 'unknown'.
     * @param collectionId The ID of the collection to work on.
     */
    void setAllFilesToUnknownFileState(String collectionId);

    /**
     * Set the file state of all unknown files to 'missing'.
     * @param collectionId The ID of the collection to work on
     */
    void setOldUnknownFilesToMissing(String collectionId);

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
     *  Retrieves the latest collection statistics for the given collection
     *  @param collectionID The ID of the collection
     *  @return {@link CollectionStat} The latest collection statistics object for the collection
     */
    CollectionStat getLatestCollectionStat(String collectionID);
    
    /**
     * Shutdown the model. This will typically consist of closing DB connections.
     */
    void close();
}
