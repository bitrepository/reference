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
package org.bitrepository.integrityclient.collector.eventhandler;

import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.integrityclient.cache.CachedIntegrityInformationStorage;
import org.bitrepository.integrityclient.checking.IntegrityChecker;
import org.bitrepository.protocol.eventhandler.EventHandler;
import org.bitrepository.protocol.eventhandler.OperationEvent;
import org.bitrepository.protocol.eventhandler.OperationEvent.OperationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handles the event from the GetFileIds operations and sends the results into the cache.
 */
public class GetFileIdsEventHandler implements EventHandler {
    
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /** The storage to send the results.*/
    private CachedIntegrityInformationStorage informationCache;
    /** The FileIDs to be */
    private final FileIDs fileIDs;
    /** The checker used for checking the integrity of the results.*/
    private final IntegrityChecker integrityChecker;
    
    /**
     * Constructor.
     * @param informationCache The cache for storing the integrity results.
     * @param integrityChecker The integrity checker for validating the results.
     * @param fileIDs The given data to perform the integrity checks upon.
     */
    public GetFileIdsEventHandler(CachedIntegrityInformationStorage informationCache, 
            IntegrityChecker integrityChecker, FileIDs fileIDs) {
        this.informationCache = informationCache;
        this.integrityChecker = integrityChecker;
        this.fileIDs = fileIDs;
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        if(event.getType().equals(OperationEventType.Failed)) {
            handleFailure(event);
            return;
        }
        
        if(event.getType().equals(OperationEventType.Complete)) {
            handleComplete(event);
            return;
        }
        
        if(event instanceof FileIDsCompletePillarEvent) {
            handleFileIDsComplete((FileIDsCompletePillarEvent) event);
        } else {
            // TODO handle differently if special case (e.g. PillarFailure).
            log.debug(event.toString());
        }
    }
    
    /**
     * Handles the results of a operation.
     * @param event The event for the completed pillar.
     */
    private void handleFileIDsComplete(FileIDsCompletePillarEvent event) {
        informationCache.addFileIDs(event.getFileIDs().getFileIDsData(), event.getState()); 
    }
    
    /**
     * Method for handling a failure.
     * @param event The event that failed.
     */
    private void handleFailure(OperationEvent event) {
        log.warn(event.getType() + " : " + event.getState() + " : " + event.getInfo());
        // TODO ??
    }
    
    /**
     * Method for handling a complete. Thus notifying the relevant instance.
     * @param event The event that has completed.
     */
    private void handleComplete(OperationEvent event) {
        log.info(event.getType() + " : " + event.getState() + " : " + event.getInfo());
        
        integrityChecker.checkFileIDs(fileIDs);
    }
}
