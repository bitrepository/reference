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

import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.checking.MaxChecksumAgeProvider;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.exception.StepFailedException;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;

/**
 * A workflow step for finding missing checksums.
 * Uses the IntegrityChecker to perform the actual check.
 */
public class HandleObsoleteChecksumsStep extends AbstractWorkFlowStep {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Settings settings;
    private final IntegrityModel store;
    private final IntegrityReporter reporter;
    private final StatisticsCollector sc;
    public static final Duration DEFAULT_MAX_CHECKSUM_AGE = ChronoUnit.YEARS.getDuration();

    public HandleObsoleteChecksumsStep(Settings settings, IntegrityModel store, IntegrityReporter reporter,
                                       StatisticsCollector statisticsCollector) {
        this.settings = settings;
        this.store = store;
        this.reporter = reporter;
        this.sc = statisticsCollector;
    }

    @Override
    public String getName() {
        return "Handle obsolete checksums reporting.";
    }

    /**
     * Queries the IntegrityModel for files with obsolete checksums. Reports them if any is returned.
     * If a pillar is configured to never have its checksums expire, it will be skipped. This is set
     * by having a maxChecksumAge of 0.
     */
    @Override
    public synchronized void performStep() throws StepFailedException {
        MaxChecksumAgeProvider maxChecksumAgeProvider = new MaxChecksumAgeProvider(DEFAULT_MAX_CHECKSUM_AGE,
                settings.getReferenceSettings().getIntegrityServiceSettings().getObsoleteChecksumSettings());

        List<String> pillars = SettingsUtils.getPillarIDsForCollection(reporter.getCollectionID());

        for (String pillar : pillars) {
            long obsoleteChecksums = 0L;
            Duration maxAge = maxChecksumAgeProvider.getMaxChecksumAge(pillar);
            if (maxAge.isZero()) {
                log.info("Skipping obsolete checksums check for pillar '" + pillar + "' as it has a " +
                        "MaxChecksumAge of 0 (i.e., checksums don't expire).");
                continue;
            } else {
                Date outdated = Date.from(Instant.now().minus(maxAge));
                try (IntegrityIssueIterator obsoleteChecksumsIterator = store.findChecksumsOlderThan(outdated, pillar,
                        reporter.getCollectionID())) {
                    String file;
                    while ((file = obsoleteChecksumsIterator.getNextIntegrityIssue()) != null) {
                        try {
                            reporter.reportObsoleteChecksum(file, pillar);
                            obsoleteChecksums++;
                        } catch (IOException e) {
                            throw new StepFailedException("Failed to report file: " + file + " as having an obsolete checksum", e);
                        }
                    }
                }
            }
            sc.getPillarCollectionStat(pillar).setObsoleteChecksums(obsoleteChecksums);
        }
    }

    public static String getDescription() {
        return "Detects and reports files that have obsolete a checksum from one or more pillars in the collection.";
    }
}
