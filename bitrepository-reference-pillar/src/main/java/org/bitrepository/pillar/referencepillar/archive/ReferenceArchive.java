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
package org.bitrepository.pillar.referencepillar.archive;

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
import org.bitrepository.common.FileStore;
import org.bitrepository.protocol.CoordinationLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for managing the files for the reference pillar. This supports a single BitRespositoryCollectionID.
 */
public class ReferenceArchive implements FileStore {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The maximum buffer for the stream interaction.*/
    public static final int MAX_BUFFER_SIZE = 32 * 1024;

    /** The list of directories to manage.*/
    private final List<ArchiveDirectory> directories = new ArrayList<ArchiveDirectory>();
    
    /** 
     * Constructor. Initialises the file directory. 
     * 
     * @param dirPaths The list of paths to the archival base directories.
     */
    public ReferenceArchive(List<String> dirPaths) {
        ArgumentValidator.checkNotNullOrEmpty(dirPaths, "List<String> dirPaths");

        for(String dir : dirPaths) {
            directories.add(new ArchiveDirectory(dir));
        }
    }

    @Override
    public File getFile(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        for(ArchiveDirectory dir : directories) {
            if(dir.hasFile(fileID)) {
                return dir.getFile(fileID);
            }
        }
        throw new IllegalArgumentException("The file '" + fileID + "' is not within the archives.");
    }

    @Override
    public boolean hasFile(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        for(ArchiveDirectory dir : directories) {
            if(dir.hasFile(fileID)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Collection<String> getAllFileIds() {
        List<String> res = new ArrayList<String>();
        for(ArchiveDirectory dir : directories) {
            res.addAll(dir.getFileIds());
        }
        return res;
    }

    @Override
    public FileInputStream getFileAsInputstream(String fileID) throws IOException {
        return new FileInputStream(getFile(fileID));
    }

    @Override
    public File downloadFileForValidation(String fileID, InputStream inputStream) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        ArgumentValidator.checkNotNull(inputStream, "inputStream");

        ArchiveDirectory dir = getDirWithMostSpace();
        File downloadedFile = null;
        synchronized(dir) {
            downloadedFile = dir.getNewFileInTempDir(fileID);
            log.debug("Downloading the file '" + fileID + "' for validation.");
            
            // Save InputStream to the file.
            BufferedOutputStream bufferedOutputstream = null;
            try {
                try {
                    bufferedOutputstream = new BufferedOutputStream(new FileOutputStream(downloadedFile));
                    byte[] buffer = new byte[MAX_BUFFER_SIZE];
                    int bytesRead = 0;
                    while ((bytesRead = inputStream.read(buffer)) != -1) {
                        bufferedOutputstream.write(buffer, 0, bytesRead);
                    }
                } finally {
                    if(bufferedOutputstream != null) {
                        bufferedOutputstream.close();
                    }
                }
            } catch (IOException e) {
                throw new CoordinationLayerException("Could not retrieve file '" + fileID + "'", e);
            }
        }
        return downloadedFile;
    }
    
    @Override
    public void moveToArchive(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        log.info("Moving the file '" + fileID + "' to archive.");

        ArchiveDirectory dir = getDirWithTmpFile(fileID);
        synchronized(dir) {
            dir.moveFromTmpToArchive(fileID);
        }
    }

    @Override
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
     * @param fileId The id of the file to locate within the tmpDir.
     * @return The file in the tmpDir.
     */
    public File getFileInTmpDir(String fileId) {
        return getDirWithTmpFile(fileId).getFileInTempDir(fileId);
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
     * @param fileId The id of the file.
     * @return The archive directory with the file.
     */
    private ArchiveDirectory getDirWithFile(String fileId) {
        for(ArchiveDirectory dir : directories) {
            if(dir.hasFile(fileId)) {
                return dir;
            }
        }
        
        throw new IllegalStateException("Does not have the file '" + fileId + "' within any archive dirs.");
    }

    /**
     * Finds the archive directory with the given file within its tmp directory, ready for archival.
     * @param fileId The id of the file.
     * @return The archive directory with the file in its tmp dir.
     */
    private ArchiveDirectory getDirWithTmpFile(String fileId) {
        for(ArchiveDirectory dir : directories) {
            if(dir.hasFileInTempDir(fileId)) {
                return dir;
            }
        }
        
        throw new IllegalStateException("Does not have the file '" + fileId + "' within any archive dirs.");
    }
    
    /**
     * Closes the reference archive.
     */
    public void close() {
        // TODO close stuff.
    }
}
