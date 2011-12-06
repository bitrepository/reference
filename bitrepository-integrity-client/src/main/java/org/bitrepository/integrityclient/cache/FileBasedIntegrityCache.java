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

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.common.utils.CalendarUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple integrity cache based on saving everything in a single file.
 * 
 */
public class FileBasedIntegrityCache implements CachedIntegrityInformationStorage {
    /** The separator for the different elements in the string representation of the FileIDInfos.*/
    private static final String SEPARATOR = "##";
    /** The separator for the different elements in the string representation of the FileIDInfos.*/
    private static final boolean APPEND_TO_FILE = true;
    
    public static final String DEFAULT_FILE_NAME = "integrity-data.cache";

    /** The container for handling the file and data cache along with synchronization between them.*/
    private FileStorage fileStorage;
    
    /**
     * Constructor.
     */
    public FileBasedIntegrityCache() {
        fileStorage = new FileStorage(DEFAULT_FILE_NAME); 
    }
    
    @Override
    public void addFileIDs(FileIDsData data, String pillarId) {
        fileStorage.synchronizeWithFile();
        boolean rewrite = false;
        for(FileIDsDataItem dataItem : data.getFileIDsDataItems().getFileIDsDataItem()) {
            FileIDInfo fileidInfo = new FileIDInfo(dataItem.getFileID(), pillarId);
            fileidInfo.setDateForLastFileIDCheck(dataItem.getCreationTimestamp());

            if(fileStorage.containsFileIDInfo(dataItem.getFileID(), pillarId)) {
                fileStorage.updateExistingFileIDInfoInCache(fileidInfo);
                rewrite = true;
            } else {
                fileStorage.addNewFileIDInfoToFile(fileidInfo);
                fileStorage.insertFileIDInfoIntoCache(fileidInfo);
            }
        }

        if(rewrite) {
            fileStorage.rewriteFileCache();
        }
    }

    @Override
    public void addChecksums(List<ChecksumDataForChecksumSpecTYPE> data, ChecksumSpecTYPE checksumType, 
            String pillarId) {
        fileStorage.synchronizeWithFile();
        boolean rewrite = false;
        for(ChecksumDataForChecksumSpecTYPE dataItem : data) {
            // Create with no 'file id date' (automatically set to Epoch).
            FileIDInfo fileidInfo = new FileIDInfo(dataItem.getFileID(), null, 
                    dataItem.getChecksumValue(), checksumType, dataItem.getCalculationTimestamp(), pillarId);

            if(fileStorage.containsFileIDInfo(dataItem.getFileID(), pillarId)) {
                fileStorage.updateExistingFileIDInfoInCache(fileidInfo);
                rewrite = true;
            } else {
                fileStorage.addNewFileIDInfoToFile(fileidInfo);
                fileStorage.insertFileIDInfoIntoCache(fileidInfo);
            }
        }

        if(rewrite) {
            fileStorage.rewriteFileCache();
        }
    }

    @Override
    public Collection<FileIDInfo> getFileInfo(String fileId) {
        fileStorage.synchronizeWithFile();
        return fileStorage.getFileIDInfos(fileId);
    }

    @Override
    public Collection<String> getAllFileIDs() {
        fileStorage.synchronizeWithFile();
        return fileStorage.getAllFileIDs();
    }
    
    /**
     * Clears the cache.
     */
    public void clearCache() {
        fileStorage.dataCache.clear();
    }
    
    /**
     * Entity for storing and synchronizing the data cache with the file cache.
     * Structure:
     * <br/> file id + ## + pillar id + ## + file id date in millis + ## + checksum + ## + checksum date in millis 
     * + ## + checksum type algorithm + ## + checksum type salt
     */
    private class FileStorage {
        /** The log.*/
        private Logger log = LoggerFactory.getLogger(getClass());

        /** The file for caching the data.*/
        private File fileCache;
        
        /** The latest timestamp for the file.*/
        private long timeStamp;
        
        /**
         * The memory cache for containing the information about the files in the system.
         * Synchronized for avoiding threading problems.
         */
        private Map<String, List<FileIDInfo>> dataCache = Collections.synchronizedMap(
                new HashMap<String, List<FileIDInfo>>());
        
        /**
         * Constructor.
         * @param fileName The name of the file.
         */
        private FileStorage(String fileName) {
            fileCache = new File(fileName);
            try {
                if(fileCache.isFile()) {
                    loadCache();
                } else {
                    System.out.println("Create a new file for the cache");
                    fileCache.createNewFile();
                }
            } catch (IOException e) {
                throw new IllegalStateException("Could not instantiate the cache '" + fileCache.getAbsolutePath() 
                        + "'", e);
            }
            
            timeStamp = fileCache.lastModified();
        }
        
        /**
         * Extracts the collection of FileIDInfos for a specific file id.
         * @param fileID The id of the file to find the data cache from.
         * @return The collection of FileIDInfos for the given file id.
         */
        private List<FileIDInfo> getFileIDInfos(String fileID) {
           return dataCache.get(fileID);
        }
        
        /**
         * @return The collection of ids for all the files in the cache.
         */
        private Collection<String> getAllFileIDs() {
            return dataCache.keySet();
        }
        
