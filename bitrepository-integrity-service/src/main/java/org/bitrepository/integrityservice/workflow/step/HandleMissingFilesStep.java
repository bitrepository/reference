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
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.PillarCollectionMetric;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.exception.StepFailedException;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * A workflow step for finding missing files.
 * Uses the IntegrityChecker to perform the actual check.
 */
public class HandleMissingFilesStep extends AbstractWorkFlowStep {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final IntegrityModel store;
    private final IntegrityReporter reporter;
    private final StatisticsCollector sc;
    private final Duration gracePeriod;

    public HandleMissingFilesStep(IntegrityModel store, IntegrityReporter reporter,
            StatisticsCollector statisticsCollector, Duration missingFileGracePeriod) {
        this.store = store;
        this.reporter = reporter;
        this.sc = statisticsCollector;
        this.gracePeriod = Objects.requireNonNull(missingFileGracePeriod, "missingFileGracePeriod");
    }

    @Override
    public String getName() {
        return "Handle check for missing files.";
    }

    /**
     * Queries the IntegrityModel for missing files on each pillar. Reports them if any is returned.
     * <p>
     * Also records the file count for the collection and each pillar here, right next to the missing-files count:
     * both are computed from the current state of the fileinfo table, so taking them from the same point in the
     * workflow keeps them consistent with each other. Computing them at separate, far-apart steps (as used to be
     * the case, with file counts only gathered once checksum validation had finished) allowed the two numbers to
     * drift apart on a collection with files being added while the workflow was running.
     */
    @Override
    public synchronized void performStep() throws StepFailedException {
        String collectionID = reporter.getCollectionID();
        List<String> pillars = SettingsUtils.getPillarIDsForCollection(collectionID);
        recordFileCounts(collectionID, pillars);

        Map<String, Long> missingFilesMap = new HashMap<>();
        for (String pillar : pillars) {
            missingFilesMap.put(pillar, 0L);
        }
        Instant missingAfterInstant = Instant.now().minus(gracePeriod);
        Date missingAfterDate = Date.from(missingAfterInstant);
        log.info("Looking for missing files, files need to be older than {} to be considered missing.",
                missingAfterDate);

        try (IntegrityIssueIterator issueIterator = store.findFilesWithMissingCopies(reporter.getCollectionID(),
                pillars, 0L, Long.MAX_VALUE)) {

            String missingFile;
            while ((missingFile = issueIterator.getNextIntegrityIssue()) != null) {
                Instant earliestDate = store.getEarliestFileInstant(reporter.getCollectionID(), missingFile);
                if (earliestDate.isBefore(missingAfterInstant)) {
                    try {
                        Set<String> pillarsWithFile = getPillarsWithFile(missingFile, reporter.getCollectionID());
                        for (String pillar : pillars) {
                            if (!pillarsWithFile.contains(pillar)) {
                                reporter.reportMissingFile(missingFile, pillar);
                                missingFilesMap.put(pillar, missingFilesMap.get(pillar) + 1);
                            }
                        }
                    } catch (IOException e) {
                        throw new StepFailedException("Failed to report file: " + missingFile + " as missing", e);
                    }
                } else {
                    log.info("The file '{}' was too recent ({}) to be considered missing.", missingFile, earliestDate);
                }

                for (String pillar : missingFilesMap.keySet()) {
                    sc.getPillarCollectionStat(pillar).setMissingFiles(missingFilesMap.get(pillar));
                }

            }
        }
    }

    /**
     * Records the collection's and each pillar's current file count and data size into the statistics collector.
     */
    private void recordFileCounts(String collectionID, List<String> pillars) {
        Map<String, PillarCollectionMetric> pillarMetrics = store.getPillarCollectionMetrics(collectionID);
        for (String pillar : pillars) {
            PillarCollectionMetric metric = pillarMetrics.get(pillar);
            if (metric == null) {
                sc.getPillarCollectionStat(pillar).setFileCount(0L);
                sc.getPillarCollectionStat(pillar).setDataSize(0L);
                sc.getPillarCollectionStat(pillar).setOldestChecksumTimestamp(null);
            } else {
                sc.getPillarCollectionStat(pillar).setFileCount(metric.getPillarFileCount());
                sc.getPillarCollectionStat(pillar).setDataSize(metric.getPillarCollectionSize());
                sc.getPillarCollectionStat(pillar).setOldestChecksumTimestamp(metric.getOldestChecksumTimestamp());
            }
        }
        sc.getCollectionStat().setFileCount(store.getNumberOfFilesInCollection(collectionID));
        sc.getCollectionStat().setDataSize(store.getCollectionFileSize(collectionID));
        sc.getCollectionStat().setLatestFileTime(store.getDateForNewestFileEntryForCollectionInstant(collectionID));
    }

    private Set<String> getPillarsWithFile(String fileID, String collectionID) {
        Collection<FileInfo> fileInfos = store.getFileInfos(fileID, collectionID);
        Set<String> pillarsWithFile = new HashSet<>();
        for (FileInfo fi : fileInfos) {
            pillarsWithFile.add(fi.getPillarId());
        }
        return pillarsWithFile;
    }

    public static String getDescription() {
        return "Detects and reports files that are missing from one or more pillars in the collection.";
    }
}
