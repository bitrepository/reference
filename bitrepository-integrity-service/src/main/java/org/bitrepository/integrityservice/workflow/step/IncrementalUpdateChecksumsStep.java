package org.bitrepository.integrityservice.workflow.step;

import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;

/**
 * Workflow step class to handle the incremental collection of checksums.  
 */
public class IncrementalUpdateChecksumsStep extends UpdateChecksumsStep {

    public IncrementalUpdateChecksumsStep(IntegrityInformationCollector collector, IntegrityModel store, IntegrityAlerter alerter,
            ChecksumSpecTYPE checksumType, Settings settings, String collectionId) {
        super(collector, store, alerter, checksumType, settings, collectionId);
    }

    @Override
    public String getName() {
        return "Collect new checksums from pillars";
    }
    
    @Override
    protected Date getLatestChecksumEntry(String pillar) {
    	return store.getDateForNewestChecksumEntryForPillar(pillar, collectionId);
    }


    /**
     * @return Description of this step.
     */
    public static String getDescription() {
        return "Contacts all pillars to retrieve the list of new checksums for the pillar";
    }
    
}
