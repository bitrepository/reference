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
package org.bitrepository.integrityservice.collector.eventhandler;

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityservice.IntegrityAlarmDispatcher;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.checking.IntegrityChecker;
import org.bitrepository.integrityservice.checking.IntegrityReport;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the event from the GetChecksums operations and sends the results into the cache.
 */
public class ChecksumsUpdaterAndValidatorEventHandler implements EventHandler {
    
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The storage to send the results.*/
    private IntegrityModel informationCache;
    /** The FileIDs to be */
    private final FileIDs fileIDs;
    /** The checker used for checking the integrity of the results.*/
    private final IntegrityChecker integrityChecker;
    /** The entity used for sending alarms.*/
    private final IntegrityAlarmDispatcher alarmDispatcher;
    
    /**
     * Constructor.
     * @param informationCache The cache for storing the integrity results.
     * @param integrityChecker The integrity checker for validating the results.
     * @param alarmDispatcher The dispatcher of alarms. 
     * @param fileIDs The given data to perform the integrity checks upon.
     */
    public ChecksumsUpdaterAndValidatorEventHandler(IntegrityModel informationCache, 
            IntegrityChecker integrityChecker, IntegrityAlarmDispatcher alarmDispatcher, FileIDs fileIDs) {
        this.informationCache = informationCache;
        this.integrityChecker = integrityChecker;
        this.fileIDs = fileIDs;
        this.alarmDispatcher = alarmDispatcher;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public void handleEvent(OperationEvent event) {
        if(event.getType().equals(OperationEventType.FAILED)) {
            handleFailure(event);
            return;
        }
        
        if(event.getType().equals(OperationEventType.COMPLETE)) {
            handleComplete(event);
            return;
        }
        
        if(event instanceof ChecksumsCompletePillarEvent) {
            handleChecksumsComplete((ChecksumsCompletePillarEvent) event);
        } else {
            // TODO handle differently if special case (e.g. PillarFailure).
            log.debug(event.toString());
        }
    }
    
    /**
     * Handles the results of the GetChecksums operation of a single pillar.
     * @param event The event with the results of the completed pillar.
     */
    private void handleChecksumsComplete(ChecksumsCompletePillarEvent event) {
        informationCache.addChecksums(event.getChecksums().getChecksumDataItems(), event.getChecksumType(), 
                event.getState());
    }
    
    /**
     * Method for handling a failure.
     * @param event The event that failed.
     */
    @SuppressWarnings("rawtypes")
    private void handleFailure(OperationEvent event) {
        log.warn(event.getType() + " : " + event.getState() + " : " + event.getInfo());
        performIntegrityCheck();
    }
    
    /**
     * Handles a Complete for the whole operation by performing a integrity check on the given checksums.
     * @param event The event that has completed.
     */
    @SuppressWarnings("rawtypes")
    private void handleComplete(OperationEvent event) {
        log.info(event.getType() + " : " + event.getState() + " : " + event.getInfo());
        performIntegrityCheck();
    }
    
    /**
     * Performs the integrity check, and sends an Alarm if any integrity problems. 
     */
    private void performIntegrityCheck() {
        IntegrityReport report = integrityChecker.checkChecksum(fileIDs);
        if(report.hasIntegrityIssues()) {
            log.warn(report.generateReport());
            alarmDispatcher.integrityFailed(report);
        } else {
            log.debug("No integrity issues found for files '" + fileIDs + "'");
        }
    }
}
