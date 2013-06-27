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
package org.bitrepository.integrityservice.checking;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.ChecksumState;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.bitrepository.integrityservice.checking.reports.ChecksumReportModel;
import org.bitrepository.integrityservice.checking.reports.MissingChecksumReportModel;
import org.bitrepository.integrityservice.checking.reports.OldMissingFileReportModel;
import org.bitrepository.integrityservice.checking.reports.ObsoleteChecksumReportModel;
import org.bitrepository.service.audit.AuditTrailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * IntegrityChecker that systematically goes through the requested files and validates their integrity.
 */
public class SimpleIntegrityChecker implements IntegrityChecker {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The cache for the integrity data.*/
    private final IntegrityModel cache;
    /** FileExistenceValdiator for validating whether all pillars contain given files.*/
    private final FileExistenceValidator fileIdChecker;
    /** Finds the obsolete checksums.*/
    private final ObsoleteChecksumFinder obsoleteChecksumFinder;
    /** Validates the checksum.*/
    private final ChecksumIntegrityValidator checksumValidator;
    
    /**
     * Constructor.
     * @param settings The settings for the system.
     * @param cache The cache with the integrity model.
     * @param auditManager the audit trail manager.
     */
    public SimpleIntegrityChecker(Settings settings, IntegrityModel cache, AuditTrailManager auditManager) {
        this.cache = cache;
        
        this.fileIdChecker = new FileExistenceValidator(settings.getCollections(), cache, auditManager);
        this.obsoleteChecksumFinder = new ObsoleteChecksumFinder(cache);
        this.checksumValidator = new ChecksumIntegrityValidator(cache, auditManager, settings.getCollections());
    }
    
    @Override
    public OldMissingFileReportModel checkFileIDs(FileIDs fileIDs, String collectionId) {
        log.info("Validating the files: '" + fileIDs + "' in collection: '" + collectionId + "'");
        // TODO could perhaps be optimised by using the method 'getMissingFileIDs' from the database ??
        Collection<String> requestedFileIDs = getRequestedFileIDs(fileIDs, collectionId);
        
        OldMissingFileReportModel report = fileIdChecker.generateReport(requestedFileIDs, collectionId);
            
        if(report.hasIntegrityIssues()) {
            log.warn("Found errors in the integrity check: " + report.generateReport());
            
            for(String deleteableFileId : report.getDeleteableFiles()) {
                log.info("The file '{}' is deleteable and will be removed from the cache.", deleteableFileId);
                cache.deleteFileIdEntry(deleteableFileId, collectionId);
            }
        }
        
        return report;
    }
    
    @Override
    public ChecksumReportModel checkChecksum(String collectionId) {
        log.info("Validating the checksum for all the files in collection '" + collectionId + "'.");
        return checksumValidator.generateReport(collectionId);
    }
    
    @Override
    public MissingChecksumReportModel checkMissingChecksums(String collectionId) {
        MissingChecksumReportModel report = new MissingChecksumReportModel(collectionId);
        
        HashSet<String> filesWithMissingChecksum = new HashSet<String>(cache.findMissingChecksums(collectionId));
        for(String fileId : filesWithMissingChecksum) {
            List<String> pillarIds = new ArrayList<String>();
            
            // TODO make a better method for this! Perhaps directly in the database.
            for(FileInfo fileinfo : cache.getFileInfos(fileId, collectionId)) {
                if(fileinfo.getFileState() == FileState.EXISTING 
                        && fileinfo.getChecksumState() == ChecksumState.UNKNOWN) {
                    pillarIds.add(fileinfo.getPillarId());
                }
            }
            
            report.reportMissingChecksum(fileId, pillarIds);
        }
        
        return report;
    }

    @Override
    public ObsoleteChecksumReportModel checkObsoleteChecksums(
        MaxChecksumAgeProvider maxChecksumAgeProvider, Collection<String> pillarIDs, String collectionId) {
        return obsoleteChecksumFinder.generateReport(maxChecksumAgeProvider, pillarIDs, collectionId);
    }
    
    /**
     * Retrieves the collection of requested file ids.
     * @param fileIDs The file ids requested.
     * @return The collection of requested file ids.
     */
    private Collection<String> getRequestedFileIDs(FileIDs fileIDs, String collectionId) {
        if(fileIDs.getAllFileIDs() != null) {
            return cache.getAllFileIDs(collectionId);
        } 
        return Arrays.asList(fileIDs.getFileID());
    }
}
