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
package org.bitrepository.integrityservice.checking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validator of checksums for a given file.
 * Uses the cache to extract information about the file for the different pillars, and then uses this information
 * to validate whether the different pillars agree upon the checksum, or the given file has integrity issues.
 */
public class ChecksumValidator {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The cache for the integrity data.*/
    private final IntegrityModel cache;
    /** The id of the file to have its checksum validated.*/
    private final String fileId;
    /** The different informations about this file for the different pillars.*/
    private final Collection<FileInfo> fileInfos;
    /** The ids of the pillars containing this file.*/
    private final List<String> pillarIds;
    
    /**
     * Constructor.
     * @param cache The cache for the integrity data.
     */
    public ChecksumValidator(IntegrityModel cache, String fileId) {
        this.cache = cache;
        this.fileId = fileId;
        this.fileInfos = cache.getFileInfos(fileId);
        this.pillarIds = getPillarIds(fileInfos);
    }

    /**
     * Performs the actual checksum validation. 
     * Starts by validating the checksum specifications of the file.
     * Then the different checksum are counted. If more than one checksum is found, then it is voted which
     * one is chosen to be the correct checksum, and the pillars where the file has a different checksum is marked
     * as having integrity issues.
     * @return The report of the checksum validation.
     */
    public IntegrityReport validateChecksum() {
        if(!validateChecksumSpec()) {
            IntegrityReport report = new IntegrityReport();
            report.addFileWithCheksumSpecIssues(fileId);
            return report;
        }
        
        // Maps between pillar and checksum, and between checksum and count of the given checksum.
        Map<String, Integer> checksumCount = getChecksumCount(fileInfos);
        
        // Vote if necessary and tell the cache of the results.
        if(checksumCount.size() > 1) {
            log.trace("Has to vote for the checksum on file '" + fileId + "'");
            String chosenChecksum = voteForChecksum(checksumCount);
            
            return handleVoteResults(chosenChecksum);
        } else {
            // All the pillars must have the same checksum, and the file does not have checksum issues.
            cache.setChecksumAgreement(fileId, pillarIds);
            IntegrityReport report = new IntegrityReport();
            report.addFileWithoutIssue(fileId);
            return report;        
        }
    }
    
    /**
     * Validates whether the checksum specifications are the same for each pillar.
     * @return Whether every pillar have delivered the same checksum.
     */
    private boolean validateChecksumSpec() {
        ChecksumSpecTYPE csType = null;
        for(FileInfo fileInfo : fileInfos) {
            if(csType == null) {
                csType = fileInfo.getChecksumType();
                continue;
            }
            
            if(csType != fileInfo.getChecksumType()) {
                log.warn("Expected to see the ChecksumSpec: '" + csType + "', but it was '" 
                        + fileInfo.getChecksumType() + "'. Integrity issue found for the file infos: {}", fileInfos);
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * Creates the map between the checksums and the count for each different checksum.
     * @param fileInfos The collection of file infos containing the checksums.
     * @return The map between checksums and their count.
     */
    private Map<String, Integer> getChecksumCount(Collection<FileInfo> fileInfos) {
        Map<String, Integer> checksumCount = new HashMap<String, Integer>();
        
        for(FileInfo fileInfo : fileInfos) {
            // Validate that the checksum has been found.
            String checksum = fileInfo.getChecksum();
            if(checksum == null || checksum.isEmpty()) {
                log.warn("The file '" + fileInfo.getFileId() + "' is missing checksum at '" + fileInfo.getPillarId() 
                        + "'. Ignoring: {}", fileInfo);
                continue;
            }
            
            if(checksumCount.containsKey(checksum)) {
                Integer count = checksumCount.get(checksum);
                checksumCount.put(checksum, count + 1);
            } else {
                checksumCount.put(checksum, 1);                
            }
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
     * Generates a integrity report based on the scenario where not all pillars agreed upon the checksum, and there 
     * had to be voted to find a common checksum, which is chosen as the correct checksum.
     * The pillars with this chosen checksum will be marked as valid, whereas the others will be marked as having 
     * checksum errors.
     * @param chosenChecksum The chosen checksum to validate the pillars against. If this is null, then no checksum is 
     * valid and all pillars will be set to having an invalid checksum.
     * @return The report for the vote containing the pillars with invalid checksum.
     */
    private IntegrityReport handleVoteResults(String chosenChecksum) {
        IntegrityReport report = new IntegrityReport();
        
        if(chosenChecksum == null) {
            cache.setChecksumError(fileId, pillarIds);
            report.addIncorrectChecksums(fileId, pillarIds);
        } else {
            List<String> missingPillars = new ArrayList<String>();
            List<String> presentPillars = new ArrayList<String>();
            
            for(FileInfo fileInfo : fileInfos) {
                if(!fileInfo.getChecksum().equals(chosenChecksum)) {
                    missingPillars.add(fileInfo.getPillarId());
                } else {
                    presentPillars.add(fileInfo.getPillarId());
                }
            }
            
            if(!presentPillars.isEmpty()) {
                cache.setChecksumAgreement(fileId, presentPillars);
            }
            if(!missingPillars.isEmpty()) {
                cache.setChecksumError(fileId, missingPillars);
            }
            report.addIncorrectChecksums(fileId, missingPillars);
        }
        
        return report;
    }
    
    /**
     * Method for extracting the pillar ids from a list of file infos.
     * @param fileInfos The collection of information about a given file.
     * @return The pillars who have information about this file.
     */
    private List<String> getPillarIds(Collection<FileInfo> fileInfos) {
        List<String> res = new ArrayList<String>();
        for(FileInfo fileInfo : fileInfos) {
            res.add(fileInfo.getPillarId());
        }
        return res;
    }
}
