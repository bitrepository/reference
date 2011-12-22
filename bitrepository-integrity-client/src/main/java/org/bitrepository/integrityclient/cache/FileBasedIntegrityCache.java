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

import java.util.Collection;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.integrityclient.cache.filebased.FileStoragedCache;

/**
 * Simple integrity cache based on saving everything in a single file.
 * 
 */
public class FileBasedIntegrityCache implements IntegrityCache {
    /** The default name for the file integrity cache.*/
    public static final String DEFAULT_FILE_NAME = "integrity-data.cache";

    /** The container for handling the file and data cache along with synchronization between them.*/
    private FileStoragedCache fileStorage;
    
    /**
     * Constructor.
     */
    public FileBasedIntegrityCache() {
        fileStorage = new FileStoragedCache(DEFAULT_FILE_NAME); 
    }
    
    @Override
    public void addFileIDs(FileIDsData data, String pillarId) {
        fileStorage.synchronizeWithFile();
        boolean rewrite = false;
        for(FileIDsDataItem dataItem : data.getFileIDsDataItems().getFileIDsDataItem()) {
            FileInfo fileidInfo = new FileInfo(dataItem.getFileID(), pillarId);
            fileidInfo.setDateForLastFileIDCheck(dataItem.getCreationTimestamp());

            if(fileStorage.containsFileIDInfo(dataItem.getFileID(), pillarId)) {
                fileStorage.updateFileIDInfoInCache(fileidInfo);
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
            FileInfo fileidInfo = new FileInfo(dataItem.getFileID(), null, new String(dataItem.getChecksumValue()), 
                    checksumType, dataItem.getCalculationTimestamp(), pillarId);

            if(fileStorage.containsFileIDInfo(dataItem.getFileID(), pillarId)) {
                fileStorage.updateFileIDInfoInCache(fileidInfo);
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
    public Collection<FileInfo> getFileInfos(String fileId) {
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
        fileStorage.clearCache();
    }
}
