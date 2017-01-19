/*
 * #%L
 * Bitmagasin integrationstest
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.store.filearchive;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bitrepository.common.ArgumentValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for managing the files for the reference pillar. This supports a single CollectionID.
 */
public class ReferenceArchive {
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The maximum buffer for the stream interaction.*/
    public static final int MAX_BUFFER_SIZE = 32 * 1024;

    /** The list of directories to manage.*/
    private final List<ArchiveDirectory> directories = new ArrayList<ArchiveDirectory>();
    
    /** 
     * Constructor. Initializes the file directory. 
     * 
     * @param dirPaths The list of paths to the archival base directories.
     */
    public ReferenceArchive(List<String> dirPaths) {
        ArgumentValidator.checkNotNullOrEmpty(dirPaths, "List<String> dirPaths");

        for(String dir : dirPaths) {
            directories.add(new ArchiveDirectory(dir));
        }
    }

    /**
     * Retrieves the file for the given file id.
     * @param fileID The id of the file to retrieve.
     * @return The file.
     */
    public File getFile(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        for(ArchiveDirectory dir : directories) {
            if(dir.hasFile(fileID)) {
                return dir.retrieveFile(fileID);
            }
        }
        throw new IllegalArgumentException("The file '" + fileID + "' is not within the archives.");
    }

    /**
     * @param fileID The id of the file to find.
     * @return Whether it has a file with the given id.
     */
    public boolean hasFile(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        for(ArchiveDirectory dir : directories) {
            if(dir.hasFile(fileID)) {
                return true;
            }
        }
        return false;
    }

    /**
     * @return All the file ids within this archive.
     */
    public Collection<String> getAllFileIds() {
        List<String> res = new ArrayList<String>();
        for(ArchiveDirectory dir : directories) {
            res.addAll(dir.getFileIds());
        }
        return res;
    }

    /**
     * Retrieves an inputstream to the given file.
     * @param fileID The id of the file.
     * @return The inputstream to the file.
     * @throws IOException If no inputstream can be made.
     */
    public FileInputStream getFileAsInputstream(String fileID) throws IOException {
        return new FileInputStream(getFile(fileID));
    }

    /**
     * Creates a file from the data in the inputstream.
     * The file will be placed in the temporary directory, and requires validation before it can be moved to the
     * file archive.
     *
     * @param fileID      The id of the file to create.
     * @param inputStream The inputstream to extract the content of the file from.
     * @return The file, which should be validated.
     * @throws IOException If it fails to download the file.
     */
    public File downloadFileForValidation(String fileID, InputStream inputStream) throws IOException {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        ArgumentValidator.checkNotNull(inputStream, "inputStream");

        ArchiveDirectory dir = getDirWithMostSpace();
        File downloadedFile = null;
        synchronized(dir) {
            downloadedFile = dir.getNewFileInTempDir(fileID);
            log.debug("Downloading the file '" + fileID + "' for validation.");
            
            // Save InputStream to the file.
            try (BufferedOutputStream bufferedOutputstream 
                    = new BufferedOutputStream(new FileOutputStream(downloadedFile))){
                byte[] buffer = new byte[MAX_BUFFER_SIZE];
                int bytesRead = 0;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    bufferedOutputstream.write(buffer, 0, bytesRead);
                }
            } 
        }
        return downloadedFile;
    }
    
    /**
     * Moves a file from the tmpDir to fileDir.
     * @param fileID The id of the file.
     */
    public void moveToArchive(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        log.info("Moving the file '" + fileID + "' to archive.");

        ArchiveDirectory dir = getDirWithTmpFile(fileID);
        synchronized(dir) {
            dir.moveFromTmpToArchive(fileID);
        }
    }

    /**
     * Removes a file from the archive.
     * @param fileID The id of the file.
     */
    public void deleteFile(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        ArchiveDirectory dir = getDirWithFile(fileID);
        synchronized(dir) {
            dir.removeFileFromArchive(fileID);
        }
    }
    
    /**
     * The replace operation atomically.
     * Removes the archived file from its directory, and moves the tmpFile into the archive dir.
     * 
     * @param fileID The id of the file to perform the replace function upon.
     */
    public synchronized void replaceFile(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        ArchiveDirectory tmpDir = getDirWithTmpFile(fileID);
        
        synchronized(tmpDir) {
            ArchiveDirectory fileDir = getDirWithFile(fileID);
            fileDir.removeFileFromArchive(fileID);
            tmpDir.moveFromTmpToArchive(fileID);
        }
    }
    
    /**
     * For retrieval of the size left in this archive.
     * @return The number of bytes left in the archive.
     */
    public long sizeLeftInArchive() {
        return getDirWithMostSpace().getBytesLeft();
    }
    
    /**
     * Retrieves the file within a tmpDir.
     * @param fileID The id of the file to locate within the tmpDir.
     * @return The file in the tmpDir.
     */
    public File getFileInTmpDir(String fileID) {
        return getDirWithTmpFile(fileID).getFileInTempDir(fileID);
    }
    
    /**
     * Ensures that no such file exists within the tmp directory.
     * 
     * @param fileID The id of the file to clean up after.
     */
    public void ensureFileNotInTmpDir(String fileID) {
        for(ArchiveDirectory dir : directories) {
            if(dir.hasFileInTempDir(fileID)) {
                log.info("Removing tmp file '" + fileID + "' from tmp dir '" + dir + "'.");
                dir.removeFileFromTmp(fileID);
            }
        }
    }
    
    /**
     * Finds the directory with the most space left.
     * @return The archive directory with the most space left.
     */
    private ArchiveDirectory getDirWithMostSpace() {
        Long largestSize = -1L;
        ArchiveDirectory res = null;
        
        for(ArchiveDirectory dir : directories) {
            if(largestSize < dir.getBytesLeft()) {
                largestSize = dir.getBytesLeft();
                res = dir;
            }
        }
        return res;
    }
    
    /**
     * Finds the archive directory with the given file within its archive directory.
     * @param fileID The id of the file.
     * @return The archive directory with the file.
     */
    private ArchiveDirectory getDirWithFile(String fileID) {
        for(ArchiveDirectory dir : directories) {
            if(dir.hasFile(fileID)) {
                return dir;
            }
        }
        
        throw new IllegalStateException("Does not have the file '" + fileID + "' within any archive dirs.");
    }

    /**
     * Finds the archive directory with the given file within its tmp directory, ready for archival.
     * @param fileID The id of the file.
     * @return The archive directory with the file in its tmp dir.
     */
    private ArchiveDirectory getDirWithTmpFile(String fileID) {
        for(ArchiveDirectory dir : directories) {
            if(dir.hasFileInTempDir(fileID)) {
                return dir;
            }
        }
        
        throw new IllegalStateException("Does not have the file '" + fileID + "' within any archive dirs.");
    }
    
    /**
     * Closes the reference archive.
     */
    public void close() {
        directories.clear();
    }
}
