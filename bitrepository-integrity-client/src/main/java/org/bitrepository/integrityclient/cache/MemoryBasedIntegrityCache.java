/*
 * #%L
 * Bitrepository Integrity Client
 * 
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
package org.bitrepository.integrityclient.cache;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumsDataGroupedByChecksumSpec;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple integrity cache which stores its data in the memory.
 * 
 * NOT INTENDED FOR PRODUCTION!!!
 * Should only be used for testing purposes.
 * TODO move to test?
 * 
 * This does not handle anything about CollectionID.
 */
public class MemoryBasedIntegrityCache implements CachedIntegrityInformationStorage {
    /** The singleton instance of this class.*/
    private static MemoryBasedIntegrityCache instance;
    
    /** 
     * @return Retrieves the singleton instance of this class.
     */
    public static synchronized MemoryBasedIntegrityCache getInstance() {
        if(instance == null) {
            instance = new MemoryBasedIntegrityCache();
        }
        return instance;
    }
    
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * The memory cache for containing the information about the files in the system.
     * Synchronized for avoiding threading problems.
     */
    private Map<String, CollectionFileIDInfo> cache = Collections.synchronizedMap(new HashMap<String, 
            CollectionFileIDInfo>());
    
    /**
     * Constructor.
     */
    private MemoryBasedIntegrityCache() {
        log.info("Instantiating " + this.getClass().getSimpleName());
    }
    
    @Override
    public void addFileIDs(FileIDsData data, String pillarId) {
        for(FileIDsDataItem fileId : data.getFileIDsDataItems().getFileIDsDataItem()) {
            log.debug("Adding/updating fileId " + fileId.getFileID());
            if(!cache.containsKey(fileId.getFileID())) {
                instantiateFileInfoListForFileId(fileId.getFileID());
            }
            updateFileId(fileId, pillarId);
        }
    }
    
    /**
     * Updates the file info for a given file id.
     * @param fileIdData The file id data to update with.
     * @param pillarId The id of pillar who delivered these file id data.
     */
    private void updateFileId(FileIDsDataItem fileIdData, String pillarId) {
        CollectionFileIDInfo fileInfos = cache.get(fileIdData.getFileID());
        if(fileInfos == null) {
            fileInfos = new CollectionFileIDInfo();
        }
        
        fileInfos.updateFileIDs(fileIdData, pillarId);
        cache.put(fileIdData.getFileID(), fileInfos);
        
    }
    
    @Override
    public void addChecksums(ChecksumsDataGroupedByChecksumSpec data, String pillarId) {
        for(ChecksumDataForChecksumSpecTYPE checksumResult : data.getChecksumDataForChecksumSpec()) {
            log.debug("Adding/updating checksums for file '" + checksumResult.getFileID() + "'");
            
            if(!cache.containsKey(checksumResult.getFileID())) {
                instantiateFileInfoListForFileId(checksumResult.getFileID());
            }
            updateChecksum(checksumResult, data.getChecksumSpec(), pillarId);
        }
    }
    
    /**
     * Updates a file info with checksum results.
     * @param checksumData The results of a checksum calculation.
     * @param checksumType The type of checksum calculation (e.g. algorithm and optionally salt).
     * @param pillarId The id of the pillar, where it has been calculated.
     */
    private void updateChecksum(ChecksumDataForChecksumSpecTYPE checksumData, ChecksumSpecTYPE checksumType, 
            String pillarId) {
        synchronized(cache) {
            CollectionFileIDInfo fileInfos = cache.get(checksumData.getFileID());
            
            fileInfos.updateChecksums(checksumData, checksumType, pillarId);
            cache.put(checksumData.getFileID(), fileInfos);
        }
    }
    
