package org.bitrepository.pillar.schedulablejobs;

import org.bitrepository.pillar.store.PillarModel;
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
    private final PillarModel model;
    
    /** The state of this workflow. */
    private String state;
    private final JobID id;
    
    /**
     * Constructor.
     * @param collectionID The id of the collection to recalculate checksum for.
     * @param manager The manager of the checksums and reference archive.
     */
    public RecalculateChecksumJob(String collectionID, PillarModel model) {
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
