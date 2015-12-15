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
import java.util.Date;
import java.util.List;

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.exception.StepFailedException;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A workflow step for finding missing checksums.
 * Uses the IntegrityChecker to perform the actual check.
 */
public class HandleMissingFilesStep extends AbstractWorkFlowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The Integrity Model. */
    private final IntegrityModel store;
    /** The report model to populate. */
    private final IntegrityReporter reporter;
    private final StatisticsCollector sc;
    /** The period in which a file should not be considered as missing. */
    private final Long gracePeriod;
    
    public HandleMissingFilesStep(IntegrityModel store, IntegrityReporter reporter, 
            StatisticsCollector statisticsCollector, Long missingFileGracePeriod) {
        this.store = store;
        this.reporter = reporter;
        this.sc = statisticsCollector;
        this.gracePeriod = missingFileGracePeriod;
    }
    
    @Override
    public String getName() {
        return "Handle check for missing files.";
    }

    /**
     * Queries the IntegrityModel for inconsistent checksums in the collection. 
     * Checks every reported inconsistent checksum, to verify that it's actually inconsistent. 
     * Updates database model to reflect the discovered situation. 
     */
    @Override
    public synchronized void performStep() throws StepFailedException {
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(reporter.getCollectionID());
        Date missingAfterDate = new Date(System.currentTimeMillis() - gracePeriod);
        for(String pillar : pillars) {
            log.info("Checking for missing files on pillar {}, files needs to be older than {} to be considered missing.", 
                    pillar, missingAfterDate);
            Long missingFiles = 0L;
            IntegrityIssueIterator issueIterator = store.getMissingFilesAtPillarByIterator(pillar, 0, 
                    Integer.MAX_VALUE, reporter.getCollectionID());
            
            String missingFile;
            while((missingFile = issueIterator.getNextIntegrityIssue()) != null) {
                Date earliestDate = store.getEarlistFileDate(reporter.getCollectionID(), missingFile);
                if(earliestDate.before(missingAfterDate)) {
                    try {
                        reporter.reportMissingFile(missingFile, pillar);
                        missingFiles++;
                    } catch (IOException e) {
                        throw new StepFailedException("Failed to report file: " + missingFile + " as missing", e);
                    }
                } else {
                    log.info("The file '{}' was too recent ({}) to be considered missing.", missingFile, earliestDate);
                }
            }
            sc.getPillarCollectionStat(pillar).setMissingFiles(missingFiles);
        }
    }

    public static String getDescription() {
        return "Detects and reports files that are missing from one or more pillars in the collection.";
    }
}
