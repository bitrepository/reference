package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;

public class FullUpdateFileIDsStep extends UpdateFileIDsStep {

    public FullUpdateFileIDsStep(IntegrityInformationCollector collector, IntegrityModel store, 
            IntegrityAlerter alerter, Settings settings, String collectionId) {
        super(collector, store, alerter, settings, collectionId);
    }

    @Override
    public String getName() {
        return "Collect all fileIDs from pillars";
    }
    
    @Override
    protected void initialStepAction() {
        store.setExistingFilesToPreviouslySeenFileState(collectionId);
    }


    /**
     * @return Description of this step.
     */
    public static String getDescription() {
        return "Contacts all pillar to retrieve the full list of files from the pillars";
    }
    
}
