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
package org.bitrepository.integrityservice.checking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Container for the results of an integrity check.
 */
public class IntegrityReport {
    /** Whether any errors was found during the check. This is set to 'true', and only changed when actual errors 
     * are reported. */
    private boolean integrityIssueReported = false;
    /** The FileIDs for the report.*/
    private List<ChecksumErrorData> checksumErrors = new ArrayList<ChecksumErrorData>();
    /** The files which are missing.*/
    private List<MissingFileData> missingFiles = new ArrayList<MissingFileData>();
    /** The files where the different pillars have different checksum speficitions for the checksum.*/
    private List<String> filesWithChecksumSpecIssues = new ArrayList<String>();
    /** The files which does not have any integrity issues.*/
    private List<String> filesWithoutIssues = new ArrayList<String>();
    /** The files which are too new to be checked whether they are missing.*/
    private List<String> newUncheckedFiles = new ArrayList<String>();
    
    /**
     * Constructor.
     */
    public IntegrityReport() {}
    
    /**
     * Adds one report to this one. All the elements from the other report are copied into this.
     * @param otherReport The report to add to this one.
     */
    public void combineWithReport(IntegrityReport otherReport) {
        checksumErrors.addAll(otherReport.getChecksumErrors());
        missingFiles.addAll(otherReport.getMissingFiles());
        newUncheckedFiles.addAll(otherReport.getNewUncheckedFiles());
        filesWithoutIssues.addAll(otherReport.getFilesWithoutIssues());
        
        integrityIssueReported |= otherReport.hasIntegrityIssues();
    }
    
    /**
     * @return Whether the integrity check gave a positive result. E.g. returns false, if any integrity problems 
     * occurred (whether any files were missing or any disagreements about checksum).
     */
    public boolean hasIntegrityIssues() {
        return integrityIssueReported;
    }
    
    /**
     * @return The map of checksum errors. Empty if no such errors.
     */
    public List<ChecksumErrorData> getChecksumErrors() {
        return checksumErrors;
    }
    
    /**
     * @return The list of missing files.
     */
    public List<MissingFileData> getMissingFiles() {
        return missingFiles;
    }

    /**
     * @return The files which does not have any integrity issues.
     */
    public List<String> getFilesWithChecksumSpecIssues() {
        return filesWithChecksumSpecIssues;
    }
    
    /**
     * @return The files which does not have any integrity issues.
     */
    public List<String> getFilesWithoutIssues() {
        return filesWithoutIssues;
    }
    
    /**
     * @return The files which are too new to be checked against whether they are missing.
     */
    public List<String> getNewUncheckedFiles() {
        return newUncheckedFiles;
    }
    
    /**
     * Insert the checksum results, when a checksum disagreement occurs.
     * This means that the integrity check found an error.
     * @param fileId The id of the file for the checksum disagreement.
     * @param pillars The list of pillars, where file has a bad checksum.
     */
    public void addIncorrectChecksums(String fileId, Collection<String> pillars) {
        checksumErrors.add(new ChecksumErrorData(fileId, pillars));
        integrityIssueReported = true;
    }
    
    /**
     * Insert the id of a file, which has not been found on every pillar.
     * This means that the integrity check found an error.
     * @param fileId The id of the file, which is missing.
     * @param pillarIds The list of ids for the pillars, where the file is missing.
     */
    public void addMissingFile(String fileId, List<String> pillarIds) {
        missingFiles.add(new MissingFileData(fileId, pillarIds));
        integrityIssueReported = true;
    }
    
    /**
     * Report a file where the different pillars have different specifications for the calculation of the checksum.
     * @param fileId The id of the file with checksum speficiation issues.
     */
    public void addFileWithCheksumSpecIssues(String fileId) {
        filesWithChecksumSpecIssues.add(fileId);
        integrityIssueReported = true;
    }
    
    /**
     * Report a file which is too new to be checked whether it is missing, since it might not have been put to all 
     * pillars yet.
     * @param fileId The id of the file which are not checked for being missing since it is too new.
     */
    public void addTooNewFile(String fileId) {
        newUncheckedFiles.add(fileId);
    }
    
    /**
     * Report a file without any integrity issues.
     * @param fileId The id of the file which does not have integrity issues.
     */
    public void addFileWithoutIssue(String fileId) {
        filesWithoutIssues.add(fileId);
    }
    
    /** 
     * @return A human readable report for the integrity problem.
     */
    public String generateReport() {
        StringBuffer res = new StringBuffer();
        res.append("No integrity problems at the files: " + filesWithoutIssues + "\n");
        
        if(!newUncheckedFiles.isEmpty()) {
            res.append("Files which are too new to be checked: " + newUncheckedFiles + "\n");
        }
        
        if(!integrityIssueReported) {
            return res.toString();
        }
        
        res.append("Integrity issues found:\n");
        for(ChecksumErrorData ced : checksumErrors) {
            res.append(ced.toString());
            res.append("\n");
        }
        res.append("\n");
        for(MissingFileData mfd : missingFiles) {
            res.append(mfd.toString());
            res.append("\n");
        }
        res.append("\n");
        for(String fileId : filesWithChecksumSpecIssues) {
            res.append("File '" + fileId + "' has checksum specification issues.");
            res.append("\n");
        }
        res.append("\n");
        
        return res.toString();
    }
}