        /**
         * Tells whether an entry with the given file id and pillar id already exists within the cache.
         * @param fileId The id of the file.
         * @param pillarId The id of the pillar.
         * @return Whether such an entry exists.
         */
        private boolean containsFileIDInfo(String fileId, String pillarId) {
            if(!dataCache.containsKey(fileId)) {
                return false;
            }
            
            for(FileIDInfo fileidInfo : dataCache.get(fileId)) {
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
        private void updateExistingFileIDInfoInCache(FileIDInfo fileidInfo) {
            List<FileIDInfo> fileidInfos = dataCache.get(fileidInfo.getFileID());
            if(fileidInfos == null) {
                insertFileIDInfoIntoCache(fileidInfo);
                return;
            }
            
            FileIDInfo oldIDInfo = null;
            
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
            dataCache.put(fileidInfo.getFileID(), fileidInfos);
        }
        
        /**
         * Converts a FileIDInfo into the following String format:
         * <br/> file id + ## + pillar id + ## + file id date in millis + ## + checksum + ## + checksum date in millis 
         * + ## + checksum type algorithm + ## + checksum type salt
         * @param fileidInfo The FileIDInfo to convert.
         * @return The String representation of the file id info.
         */
        private String convertFileIDInfoToString(FileIDInfo fileidInfo) {
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
            res.append(fileidInfo.getChecksumType().getChecksumSalt());

            return res.toString();
        }
        
        /**
         * Loads the file cache into the data cache.
         * @throws IOException If the file cannot be loaded.
         */
        private synchronized void loadCache() throws IOException {
            log.info("(Re)Loads the cache from file.");
            synchronized(fileCache) {
                dataCache.clear();
                BufferedReader reader = new BufferedReader(new FileReader(fileCache));
                
                String line;
                while((line = reader.readLine()) != null) {
                    // insert into dataCache;
                    FileIDInfo fileidInfo = interpriteLine(line);
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
        private synchronized void insertFileIDInfoIntoCache(FileIDInfo fileidInfo) {
            List<FileIDInfo> infos;
            if(dataCache.containsKey(fileidInfo.getFileID())) {
                infos = dataCache.get(fileidInfo.getFileID());
                
                if(infos != null) {
                    // go through and remove any previous entry for the pillar.
                    for(int i = 0; i < infos.size(); i++) {
                        if(infos.get(i).getPillarId().equals(fileidInfo.getPillarId())) {
                            infos.remove(i);
                            i--;
                        }
                    }
                }
            } else {
                infos = Collections.synchronizedList(new ArrayList<FileIDInfo>());
            }
            
            infos.add(fileidInfo);
            dataCache.put(fileidInfo.getFileID(), infos);
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
        private FileIDInfo interpriteLine(String line) {
            String[] items = line.split(SEPARATOR);
            
            // file id and pillar id required.
            if(items.length < 2) {
                return null;
            }
            FileIDInfo res = new FileIDInfo(items[0], items[1]);
            
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
                    checksumType.setChecksumSalt(items[6]);
                }
                
                res.setChecksumType(checksumType);
            }
            
            return res;
        }
        
        /**
         * Validates that the data cache is up-to-date with the file cache.
         * If the timestamp differs, then the cache is reloaded.
         */
        private void synchronizeWithFile() {
            if(fileCache.lastModified() > timeStamp) {
                try {
                    loadCache();
                    timeStamp = fileCache.lastModified();
                } catch (IOException e) {
                    throw new IllegalStateException("Could not reload the cache from file '" 
                            + fileCache.getAbsolutePath() + "'", e);
                }
            }
        }
        
        /**
         * Rewrites the data cache to the memory.
         * Creates a new temporary file, where the data cache is written to, before it overwrites the data file.
         */
        private void rewriteFileCache() {
            log.info("Rewriting the file cache");
            File temporaryFile = new File(fileCache.getName() + ".tmp");
            synchronized(fileCache) {
                try {
                    temporaryFile.createNewFile();
                    for(String fileId : dataCache.keySet()) {
                        for(FileIDInfo fileidInfo : dataCache.get(fileId)) {
                            addNewFileIDInfoToFile(fileidInfo, temporaryFile);
                        }
                    }
                    temporaryFile.renameTo(fileCache);
                } catch (IOException e) {
                    throw new RuntimeException("Could not recreate the file cache from memory.", e);
                } finally {
                    temporaryFile.delete();
                }
            }
            timeStamp = fileCache.lastModified();
        }
        
        /**
         * Adds a new entry to the file storage.
         * @param fileidInfo The FileIDInfo to add to the file storage.
         */
        private void addNewFileIDInfoToFile(FileIDInfo fileidInfo, File fileToAddTo) {
            synchronized(fileToAddTo) {
                try {
                    FileWriter writer = new FileWriter(fileToAddTo, APPEND_TO_FILE);
                    writer.append(convertFileIDInfoToString(fileidInfo) + "\n");
                    writer.flush();
                    writer.close();
                } catch (IOException e) {
                    throw new RuntimeException("Could not add a new entry to the file cache.", e);
                }
                
                timeStamp = fileCache.lastModified();
            }
        }
        
        /**
         * Method for adding a new FileIDInfo to the file.
         * @param fileidInfo The new entry to add to the file.
         */
        private void addNewFileIDInfoToFile(FileIDInfo fileidInfo) {
            addNewFileIDInfoToFile(fileidInfo, fileCache);
        }
    }
}
