/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityclient.checking;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.FileIDs;

/**
 * Container for the results of an integrity check.
 */
public class IntegrityReport {
    /** Whether any errors was found during the check. This is set to 'true', and only changed when actual errors 
     * are reported. */
    private boolean valid = true;
    /** The FileIDs for the report.*/
    private FileIDs fileIDs;
    /** A mapping between files and a list of pillars, where the file has a bad checksum. */
    private Map<String, Collection<String>> checksumErrors = new HashMap<String, Collection<String>>();
    
    /** The files which are missing.*/
    private Map<String, List<String>> missingFileIDs = new HashMap<String, List<String>>();
    
    /**
     * Constructor.
     * @param fileIDs The FileIDs for this report.
     */
    public IntegrityReport(FileIDs fileIDs) {
        this.fileIDs = fileIDs;
    }
    
    /**
     * @return Whether the integrity check gave a positive result. E.g. returns false, if any integrity problems 
     * occurred (whether any files were missing or any disagreements about checksum).
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * @return The FileIDs for this report.
     */
    public FileIDs getFileIDs() {
        return fileIDs;
    }
    
    /**
     * @return The map of checksum errors. Empty if no such errors.
     */
    public Map<String, Collection<String>> getChecksumErrors() {
        return checksumErrors;
    }
    
    /**
     * @return The map of missing file ids and the pillars, where they are missing.
     */
    public Map<String, List<String>> getMissingFileIDs() {
        return missingFileIDs;
    }
    
    /**
     * Insert the checksum results, when a checksum disagreement occurs.
     * This means that the integrity check found an error.
     * @param fileId The id of the file for the checksum disagreement.
     * @param pillars The list of pillars, where file has a bad checksum.
     */
    public void addIncorrectChecksums(String fileId, Collection<String> pillars) {
        checksumErrors.put(fileId, pillars);
        valid = false;
    }
    
    /**
     * Insert the id of a file, which has not been found on every pillar.
     * This means that the integrity check found an error.
     * @param fileId The id of the file, which is missing.
     * @param pillarIds The list of ids for the pillars, where the file is missing.
     */
    public void addMissingFile(String fileId, List<String> pillarIds) {
        missingFileIDs.put(fileId, pillarIds);
        valid = false;
    }
    
    /** 
     * @return A human readable report for the integrity problem.
     */
    public String generateReport() {
        if(valid) {
            return "Valid";
        }
        StringBuffer res = new StringBuffer();
        
        res.append("Invalid integrity on " + fileIDs + "\n");
        
        if(!checksumErrors.isEmpty()) {
            res.append("Checksum errors: \n");
            for(Map.Entry<String, Collection<String>> fileErrorAtPillars : checksumErrors.entrySet()) {
                res.append(fileErrorAtPillars.getKey());
                res.append(" : ");
                res.append(fileErrorAtPillars.getValue());
                res.append("\n");
            }
        }
        
        if(!missingFileIDs.isEmpty()) {
            res.append("Missing files: \n");
            for(Map.Entry<String, List<String>> missingFile : missingFileIDs.entrySet()) {
                res.append(missingFile.getKey() + ": ");
                for(String pillarId : missingFile.getValue()) {
                    res.append(pillarId + ", ");
                }
                res.append("\n");
            }
        }
        
        return res.toString();
    }
}
