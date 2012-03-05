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
package org.bitrepository.pillar.referencepillar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.FileStore;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.protocol.CoordinationLayerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for managing the files for the reference pillar. This supports a single BitRespositoryCollectionID.
 */
public class ReferenceArchive implements FileStore {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    // TODO replace these constants with settings.
    /** Constant for the temporary directory name.*/
    public static final String TEMPORARY_DIR = "tmpDir";
    /** Constant for the file directory name.*/
    public static final String FILE_DIR = "fileDir";
    /** Constant for the retain directory name.*/
    public static final String RETAIN_DIR = "retainDir";
    /** The maximum buffer for the stream interaction.*/
    public static final int MAX_BUFFER_SIZE = 32 * 1024;

    /** The directory for the files. Contains three sub directories: tempDir, fileDir and retainDir.*/
    private File baseDepositDir;

    /** The directory where files are being downloaded to before they are put into the filedir. */
    private File tmpDir;
    /** The directory where the files are being stored.*/
    private final File fileDir;
    /** The directory where the files are moved, when they are removed from the archive.*/
    private final File retainDir;

    /** 
     * Constructor. Initialises the file directory. 
     * 
     * @param dir The directory
     */
    public ReferenceArchive(String dirName) {
        ArgumentValidator.checkNotNullOrEmpty(dirName, "String dirName");

        // Instantiate the directories for this archive.
        baseDepositDir = FileUtils.retrieveDirectory(dirName);
        tmpDir = FileUtils.retrieveSubDirectory(baseDepositDir, TEMPORARY_DIR);
        fileDir = FileUtils.retrieveSubDirectory(baseDepositDir, FILE_DIR);
        retainDir = FileUtils.retrieveSubDirectory(baseDepositDir, RETAIN_DIR);
    }

    @Override
    public File getFile(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        File res = new File(fileDir, fileID);
        if(!res.isFile()) {
            throw new IllegalArgumentException("The file '" + fileID + "' is not within the archive.");
        }
        return res;
    }

    @Override
    public boolean hasFile(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        return (new File(fileDir, fileID)).isFile();
    }

    @Override
    public Collection<String> getAllFileIds() {
        String[] ids = fileDir.list();
        List<String> res = new ArrayList<String>();
        for(String id : ids) {
            res.add(id);
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

        // Download the file first, then move it to the fileDir.
        File downloadedFile = new File(tmpDir, fileID);
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

        return downloadedFile;
    }
    
    @Override
    public void moveToArchive(String fileID) {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        log.info("Moving the file '" + fileID + "' to archive.");

        File downloadedFile = new File(tmpDir, fileID);
        File archivedFile = new File(fileDir, fileID);
        
        // Move the file to the fileDir.
        FileUtils.moveFile(downloadedFile, archivedFile);
    }

    @Override
    public void replaceFile(String fileID, InputStream inputStream) throws IOException {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        ArgumentValidator.checkNotNull(inputStream, "inputStream");
        
        // delete the old file.
        deleteFile(fileID);

        // Store the new file.
        downloadFileForValidation(fileID, inputStream);
        moveToArchive(fileID);
    }

    @Override
    public void deleteFile(String fileID) throws FileNotFoundException {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");

        // Move old file to retain area.
        File oldFile = new File(fileDir, fileID);
        if(!oldFile.isFile()) {
            throw new FileNotFoundException("Cannot locate the file to delete '" + oldFile.getAbsolutePath() + "'!");
        }
        File retainFile = new File(retainDir, fileID);
        
        // If a version of the file already has been retained, then it should be deprecated.
        if(retainFile.exists()) {
            FileUtils.deprecateFile(retainFile);
        }
        
        FileUtils.moveFile(oldFile, retainFile);
    }
    
    /**
     * The replace operation atomically.
     * Validates that the old file and the new file exists, then deletes the old file (moves it to retain dir) and 
     * moves the new file to the fileDir (from the tmpDir).
     * 
     * @param fileID The id of the file to perform the replace function upon.
     * @throws FileNotFoundException If the new file with the given fileID does not exist within the tmpDir, or if the 
     * old file with the given fileID does not exist within the fileDir. 
     */
    public synchronized void replaceFile(String fileID) throws FileNotFoundException {
        ArgumentValidator.checkNotNullOrEmpty(fileID, "String fileID");
        
        File oldFile = new File(fileDir, fileID);
        if(!oldFile.isFile()) {
            throw new FileNotFoundException("Cannot locate the file to be replaced '" + oldFile.getAbsolutePath() 
                    + "'.");
        }
        File newFile = new File(tmpDir, fileID);
        if(!oldFile.isFile()) {
            throw new FileNotFoundException("Cannot locate the file to replace '" + newFile.getAbsolutePath() + "'.");
        }
        
        deleteFile(fileID);
        moveToArchive(fileID);
    }
    
    /**
     * For retrieval of the size left in this archive.
     * @return The number of bytes left in the archive.
     */
    public long sizeLeftInArchive() {
        return baseDepositDir.getFreeSpace();
    }
}
