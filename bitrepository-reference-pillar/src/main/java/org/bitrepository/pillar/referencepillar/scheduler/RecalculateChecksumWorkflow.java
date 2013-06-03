package org.bitrepository.pillar.referencepillar.scheduler;

import org.bitrepository.pillar.referencepillar.archive.ReferenceChecksumManager;
import org.bitrepository.service.workflow.Workflow;
import org.bitrepository.service.workflow.WorkflowID;
import org.bitrepository.service.workflow.WorkflowStatistic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Workflow for recalculating the checksums of the files of a given collection.
 */
public class RecalculateChecksumWorkflow implements Workflow {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The id of the collection to recalculate the checksum for.*/
    private final String collectionId;
    /** The manager of the checksum and reference archive.*/
    private final ReferenceChecksumManager manager;
    
    /** The state of this workflow. */
    private String workflowState;
    
    /**
     * Constructor.
     * @param collectionId The id of the collection to recalculate checksum for.
     * @param manager The manager of the checksums and reference archive.
     */
    public RecalculateChecksumWorkflow(String collectionId, ReferenceChecksumManager manager) {
        this.collectionId = collectionId;
        this.manager = manager;
        workflowState = "Has not yet run.";
    }
    
    @Override
    public void start() {
        log.info("Recalculating old checksums.");
        workflowState = "Running";
        manager.ensureStateOfAllData(collectionId);
        workflowState = "Currently not running";
    }

    @Override
    public String currentState() {
        return workflowState;
    }

    @Override
    public String getDescription() {
        return "Recalculates the checksums for collection: '" + collectionId + "'.";
    }

    @Override
    public WorkflowStatistic getWorkflowStatistics() {
        return null;
    }

    @Override
    public WorkflowID getWorkflowID() {
        return null;
    }
}
