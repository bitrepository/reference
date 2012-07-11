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
import java.util.List;

/**
 * The report for a missing checksum check.
 */
public class MissingChecksumReport implements IntegrityReport {
    /** The list of missing checksums.*/
    private final List<MissingChecksum> missingChecksum = new ArrayList<MissingChecksum>();
    
    /**
     * Report missing checksum.
     * @param fileId The id of the file which are missing the checksum.
     * @param pillarIds The pillars where the checksum fo the file is missing.
     */
    public void reportMissingChecksum(String fileId, List<String> pillarIds) {
        missingChecksum.add(new MissingChecksum(fileId, pillarIds));
    }
    
    @Override
    public boolean hasIntegrityIssues() {
        return !missingChecksum.isEmpty();
    }
    
    @Override
    public String generateReport() {
        if(!hasIntegrityIssues()) {
            return "No missing checksums. \n";
        }
        
        StringBuilder res = new StringBuilder();
        res.append("Files missing their checksum and at which pillars the checksum is missing: \n");
        for(MissingChecksum mf : missingChecksum) {
            res.append(mf.fileId + " : " + mf.pillarIds + "\n");
        }
        return res.toString();
    }
    
    /**
     * @return The missing checksums.
     */
    public List<MissingChecksum> getMissingChecksums() {
        return missingChecksum;
    }
    
    /**
     * Container for the information about a single file missing the checksum at some pillar(s).
     */
    public class MissingChecksum {
        /** The id of the file where the checksum is missing.*/
        final String fileId;
        /** The list of id for the pillars where the checksum of the file is missing. */
        final List<String> pillarIds;
        
        /**
         * Constructor.
         * @param fileId The id of the file where the checksum is missing.
         * @param pillarIds The list of ids for the pillars where the checksum of the file is missing. 
         */
        public MissingChecksum(String fileId, List<String> pillarIds) {
            this.fileId = fileId;
            this.pillarIds = pillarIds;
        }
        
        /**
         * @return The id of the file which is missing the checksum.
         */
        public String getFileId() {
            return fileId;
        }
        
        /**
         * @return The ids of the pillars who are missing the checksum.
         */
        public List<String> getPillarIds() {
            return pillarIds;
        }
    }
}
