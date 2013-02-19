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

import java.util.HashSet;
import java.util.Set;

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.reports.ChecksumReportModel;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.settings.repositorysettings.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Performs the validation of the integrity for the checksums.
 */
public class ChecksumIntegrityValidator {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The cache for the integrity data.*/
    private final IntegrityModel cache;
    /** The audit trail manager.*/
    private final AuditTrailManager auditManager;
    /** The ids of the pillars in the collection.*/
    private final Collection collection;
    
    /**
     * Constructor.
     * @param cache The cache with the integrity model.
     * @param auditManager the audit trail manager.
     * @param pillarIds This ids of the pillars in the collection.
     */
    public ChecksumIntegrityValidator(IntegrityModel cache, AuditTrailManager auditManager, Collection collection) {
        this.cache = cache;
        this.auditManager = auditManager;
        this.collection = collection;
    }

    /**
     * Performs the validation of the checksums for the given file ids.
     * This includes voting if some of the checksums differs between the pillars.
     *
     * @return The report for the results of the validation.
     */
    public ChecksumReportModel generateReport() {
        ChecksumReportModel report = new ChecksumReportModel();
        
        for(String fileId : cache.getFilesWithInconsistentChecksums()) {
            handleChecksumIssue(fileId, report);
        }
        cache.setFilesWithConsistentChecksumToValid();
        
        return report;
    }
    
    /**
     * Validates the checksum for a single file. 
     * @param fileId The id of the file to validate.
     * @param report The report with the results of the validation.
     */
    private void handleChecksumIssue(String fileId, ChecksumReportModel report) {
        java.util.Collection<FileInfo> fileinfos = cache.getFileInfos(fileId);
        java.util.Collection<String> uniqueChecksums = getDistinctChecksums(fileinfos);
        
        if(uniqueChecksums.size() > 1) {
            auditManager.addAuditEvent(collection.getID(), fileId, "IntegrityService", "Checksum inconsistency for file '" + fileId + "'. "
                    + "The pillar have more than one unique checksum.", "IntegrityService validating the checksums.", 
                    FileAction.INCONSISTENCY);
            
            for(FileInfo fi : fileinfos) {
                report.reportChecksumIssue(fi.getFileId(), fi.getPillarId(), fi.getChecksum());
            }
            cache.setChecksumError(fileId, collection.getPillarIDs().getPillarID());
        } else {
            cache.setChecksumAgreement(fileId, collection.getPillarIDs().getPillarID());
            log.debug("No checksum issues found for the file '" + fileId + "'.");
        }
    }
    
    /**
     * Creates a collection of the distinct checksums within the given collection of file infos.
     * @param fileInfos The collection of file infos containing the checksums.
     * @return The collection of distinct checksums.
     */
    private java.util.Collection<String> getDistinctChecksums(java.util.Collection<FileInfo> fileInfos) {
        Set<String> res = new HashSet<String>();
        
        for(FileInfo fileInfo : fileInfos) {
            String checksum = fileInfo.getChecksum();
            if(checksum == null) {
                log.info("The file '" + fileInfo.getFileId() + "' is missing checksum at '" + fileInfo.getPillarId() 
                        + "'. Ignoring: {}", fileInfo);
                continue;
            }
            
            res.add(checksum);
        }
        
        return res;
    }
}
