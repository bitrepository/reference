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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow for recalculating the checksums of the files of a given collection.
 */
public class RecalculateChecksumJob implements SchedulableJob {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The id of the collection to recalculate the checksum for.*/
    private final String collectionID;
    /** The manager of the checksum and reference archive.*/
    private final StorageModel model;
    
    /** The state of this workflow. */
    private String state;
    private final JobID id;
    
    /**
     * Constructor.
     * @param collectionID The id of the collection to recalculate checksum for.
     * @param manager The manager of the checksums and reference archive.
     */
    public RecalculateChecksumJob(String collectionID, StorageModel model) {
        this.collectionID = collectionID;
        this.model = model;
        id = new JobID(getClass().getSimpleName(), collectionID);
    }
    
    @Override
    public void start() {
        log.info("Recalculating old checksums.");
        state = "Running";
        model.verifyFileToCacheConsistencyOfAllData(collectionID);
        state = null;
    }

    @Override
    public String currentState() {
        if(state == null) {
            return NOT_RUNNING;
        } else {
            return state;
        }
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
}
