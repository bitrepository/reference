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
import java.util.List;

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.reports.MissingFileReportModel;
import org.bitrepository.service.audit.AuditTrailManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An integrity checksum tool for validating the existence of files at given pillars.
 */
public class FileExistenceValidator {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The cache for the integrity data.*/
    private final IntegrityModel cache;
    /** The settings.*/
    private final Settings settings;
    /** The audit trail manager.*/
    private final AuditTrailManager auditManager;
    
    /**
     * Constructor.
     * @param settings The settings for the system.
     * @param cache The cache with the integrity model.
     * @param auditManager the audit trail manager.
     */
    public FileExistenceValidator(Settings settings, IntegrityModel cache, AuditTrailManager auditManager) {
        this.cache = cache;
        this.settings = settings;
        this.auditManager = auditManager;
    }
    
    /**
     * Validates the existence of the given file ids, and creates a report with on the pillars where the files 
     * are missing.
     * This will also detect if a file is deleteable, thus not existing at any pillar and can be deleted from the 
     * integrity model.
     * TODO change so we do not need a list of file ids.
     *  
     * @param requestedFileIDs The list of files to validate.
     * @return The report for the existence state of the given files.
     */
    public MissingFileReportModel generateReport(Collection<String> requestedFileIDs) {
        MissingFileReportModel report = new MissingFileReportModel();
        for(String fileId : requestedFileIDs) {
            List<String> pillarIds = cache.getPillarsMissingFile(fileId);

            if(pillarIds.isEmpty()) {
                log.trace("No one is missing the file '{}'", fileId);
                continue;
            }
            
            auditManager.addAuditEvent(fileId, "IntegrityService", "The file '" + fileId +"' does not exist at "
                    + "the pillars '" + pillarIds + "'", "IntegrityService checking files.", 
                    FileAction.INCONSISTENCY);
            
            if(isAllPillars(pillarIds)) {
                report.reportDeletableFile(fileId);
            } else {
                report.reportMissingFile(fileId, pillarIds);
            }
        }
        
        return report;
    }
    
    /**
     * Validates whether the given list of pillar ids contains the ids of all the known pillars.
     * @param pillarIds The list of pillar ids.
     * @return Whether the list of pillar ids is equivalent to list in settings. 
     */
    private boolean isAllPillars(List<String> pillarIds) {
        List<String> knownPillars = new ArrayList<String>(
                settings.getCollectionSettings().getClientSettings().getPillarIDs());
        
        for(String pillarId : pillarIds) {
            knownPillars.remove(pillarId);
        }
        
        return knownPillars.isEmpty();
    }
}
