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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.exception.StepFailedException;
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
    private final StatisticsCollector sc;
    private final Map<String, Long> pillarChecksumErrors;
    private Long allPillarChecksumErrors = 0L;
    private Long collectionChecksumErrors = 0L; 
    
    public HandleChecksumValidationStep(IntegrityModel store, AuditTrailManager auditManager, 
            IntegrityReporter reporter, StatisticsCollector statisticsCollector) {
        this.store = store;
        this.auditManager = auditManager;
        this.reporter = reporter;
        this.sc = statisticsCollector;
        pillarChecksumErrors = new HashMap<>();
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(reporter.getCollectionID());
        for(String pillar : pillars) {
            pillarChecksumErrors.put(pillar, new Long(0));
        }
    }
    
    @Override
    public String getName() {
        return "Handle validation of checksums.";
    }

    /**
     * Queries the IntegrityModel for missing files on each pillar. Reports them if any is returned.
     */
    @Override
    public synchronized void performStep() throws Exception {
        IntegrityIssueIterator inconsistentFilesIterator 
            = store.getFilesWithInconsistentChecksums(reporter.getCollectionID());
        String fileID;
        try {
            while((fileID = inconsistentFilesIterator.getNextIntegrityIssue()) != null) {
                handleChecksumInconsistency(store.getFileInfos(fileID, reporter.getCollectionID()), fileID);
                collectionChecksumErrors++;
            }
        } finally {
            inconsistentFilesIterator.close();
        }
        for(Entry<String, Long> entry : pillarChecksumErrors.entrySet()) {
            sc.getPillarCollectionStat(entry.getKey()).setChecksumErrors(entry.getValue() + allPillarChecksumErrors);
        }
        sc.getCollectionStat().setChecksumErrors(collectionChecksumErrors);
    }

    /**
     * Locates the source of the checksum inconsistency.
     * If only a single pillar is inconsistent with the majority, then it alone will be set to checksum error for the 
     * file.
     * Otherwise all the pillars will be set to checksum error for the file.
     * @param infos The FileInfos
     * @param fileID The id of the file.
     * @throws StepFailedException 
     */
    private void handleChecksumInconsistency(Collection<FileInfo> infos, String fileID) throws StepFailedException {
        Map<String, List<String>> checksumMap = getChecksumMapping(infos);
        String pillarID = findSingleInconsistentPillar(checksumMap);

        createAuditForInconsistentChecksum(pillarID, fileID);
        try {
            if(pillarID == null) {
                allPillarChecksumErrors++;
                for(FileInfo info : infos) {
                    reporter.reportChecksumIssue(fileID, info.getPillarId());
                }
            } else {
                pillarChecksumErrors.put(pillarID, pillarChecksumErrors.get(pillarID) + 1);
                reporter.reportChecksumIssue(fileID, pillarID);
            }    
        } catch (IOException e) {
            throw new StepFailedException("Failed to report file: " + fileID + " as having a checksum issue", e);
        }
        
    }
    
    /**
     * Creates a audit-trail for inconsistency between checksums.
     * If only one pillar is alone with a checksum compared to all the others, then it is pointed out at the possible 
     * cause.
     * @param pillarId The id of the pillar, which is inconsistent with the other pillars. Or null, if no single pillar
     * is causing the inconsistency.
     * @param fileId The id of the file.
     */
    private void createAuditForInconsistentChecksum(String pillarId, String fileId) {
        String auditText;
        
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
