/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2013 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.schedulablejobs;

import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.service.workflow.JobID;
import org.bitrepository.service.workflow.SchedulableJob;
import org.bitrepository.service.workflow.WorkflowContext;
import org.bitrepository.service.workflow.WorkflowState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow for recalculating the checksums of the files of a given collection.
 */
public class RecalculateChecksumJob implements SchedulableJob {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String collectionID;
    private final StorageModel model;
    private WorkflowState state = WorkflowState.NOT_RUNNING;
    private final JobID id;

    /**
     * @param collectionID The id of the collection to recalculate checksum for.
     * @param model        The storage model for the pillar.
     */
    public RecalculateChecksumJob(String collectionID, StorageModel model) {
        this.collectionID = collectionID;
        this.model = model;
        id = new JobID(getClass().getSimpleName(), collectionID);
    }

    @Override
    public void start() {
        log.info("Recalculating old checksums for collection '{}'", collectionID);
        try {
            state = WorkflowState.RUNNING;
            model.verifyFileToCacheConsistencyOfAllData(collectionID);
        } finally {
            state = WorkflowState.NOT_RUNNING;
        }
    }

    @Override
    public WorkflowState currentState() {
        return state;
    }

    @Override
    public String getDescription() {
        return "Recalculates the checksums for collection: '" + collectionID + "'.";
    }

    @Override
    public JobID getJobID() {
        return id;
    }

    @Override
    public void initialise(WorkflowContext context, String collectionID) {
        //Not used as reference pillar workflows are defined compile time.
    }

    @Override
    public void setCurrentState(WorkflowState newState) {
        this.state = newState;
    }

    @Override
    public String getHumanReadableState() {
        return state.name();
    }
}
