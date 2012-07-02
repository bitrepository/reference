/*
 * #%L
 * Bitrepository Integrity Service
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
package org.bitrepository.integrityservice.checking.reports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The report for the results of a checksum validation.
 */
public class ChecksumReport implements IntegrityReport {
    /** The list of ids for the files without any issues.*/
    private final List<String> filesWithoutIssues = new ArrayList<String>();
    /** The map between the ids of the files with issues and their respective checksum issue.*/
    private final Map<String, ChecksumIssue> filesWithIssues = new HashMap<String, ChecksumIssue>();
    /** The list of ids for the files with checksum specification issues.*/
    private final List<String> filesWithChecksumSpecIssues = new ArrayList<String>();
    
    /**
     * Keeps track of the id of a file which has no checksum issues.
     * @param fileId The id of the file without checksum issues.
     */
    public void reportNoChecksumIssues(String fileId) {
        filesWithoutIssues.add(fileId);
    }
    
    /**
     * Handle the report about the pillar ids which agree about a checksum for a given file.
     * @param fileId The id of the file.
     * @param pillarIds The ids of the pillars which agree about the checksum.
     * @param checksum The agreed upon checksum.
     */
    public void reportChecksumAgreement(String fileId, List<String> pillarIds, String checksum) {
        if(!filesWithIssues.containsKey(fileId)) {
            filesWithIssues.put(fileId, new ChecksumIssue(fileId));
        }
        
        filesWithIssues.get(fileId).setAgreeingPillars(checksum, pillarIds);
    }
    
    /**
     * Handle the report about a pillar which does not agree upon the common checksum for the given file.
     * @param fileId The id of the file with the checksum issue.
     * @param pillarId The id of the pillar which does not agree upon the checksum.
     * @param checksum The checksum of the pillar.
     */
    public void reportChecksumError(String fileId, String pillarId, String checksum) {
        if(!filesWithIssues.containsKey(fileId)) {
            filesWithIssues.put(fileId, new ChecksumIssue(fileId));
        }
        
        filesWithIssues.get(fileId).addDisagreeingPillar(pillarId, checksum);
    }
    
    /**
     * Handle the report about a file id where the checksum specifications differ between the pillars.
     * @param fileId The id of the file with checksum spec issues.
     */
    public void reportBadChecksumSpec(String fileId) {
        filesWithChecksumSpecIssues.add(fileId);
    }
    
    @Override
    public boolean hasIntegrityIssues() {
        return !filesWithIssues.isEmpty() || !filesWithChecksumSpecIssues.isEmpty();
    }

    @Override
    public String generateReport() {
        if(hasIntegrityIssues()) {
            return "No checksums issues. \n";
        }
        
        StringBuilder res = new StringBuilder();
        res.append("Reported checksum issues: \n");
        for(ChecksumIssue ci : filesWithIssues.values()) {
            res.append(ci.toString());
        }
        
        if(!filesWithChecksumSpecIssues.isEmpty()) {
            res.append("Files with checksum specification issues: " + filesWithChecksumSpecIssues + "\n");
        }
        
        return res.toString();
    }

    /**
     * Container for the information about a single file with checksum issues.
     * Including the pillar ids both for the pillars who have agreed upon a given checksum, and the pillars
     * which does not agree (if no agreement, then all the pillars 'does not agree').
     */
    private class ChecksumIssue {
        /** The id of the file where the checksum is missing.*/
        final String fileId;
        /** The agreed upon checksum for the file.*/
        String checksum = null;
        /** The list of id for the pillars who agree about the checksum. */
        List<String> agreingPillars;
        /** Mapping between the pillars who disagree about the common checksum, and their respective checksum.*/
        final Map<String, String> disAgreeingPillars;
        
        /**
         * Constructor.
         * @param fileId The id of the file where the checksum is missing.
         * @param pillarIds The list of ids for the pillars where the checksum of the file is missing. 
         */
        public ChecksumIssue(String fileId) {
            this.fileId = fileId;
            this.agreingPillars = new ArrayList<String>();
            this.disAgreeingPillars = new HashMap<String, String>();
        }
        
        /**
         * Add the information about a pillar, which does not agree about the common checksum.
         * @param pillarId The id of the pillar.
         * @param checksum The checksum for the pillar.
         */
        public void addDisagreeingPillar(String pillarId, String checksum) {
            // TODO check whether it actually is the right checksum?
            disAgreeingPillars.put(pillarId, checksum);
        }
        
        /**
         * Add the information about pillars who agree about the checksum for the file.
         * @param checksum The agreed upon checksum of the file.
         * @param pillarIds The ids of the pillars, who agree with the checksum.
         */
        public void setAgreeingPillars(String checksum, List<String> pillarIds) {
            this.checksum = checksum;
            this.agreingPillars = pillarIds;
        }
        
        @Override
        public String toString() {
            StringBuilder res = new StringBuilder();
            if(checksum != null) {
                res.append("The file '" + fileId + "' has the agreed upon checksum '" + checksum + "', which"
                        + "is present at the pillars '" + agreingPillars + "'.\n");
            } else {
                res.append("The file '" + fileId + "' has no agreed upon checkums.\n");
            }
            res.append("The pillars who disagree about the checksum has the following checksum: '" 
                    + disAgreeingPillars + "'");
            
            return fileId + "";
        }
    }
}
