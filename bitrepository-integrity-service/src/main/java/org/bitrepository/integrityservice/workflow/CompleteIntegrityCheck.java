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
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.step.FindMissingChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.FindObsoleteChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.IntegrityValidationChecksumStep;
import org.bitrepository.integrityservice.workflow.step.IntegrityValidationFileIDsStep;
import org.bitrepository.integrityservice.workflow.step.RemoveDeletableFileIDsFromDatabaseStep;
import org.bitrepository.integrityservice.workflow.step.SetOldUnknownFilesToMissingStep;
import org.bitrepository.integrityservice.workflow.step.UpdateChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.UpdateFileIDsStep;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.workflow.StepBasedWorkflow;

/**
 * Simple workflow for performing integrity checks of the system. 
 * Starts by updating the file ids in the integrity model, followed by updating the checksums in the integrity model.
 * Then the data is validated for integrity issues.
 * And finally it is verified whether any missing or obsolete checksums can be found.
 */
public class CompleteIntegrityCheck extends StepBasedWorkflow {
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
    /** The audit trail manager.*/
    private final AuditTrailManager auditManager;
    
    /**
     * @param settings The settings.
     * @param collector The collector for collecting the file ids and the checksums.
     * @param store The storage for the integrity data.
     * @param checker The checker for validating the content of the database.
     * @param alerter The integrity alerter for sending alarms, when necessary.
     * @param auditManager The audit trial manager.
     */
    public CompleteIntegrityCheck(Settings settings, IntegrityInformationCollector collector, IntegrityModel store,
                                  IntegrityChecker checker, IntegrityAlerter alerter, AuditTrailManager auditManager) {
        this.settings = settings;
        this.collector = collector;
        this.store = store;
        this.checker = checker;
        this.alerter = alerter;
        this.auditManager = auditManager;
    }
    
    @Override
    public void start() {
        super.start();
        try {
            UpdateFileIDsStep updateFileIDsStep = new UpdateFileIDsStep(collector, store, alerter,
                    settings);
            performStep(updateFileIDsStep);
            
            UpdateChecksumsStep updateChecksumStep = new UpdateChecksumsStep(collector, store, alerter,
                    ChecksumUtils.getDefault(settings), settings);
            performStep(updateChecksumStep);

            SetOldUnknownFilesToMissingStep setUnknownFilesToMissingStep = new SetOldUnknownFilesToMissingStep(store);
            performStep(setUnknownFilesToMissingStep);
            
            IntegrityValidationFileIDsStep validateFileidsStep = new IntegrityValidationFileIDsStep(checker, alerter);
            performStep(validateFileidsStep);
            
            RemoveDeletableFileIDsFromDatabaseStep removeDeletableFileIDsFromDatabaseStep 
                    = new RemoveDeletableFileIDsFromDatabaseStep(store, validateFileidsStep.getReport(), 
                            auditManager, settings);
            performStep(removeDeletableFileIDsFromDatabaseStep);
            
            IntegrityValidationChecksumStep validateChecksumStep = new IntegrityValidationChecksumStep(checker, 
                    alerter);
            performStep(validateChecksumStep);
            
            FindMissingChecksumsStep findMissingChecksums = new FindMissingChecksumsStep(checker, alerter);
            performStep(findMissingChecksums);
            
            FindObsoleteChecksumsStep findObsoleteChecksums = new FindObsoleteChecksumsStep(settings, checker, alerter);
            performStep(findObsoleteChecksums);
        } finally {
            finish();
        }
    }

    @Override
    public String getDescription() {
        return "Retrieves all fileIDs and checksums from all pillars and checks for all potential integrity " +
                "problems."
                //+ LINEFEED + getStepDescriptions()
                ;
    }
}
