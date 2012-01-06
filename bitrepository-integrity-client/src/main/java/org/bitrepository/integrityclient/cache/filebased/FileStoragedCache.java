package org.bitrepository.integrityclient.cache.filebased;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.integrityclient.cache.FileInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entity for storing and synchronizing the data cache with the file cache.
 * File format:
 * <br/> file id + ## + pillar id + ## + file id date in millis + ## + checksum + ## + checksum date in millis 
 * + ## + checksum type algorithm + ## + checksum type salt
 */
public class FileStoragedCache {
    /** The separator for the different elements in the string representation of the FileIDInfos.*/
    private static final String SEPARATOR = "##";
    /** The separator for the different elements in the string representation of the FileIDInfos.*/
    private static final boolean APPEND_TO_FILE = true;
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The file for caching the data.*/
    private File store;
    
    /** The latest timestamp for the file.*/
    private long timeStamp;
    
    /**
     * The memory cache for containing the information about the files in the system.
     * Synchronized for avoiding threading problems.
     */
    private Map<String, List<FileInfo>> cache = Collections.synchronizedMap(
            new HashMap<String, List<FileInfo>>());
    
    /**
     * Constructor.
     * @param fileName The name of the file.
     */
    public FileStoragedCache(String fileName) {
        store = new File(fileName);
        try {
            if(store.isFile()) {
                loadCacheFromFile();
            } else {
                store.createNewFile();
            }
        } catch (IOException e) {
            throw new IllegalStateException("Could not instantiate the cache '" + store.getAbsolutePath() 
                    + "'", e);
        }
        
        timeStamp = store.lastModified();
    }
    
    /**
     * Extracts the collection of FileIDInfos for a specific file id.
     * @param fileID The id of the file to find the data cache from.
     * @return The collection of FileIDInfos for the given file id.
     */
    public List<FileInfo> getFileIDInfos(String fileID) {
       return cache.get(fileID);
    }
    
    /**
     * @return The collection of ids for all the files in the cache.
     */
    public Collection<String> getAllFileIDs() {
        return cache.keySet();
    }
    
    /**
     * Tells whether an entry with the given file id and pillar id already exists within the cache.
     * @param fileId The id of the file.
     * @param pillarId The id of the pillar.
     * @return Whether such an entry exists.
     */
    public boolean containsFileIDInfo(String fileId, String pillarId) {
        if(!cache.containsKey(fileId)) {
            return false;
        }
        
        for(FileInfo fileidInfo : cache.get(fileId)) {
            if(fileidInfo.getPillarId().equals(pillarId)) {
                return true;
            }
        }
        
        return false;
    }
    
    
    /**
     * Updates an existing file id into in the cache with a new entry.
     * Will only override the existing entry, if the data has a newer timestamp. E.g. the checksum value is only 
     * overridden if the checksum timestamp is newer than the current one.
     * @param fileidInfo The fileidInfo to update with.
     */
    public void updateFileIDInfoInCache(FileInfo fileidInfo) {
        List<FileInfo> fileidInfos = cache.get(fileidInfo.getFileID());
        if(fileidInfos == null) {
            insertFileIDInfoIntoCache(fileidInfo);
            return;
        }
        
        FileInfo oldIDInfo = null;
        
        // go through and remove any previous entry for the pillar.
        for(int i = 0; i < fileidInfos.size(); i++) {
            if(fileidInfos.get(i).getPillarId().equals(fileidInfo.getPillarId())) {
                oldIDInfo = fileidInfos.get(i);
                fileidInfos.remove(i);
                i--;
            }
        }
        
        if(oldIDInfo != null) {
            // use the newest file id date
            if(oldIDInfo.getDateForLastFileIDCheck().toGregorianCalendar().getTimeInMillis() 
                    < fileidInfo.getDateForLastFileIDCheck().toGregorianCalendar().getTimeInMillis()) {
                oldIDInfo.setDateForLastFileIDCheck(fileidInfo.getDateForLastFileIDCheck());
            }
            
            // use newest checksum
            if(oldIDInfo.getDateForLastChecksumCheck().toGregorianCalendar().getTimeInMillis()
                    < fileidInfo.getDateForLastChecksumCheck().toGregorianCalendar().getTimeInMillis()) {
                oldIDInfo.setChecksum(fileidInfo.getChecksum());
                oldIDInfo.setDateForLastChecksumCheck(fileidInfo.getDateForLastChecksumCheck());
                oldIDInfo.setChecksumType(fileidInfo.getChecksumType());
            }
        } else {
            oldIDInfo = fileidInfo;
        }
        
        fileidInfos.add(oldIDInfo);
        cache.put(fileidInfo.getFileID(), fileidInfos);
    }
    
