package org.bitrepository.integrityclient.cache;

import java.util.ArrayList;
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
    private Map<String, List<FileIDInfo>> cache = Collections.synchronizedMap(new HashMap<String, List<FileIDInfo>>());
    
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
        synchronized(cache) {
            List<FileIDInfo> fileInfos = cache.get(fileIdData.getFileID());
            
            // Extract current file info or create a new one.
            FileIDInfo currentInfo = null;
            for(FileIDInfo fileInfo : fileInfos) {
                if(fileInfo.getPillarId().equals(pillarId)) {
                    currentInfo = fileInfo;
                    fileInfos.remove(fileInfo);
                }
            }
            if(currentInfo == null) {
                currentInfo = new FileIDInfo(fileIdData.getFileID(), pillarId);
            }
            
            // Update the file info
            currentInfo.setDateForLastFileIDCheck(fileIdData.getCreationTimestamp());
            
            // put it back into the list and that back into the cache.
            fileInfos.add(currentInfo);
            cache.put(fileIdData.getFileID(), fileInfos);
        }
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
            List<FileIDInfo> fileInfos = cache.get(checksumData.getFileID());
            
            // Extract current file info and update it or create a new one.
            FileIDInfo currentInfo = null;
            for(FileIDInfo fileInfo : fileInfos) {
                if(fileInfo.getPillarId().equals(pillarId)) {
                    currentInfo = fileInfo;
                    fileInfos.remove(fileInfo);
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
            fileInfos.add(currentInfo);
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
        List<FileIDInfo> fileIdInfo = new ArrayList<FileIDInfo>();
        cache.put(fileId, fileIdInfo);
    }
    
    @Override
    public List<FileIDInfo> getFileInfo(String fileId) {
        return cache.get(fileId);
    }
}
