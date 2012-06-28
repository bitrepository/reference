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
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;

/**
 * Store of cached integrity information.
 */
public interface IntegrityModel {
    /**
     * Add file ID data to cache.
     * @param data The received data.
     * @param expectedFileIDs The expected FileIDs. Those missing will be set to 'missing'.
     * @param pillarId The id of the pillar the received data comes from.
     */
    void addFileIDs(FileIDsData data, FileIDs expectedFileIDs, String pillarId);

    /**
     * Add checksum data to cache.
     * @param data The received data.
     * @param pillarId The id of the pillar the received data comes from.
     */
    void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, ChecksumSpecTYPE checksumType, String pillarId);

    /**
     * Retrieves the information of a given file id for all pillars.
     * @param fileId The id of the file. 
     * @return The collection of information about this file.
     */
    Collection<FileInfo> getFileInfos(String fileId);
    
    /**
     * Retrieves all the file ids in the collection.
     * @return The collection of file ids.
     */
    Collection<String> getAllFileIDs();
    
    /**
     * @param pillarId The pillar.
     * @return Retrieves the number of files in the state 'EXISTING' at a given pillar.
     */
    long getNumberOfFiles(String pillarId);
    
    /**
     * @param pillarId The pillar.
     * @return Retrieves the number of files in the state 'MISSING' at a given pillar.
     */
    long getNumberOfMissingFiles(String pillarId);
    
    /**
     * @param pillarId The pillar.
     * @return The number of files with checksum state 'ERROR' for the given pillar.
     */
    long getNumberOfChecksumErrors(String pillarId);
    
    /**
     * Sets the file to be missing at the given pillars.
     * @param fileId The id of the file, which is missing at some pillars.
     * @param pillarIds The ids of the pillars, where the file is missing.
     */
    void setFileMissing(String fileId, Collection<String> pillarIds);
    
    /**
     * Sets the checksum state of the file to be erroneous at the given pillars.
     * @param fileId The id of the file, which has erroneous checksum at some pillars.
     * @param pillarIds The ids of the pillars, where the file has a erroneous checksum.
     */
    void setChecksumError(String fileId, Collection<String> pillarIds);
    
    /**
     * Sets the checksum state of the file to be valid at the given pillars.
     * @param fileId The id of the file, which has valid checksum at some pillars.
     * @param pillarIds The ids of the pillars, where the file has a valid checksum.
     */
    void setChecksumAgreement(String fileId, Collection<String> pillarIds);
    
    /**
     * Removes a given file id from the cache.
     * @param fileId The id of the file to be removed from cache.
     */
    void deleteFileIdEntry(String fileId);
    
    /**
     * Locates the files which exists but are missing their checksum at any pillar.
     * @return The list of file ids for the files which exists but are missing their checksum at any pillar.
     */
    List<String> findMissingChecksums();
    
    /**
     * Locates the id of all the files which are older than a given date.
     * @param date The date for the checksum to be old than.
     * @return The list of ids for the files which have an old checksum.
     */
    Collection<String> findChecksumsOlderThan(Date date);
    
    /**
     * Locates the files which are missing at any pillar.
     * @return The list of file ids for the files which are missing at any pillar.
     */
    List<String> findMissingFiles();
    
    /**
     * Checks whether a given file is missing and returns the list of pillars, where it is missing.
     * @param fileId The id of the file to check whether it is missing.
     * @return The list of pillars where it is missing (empty list, if not missing at any pillar).
     */
    List<String> isMissing(String fileId);
}
