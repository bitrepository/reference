package org.bitrepository.integrityservice.scheduler.workflow;

import org.bitrepository.access.getchecksums.GetChecksumsClient;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.scheduler.workflow.step.CollectChecksumsStep;
import org.bitrepository.integrityservice.scheduler.workflow.step.FindMissingChecksumsStep;

/**
 * Simple workflow for finding and collecting missing checksums.
 * TODO perform validationg afterwards!
 */
public class MissingChecksumsWorkflow extends StepBasedWorkflow {
    /** The settings.*/
    private final Settings settings;
    /** The client for retrieving the checksums.*/
    private final GetChecksumsClient client;
    /** The storage of integrity data.*/
    private final IntegrityModel store;
    
    /**
     * Constructor.
     * @param settings The settings.
     * @param client The client for collecting the checksums.
     * @param store The storage for the integrity data.
     */
    public MissingChecksumsWorkflow(Settings settings, GetChecksumsClient client, IntegrityModel store) {
        this.settings = settings;
        this.client = client;
        this.store = store;
    }
    
    @Override
    public void start() {
        FindMissingChecksumsStep findMissingChecksums = new FindMissingChecksumsStep(store);
        performStep(findMissingChecksums);
        
        for(String fileid : findMissingChecksums.getResults()) {
            CollectChecksumsStep collectStep = new CollectChecksumsStep(settings, client, store, fileid);
            performStep(collectStep);
        }
        
        // TODO validate!
    }
}
