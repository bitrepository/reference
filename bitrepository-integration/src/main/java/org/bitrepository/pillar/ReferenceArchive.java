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
package org.bitrepository.pillar;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.FileStore;
import org.bitrepository.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for managing the files for the reference pillar.
 * It has a very simple structure for keeping the files according to SLA.
 * Each SLA has its own subdirectory in the filedir for this pillar.
 */
public class ReferenceArchive implements FileStore {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    // Constants
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
        File res = new File(fileDir, fileID);
        if(!res.isFile()) {
            throw new IllegalArgumentException("The file '" + fileID + "' is not within the archive.");
        }
        return res;
    }

    @Override
    public boolean hasFile(String fileID) {
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
    public FileInputStream getFileAsInputstream(String fileID) throws Exception {
        return new FileInputStream(getFile(fileID));
    }

    @Override
    public void storeFile(String fileID, InputStream inputStream) throws Exception {
        // Download the file first, then move it to the fileDir.
        File downloadedFile = new File(tmpDir, fileID);
        File archivedFile = new File(fileDir, fileID);

        // Save InputStream to the file.
        BufferedOutputStream bufferedOutputstream = null;
        try {
            bufferedOutputstream = new BufferedOutputStream(new FileOutputStream(downloadedFile));
            byte[] buffer = new byte[MAX_BUFFER_SIZE];
            int bytesRead = 0;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                bufferedOutputstream.write(buffer, 0, bytesRead);
            }
        }
        finally {
            bufferedOutputstream.close();
        }

        // Move the file to the fileDir.
        downloadedFile.renameTo(archivedFile);
    }

    @Override
    public void replaceFile(String fileID, InputStream inputStream) throws Exception {
        // delete the old file.
        deleteFile(fileID);

        // Store the new file.
        storeFile(fileID, inputStream);
    }

    @Override
    public void deleteFile(String fileID) throws Exception {
        // Move old file to retain area.
        File oldFile = new File(fileDir, fileID);
        if(!oldFile.isFile()) {
            throw new FileNotFoundException("Cannot locate the file to replace '" + oldFile.getAbsolutePath() + "'!");
        }
        File retainFile = new File(retainDir, fileID);
        oldFile.renameTo(retainFile);
    }
}
