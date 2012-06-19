/*
 * #%L
 * Bitrepository Integrity Client
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.integrityservice;

import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.collector.eventhandler.ChecksumsUpdaterAndValidatorEventHandler;
import org.bitrepository.integrityservice.scheduler.IntegrityScheduler;
import org.bitrepository.integrityservice.scheduler.WorkflowTask;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.service.LifeCycledService;
import org.bitrepository.service.audit.AuditTrailManager;
import org.bitrepository.service.contributor.ContributorMediator;
import org.bitrepository.service.contributor.SimpleContributorMediator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Simple integrity service.
 */
public class SimpleIntegrityService implements IntegrityService, LifeCycledService {
    /** The log.*/
    private final Logger log = LoggerFactory.getLogger(getClass());

    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_OBSOLETE_CHECKSUM_WORKFLOW = "The Obsolete Checksum Collector Workflow";
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_ALL_FILEIDS_WORKFLOW = "The FileIDs Collector Workflow";
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_ALL_CHECKSUMS_WORKFLOW = "The Checksums Collector Workflow";
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_INTEGRITY_VALIDATOR_WORKFLOW = "The Integrity Validator Workflow.";

    /** The scheduler. */
    private final IntegrityScheduler scheduler;
    /** The information collector. */
    private final IntegrityInformationCollector collector;
    /** The cache.*/
    private final IntegrityModel cache;
    /** The integrity checker.*/
    private final IntegrityChecker checker;
    /** The settings. */
    private final Settings settings;
    /** The dispatcher of alarms.*/
    private final IntegrityAlerter alerter;
    /** Provides GetStatus and GetAuditTrails functionality. */
    private final ContributorMediator contributor;
    /** The messagebus for communication.*/
    private final MessageBus messageBus;
    
    /**
     * Constructor.
     * @param settings The settings for the service.
     */
    public SimpleIntegrityService(IntegrityModel model, IntegrityScheduler scheduler, IntegrityChecker checker, 
            IntegrityAlerter alerter, IntegrityInformationCollector collector, AuditTrailManager auditManager, 
            Settings settings, MessageBus messageBus) {
        this.settings = settings;
        this.messageBus = messageBus;
        this.cache = model;
        this.scheduler = scheduler;
        this.checker = checker;
        this.alerter = alerter;
        this.collector = collector;
        
        this.contributor = new SimpleContributorMediator(messageBus, settings, auditManager);
        contributor.start();
    }
    
    @Override
    public void startChecksumIntegrityCheck(long millisSinceLastUpdate, long intervalBetweenChecks) {
//        EventHandler eventHandler = new ChecksumsUpdaterAndValidatorEventHandler(cache, checker, alerter, 
//                getFileIDsWithAllFileIDs());
//        
//        // Default checksum used.
//        ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
//        checksumType.setChecksumType(ChecksumType.fromValue(
//                settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType()));
//        
//        Workflow workflow = new CollectObsoleteChecksumsWorkflow(intervalBetweenChecks, 
//                DEFAULT_NAME_OF_OBSOLETE_CHECKSUM_WORKFLOW, millisSinceLastUpdate, checksumType, collector, cache, 
//                eventHandler);
//        
//        scheduler.putWorkflow(workflow);
    }
    
    @Override
    public void startAllFileIDsIntegrityCheck(long intervalBetweenCollecting) {
//        EventHandler eventHandler = new FileIDsUpdaterAndValidatorEventHandler(cache, checker, alerter, 
//                getFileIDsWithAllFileIDs());
//        Workflow workflow = new CollectAllFileIDsWorkflow(intervalBetweenCollecting, 
//                DEFAULT_NAME_OF_ALL_FILEIDS_WORKFLOW, settings, collector, eventHandler);
//        scheduler.putWorkflow(workflow);
    }

    @Override
    public void startAllChecksumsIntegrityCheck(long intervalBetweenCollecting) {
//        EventHandler eventHandler = new ChecksumsUpdaterAndValidatorEventHandler(cache, checker, alerter, 
//                getFileIDsWithAllFileIDs());
//        ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
//        checksumType.setChecksumType(ChecksumType.fromValue(
//                settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType()));
//        
//        Workflow workflow = new CollectAllChecksumsWorkflow(intervalBetweenCollecting, 
//                DEFAULT_NAME_OF_ALL_CHECKSUMS_WORKFLOW, checksumType, settings, collector, eventHandler);
//        
//        scheduler.putWorkflow(workflow);
    }
    
    /**
     * Initiates the scheduling of a checking of all the checksums and file id for all the data in the cache.
     * @param intervalBetweenCollecting The time between the workflow is run.
     */
    public void startIntegrityValidator(long intervalBetweenCollecting) {
//        Workflow workflow = new IntegrityValidatorWorkflow(intervalBetweenCollecting, 
//                DEFAULT_NAME_OF_INTEGRITY_VALIDATOR_WORKFLOW, checker);
//        
//        scheduler.putWorkflow(workflow);
    }
    
    /**
     * Collects and integrity checks the checksum for a given file on all pillars. 
     * Algorithm and salt are optional and can be used for requiring a recalculation of the checksums.
     * @param fileID The id of the file to collect its checksum for.
     * @param checksumAlgorithm The algorithm to use for the checksum collecting. 
     * If null, then the default from settings is used.
     * @param salt The salt for the checksum calculation. If null or empty string, then no salt is used. 
     * @param auditTrailInformation The information for the audit.
     */
    public void checkChecksums(String fileID, String checksumAlgorithm, String salt, String auditTrailInformation) {
        FileIDs fileIDs = new FileIDs();
        fileIDs.setFileID(fileID);
        
        ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
        if(checksumAlgorithm == null || checksumAlgorithm.isEmpty()) {
            checksumType.setChecksumType(ChecksumType.fromValue(
                    settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType()));
        } else {
            checksumType.setChecksumType(ChecksumType.fromValue(checksumAlgorithm));
        }
        checksumType.setChecksumSalt(salt.getBytes());
        
        EventHandler eventHandler = new ChecksumsUpdaterAndValidatorEventHandler(cache, checker, alerter, 
                getFileIDsWithAllFileIDs());
        collector.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), 
                fileIDs, checksumType, auditTrailInformation, eventHandler);
    }
    
    /**
     * TODO perhaps move to utility???
     * @return A FileIDs where all FileIDs are set.
     */
    private FileIDs getFileIDsWithAllFileIDs() {
        FileIDs allFileIDs = new FileIDs();
        allFileIDs.setAllFileIDs("TRUE");
        return allFileIDs;
    }
    
    @Override
    public Collection<WorkflowTask> getWorkflows() {
        return scheduler.getWorkflows();
    }
    
    @Override
    public long getNumberOfFiles(String pillarId) {
        return cache.getNumberOfFiles(pillarId);
    }
    
    @Override
    public long getNumberOfMissingFiles(String pillarId) {
        return cache.getNumberOfMissingFiles(pillarId);
    }
    
    @Override
    public long getNumberOfChecksumErrors(String pillarId) {
        return cache.getNumberOfChecksumErrors(pillarId);
    }

    @Override
    public void start() {
        //Nothing to do.
    }

    @Override
    public void shutdown() {
        if(messageBus != null) {
            try {
                messageBus.close();
            } catch (Exception e) {
                log.warn("Encountered issues when closing down the messagebus.", e);
            }
        }
        if(contributor != null) {
            contributor.close();
        }
    }
}
