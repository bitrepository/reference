package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;

public class IncrementalUpdateFileIDsStep extends UpdateFileIDsStep {

    public IncrementalUpdateFileIDsStep(IntegrityInformationCollector collector, IntegrityModel store, 
            IntegrityAlerter alerter, Settings settings, String collectionId, IntegrityContributors integrityContributors) {
        super(collector, store, alerter, settings, collectionId, integrityContributors);
    }

    @Override
    public String getName() {
        return "Collect new fileIDs from pillars";
    }

    /**
     * @return Description of this step.
     */
    public static String getDescription() {
        return "Contacts all pillar to retrieve the list of new files from the pillars";
    }
    
}
