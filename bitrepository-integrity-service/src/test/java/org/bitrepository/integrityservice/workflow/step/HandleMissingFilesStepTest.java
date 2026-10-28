/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2026 Royal Danish Library and The State Archives, Denmark
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

import org.bitrepository.TestGroups;
import org.bitrepository.integrityservice.cache.FileInfo;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class HandleMissingFilesStepTest extends WorkflowstepTest {
    private static final String TEST_PILLAR_2 = "test-pillar-2";
    private static final String TEST_FILE = "test-file-1";

    private IntegrityReporter reporter;
    private StatisticsCollector statisticsCollector;
    private HandleMissingFilesStep step;

    @BeforeEach
    void setupStep() {
        settings.getRepositorySettings().getCollections().getCollection().getFirst().getPillarIDs().getPillarID()
                .add(TEST_PILLAR_2);
        reporter = mock(IntegrityReporter.class);
        when(reporter.getCollectionID()).thenReturn(TEST_COLLECTION);
        statisticsCollector = new StatisticsCollector(TEST_COLLECTION);
        step = new HandleMissingFilesStep(model, reporter, statisticsCollector, Duration.ofHours(1));
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void reportsFileAsMissingOnPillarsWithoutIt() throws Exception {
        whenFileIsMissingSomewhere(Instant.EPOCH, new FileInfo(TEST_FILE, TEST_PILLAR_1));

        step.performStep();

        verify(reporter).reportMissingFile(TEST_FILE, TEST_PILLAR_2);
        verify(reporter, never()).reportMissingFile(eq(TEST_FILE), eq(TEST_PILLAR_1));
        assertEquals(0L, statisticsCollector.getPillarCollectionStat(TEST_PILLAR_1).getMissingFiles());
        assertEquals(1L, statisticsCollector.getPillarCollectionStat(TEST_PILLAR_2).getMissingFiles());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void reportsFileAsMissingOnAllPillarsWhenNoPillarHasIt() throws Exception {
        whenFileIsMissingSomewhere(Instant.EPOCH);

        step.performStep();

        verify(reporter).reportMissingFile(TEST_FILE, TEST_PILLAR_1);
        verify(reporter).reportMissingFile(TEST_FILE, TEST_PILLAR_2);
        assertEquals(1L, statisticsCollector.getPillarCollectionStat(TEST_PILLAR_1).getMissingFiles());
        assertEquals(1L, statisticsCollector.getPillarCollectionStat(TEST_PILLAR_2).getMissingFiles());
    }

    @Test
    @Tag(TestGroups.REGRESSIONTEST)
    void doesNotReportFilesNewerThanTheGracePeriod() throws Exception {
        whenFileIsMissingSomewhere(Instant.now(), new FileInfo(TEST_FILE, TEST_PILLAR_1));

        step.performStep();

        verify(reporter, never()).reportMissingFile(any(), any());
        assertEquals(0L, statisticsCollector.getPillarCollectionStat(TEST_PILLAR_2).getMissingFiles());
    }

    private void whenFileIsMissingSomewhere(Instant earliestFileInstant, FileInfo... fileInfos) throws IOException {
        IntegrityIssueIterator issues = mock(IntegrityIssueIterator.class);
        when(issues.getNextIntegrityIssue()).thenReturn(TEST_FILE).thenReturn(null);
        when(model.findFilesWithMissingCopies(eq(TEST_COLLECTION), any(), any(), any())).thenReturn(issues);
        when(model.getEarliestFileInstant(TEST_COLLECTION, TEST_FILE)).thenReturn(earliestFileInstant);
        when(model.getFileInfos(TEST_FILE, TEST_COLLECTION)).thenReturn(List.of(fileInfos));
    }
}
