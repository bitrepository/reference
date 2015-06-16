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
import java.sql.SQLException;
import java.util.List;

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A workflow step for finding missing checksums.
 * Uses the IntegrityChecker to perform the actual check.
 */
public class HandleMissingChecksumsStep extends AbstractWorkFlowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The Integrity Model. */
    private final IntegrityModel store;
    /** The report model to populate */
    private final IntegrityReporter reporter;
    private final StatisticsCollector sc;
    
    public HandleMissingChecksumsStep(IntegrityModel store, IntegrityReporter reporter, 
            StatisticsCollector statisticsCollector) {
        this.store = store;
        this.reporter = reporter;
        this.sc = statisticsCollector;
    }
    
    @Override
    public String getName() {
        return "Handle missing checksums reporting.";
    }

    /**
     * Queries the IntegrityModel for files with missing checksums. Reports them if any is returned.
     * @throws SQLException 
     */
    @Override
    public synchronized void performStep() throws Exception {
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(reporter.getCollectionID());

        for(String pillar : pillars) {
            Long missingChecksums = 0L;
            IntegrityIssueIterator missingChecksumsIterator 
                = store.findFilesWithMissingChecksum(reporter.getCollectionID(), pillar);
            
            String missingFile;
            try {
                while((missingFile = missingChecksumsIterator.getNextIntegrityIssue()) != null) {
                    try {
                        reporter.reportMissingChecksum(missingFile, pillar);
                        missingChecksums++;
                    } catch (IOException e) {
                        log.error("Failed to report file: " + missingFile + " as having a missing checksum", e);
                    }
                }
            } finally {
                    missingChecksumsIterator.close();
            }
            sc.getPillarCollectionStat(pillar).setMissingChecksums(missingChecksums);
        }
    }

    public static String getDescription() {
        return "Detects and reports files that are missing a checksum from one or more pillars in the collection.";
    }
}

