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
package org.bitrepository.integrityclient;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.integrityclient.cache.FileInfo;
import org.bitrepository.integrityclient.cache.IntegrityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple integrity model which stores its data in the memory.
 * 
 * NOT INTENDED FOR PRODUCTION!!!
 * 
 * This does not handle anything about CollectionID.
 */
public class TestIntegrityModel implements IntegrityModel {
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
    public TestIntegrityModel() {
        log.info("Instantiating " + this.getClass().getSimpleName());
    }
    
    @Override
    public void addFileIDs(FileIDsData data, String pillarId) {
        for(FileIDsDataItem fileId : data.getFileIDsDataItems().getFileIDsDataItem()) {
            log.debug("Adding/updating fileId '" + fileId.getFileID() + "' for the pillar '" + pillarId + "'");
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
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, ChecksumSpecTYPE checksumType, 
            String pillarId) {
        for(ChecksumDataForChecksumSpecTYPE checksumResult : data) {
            log.debug("Adding/updating checksums for file '" + checksumResult.getFileID() + "' for pillar '" 
                    + pillarId + "'");
            
            if(!cache.containsKey(checksumResult.getFileID())) {
                instantiateFileInfoListForFileId(checksumResult.getFileID());
            }
            updateChecksum(checksumResult, checksumType, pillarId);
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
    public List<FileInfo> getFileInfos(String fileId) {
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
        private List<FileInfo> fileIDInfos;
        
        /**
         * Constructor. Initializes and empty list of FileIDInfos.
         */
        CollectionFileIDInfo() {
            fileIDInfos = new ArrayList<FileInfo>();
        }
        
        /**
         * Updates the FileIDInfo for a given pillar based on the results of a GetFileIDs operation.
         * @param fileIdData The resulting date from the GetFileIDs operation.
         * @param pillarId The id of the pillar.
         */
        void updateFileIDs(FileIDsDataItem fileIdData, String pillarId) {
            // Extract current file info or create a new one.
            FileInfo currentInfo = null;
            for(int i = 0; i < fileIDInfos.size(); i++) {
                FileInfo fileInfo = fileIDInfos.get(i);
                if(fileInfo.getPillarId().equals(pillarId)) {
                    currentInfo = fileInfo;
                    fileIDInfos.remove(i);
                    i--; // make up for removal
                }
            }
            if(currentInfo == null) {
                currentInfo = new FileInfo(fileIdData.getFileID(), pillarId);
            }
            
            // Update the file info
            currentInfo.setDateForLastFileIDCheck(fileIdData.getLastModificationTime());
            
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
            FileInfo currentInfo = null;
            for(FileInfo fileInfo : fileIDInfos) {
                if(fileInfo.getPillarId().equals(pillarId)) {
                    currentInfo = fileInfo;
                    fileIDInfos.remove(fileInfo);
                }
            }
            if(currentInfo == null) {
                // create a new file info
                currentInfo = new FileInfo(checksumData.getFileID(), checksumData.getCalculationTimestamp(), 
                        new String(checksumData.getChecksumValue()), checksumType, 
                        checksumData.getCalculationTimestamp(), pillarId);
            } else {
                // Update the existing file info
                currentInfo.setDateForLastFileIDCheck(checksumData.getCalculationTimestamp());
                currentInfo.setDateForLastChecksumCheck(checksumData.getCalculationTimestamp());
                currentInfo.setChecksum(new String(checksumData.getChecksumValue()));
                currentInfo.setChecksumType(checksumType);
            }
            
            // put it back into the list and that back into the cache.
            fileIDInfos.add(currentInfo);
        }
        
        /**
         * @return All the FileIDInfos for this given file.
         */
        List<FileInfo> getFileIDInfos() {
            return fileIDInfos;
        }
    }

    @Override
    public long getNumberOfFiles(String pillarId) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getNumberOfMissingFiles(String pillarId) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public long getNumberOfChecksumErrors(String pillarId) {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setFileMissing(String fileId, Collection<String> pillarIds) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setChecksumError(String fileId, Collection<String> pillarIds) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void setChecksumAgreement(String fileId, Collection<String> pillarIds) {
        // TODO Auto-generated method stub
        
    }
}
