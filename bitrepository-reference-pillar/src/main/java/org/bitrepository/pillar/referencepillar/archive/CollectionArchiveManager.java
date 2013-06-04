/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.referencepillar.archive;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.common.filestore.DefaultFileInfo;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.settings.referencesettings.CollectionDirs;

/**
 * 
 */
public class CollectionArchiveManager implements FileStore {
    /** The mapping between the collections and their archives.*/
    private final Map<String, ReferenceArchive> archives = new HashMap<String, ReferenceArchive>();
    
    /**
     * Constructor.
     * @param settings The settings for the repository.
     */
    public CollectionArchiveManager(Settings settings) {
        initiateSpecificDirs(settings);
        initiateDefaultDirs(settings);
    }
    
    /**
     * Initiates the specified archive directories.
     * @param settings The settings.
     */
    private void initiateSpecificDirs(Settings settings) {
        for(CollectionDirs cd : settings.getReferenceSettings().getPillarSettings().getCollectionDirs()){
            if(cd.getCollectionID().isEmpty()) {
                continue;
            } else {
                initiateArchive(cd.getCollectionID(), cd.getFileDirs());
            }
        }        
    }
    
    /**
     * Initiates the default archive directories.
     * These will only be used for the collections, which does not have a specified directory.
     * @param settings The settings.
     */
    private void initiateDefaultDirs(Settings settings) {
        for(CollectionDirs cd : settings.getReferenceSettings().getPillarSettings().getCollectionDirs()){
            if(cd.getCollectionID().isEmpty()) {
                initiateArchive(SettingsUtils.getCollectionIDsForPillar(settings.getComponentID()),
                        cd.getFileDirs());
            }
        }        
    }
    
    /**
     * Initiates the archives for a set of collection ids and their respective directory paths.
     * Only creates archives for the collection, if it does not already has one.
     * @param collectionIds
     * @param fileDirs
     */
    private void initiateArchive(Collection<String> collectionIds, Collection<String> fileDirs) {
        for(String colId : collectionIds) {
            List<String> dirs = new ArrayList<String>();
            for(String dir : fileDirs) {
                dirs.add(new File(dir, colId).getPath());
            }
            
            if(!archives.containsKey(colId)) {
                archives.put(colId, new ReferenceArchive(dirs));
            }
        }
    }

    @Override
    public FileInfo getFileInfo(String fileID, String collectionId) {
        return new DefaultFileInfo(getArchive(collectionId).getFile(fileID));
    }

    @Override
    public boolean hasFile(String fileID, String collectionId) {
        return getArchive(collectionId).hasFile(fileID);
    }

    @Override
    public Collection<String> getAllFileIds(String collectionId) {
        return getArchive(collectionId).getAllFileIds();
    }

    @Override
    public FileInfo downloadFileForValidation(String fileID, String collectionId, InputStream inputStream) 
            throws IOException {
        return new DefaultFileInfo(getArchive(collectionId).downloadFileForValidation(fileID, inputStream));
    }

    @Override
    public void moveToArchive(String fileID, String collectionId) {
        getArchive(collectionId).moveToArchive(fileID);
    }

    @Override
    public void deleteFile(String fileID, String collectionId) {
        getArchive(collectionId).deleteFile(fileID);        
    }
    
    @Override
    public synchronized void replaceFile(String fileID, String collectionId) {
        getArchive(collectionId).replaceFile(fileID);
    }
    
    @Override
    public long sizeLeftInArchive(String collectionId) {
        return getArchive(collectionId).sizeLeftInArchive();
    }
    
    @Override
    public FileInfo getFileInTmpDir(String fileId, String collectionId) {
        return new DefaultFileInfo(getArchive(collectionId).getFileInTmpDir(fileId));
    }
    
    @Override
    public void ensureFileNotInTmpDir(String fileId, String collectionId) {
        getArchive(collectionId).ensureFileNotInTmpDir(fileId);
    }
    
    @Override
    public void close() {
        for(ReferenceArchive ra : archives.values()){
            ra.close();
        }
    }
    
    /**
     * Validates the existence of the archive before accessing it.
     * If it does not exist, then an IllegalStateException is thrown. 
     * @param collectionId The id of the collection.
     * @return The archive for the collection.
     */
    private ReferenceArchive getArchive(String collectionId) {
        if(archives.containsKey(collectionId)) {
            return archives.get(collectionId);
        } else {
            throw new IllegalStateException("The collection '" + collectionId + "' has no attached archive.");
        }
    }
}
