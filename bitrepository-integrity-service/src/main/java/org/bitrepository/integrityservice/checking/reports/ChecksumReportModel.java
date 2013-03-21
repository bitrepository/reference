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

import java.util.HashMap;
import java.util.Map;

/**
 * The report for the results of a checksum validation.
 */
public class ChecksumReportModel implements IntegrityReportModel {
    /** The map between the ids of the files with issues and their respective checksum issue.*/
    private final Map<String, ChecksumIssue> filesWithIssues = new HashMap<String, ChecksumIssue>();
    
    /**
     * Handle the report about a pillar which does not agree upon the common checksum for the given file.
     * @param fileId The id of the file with the checksum issue.
     * @param pillarId The id of the pillar which does not agree upon the checksum.
     * @param checksum The checksum of the pillar.
     */
    public void reportChecksumIssue(String fileId, String pillarId, String checksum) {
        if(!filesWithIssues.containsKey(fileId)) {
            filesWithIssues.put(fileId, new ChecksumIssue(fileId));
        }
        
        filesWithIssues.get(fileId).addPillarWithChecksum(pillarId, checksum);
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

    @Override
    public String generateSummaryOfReport() {
        if(!hasIntegrityIssues()) {
            return "No checksums issues. \n";
        }
        
        return "Reported checksum issues for " + filesWithIssues.size() + " files.";
    }

    /**
     * @return The validated files with issues. A map between the file ids and their issues.
     */
    public Map<String, ChecksumIssue> getFilesWithIssues() {
        return new HashMap<String, ChecksumIssue>(filesWithIssues);
    }

    /**
     * Container for the information about a single file with checksum issues.
     * Has a map between each pillar and its checksum for the given file.
     */
    public class ChecksumIssue {
        /** The id of the file where the checksum is missing.*/
        private final String fileId;
        /** Mapping between the pillars and their respective checksum.*/
        private final Map<String, String> pillarChecksumMap;
        
        /**
         * Constructor.
         * @param fileId The id of the file where the checksum is missing.
         */
        public ChecksumIssue(String fileId) {
            this.fileId = fileId;
            this.pillarChecksumMap = new HashMap<String, String>();
        }
        
        /**
         * Add the checksum and the id of the pillar for the file at the given pillar.
         * @param pillarId The id of the pillar.
         * @param checksum The checksum for the file at the given pillar.
         */
        public void addPillarWithChecksum(String pillarId, String checksum) {
            pillarChecksumMap.put(pillarId, checksum);
        }
        
        /**
         * @return The id of the file for this checksum issue.
         */
        public String getFileId() {
            return fileId;
        }
        
        /**
         * @return The mapping between the pillar ids and their checksum.
         */
        public Map<String, String> getPillarChecksumMap() {
            return new HashMap<String, String>(pillarChecksumMap);
        }
        
        @Override
        public String toString() {
            return "The file '" + fileId + "' has the following checkums for the pillars: " + pillarChecksumMap;
        }
    }
}
