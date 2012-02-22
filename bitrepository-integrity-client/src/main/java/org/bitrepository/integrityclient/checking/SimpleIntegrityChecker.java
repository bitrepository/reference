/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityclient.cache.FileInfo;
import org.bitrepository.integrityclient.cache.IntegrityCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IntegrityChecker that systematically goes through the requested files and validates their integrity.
 */
public class SimpleIntegrityChecker implements IntegrityChecker {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The cache for */
    private final IntegrityCache cache;
    /** The settings.*/
    private final Settings settings;

    /**
     * Constructor.
     */
    public SimpleIntegrityChecker(Settings settings, IntegrityCache cache) {
        this.cache = cache;
        this.settings = settings;
    }
    
    @Override
    public IntegrityReport checkFileIDs(FileIDs fileIDs) {
        log.info("Validating the files: '" + fileIDs + "'");
        Collection<String> requestedFileIDs = getRequestedFileIDs(fileIDs);
        
        IntegrityReport report = new IntegrityReport(fileIDs);
        
        for(String fileId : requestedFileIDs) {
            log.info("File check on file '" + fileId + "'");
            List<String> pillarIds = checkFileID(fileId);
            if(!pillarIds.isEmpty()) {
                log.info("File '" + fileId + "' is missing at " + pillarIds);
                cache.setFileMissing(fileId, pillarIds);
                report.addMissingFile(fileId, pillarIds);
            } else {
                log.info("The file '" + fileId + "' is not missing anywhere!");
            }
        }
        
        if(!report.isValid()) {
            log.warn("Found errors in the integrity check: " + report.generateReport());
        }
        
        return report;
    }
    
    @Override
    public IntegrityReport checkChecksum(FileIDs fileIDs) {
        log.info("Validating the checksum for the files: '" + fileIDs + "'");
        Collection<String> requestedFileIDs = getRequestedFileIDs(fileIDs);
        
        IntegrityReport report = new IntegrityReport(fileIDs);
        
        for(String fileId : requestedFileIDs) {
            Map<String, Integer> checksumResults = validateChecksumAndGetCount(fileId);
            if(checksumResults.size() > 1) {
                report.addIncorrectChecksums(fileId, checksumResults);
            }
        }
        
        if(!report.isValid()) {
            log.warn("Found errors in the integrity check: " + report.generateReport());
        }
        
        return report;
    }
    
    /**
     * Retrieves the collection of requested file ids.
     * @param fileIDs The file ids requested.
     * @return The collection of requested file ids.
     */
    private Collection<String> getRequestedFileIDs(FileIDs fileIDs) {
        if(fileIDs.getAllFileIDs() != null) {
            return cache.getAllFileIDs();
        } 
        return Arrays.asList(fileIDs.getFileID());
    }
    
    /**
     * Checks whether all pillars contain a given file id.
     * 
     * @param fileId The id of the file to validate.
     * @return The pillars, where the file is missing.
     */
    private List<String> checkFileID(String fileId) {
        List<String> unfoundPillars = settings.getCollectionSettings().getClientSettings().getPillarIDs();
        
        for(FileInfo fileinfo : cache.getFileInfos(fileId)) {
            if(!unfoundPillars.remove(fileinfo.getPillarId())) {
                log.warn("Not expected pillar '" + fileinfo.getPillarId() + "' for file '" + fileId + "'");
            }
        }
        
        return unfoundPillars;
    }
    
    /**
     * Validates the checksum for a given file id is identical at all pillars.
     * TODO validate the checksum type (e.g. algorithm and salt).
     * TODO also validate, that all pillars contains the files.
     * 
     * @param fileId The id of the pillar to have its checksums validated. 
     */
    private Map<String, Integer> validateChecksumAndGetCount(String fileId) {
        Map<String, Integer> checksumCount = new HashMap<String, Integer>();
        
        
        Map<String, String> checksums = getChecksums(fileId);
        for(Map.Entry<String, String> checksumForPillar : checksums.entrySet()) {
            // Validate that the checksum has been found.
            String checksum = checksumForPillar.getValue();
            if(checksum == null || checksum.isEmpty()) {
                log.warn("The file '" + fileId + "' is missing checksum at '" + checksumForPillar.getKey() 
                        + "'. Ignoring: {}", checksumForPillar);
                continue;
            }
            
            if(checksumCount.containsKey(checksum)) {
                Integer count = checksumCount.get(checksum);
                checksumCount.put(checksum, count + 1);
            } else {
                checksumCount.put(checksum, 1);                
            }
        }
        
        // Vote if necessary and tell the cache of the results.
        if(checksumCount.size() > 1) {
            log.trace("Has to vote for the checksum on file '" + fileId + "'");
            String chosenChecksum = voteForChecksum(checksumCount);
            
            if(chosenChecksum == null) {
                cache.setChecksumError(fileId, checksums.keySet());
            } else {
                List<String> missingPillars = new ArrayList<String>();
                List<String> presentPillars = new ArrayList<String>();
                
                for(Map.Entry<String, String> checksumForPillar : checksums.entrySet()) {
                    if(!checksumForPillar.getValue().equals(chosenChecksum)) {
                        missingPillars.add(checksumForPillar.getKey());
                    } else {
                        presentPillars.add(checksumForPillar.getKey());
                    }
                }
                
                cache.setChecksumError(fileId, missingPillars);
                cache.setChecksumAgreement(fileId, presentPillars);
            }
        } else {
            cache.setChecksumAgreement(fileId, checksums.keySet());
        }
        
        return checksumCount; 
    }
    
    /**
     * Votes for finding the checksum.
     * 
     * @param checksumCounts A map between the checksum and the amount of times it has been seen in the system.
     * @return The checksum with the largest amount of votes. Null is returned, if no singe checksum has the largest
     * number of votes.
     */
    private String voteForChecksum(Map<String, Integer> checksumCounts) {
        log.trace("Voting between: " + checksumCounts);
        String chosenChecksum = null;
        Integer largest = 0;
        for(Map.Entry<String, Integer> checksumCount : checksumCounts.entrySet()) {
            if(checksumCount.getValue().compareTo(largest) > 0) {
                chosenChecksum = checksumCount.getKey();
                largest = checksumCount.getValue();
            } else 
            // Two with largest => no chosen.
            if(checksumCount.getValue().compareTo(largest) == 0) {
                chosenChecksum = null;
            }
        }
        log.trace("Voting result: " + chosenChecksum);
        return chosenChecksum;
    }
    
    /**
     * Retrieves a mapping between the pillar ids and their checksums for a given file.
     * @param fileId The id of the file.
     * @return The map between the pillar ids and their checksum of this given file.
     */
    private Map<String, String> getChecksums(String fileId) {
        Map<String, String> checksums = new HashMap<String, String>();
        
        for(FileInfo fileinfo : cache.getFileInfos(fileId)) {
            checksums.put(fileinfo.getPillarId(), fileinfo.getChecksum());
        }
        
        return checksums;
    }
}
