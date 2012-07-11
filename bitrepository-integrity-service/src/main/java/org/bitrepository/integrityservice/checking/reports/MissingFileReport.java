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
 * The report for a missing files check.
 * Will report on files missing on some pillars and the files which are missing at all pillars, which then can be 
 * deleted from the system.
 */
public class MissingFileReport implements IntegrityReport {
    /** The list of missing files.*/
    private final List<MissingFile> missingFiles = new ArrayList<MissingFile>();
    /** The list of files, which can be deleted.*/
    private final List<String> deleteableFiles = new ArrayList<String>();
    
    /**
     * Report missing files.
     * @param fileId The id of the file which is missing.
     * @param pillarIds The pillars where the file is missing.
     */
    public void reportMissingFile(String fileId, List<String> pillarIds) {
        missingFiles.add(new MissingFile(fileId, pillarIds));
    }
    
    /**
     * Reports a file, which is missing at every pillar and thus deleteable.
     * @param fileId The id of the file, which can be deleted from the system.
     */
    public void reportDeletableFile(String fileId) {
        deleteableFiles.add(fileId);
    }
    
    /**
     * @return The list of files, which are deleteable.
     */
    public List<String> getDeleteableFiles() {
        return new ArrayList<String>(deleteableFiles);
    }
    
    /**
     * @return The map between the ids of the missing files and the corresponding pillar ids.
     */
    public Map<String, List<String>> getMissingFiles() {
        Map<String, List<String>> res = new HashMap<String, List<String>>();
        for(MissingFile mf : missingFiles) {
            res.put(mf.fileId, mf.pillarIds);
        }
        return res;
    }
    
    @Override
    public boolean hasIntegrityIssues() {
        return !missingFiles.isEmpty() || !deleteableFiles.isEmpty();
    }
    
    @Override
    public String generateReport() {
        if(!hasIntegrityIssues()) {
            return "No missing files. \n";
        }
        
        StringBuilder res = new StringBuilder();
        res.append("Missing files and at which pillars they are missing: \n");
        for(MissingFile mf : missingFiles) {
            res.append(mf.fileId + " : " + mf.pillarIds + "\n");
        }
        res.append("\n");
        if(!deleteableFiles.isEmpty()) {
            res.append("Files which are missing at every pillar and thus can be deleted: \n");
            res.append(deleteableFiles + "\n");
        }
        
        return res.toString();
    }
    
    /**
     * Container for the information about a single missing file.
     */
    private class MissingFile {
        /** The id of the file which is missing.*/
        final String fileId;
        /** The list of ids for the pillars where the file is missing. */
        final List<String> pillarIds;
        
        /**
         * Constructor.
         * @param fileId The id of the file which is missing.
         * @param pillarIds The list of ids for the pillars where the file is missing. 
         */
        public MissingFile(String fileId, List<String> pillarIds) {
            this.fileId = fileId;
            this.pillarIds = pillarIds;
        }
    }
}
