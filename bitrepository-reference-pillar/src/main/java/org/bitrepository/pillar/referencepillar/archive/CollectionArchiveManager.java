package org.bitrepository.pillar.referencepillar.archive;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.bitrepository.common.FileStore;
import org.bitrepository.common.settings.Settings;
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
        for(CollectionDirs cd : settings.getReferenceSettings().getPillarSettings().getCollectionDirs()){
            archives.put(cd.getCollectionID(), new ReferenceArchive(cd.getFileDir()));
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
