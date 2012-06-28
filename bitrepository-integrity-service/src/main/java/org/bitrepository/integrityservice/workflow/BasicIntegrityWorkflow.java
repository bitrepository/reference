/*
 * #%L
 * Bitrepository Integrity Service
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
