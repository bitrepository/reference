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
package org.bitrepository.integrityservice.workflow.step;

import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The step for collecting of all file ids from all pillars.
 */
public class UpdateFileIDsStep implements WorkflowStep {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The constant for all file ids.*/
    private static final String ALL_FILE_IDS = "true";
    
    /** The settings.*/
    private final Settings settings;
    /** The collector for retrieving the file ids.*/
    private final IntegrityInformationCollector collector;
    /** The model where the integrity data is stored.*/
    private final IntegrityModel store;
    
    /**
     * Constructor.
     * @param settings The settings.
     * @param client The client for collecting the fileids.
     * @param store The storage for the integrity data.
     * @param fileid The id of the file to collect.
     */
    public UpdateFileIDsStep(Settings settings, IntegrityInformationCollector collector, IntegrityModel store) {
        this.settings = settings;
        this.collector = collector;
        this.store = store;
    }
    
    @Override
    public String getName() {
        return "Collecting all file ids.";
    }

    @Override
    public synchronized void performStep() {
        log.debug("Begin collecting the file ids.");
        
        FileIDsEventHandler eventHandler = new FileIDsEventHandler();
        
        collector.getFileIDs(settings.getCollectionSettings().getClientSettings().getPillarIDs(), getFileIds(), 
                "IntegrityService: " + getName(), eventHandler);
        while(eventHandler.isRunning()) {
            try {
                this.wait();
            } catch (InterruptedException e) {
                log.debug("Interrupted while waiting for step to finish.", e);
            }
        }
        
        log.debug("Finished collecting the file ids.");
    }
    
    /**
     * @return The FileIDs object for the specific file.
     */
    private FileIDs getFileIds() {
        FileIDs fileids = new FileIDs();
        fileids.setAllFileIDs(ALL_FILE_IDS);
        return fileids;
    }
    
    /**
     * Handles the results of the GetFileIDs conversation.
     * Sends a notify when everything is complete or failed.
     */
    private class FileIDsEventHandler implements EventHandler {
        /** Tells whether the event has finished.*/
        private boolean isFinished = false;
        
        @Override
        public void handleEvent(OperationEvent event) {
            if(event.getType() == OperationEventType.COMPONENT_COMPLETE) {
                log.debug("Handle the GetFileIDs results from a single pillar.");
                handleResult((FileIDsCompletePillarEvent) event);
            } else if(event.getType() == OperationEventType.COMPLETE) {
                log.debug("Complete: " + event.toString());
                isFinished = true;
                notify();
            } else if(event.getType() == OperationEventType.FAILED) {
                log.warn("Failure: " + event.toString());
                isFinished = true;
                notify();
            }
        }
        
        /**
         * Handle the results of the GetFileIDs operation at a single pillar.
         * @param event The event for the completion of a GetFileIDs for a single pillar.
         */
        private void handleResult(FileIDsCompletePillarEvent event) {
            store.addFileIDs(event.getFileIDs().getFileIDsData(), getFileIds(), event.getContributorID());
        }
        
        /**
         * @return Whether the event is still running
         */
        public boolean isRunning() {
            return !isFinished;
        }        
    }
}
