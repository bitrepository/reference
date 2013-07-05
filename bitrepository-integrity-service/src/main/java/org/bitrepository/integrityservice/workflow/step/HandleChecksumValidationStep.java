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
package org.bitrepository.integrityservice.workflow.step;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A workflow step for finding inconsistency between the checksums.
 */
public class HandleChecksumValidationStep extends AbstractWorkFlowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The Integrity Model. */
    private final IntegrityModel store;
    /** The report model to populate */
    private final IntegrityReporter reporter;
    /** The audit trail manager.*/
    private final AuditTrailManager auditManager;
    
    public HandleChecksumValidationStep(IntegrityModel store, AuditTrailManager auditManager, 
            IntegrityReporter reporter) {
        this.store = store;
        this.auditManager = auditManager;
        this.reporter = reporter;
    }
    
    @Override
    public String getName() {
        return "Handle files missing at some pillars.";
    }

    /**
     * Queries the IntegrityModel for missing files on each pillar. Reports them if any is returned.
     */
    @Override
    public synchronized void performStep() {
        List<String> inconsistentFiles = store.getFilesWithInconsistentChecksums(reporter.getCollectionID());
        List<String> collectionPillars = SettingsUtils.getPillarIDsForCollection(reporter.getCollectionID());
        for(String file : inconsistentFiles) {
            Collection<FileInfo> infos = store.getFileInfos(file, reporter.getCollectionID());
            Set<String> checksums = getUniqueChecksums(infos);
            if(checksums.size() > 1) {
                auditManager.addAuditEvent(reporter.getCollectionID(), file, "IntegrityService", 
                        "Checksum inconsistency for file '" + file + "'. The pillar have more than one unique checksum.",
                        "IntegrityService validating the checksums.", FileAction.INCONSISTENCY);
                for(FileInfo info : infos) {
                    reporter.reportChecksumIssue(file, info.getPillarId());
                }
                store.setChecksumError(file, collectionPillars, reporter.getCollectionID());
            } else {
                log.error("File with inconsistent checksums from SQL have apparently not inconsistency according to "
                        + "Java! This is a scenario, which must never occur!!!");
                store.setChecksumAgreement(file, collectionPillars, reporter.getCollectionID());
            }
        }
        
        store.setFilesWithConsistentChecksumToValid(reporter.getCollectionID());
        
    }
    
    private Set<String> getUniqueChecksums(Collection<FileInfo> infos) {
        Set<String> checksums = new HashSet<String>();
        
        for(FileInfo info : infos) {
            if(info.getChecksum() != null) {
                checksums.add(info.getChecksum());
            }
        }
        
        return checksums;
    }

    public static String getDescription() {
        return "Validates checksum consistency, and updates database to reflect the situation.";
    }
}
