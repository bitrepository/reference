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
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;

/**
 * A workflow step for persisting the statistics entry gathered over the course of the workflow.
 */
public class CreateStatisticsEntryStep extends AbstractWorkFlowStep {
    private final IntegrityModel store;
    private final String collectionID;
    private final StatisticsCollector sc;

    public CreateStatisticsEntryStep(IntegrityModel store, String collectionID, StatisticsCollector statisticsCollector) {
        this.store = store;
        this.collectionID = collectionID;
        this.sc = statisticsCollector;
    }

    @Override
    public String getName() {
        return "Create statistics";
    }

    /**
     * Creates a statistics entry in the store from the data gathered by the previous workflow steps.
     * <p>
     * File counts and data sizes are gathered earlier, by {@link HandleMissingFilesStep}, alongside the
     * missing-files count rather than here, so that the two remain consistent with each other.
     */
    @Override
    public synchronized void performStep() {
        store.createStatistics(collectionID, sc);
    }

    public static String getDescription() {
        return "Creates a new statistics entry in the database.";
    }
}
