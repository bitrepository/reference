package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;

/**
 * Workflow step class to handle the incremental collection of checksums.  
 */
public class IncrementalUpdateChecksumsStep extends UpdateChecksumsStep {

    public IncrementalUpdateChecksumsStep(IntegrityInformationCollector collector, IntegrityModel store, IntegrityAlerter alerter,
            ChecksumSpecTYPE checksumType, Settings settings, String collectionID, IntegrityContributors integrityContributors) {
        super(collector, store, alerter, checksumType, settings, collectionID, integrityContributors);
    }

    @Override
    public String getName() {
        return "Collect new checksums from pillars";
    }

    /**
     * @return Description of this step.
     */
    public static String getDescription() {
        return "Contacts all pillars to retrieve the list of new checksums for the pillar";
    }
    
}
