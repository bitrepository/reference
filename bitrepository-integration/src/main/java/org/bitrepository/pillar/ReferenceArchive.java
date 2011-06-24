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

import java.io.File;

import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for managing the files for the reference pillar.
 * It has a very simple structure for keeping the files according to SLA.
 * Each SLA has its own subdirectory in the filedir for this pillar.
 */
public class ReferenceArchive {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** The directory for the files. Each SLA has its own sub-directory to this base directory, which also contains
     * a temporary directory where files are downloaded to before they are archived in their SLA file directory.*/
    private File baseDepositDir;
    
    /** The directory where files are being downloaded to before they are put into the directory for the
     * corresponding SLA. */
    private File tmpDir;
    
    /** 
     * Constructor. Initialises the file directory. 
     * 
     * @param dir The directory
     */
    public ReferenceArchive(String dirName) {
        ArgumentValidator.checkNotNullOrEmpty(dirName, "String dirName");
        
        // Instantiate the directories for this archive.
        baseDepositDir = FileUtils.retrieveDirectory(dirName);
        tmpDir = FileUtils.retrieveSubDirectory(baseDepositDir, "tmp");
    }
    
    /**
     * Method for retrieving a file from the file deposit area of this 
     * reference pillar.
     * 
     * @param fileId The id of the file to find.
     * @param slaId The slaId for the given file to be found.
     * @return The file, or if the file does not exist (or is not a file) then 
     * a null is returned.
     */
    public File findFile(String fileId, String slaId) {
        File slaDir = getSlaDir(slaId);
        File res = new File(slaDir, fileId);
        if(!res.isFile()) {
            log.debug("The file '" + fileId + "' belonging to the SLA '"
                    + slaId + "' cannot be found at '" + res.getAbsolutePath()
                    + "' where it should be located.");
            return null;
        }
        return res;
    }
    
    /**
     * Method for retrieving the directory for a given SLA.
     * 
     * @param slaId The id for the SLA.
     * @return The directory for the given SLA.
     */
    private File getSlaDir(String slaId) {
        File slaDir = FileUtils.retrieveSubDirectory(baseDepositDir, slaId);
        return slaDir;
    }
    
    /**
     * Method for instantiating a file to be downloaded.
     * 
     * @param fileId The id of the file to download.
     * @return A file for the given fileId at the temporary directory.
     */
    public File getNewFile(String fileId) {
        File res = new File(tmpDir, fileId);
        return res;
    }
    
    /**
     * Method for archiving a file. Moves a file in the temporary directory to
     * the directory for the given SLA. 
     * 
     * @param fileId The id of the file to be archives.
     * @param slaId The id of the SLA which the file belongs to.
     */
    public void archiveFile(String fileId, String slaId) {
        File oldLocation = new File(tmpDir, fileId);
        if(!oldLocation.isFile()) {
        	throw new IllegalArgumentException("The file '" + oldLocation + "' does not exist.");
        }
        File newLocation = new File(getSlaDir(slaId), fileId);
        if(!newLocation.isFile()) {
        	throw new IllegalArgumentException("The file '" + newLocation + "' does already exist.");
        }
        
        oldLocation.renameTo(newLocation);
    }
}