    /**
     * Converts a FileIDInfo into the following String format:
     * <br/> file id + ## + pillar id + ## + file id date in millis + ## + checksum + ## + checksum date in millis 
     * + ## + checksum type algorithm + ## + checksum type salt
     * @param fileidInfo The FileIDInfo to convert.
     * @return The String representation of the file id info.
     */
    private String convertFileIDInfoToString(FileInfo fileidInfo) {
        StringBuffer res = new StringBuffer();
        res.append(fileidInfo.getFileID());
        res.append(SEPARATOR);
        res.append(fileidInfo.getPillarId());
        
        // append the file id date
        if(fileidInfo.getDateForLastFileIDCheck() == null) {
            return res.toString();
        }
        res.append(SEPARATOR);
        res.append(fileidInfo.getDateForLastFileIDCheck().toGregorianCalendar().getTimeInMillis());
        
        // append checksum
        if(fileidInfo.getChecksum() == null) {
            return res.toString();
        }
        res.append(SEPARATOR);
        res.append(fileidInfo.getChecksum());

        // append checksum date
        if(fileidInfo.getDateForLastChecksumCheck() == null) {
            return res.toString();
        }
        res.append(SEPARATOR);
        res.append(fileidInfo.getDateForLastChecksumCheck().toGregorianCalendar().getTimeInMillis());

        // append checksum type algorithm
        if(fileidInfo.getChecksumType() == null || fileidInfo.getChecksumType().getChecksumType() == null) {
            return res.toString();
        }
        res.append(SEPARATOR);
        res.append(fileidInfo.getChecksumType().getChecksumType());
        
        // append checksum type salt
        if(fileidInfo.getChecksumType() == null || fileidInfo.getChecksumType().getChecksumSalt() == null) {
            return res.toString();
        }
        res.append(SEPARATOR);
        res.append(new String(fileidInfo.getChecksumType().getChecksumSalt()));

        return res.toString();
    }
    
    /**
     * Loads the file cache into the data cache.
     * @throws IOException If the file cannot be loaded.
     */
    private synchronized void loadCacheFromFile() throws IOException {
        log.info("(Re)Loads the cache from file.");
        synchronized(store) {
            cache.clear();
            BufferedReader reader = new BufferedReader(new FileReader(store));
            
            String line;
            while((line = reader.readLine()) != null) {
                // insert into dataCache;
                FileInfo fileidInfo = interpriteLine(line);
                if(fileidInfo != null) {
                    insertFileIDInfoIntoCache(fileidInfo);
                } else {
                    log.warn("Could not interprite line '" + line + "' as a file id info.");
                }
            }
        }
    }
    
    /**
     * Inserts a FileIDInfo into the cache, and removes any previous FileIDInfo for the same file and pillar.
     * @param fileidInfo The FileIDInfo to insert into the cache.
     */
    public synchronized void insertFileIDInfoIntoCache(FileInfo fileidInfo) {
        List<FileInfo> infos;
        if(cache.containsKey(fileidInfo.getFileID())) {
            infos = cache.get(fileidInfo.getFileID());
            
            // go through and remove any previous entry for the pillar.
            for(int i = 0; i < infos.size(); i++) {
                if(infos.get(i).getPillarId().equals(fileidInfo.getPillarId())) {
                    infos.remove(i);
                    i--;
                }
            }
        } else {
            infos = Collections.synchronizedList(new ArrayList<FileInfo>());
        }
        
        infos.add(fileidInfo);
        cache.put(fileidInfo.getFileID(), infos);
    }
    
