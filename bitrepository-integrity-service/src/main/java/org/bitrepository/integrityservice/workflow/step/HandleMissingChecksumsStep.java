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
import org.bitrepository.integrityservice.cache.PillarCollectionStat;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.exception.StepFailedException;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * A workflow step for finding missing checksums.
 * Uses the IntegrityChecker to perform the actual check.
 * <p>
 * Only a workflow that re-verifies every file in the collection (a full sweep) can authoritatively determine
 * this count. A workflow that only touches a subset of files (e.g. an incremental check) cannot tell whether a
 * file it left untouched is genuinely missing its checksum or simply wasn't due for re-checking, so it must not
 * overwrite a previously established count with a partial recomputation - it instead carries the previous count
 * forward, see {@code canDetectMissingChecksums}.
 */
public class HandleMissingChecksumsStep extends AbstractWorkFlowStep {
    private final IntegrityModel store;
    private final IntegrityReporter reporter;
    private final StatisticsCollector sc;
    private final Instant cutoffDate;
    private final boolean canDetectMissingChecksums;

    /**
     * @param cutoffDate                 the cutoff date to use when scanning for missing checksums. Only
     *                                    consulted when {@code canDetectMissingChecksums} is true.
     * @param canDetectMissingChecksums  whether this workflow run re-verifies every file and can therefore
     *                                    authoritatively (re)compute the missing checksums count. When false,
     *                                    the previously reported count is carried forward unchanged instead.
     */
    public HandleMissingChecksumsStep(IntegrityModel store, IntegrityReporter reporter, StatisticsCollector statisticsCollector,
                                      Instant cutoffDate, boolean canDetectMissingChecksums) {
        this.store = store;
        this.reporter = reporter;
        this.sc = statisticsCollector;
        this.cutoffDate = cutoffDate;
        this.canDetectMissingChecksums = canDetectMissingChecksums;
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

        if (canDetectMissingChecksums) {
            scanForMissingChecksums(pillars);
        } else {
            carryForwardPreviouslyReportedMissingChecksums(pillars);
        }
    }

    private void scanForMissingChecksums(List<String> pillars) throws StepFailedException {
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

    /**
     * Carries the previously reported missing-checksums count forward unchanged, for pillars where no such
     * count has been reported yet (e.g. before the first ever complete check), the count defaults to 0.
     */
    private void carryForwardPreviouslyReportedMissingChecksums(List<String> pillars) {
        Map<String, PillarCollectionStat> previousStats = store.getLatestPillarStats(reporter.getCollectionID()).stream()
                .collect(Collectors.toMap(PillarCollectionStat::getPillarID, Function.identity()));

        for (String pillar : pillars) {
            PillarCollectionStat previousStat = previousStats.get(pillar);
            Long missingChecksums = previousStat != null ? previousStat.getMissingChecksums() : 0L;
            sc.getPillarCollectionStat(pillar).setMissingChecksums(missingChecksums);
        }
    }

    public static String getDescription() {
        return "Detects and reports files that are missing a checksum from one or more pillars in the collection.";
    }
}
