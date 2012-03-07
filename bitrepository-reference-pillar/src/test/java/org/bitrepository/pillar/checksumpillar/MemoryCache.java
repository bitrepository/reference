package org.bitrepository.pillar.checksumpillar;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.pillar.checksumpillar.cache.ChecksumCache;

/**
 * Very simple memory based implementation of the ChecksumCache.
 * Everything is kept within a map between the file ids and their checksum.
 */
public class MemoryCache implements ChecksumCache {
    
    /**
     * The checksum mapping between the file ids and their checksum.
     */
    private Map<String, String> checksumMap = new HashMap<String, String>();
    
    public MemoryCache() {}
    
    @Override
    public String getChecksum(String fileId) {
        return checksumMap.get(fileId);
    }
    
    @Override
    public Collection<String> getFileIDs(FileIDs fileIds) {
        if(fileIds.isSetFileID()) {
            if(checksumMap.containsKey(fileIds.getFileID())) {
                return Arrays.asList(fileIds.getFileID());
            } else {
                return new ArrayList<String>();
            }
        }
        
        return checksumMap.keySet();
    }
    
    @Override
    public Map<String, Date> getLastModifiedDate(FileIDs fileIds) {
        Map<String, Date> res = new HashMap<String, Date>();
        
        for(String fileId : getFileIDs(fileIds)) {
            res.put(fileId, new Date());
        }
        
        return res;
    }
    
    @Override
    public void putEntry(String fileId, String checksum) {
        checksumMap.put(fileId, checksum);
    }
    
    @Override
    public void deleteEntry(String fileId) {
        checksumMap.remove(fileId);
    }
    
    @Override
    public void replaceEntry(String fileId, String oldChecksum, String newChecksum) {
        if(checksumMap.get(fileId) != oldChecksum) {
            throw new IllegalStateException("Cannot replace the entry for '" + fileId + "' since it does not "
                    + "have the checksum '" + oldChecksum + "'.");
        }
        
        checksumMap.put(fileId, newChecksum);
    }
    
    @Override
    public boolean hasFile(String fileId) {
        return checksumMap.containsKey(fileId);
    }
    
    /**
     * Removes every entry in the cache.
     * Used for cleaning up between tests.
     */
    public void cleanUp() {
        checksumMap.clear();
    }
}