    /**
     * Instantiates a new List for the file id infos for a given file id.
     * @param fileId The id of the file to be inserted into the cache. 
     */
    private synchronized void instantiateFileInfoListForFileId(String fileId) {
        if(cache.containsKey(fileId)) {
            log.warn("Attempt to instantiate file, which already exists, averted");
            return;
        }
        CollectionFileIDInfo fileIdInfo = new CollectionFileIDInfo();
        cache.put(fileId, fileIdInfo);
    }
    
    @Override
    public List<FileIDInfo> getFileInfo(String fileId) {
        return cache.get(fileId).getFileIDInfos();
    }

    @Override
    public Collection<String> getAllFileIDs() {
        return cache.keySet();
    }
    
    /**
     * Clean the cache for test purposes only!
     */
    public void clearCache() {
        cache.clear();
    }

    /**
     * Container for the collection of FileIDInfos for a single file.
     * 
     * All functions are package-protected.
     */
    private class CollectionFileIDInfo {
        /** The collection of FileIDInfos.*/
        private List<FileIDInfo> fileIDInfos;
        
        /**
         * Constructor. Initializes and empty list of FileIDInfos.
         */
        CollectionFileIDInfo() {
            fileIDInfos = new ArrayList<FileIDInfo>();
        }
        
        /**
         * Updates the FileIDInfo for a given pillar based on the results of a GetFileIDs operation.
         * @param fileIdData The resulting date from the GetFileIDs operation.
         * @param pillarId The id of the pillar.
         */
        void updateFileIDs(FileIDsDataItem fileIdData, String pillarId) {
            // Extract current file info or create a new one.
            FileIDInfo currentInfo = null;
            for(int i = 0; i < fileIDInfos.size(); i++) {
                FileIDInfo fileInfo = fileIDInfos.get(i);
                if(fileInfo.getPillarId().equals(pillarId)) {
                    currentInfo = fileInfo;
                    fileIDInfos.remove(i);
                    i--; // make up for removal
                }
            }
            if(currentInfo == null) {
                currentInfo = new FileIDInfo(fileIdData.getFileID(), pillarId);
            }
            
            // Update the file info
            currentInfo.setDateForLastFileIDCheck(fileIdData.getCreationTimestamp());
            
            // put it back into the list and that back into the cache.
            fileIDInfos.add(currentInfo);
        }
        
        /**
         * Updates the FileIDInfo for a given pillar based on the results of a GetChecksums operation.
         * Also updates the 'latestChecksumUpdate'.
         * 
         * @param checksumData The results of the GetChecksumOperation for this given file.
         * @param checksumType The type of checksum (e.g. algorithm and optional salt).
         * @param pillarId The id of the pillar.
         */
        void updateChecksums(ChecksumDataForChecksumSpecTYPE checksumData, ChecksumSpecTYPE checksumType,
                String pillarId) {
            
            // Extract current file info and update it or create a new one.
            FileIDInfo currentInfo = null;
            for(FileIDInfo fileInfo : fileIDInfos) {
                if(fileInfo.getPillarId().equals(pillarId)) {
                    currentInfo = fileInfo;
                    fileIDInfos.remove(fileInfo);
                }
            }
            if(currentInfo == null) {
                // create a new file info
                currentInfo = new FileIDInfo(checksumData.getFileID(), checksumData.getCalculationTimestamp(), 
                        checksumData.getChecksumValue(), checksumType, checksumData.getCalculationTimestamp(), 
                        pillarId);
            } else {
                // Update the existing file info
                currentInfo.setDateForLastFileIDCheck(checksumData.getCalculationTimestamp());
                currentInfo.setDateForLastChecksumCheck(checksumData.getCalculationTimestamp());
                currentInfo.setChecksum(checksumData.getChecksumValue());
                currentInfo.setChecksumType(checksumType);
            }
            
            // put it back into the list and that back into the cache.
            fileIDInfos.add(currentInfo);
        }
        
        /**
         * @return All the FileIDInfos for this given file.
         */
        List<FileIDInfo> getFileIDInfos() {
            return fileIDInfos;
        }
    }
}
