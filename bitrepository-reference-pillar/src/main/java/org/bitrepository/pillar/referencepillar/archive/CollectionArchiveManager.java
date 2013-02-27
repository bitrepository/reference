package org.bitrepository.pillar.referencepillar.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.common.FileStore;
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
                initiateArchive(SettingsUtils.getCollectionIDsForPillar(settings, settings.getComponentID()), 
                        cd.getFileDirs());
            }
        }        
    }
    
    /**
     * Initiates the archives for a set of collection ids and their respective directory paths.
     * Only creates archives for the collection, if it does not already has one.
     * @param collectionIDs
     * @param fileDirs
     */
    private void initiateArchive(Collection<String> collectionIDs, Collection<String> fileDirs) {
        for(String colId : collectionIDs) {
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
    public FileInputStream getFileAsInputstream(String fileID, String collectionId) throws IOException {
        return archives.get(collectionId).getFileAsInputstream(fileID);
    }

    @Override
    public File getFile(String fileID, String collectionId) {
        return archives.get(collectionId).getFile(fileID);
    }

    @Override
    public boolean hasFile(String fileID, String collectionId) {
        return archives.get(collectionId).hasFile(fileID);
    }

    @Override
    public Collection<String> getAllFileIds(String collectionId) {
        return archives.get(collectionId).getAllFileIds();
    }

    @Override
    public File downloadFileForValidation(String fileID, String collectionId, InputStream inputStream) {
        if (archives.get(collectionId) == null) {
            throw new RuntimeException("No archive for collectionID: " +  collectionId +
            "\nArchives are:" +archives);
        }
        return archives.get(collectionId).downloadFileForValidation(fileID, inputStream);
    }

    @Override
    public void moveToArchive(String fileID, String collectionId) {
        archives.get(collectionId).moveToArchive(fileID);
    }

    @Override
    public void deleteFile(String fileID, String collectionId) {
        archives.get(collectionId).deleteFile(fileID);        
    }
    
    /**
     * The replace operation atomically.
     * Removes the archived file from its directory, and moves the tmpFile into the archive dir.
     * 
     * @param fileID The id of the file to perform the replace function upon.
     */
    public synchronized void replaceFile(String fileID, String collectionId) {
        archives.get(collectionId).replaceFile(fileID);
    }
    
    /**
     * For retrieval of the size left in this archive.
     * @return The number of bytes left in the archive.
     */
    public long sizeLeftInArchive(String collectionId) {
        return archives.get(collectionId).sizeLeftInArchive();
    }
    
    /**
     * Retrieves the file within a tmpDir.
     * @param fileId The id of the file to locate within the tmpDir.
     * @return The file in the tmpDir.
     */
    public File getFileInTmpDir(String fileId, String collectionId) {
        return archives.get(collectionId).getFileInTmpDir(fileId);
    }
    
    /**
     * Ensures that no such file exists within the tmp directory.
     * 
     * @param fileId The id of the file to clean up after.
     */
    public void ensureFileNotInTmpDir(String fileId, String collectionId) {
        archives.get(collectionId).ensureFileNotInTmpDir(fileId);
    }
    
    /**
     * Closes all the archives.
     */
    public void close() {
        for(ReferenceArchive ra : archives.values()){
            ra.close();
        }
    }
}
