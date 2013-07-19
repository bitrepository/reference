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

import java.io.IOException;

import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.integrityservice.IntegrityServiceManager;
import org.bitrepository.integrityservice.reports.BasicIntegrityReporter;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.integrityservice.workflow.step.CreateStatisticsEntryStep;
import org.bitrepository.integrityservice.workflow.step.HandleChecksumValidationStep;
import org.bitrepository.integrityservice.workflow.step.HandleDeletedFilesStep;
import org.bitrepository.integrityservice.workflow.step.HandleMissingChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.HandleMissingFilesStep;
import org.bitrepository.integrityservice.workflow.step.HandleObsoleteChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.UpdateChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.UpdateFileIDsStep;
import org.bitrepository.service.workflow.JobID;
import org.bitrepository.service.workflow.Workflow;
import org.bitrepository.service.workflow.WorkflowContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple workflow for performing integrity checks of the system. 
 * Starts by updating the file ids in the integrity model, followed by updating the checksums in the integrity model.
 * Then the data is validated for integrity issues.
 * And finally it is verified whether any missing or obsolete checksums can be found.
 */
public abstract class IntegrityCheckWorkflow extends Workflow {
    private final Logger log = LoggerFactory.getLogger(getClass());
    /** The context for the workflow.*/
    protected IntegrityWorkflowContext context;
    protected String collectionID;
    protected IntegrityReporter latestReport = null;
    /**
     * Remember to call the initialise method needs to be called before the start method.
     */
    public IntegrityCheckWorkflow() {}

    @Override
    public void initialise(WorkflowContext context, String collectionID) {
        this.context = (IntegrityWorkflowContext)context;
        this.collectionID = collectionID;
        jobID = new JobID(getClass().getSimpleName(), collectionID);
    }
    
    protected abstract UpdateFileIDsStep getUpdateFileIDsStep();
    
    public IntegrityReporter getLatestIntegrityReport() {
        return latestReport;
    }
    
    @Override
    public void start() {

        if (context == null) {
            throw new IllegalStateException("The workflow can not be started before the initialise method has been " +
                    "called.");
        }
        IntegrityReporter reporter = new BasicIntegrityReporter(jobID.getCollectionID(), jobID.getWorkflowName(),
                IntegrityServiceManager.getIntegrityReportStorageDir());
        
        super.start();
        try {
            UpdateFileIDsStep updateFileIDsStep = getUpdateFileIDsStep();
            performStep(updateFileIDsStep);
            
            UpdateChecksumsStep updateChecksumStep = new UpdateChecksumsStep(
                    context.getCollector(), context.getStore(), context.getAlerter(),
                    ChecksumUtils.getDefault(context.getSettings()), context.getSettings(), collectionID);
            performStep(updateChecksumStep);

            HandleDeletedFilesStep handleDeletedFilesStep = new HandleDeletedFilesStep(context.getStore(), reporter);
            performStep(handleDeletedFilesStep);
            
            HandleMissingFilesStep handleMissingFilesStep = new HandleMissingFilesStep(context.getStore(),reporter);
            performStep(handleMissingFilesStep);
            
            HandleChecksumValidationStep handleChecksumValidationStep 
                    = new HandleChecksumValidationStep(context.getStore(), context.getAuditManager(), reporter);
            performStep(handleChecksumValidationStep);
            
            HandleMissingChecksumsStep handleMissingChecksumsStep 
                    = new HandleMissingChecksumsStep(context.getStore(), reporter);
            performStep(handleMissingChecksumsStep);
            
            HandleObsoleteChecksumsStep handleObsoleteChecksumsStep 
                    = new HandleObsoleteChecksumsStep(context.getSettings(), context.getStore(), reporter);
            performStep(handleObsoleteChecksumsStep);
            
            CreateStatisticsEntryStep createStatistics = new CreateStatisticsEntryStep(
                    context.getStore(), collectionID);
            performStep(createStatistics);

            if(reporter.hasIntegrityIssues()) {
                context.getAlerter().integrityFailed(reporter.generateSummaryOfReport(), collectionID);
            }
            try {
                reporter.generateReport();
            } catch (IOException e) {
                log.error("Failed to generate integrity report", e);
            }
            
            latestReport = reporter;
            
        } finally {
            finish();
        }
    }
}
