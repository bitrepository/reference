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
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.reports.ChecksumReport;
import org.bitrepository.service.audit.AuditTrailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Validates the checksum integrity.
 */
public class ChecksumIntegrityValidator {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The cache for the integrity data.*/
    private final IntegrityModel cache;
    /** The audit trail manager.*/
    private final AuditTrailManager auditManager;
    /** The list of pillar ids.*/
    private final List<String> pillarIds;
    
    /**
     * Constructor.
     * @param settings The settings for the system.
     * @param cache The cache with the integrity model.
     * @param auditManager the audit trail manager.
     */
    public ChecksumIntegrityValidator(Settings settings, IntegrityModel cache, AuditTrailManager auditManager) {
        this.cache = cache;
        this.auditManager = auditManager;
        
        this.pillarIds = settings.getCollectionSettings().getClientSettings().getPillarIDs();
    }

    /**
     * Performs the validation of the checksums for the given file ids.
     * This includes voting if some of the checksums differs between the pillars.
     * 
     * @param requestedFileIDs The list of ids for the files to validate.
     * @return The report for the results of the validation.
     */
    public ChecksumReport generateReport(Collection<String> requestedFileIDs) {
        ChecksumReport report = new ChecksumReport();
        
        for(String fileId : requestedFileIDs) {
            Collection<FileInfo> fileinfos = cache.getFileInfos(fileId);
            if(!validateChecksumSpec(fileinfos)) {
                report.reportBadChecksumSpec(fileId);
                auditManager.addAuditEvent(fileId, "IntegrityService", "The file '" + fileId +"' does not have the "
                        + "same checksum type at all pillars", "IntegrityService checking files.", 
                        FileAction.INCONSISTENCY);
            } else {
                validateSingleFile(fileId, fileinfos, report);
            }
        }
        
        return report;
    }
    
    /**
     * Validates whether the checksum specifications are the same for each pillar.
     * @return Whether every pillar have delivered the same type of checksum.
     */
    private boolean validateChecksumSpec(Collection<FileInfo> fileinfos) {
        ChecksumSpecTYPE csType = null;
        for(FileInfo fileInfo : fileinfos) {
            if(csType == null) {
                csType = fileInfo.getChecksumType();
                continue;
            }

            if(!csType.equals(fileInfo.getChecksumType())) {
                log.warn("Expected to see the ChecksumSpec: '" + csType + "', but it was '"
                        + fileInfo.getChecksumType() + "'. Integrity issue found for the file infos: {}", fileinfos);
                return false;
            }
        }

        return true;
    }
    
    /**
     * 
     * @param fileinfos
     * @param report
     */
    private void validateSingleFile(String fileId, Collection<FileInfo> fileinfos, ChecksumReport report) {
        Map<String, Integer> checksumCount = getChecksumCount(fileinfos);
        
        if(checksumCount.size() > 1) {
            log.debug("Has to vote for the checksum on file '" + fileId + "'");
            String chosenChecksum = voteForChecksum(checksumCount);
            handleVoteResults(chosenChecksum, fileId, fileinfos, report);
            
            auditManager.addAuditEvent(fileId, "IntegrityService", "Checksum inconsistency for file '" + fileId + "':"
                    + fileinfos, "IntegrityService validating the checksums.", 
                    FileAction.INCONSISTENCY);
        } else {
            cache.setChecksumAgreement(fileId, pillarIds);
            report.reportNoChecksumIssues(fileId);
        }
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
     * 
     * @param chosenChecksum The chosen checksum to validate the pillars against. If this is null, then no checksum is 
     * valid and all pillars will be set to having an invalid checksum.
     * @param fileId The id of the file to handle the voting results of.
     * @param fileinfos The collection of fileinfos for the given file.
     * @param report The report where the results are put.
     */
    private void handleVoteResults(String chosenChecksum, String fileId, Collection<FileInfo> fileinfos, ChecksumReport report) {
        if(chosenChecksum == null) {
            cache.setChecksumError(fileId, pillarIds);
            for(FileInfo fi : fileinfos) {
                report.reportChecksumError(fileId, fi.getPillarId(), fi.getChecksum());
            }
        } else {
            List<String> pillarsWithCorrectChecksum = new ArrayList<String>();
            List<String> pillarsWithWrongChecksum = new ArrayList<String>();
            
            for(FileInfo fileInfo : fileinfos) {
                if(fileInfo.getChecksum().equals(chosenChecksum)) {
                    pillarsWithCorrectChecksum.add(fileInfo.getPillarId());
                } else {
                    pillarsWithWrongChecksum.add(fileInfo.getPillarId());
                    report.reportChecksumError(fileId, fileInfo.getPillarId(), fileInfo.getChecksum());
                }
            }
            
            if(!pillarsWithCorrectChecksum.isEmpty()) {
                cache.setChecksumAgreement(fileId, pillarsWithCorrectChecksum);
                report.reportChecksumAgreement(fileId, pillarsWithCorrectChecksum, chosenChecksum);
            }
            if(!pillarsWithWrongChecksum.isEmpty()) {
                cache.setChecksumError(fileId, pillarsWithWrongChecksum);
            }
        }
    }
}
