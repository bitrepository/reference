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

import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.cache.database.IntegrityIssueIterator;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.service.exception.StepFailedException;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;

import java.io.IOException;
import java.util.Date;
import java.util.Set;

/**
 * A workflow step for removing files no longer present.
 * Uses the IntegrityChecker to perform the actual check.
 */
public class HandleDeletedFilesStep extends AbstractWorkFlowStep {
    private final IntegrityModel store;
    private final IntegrityReporter reporter;
    private final Date workflowStart;
    private final Set<String> pillarsToClean;

    public HandleDeletedFilesStep(IntegrityModel store, IntegrityReporter reporter, Date workflowStart,
                                  Set<String> pillarsToClean) {
        this.store = store;
        this.reporter = reporter;
        this.workflowStart = workflowStart;
        this.pillarsToClean = pillarsToClean;
    }

    @Override
    public String getName() {
        return "Handle files that are no longer in the collection.";
    }

    /**
     * Queries the IntegrityModel for 'orphan files'. Reports and removes them if any are returned.
     */
    @Override
    public synchronized void performStep() throws StepFailedException {
        for (String pillar : pillarsToClean) {
            try (IntegrityIssueIterator deletedFilesIterator = store.findOrphanFiles(reporter.getCollectionID(),
                    pillar, workflowStart)) {
                String deletedFile;
                while ((deletedFile = deletedFilesIterator.getNextIntegrityIssue()) != null) {
                    store.deleteFileIdEntry(reporter.getCollectionID(), pillar, deletedFile);
                    try {
                        reporter.reportDeletedFile(pillar, deletedFile);
                    } catch (IOException e) {
                        throw new StepFailedException("Failed to report file: " + deletedFile + " as deleted", e);
                    }
                }
            }
        }
    }

    public static String getDescription() {
        return "Detects and removes files that are no longer in the collection.";
    }
}
