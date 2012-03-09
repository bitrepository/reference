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
package org.bitrepository.integrityclient;

import java.util.Collection;

import org.bitrepository.access.AccessComponentFactory;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityclient.cache.IntegrityModel;
import org.bitrepository.integrityclient.checking.IntegrityChecker;
import org.bitrepository.integrityclient.collector.IntegrityInformationCollector;
import org.bitrepository.integrityclient.collector.eventhandler.ChecksumsUpdaterAndValidatorEventHandler;
import org.bitrepository.integrityclient.collector.eventhandler.FileIDsUpdaterAndValidatorEventHandler;
import org.bitrepository.integrityclient.workflow.IntegrityWorkflowScheduler;
import org.bitrepository.integrityclient.workflow.Workflow;
import org.bitrepository.integrityclient.workflow.scheduler.CollectAllChecksumsWorkflow;
import org.bitrepository.integrityclient.workflow.scheduler.CollectAllFileIDsWorkflow;
import org.bitrepository.integrityclient.workflow.scheduler.CollectObsoleteChecksumsWorkflow;
import org.bitrepository.integrityclient.workflow.scheduler.IntegrityValidatorWorkflow;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.security.SecurityManager;

/**
 * Simple integrity service.
 */
public class SimpleIntegrityService {
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_OBSOLETE_CHECKSUM_TRIGGER = "The Obsolete Checksum Collector Workflow";
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_ALL_FILEIDS_TRIGGER = "The FileIDs Collector Workflow";
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_ALL_CHECKSUMS_TRIGGER = "The Checksums Collector Workflow";
    /** The default name of the trigger.*/
    private static final String DEFAULT_NAME_OF_INTEGRITY_VALIDATOR_WORKFLOW = "The Integrity Validator Workflow.";

    /** The scheduler. */
    private final IntegrityWorkflowScheduler scheduler;
    /** The information collector. */
    private final IntegrityInformationCollector collector;
    /** The cache.*/
    private final IntegrityModel cache;
    /** The integrity checker.*/
    private final IntegrityChecker checker;
    /** The settings. */
    private final Settings settings;
    /** The dispatcher of alarms.*/
    private final IntegrityAlarmDispatcher alarmDispatcher;
    /** The messagebus for communication.*/
    private final MessageBus messageBus;
    
    /**
     * Constructor.
     * @param settings The settings for the service.
     */
    public SimpleIntegrityService(Settings settings, SecurityManager securityManager) {
        this.settings = settings;
        this.messageBus = ProtocolComponentFactory.getInstance().getMessageBus(settings, securityManager); 
        this.cache = IntegrityServiceComponentFactory.getInstance().getCachedIntegrityInformationStorage(settings);
        this.scheduler = IntegrityServiceComponentFactory.getInstance().getIntegrityInformationScheduler(settings);
        this.checker = IntegrityServiceComponentFactory.getInstance().getIntegrityChecker(settings, cache);
        this.alarmDispatcher = new IntegrityAlarmDispatcher(settings, messageBus);
        this.collector = IntegrityServiceComponentFactory.getInstance().getIntegrityInformationCollector(
                cache, checker, 
                AccessComponentFactory.getInstance().createGetFileIDsClient(settings, securityManager),
                AccessComponentFactory.getInstance().createGetChecksumsClient(settings, securityManager),
                settings, messageBus);
        
    }
    
    /**
     * Initiates the scheduling of checksum collecting and integrity checking.
     * @param millisSinceLastUpdate The time since last update for a checksum to be calculated.
     * @param intervalBetweenChecks The time between checking for outdated checksums.
     */
    public void startChecksumIntegrityCheck(long millisSinceLastUpdate, long intervalBetweenChecks) {
        EventHandler eventHandler = new ChecksumsUpdaterAndValidatorEventHandler(cache, checker, alarmDispatcher, 
                getFileIDsWithAllFileIDs());
        
        // Default checksum used.
        ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
        checksumType.setChecksumType(ChecksumType.fromValue(
                settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType()));
        
        Workflow trigger = new CollectObsoleteChecksumsWorkflow(intervalBetweenChecks, millisSinceLastUpdate, 
                checksumType, collector, cache, eventHandler);
        
        scheduler.putTrigger(DEFAULT_NAME_OF_OBSOLETE_CHECKSUM_TRIGGER, trigger);
    }
    