    /**
     * Reads a line and converts it into a FileIDInfo.
     * The line must have the format (though only file id and pillar id is required):
     * <br/> file id + ## + pillar id + ## + file id date in millis + ## + checksum + ## + checksum date in millis 
     * + ## + checksum type algorithm + ## + checksum type salt
     * 
     * @param line The line to interprite.
     * @return The FileIDInfo for the line. Or null, if something was bad.
     */
    private FileInfo interpriteLine(String line) {
        String[] items = line.split(SEPARATOR);
        
        // file id and pillar id required.
        if(items.length < 2) {
            return null;
        }
        FileInfo res = new FileInfo(items[0], items[1]);
        
        // The file id date.
        if(items.length >= 3) {
            try {
                long time = Long.parseLong(items[2]);
                res.setDateForLastFileIDCheck(CalendarUtils.getFromMillis(time));
            } catch (NumberFormatException e) {
                log.warn("Could not interprite the item '" + items[2] + "' as a time stamp for file id. "
                        + "Sets to epoch instead.", e);
                res.setDateForLastFileIDCheck(CalendarUtils.getEpoch());
            }
        }
        
        // The checksum
        if(items.length >= 4) {
            res.setChecksum(items[3]);
        }
                    
        // The checksum date.
        if(items.length >= 5) {
            try {
                long time = Long.parseLong(items[4]);
                res.setDateForLastFileIDCheck(CalendarUtils.getFromMillis(time));
            } catch (NumberFormatException e) {
                log.warn("Could not interprite the item '" + items[4] + "' as a time stamp for checksum "
                        + "calculation. Sets to epoch instead.", e);
                res.setDateForLastFileIDCheck(CalendarUtils.getEpoch());
            }
        }

        // The checksum type. 6'th argument is algorithm and 7'th argument is the optional salt.
        if(items.length >= 6) {
            ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
            checksumType.setChecksumType(items[5]);
            
            if(items.length >= 7) {
                checksumType.setChecksumSalt(items[6].getBytes());
            }
            
            res.setChecksumType(checksumType);
        }
        
        return res;
    }
    
    /**
     * Validates that the data cache is up-to-date with the file cache.
     * If the timestamp differs, then the cache is reloaded.
     */
    public void synchronizeWithFile() {
        if(store.lastModified() > timeStamp) {
            try {
                loadCacheFromFile();
                timeStamp = store.lastModified();
            } catch (IOException e) {
                throw new IllegalStateException("Could not reload the cache from file '" 
                        + store.getAbsolutePath() + "'", e);
            }
        }
    }
    
    /**
     * Rewrites the file cache to the in-memory cache.
     * Creates a new temporary file, where the data cache is written to, before it overwrites the data file.
     */
    public void rewriteFileCache() {
        log.info("Rewriting the file cache");
        File temporaryFile = new File(store.getName() + ".tmp");
        synchronized(store) {
            try {
                temporaryFile.createNewFile();
                for(String fileId : cache.keySet()) {
                    for(FileInfo fileidInfo : cache.get(fileId)) {
                        addNewFileIDInfoToFile(fileidInfo, temporaryFile);
                    }
                }
                temporaryFile.renameTo(store);
            } catch (IOException e) {
                throw new RuntimeException("Could not recreate the file cache from memory.", e);
            } finally {
                temporaryFile.delete();
            }
        }
        timeStamp = store.lastModified();
    }
    
    /**
     * Adds a new entry to the file storage.
     * @param fileidInfo The FileIDInfo to add to the file storage.
     */
    private void addNewFileIDInfoToFile(FileInfo fileidInfo, File fileToAddTo) {
        synchronized(fileToAddTo) {
            try {
                FileWriter writer = new FileWriter(fileToAddTo, APPEND_TO_FILE);
                writer.append(convertFileIDInfoToString(fileidInfo) + "\n");
                writer.flush();
                writer.close();
            } catch (IOException e) {
                throw new RuntimeException("Could not add a new entry to the file cache.", e);
            }
            
            timeStamp = store.lastModified();
        }
    }
    
    /**
     * Method for adding a new FileIDInfo to the file.
     * @param fileidInfo The new entry to add to the file.
     */
    public void addNewFileIDInfoToFile(FileInfo fileidInfo) {
        addNewFileIDInfoToFile(fileidInfo, store);
    }

    /**
     * Clears the cache.
     */
    public void clearCache() {
        cache.clear();
    }
}
