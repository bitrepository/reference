package org.bitrepository.integrityservice.workflow;

import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.step.FindMissingChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.FindObsoleteChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.IntegrityValidationStep;
import org.bitrepository.integrityservice.workflow.step.UpdateChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.UpdateFileIDsStep;

/**
 * Simple workflow for performing integrity checks of the system. 
 * Starts by updating the file ids in the integrity model, followed by updating the checksums in the integrity model.
 * Then the data is validated for integrity issues.
 * And finally it is verified whether any missing or obsolete checksums can be found.
 */
public class BasicIntegrityWorkflow extends StepBasedWorkflow {
    /** The settings.*/
    private final Settings settings;
    /** The client for retrieving the checksums.*/
    private final IntegrityInformationCollector collector;
    /** The storage of integrity data.*/
    private final IntegrityModel store;
    /** The checker for finding integrity issues.*/
    private final IntegrityChecker checker;
    /** The alerter for dispatching alarms in the case of integrity issues.*/
    private final IntegrityAlerter alerter;
    
    /**
     * Constructor.
     * @param settings The settings.
     * @param client The client for collecting the checksums.
     * @param store The storage for the integrity data.
     */
    public BasicIntegrityWorkflow(Settings settings, IntegrityInformationCollector collector, IntegrityModel store,
            IntegrityChecker checker, IntegrityAlerter alerter) {
        this.settings = settings;
        this.collector = collector;
        this.store = store;
        this.checker = checker;
        this.alerter = alerter;
    }
    
    @Override
    public void start() {
        UpdateFileIDsStep fileIDsStep = new UpdateFileIDsStep(settings, collector, store);
        performStep(fileIDsStep);
        
        UpdateChecksumsStep checksumStep = new UpdateChecksumsStep(settings, collector, store);
        performStep(checksumStep);
        
        IntegrityValidationStep integrityStep = new IntegrityValidationStep(checker, alerter);
        performStep(integrityStep);
        
        FindMissingChecksumsStep findMissingChecksums = new FindMissingChecksumsStep(store, alerter);
        performStep(findMissingChecksums);
        
        FindObsoleteChecksumsStep findObsoleteChecksums = new FindObsoleteChecksumsStep(store, alerter,
                settings.getReferenceSettings().getIntegrityServiceSettings().getTimeBeforeMissingFileCheck());
        performStep(findObsoleteChecksums);
    }
}
