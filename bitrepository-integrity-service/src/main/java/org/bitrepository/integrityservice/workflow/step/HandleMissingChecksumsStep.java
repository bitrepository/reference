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

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.exception.StepFailedException;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * A workflow step for finding missing checksums.
 * Uses the IntegrityChecker to perform the actual check.
 */
public class HandleMissingChecksumsStep extends AbstractWorkFlowStep {
    private final IntegrityModel store;
    private final IntegrityReporter reporter;
    private final StatisticsCollector sc;
    private final Date cutoffDate;

    public HandleMissingChecksumsStep(IntegrityModel store, IntegrityReporter reporter, StatisticsCollector statisticsCollector,
                                      Date latestChecksumUpdate) {
        this.store = store;
        this.reporter = reporter;
        this.sc = statisticsCollector;
        this.cutoffDate = latestChecksumUpdate;
    }

    @Override
    public String getName() {
        return "Handle missing checksums reporting.";
    }

    /**
     * Queries the IntegrityModel for files with missing checksums. Reports them if any is returned.
     *
     * @throws StepFailedException   if the report file could not be written
     * @throws IllegalStateException if there was a problem with the database
     */
    @Override
    public synchronized void performStep() throws StepFailedException {
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(reporter.getCollectionID());

        for (String pillar : pillars) {
            Long missingChecksums = 0L;


            String missingFile;
            try (IntegrityIssueIterator missingChecksumsIterator = store.findFilesWithMissingChecksum(reporter.getCollectionID(), pillar,
                    cutoffDate)) {
                while ((missingFile = missingChecksumsIterator.getNextIntegrityIssue()) != null) {
                    try {
                        reporter.reportMissingChecksum(missingFile, pillar);
                        missingChecksums++;
                    } catch (IOException e) {
                        throw new StepFailedException("Failed to report file: " + missingFile + " as having a missing checksum", e);
                    }
                }
            }
            sc.getPillarCollectionStat(pillar).setMissingChecksums(missingChecksums);
        }
    }

    public static String getDescription() {
        return "Detects and reports files that are missing a checksum from one or more pillars in the collection.";
    }
}

