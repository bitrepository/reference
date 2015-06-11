package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;

/**
 * Workflow step class to handle the full collection of checksums 
 */
public class FullUpdateChecksumsStep extends UpdateChecksumsStep {

    public FullUpdateChecksumsStep(IntegrityInformationCollector collector, IntegrityModel store, IntegrityAlerter alerter,
            ChecksumSpecTYPE checksumType, Settings settings, String collectionId) {
        super(collector, store, alerter, checksumType, settings, collectionId);
    }

    @Override
    public String getName() {
        return "Collect all checksums from pillars";
    }

    // Set existing checksums to previously seen.
    @Override
    protected void initialStepAction() {
        //store.resetChecksumCollectionProgress(collectionId);
        store.setExistingChecksumsToPreviouslySeen(collectionId);
    }

    // Set previously seen checksums to missing.
    @Override
    protected void finalStepAction() {
        store.setPreviouslySeenChecksumsToMissing(collectionId);
    }

    /**
     * @return Description of this step.
     */
    public static String getDescription() {
        return "Contacts all pillars to retrieve the list of checksums for the pillar";
    }

}
