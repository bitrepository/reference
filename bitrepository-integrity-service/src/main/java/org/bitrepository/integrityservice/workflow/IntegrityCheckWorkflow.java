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
import java.util.Date;

import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.IntegrityServiceManager;
import org.bitrepository.integrityservice.reports.BasicIntegrityReporter;
import org.bitrepository.integrityservice.reports.IntegrityReporter;
import org.bitrepository.integrityservice.statistics.StatisticsCollector;
import org.bitrepository.integrityservice.workflow.step.CreateStatisticsEntryStep;
import org.bitrepository.integrityservice.workflow.step.HandleChecksumValidationStep;
import org.bitrepository.integrityservice.workflow.step.HandleDeletedFilesStep;
import org.bitrepository.integrityservice.workflow.step.HandleMissingChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.HandleMissingFilesStep;
import org.bitrepository.integrityservice.workflow.step.HandleObsoleteChecksumsStep;
import org.bitrepository.integrityservice.workflow.step.UpdateFileInfosStep;
import org.bitrepository.service.workflow.JobID;
import org.bitrepository.service.workflow.Workflow;
import org.bitrepository.service.workflow.WorkflowContext;
import org.bitrepository.service.workflow.WorkflowState;
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
    protected IntegrityContributors integrityContributors;
    protected Date workflowStart;
    /** The default number of retries if none is set in ReferenceSettings */
    private static final int DEFAULT_MAX_RETRIES = 3;
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
    
    protected abstract UpdateFileInfosStep getUpdateFileInfosStep();
    
    protected abstract boolean cleanDeletedFiles();
    
    protected abstract Date getChecksumUpdateCutoffDate();
    
    @Override
    public void start() {
        
        workflowStart = new Date();
        
        if (context == null) {
            throw new IllegalStateException("The workflow can not be started before the initialise method has been " +
                    "called.");
        }
        IntegrityReporter reporter = new BasicIntegrityReporter(jobID.getCollectionID(), jobID.getWorkflowName(),
                IntegrityServiceManager.getIntegrityReportStorageDir());
        
        super.start();
        try {
            StatisticsCollector statisticsCollector = new StatisticsCollector(collectionID);
            Integer maxRetries 
                = context.getSettings().getReferenceSettings().getIntegrityServiceSettings().getComponentRetries();
            integrityContributors = new IntegrityContributors(SettingsUtils.getPillarIDsForCollection(collectionID), 
                    maxRetries == null ? DEFAULT_MAX_RETRIES : maxRetries);

            UpdateFileInfosStep updateFileInfosStep = getUpdateFileInfosStep();
            performStep(updateFileInfosStep);
            
            if(cleanDeletedFiles()) {
                HandleDeletedFilesStep handleDeletedFilesStep = new HandleDeletedFilesStep(context.getStore(), 
                        reporter, workflowStart, integrityContributors.getFinishedContributors());
                performStep(handleDeletedFilesStep);
            }
            
            statisticsCollector.getCollectionStat().setStatsTime(new Date());
            Long missingFileGracePeriod 
                = context.getSettings().getReferenceSettings().getIntegrityServiceSettings().getTimeBeforeMissingFileCheck();
            HandleMissingFilesStep handleMissingFilesStep = new HandleMissingFilesStep(context.getStore(), reporter,
                    statisticsCollector, missingFileGracePeriod);
            performStep(handleMissingFilesStep);
            
            HandleChecksumValidationStep handleChecksumValidationStep 
                    = new HandleChecksumValidationStep(context.getStore(), context.getAuditManager(), reporter, 
                            statisticsCollector);
            performStep(handleChecksumValidationStep);
            
            HandleMissingChecksumsStep handleMissingChecksumsStep = new HandleMissingChecksumsStep(context.getStore(), 
                    reporter, statisticsCollector, getChecksumUpdateCutoffDate());
            performStep(handleMissingChecksumsStep);
            
            HandleObsoleteChecksumsStep handleObsoleteChecksumsStep 
                    = new HandleObsoleteChecksumsStep(context.getSettings(), context.getStore(), reporter, 
                            statisticsCollector);
            performStep(handleObsoleteChecksumsStep);
            
            CreateStatisticsEntryStep createStatistics = new CreateStatisticsEntryStep(
                    context.getStore(), collectionID, statisticsCollector);
            performStep(createStatistics);

            if(currentState() != WorkflowState.ABORTED) {
                if(reporter.hasIntegrityIssues()) {
                    context.getAlerter().integrityFailed(reporter.generateSummaryOfReport(), collectionID);
                }
                try {
                    reporter.generateReport();
                    IntegrityServiceManager.getIntegrityReportProvider().setLatestReport(collectionID, reporter.getReportDir());
                } catch (IOException e) {
                    log.error("Failed to generate integrity report", e);
                    context.getAlerter().integrityComponentFailure("Failed to generate integrity report", collectionID);
                }   
            }
        } finally {
            finish();
        }
    }
}
