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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.FileState;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.service.audit.AuditTrailManager;
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
    public synchronized void performStep() throws Exception {
        IntegrityIssueIterator inconsistentFilesIterator 
            = store.getFilesWithInconsistentChecksums(reporter.getCollectionID());
        List<String> collectionPillars = SettingsUtils.getPillarIDsForCollection(reporter.getCollectionID());
        String file;
        try {
            while((file = inconsistentFilesIterator.getNextIntegrityIssue()) != null) {
                Collection<FileInfo> infos = store.getFileInfos(file, reporter.getCollectionID());
                Set<String> checksums = getUniqueChecksums(infos);
                if(checksums.size() > 1) {
                    createAuditForInconsistentChecksum(infos, file);
                    for(FileInfo info : infos) {
                        try {
                            reporter.reportChecksumIssue(file, info.getPillarId());
                        } catch (IOException e) {
                            log.error("Failed to report file: " + file + " as having a checksum issue", e);
                        }
                    }
                    store.setChecksumError(file, getPillarsFileExisting(infos), reporter.getCollectionID());
                } else {
                    log.error("File with inconsistent checksums from SQL have apparently not inconsistency according to "
                            + "Java! This is a scenario, which must never occur!!!");
                    store.setChecksumAgreement(file, collectionPillars, reporter.getCollectionID());
                }
            }
        } finally {
            inconsistentFilesIterator.close();
        }
        store.setFilesWithConsistentChecksumToValid(reporter.getCollectionID());
    }
    
    /**
     * Retrieves the unique checksums for the files, which exists.
     * Ignores files with another filestate than 'EXISTING'.
     * @param infos The FileInfo with information about the file at the different pillars. 
     * @return The set of unique checksums.
     */
    private Set<String> getUniqueChecksums(Collection<FileInfo> infos) {
        Set<String> checksums = new HashSet<String>();
        
        for(FileInfo info : infos) {
            if((info.getChecksum() != null) && (info.getFileState() == FileState.EXISTING)) {
                checksums.add(info.getChecksum());
            }
        }
        
        return checksums;
    }
    
    /**
     * Extract the pillars where the file has state 'EXISTING'.
     * @param infos The FileInfo with information about the file at the different pillars. 
     * @return The list of pillars which has the file.
     */
    private List<String> getPillarsFileExisting(Collection<FileInfo> infos) {
        List<String> res = new ArrayList<String>();
        
        for(FileInfo info : infos) {
            if(info.getFileState() == FileState.EXISTING) {
                res.add(info.getPillarId());
            }
        }
        
        return res;
    }
    
    /**
     * Creates a audit-trail for inconsistency between checksums.
     * If only one pillar is alone with a checksum compared to all the others, then it is pointed out at the possible 
     * cause.
     * @param infos The information about the file at all the pillars.
     * @param fileId The id of the file.
     */
    private void createAuditForInconsistentChecksum(Collection<FileInfo> infos, String fileId) {
        String auditText;
        Map<String, List<String>> checksumMap = getChecksumMapping(infos);
        String pillarId = findSingleInconsistentPillar(checksumMap);
        
        if(pillarId != null) {
            auditText = "Checksum inconsistency for the file '" + fileId + "' at collection '" 
                    + reporter.getCollectionID() + "'. Possibly corrupt at pillar '" + pillarId 
                    + "', since all the other pillars agree upon another checksum.";
        } else {
            auditText = "Checksum inconsistency for the file '" + fileId + "' at collection '" 
                    + reporter.getCollectionID() + "'. The pillars have registered more than one unique "
                    + "checksum for the file.";
        }
        auditManager.addAuditEvent(reporter.getCollectionID(), fileId, "IntegrityService", 
                auditText, "IntegrityService validating the checksums.", FileAction.INCONSISTENCY, null, null);
    }
    
    /**
     * Retrieves the mapping between the checksums and the pillars, e.g. which pillars have a given checksum.
     * @param infos The information about a given file at all the pillars. 
     * @return The mapping between checksums and the pillars with that checksum.
     */
    private Map<String, List<String>> getChecksumMapping(Collection<FileInfo> infos) {
        Map<String, List<String>> checksumMap = new HashMap<String, List<String>>();
        for(FileInfo info : infos) {
            List<String> pillarIdsForChecksum;
            if(checksumMap.containsKey(info.getChecksum())) {
                pillarIdsForChecksum = checksumMap.get(info.getChecksum());
            } else {
                pillarIdsForChecksum = new ArrayList<String>();
            }
            pillarIdsForChecksum.add(info.getPillarId());
            checksumMap.put(info.getChecksum(), pillarIdsForChecksum);
        }
        
        return checksumMap;
    }
    
    /**
     * Tries to find a single pillar, who causes the inconsistency, otherwise a null is returned.
     * This pillar must be alone with its checksum, whereas all the other pillars (minimum 2 other pillars) must 
     * agree upon another checksum. 
     * @param checksumMap The mapping between checksums and the pillars with that checksum.
     * @return The id of the pillar possibly causing the inconsistency, or null if no single pillar can be found.
     */
    private String findSingleInconsistentPillar(Map<String, List<String>> checksumMap) {
        if(checksumMap.size() != 2) {
            return null;
        }
        List<List<String>> pillarLists = new ArrayList<List<String>>(checksumMap.values());
        
        if((pillarLists.get(0).size() > 1) && (pillarLists.get(1).size() > 1)) {
            return null;
        }
        if((pillarLists.get(0).size() == 1) && (pillarLists.get(1).size() == 1)) {
            return null;
        }
        
        if(pillarLists.get(0).size() == 1) {
            return pillarLists.get(0).get(0);
        } else {
            return pillarLists.get(1).get(0);
        }
    }
}