    /**
     * Initiates the scheduling of collecting and checking of all the file ids from all the pillars.
     * @param intervalBetweenCollecting The time between collecting all the file ids.
     */
    public void startAllFileIDsIntegrityCheck(long intervalBetweenCollecting) {
        EventHandler eventHandler = new FileIDsUpdaterAndValidatorEventHandler(cache, checker, alarmDispatcher, 
                getFileIDsWithAllFileIDs());
        Workflow trigger = new CollectAllFileIDsWorkflow(intervalBetweenCollecting, settings, collector, eventHandler);
        
        scheduler.putTrigger(DEFAULT_NAME_OF_ALL_FILEIDS_TRIGGER, trigger);
    }

    /**
     * Initiates the scheduling of collecting and checking of all the checksums from all the pillars.
     * @param intervalBetweenCollecting The time between collecting all the file ids.
     */
    public void startAllChecksumsIntegrityCheck(long intervalBetweenCollecting) {
        EventHandler eventHandler = new ChecksumsUpdaterAndValidatorEventHandler(cache, checker, alarmDispatcher, 
                getFileIDsWithAllFileIDs());
        ChecksumSpecTYPE checksumType = new ChecksumSpecTYPE();
        checksumType.setChecksumType(ChecksumType.fromValue(
                settings.getCollectionSettings().getProtocolSettings().getDefaultChecksumType()));
        
        Workflow trigger = new CollectAllChecksumsWorkflow(intervalBetweenCollecting, checksumType, 
                settings, collector, eventHandler);
        
        scheduler.putTrigger(DEFAULT_NAME_OF_ALL_CHECKSUMS_TRIGGER, trigger);
    }
    
    /**
     * Initiates the scheduling of a checking of all the checksums and file id for all the data in the cache.
     * @param intervalBetweenCollecting The time between the workflow is run.
     */
    public void startIntegrityValidator(long intervalBetweenCollecting) {
        Workflow trigger = new IntegrityValidatorWorkflow(intervalBetweenCollecting, checker);
        
        scheduler.putTrigger(DEFAULT_NAME_OF_INTEGRITY_VALIDATOR_WORKFLOW, trigger);

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
        
        EventHandler eventHandler = new ChecksumsUpdaterAndValidatorEventHandler(cache, checker, alarmDispatcher, 
                getFileIDsWithAllFileIDs());
        collector.getChecksums(settings.getCollectionSettings().getClientSettings().getPillarIDs(), 
                fileIDs, checksumType, auditTrailInformation, eventHandler);
    }
    
    /**
     * Retrieves all the scheduled tasks in the system, which are running.
     * @return The names of the tasks, which are scheduled by the system.
     */
    public Collection<Workflow> getScheduledTasks() {
        return scheduler.getWorkflows();
    }
    
    /**
     * @param pillarId The pillar which has the files.
     * @return The number of files on the given pillar.
     */
    public long getNumberOfFiles(String pillarId) {
        return cache.getNumberOfFiles(pillarId);
    }
    
    /**
     * @param pillarId The pillar which might be missing some files.
     * @return The number of files missing for the given pillar.
     */
    public long getNumberOfMissingFiles(String pillarId) {
        return cache.getNumberOfFiles(pillarId);
    }
    
    /**
     * @param pillarId The pillar which might contain files with checksum error.
     * @return The number of files with checksum error at the given pillar.
     */
    public long getNumberOfChecksumErrors(String pillarId) {
        return cache.getNumberOfChecksumErrors(pillarId);
    }
    
    public void close() {
        // TODO!
    }
    
    /**
     * @return A FileIDs where all FileIDs are set.
     */
    private FileIDs getFileIDsWithAllFileIDs() {
        FileIDs allFileIDs = new FileIDs();
        allFileIDs.setAllFileIDs("TRUE");
        return allFileIDs;
    }
}
