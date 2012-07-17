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
    
    @Override
    public boolean hasIntegrityIssues() {
        return !filesWithIssues.isEmpty();
    }

    @Override
    public String generateReport() {
        if(!hasIntegrityIssues()) {
            return "No checksums issues. \n";
        }
        
        StringBuilder res = new StringBuilder();
        res.append("Reported checksum issues: \n");
        for(ChecksumIssue ci : filesWithIssues.values()) {
            res.append(ci.toString());
        }
        
        return res.toString();
    }
    
    /**
     * @return The validated files without issues.
     */
    public List<String> getFilesWithoutIssues() {
        return new ArrayList<String>(filesWithoutIssues);
    }

    /**
     * @return The validated files with issues. A map between the file ids and their issues.
     */
    public Map<String, ChecksumIssue> getFilesWithIssues() {
        return new HashMap<String, ChecksumIssue>(filesWithIssues);
    }

    /**
     * Container for the information about a single file with checksum issues.
     * Including the pillar ids both for the pillars who have agreed upon a given checksum, and the pillars
     * which does not agree (if no agreement, then all the pillars 'does not agree').
     * By agreement is meant a majority has agreed to a common checksum.
     */
    public class ChecksumIssue {
        /** The id of the file where the checksum is missing.*/
        private final String fileId;
        /** The agreed upon checksum for the file.*/
        private String checksum = null;
        /** The list of id for the pillars who agree about the checksum. */
        private List<String> agreingPillars;
        /** Mapping between the pillars who disagree about the common checksum, and their respective checksum.*/
        private final Map<String, String> disAgreeingPillars;
        
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
        
        /**
         * @return The id of the file for this checksum issue.
         */
        public String getFileId() {
            return fileId;
        }
        
        /**
         * @return The mapping between the disagreeing pillar ids and their checksum.
         */
        public Map<String, String> getDisagreeingPillars() {
            return new HashMap<String, String>(disAgreeingPillars);
        }
        
        /**
         * @return The checksum agreed upon.
         */
        public String getChecksum() {
            return checksum;
        }
        
        /**
         * @return The list of pillars agreeing on the common checksum.
         */
        public List<String> getAgreeingPillars() {
            return new ArrayList<String>(agreingPillars);
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
            
            return res.toString();
        }
    }
}
