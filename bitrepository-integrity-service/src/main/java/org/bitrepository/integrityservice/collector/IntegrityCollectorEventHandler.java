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
package org.bitrepository.integrityservice.collector;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The eventhandler for the integrity collector.
 * 
 * Notifies the monitor 
 */
public class IntegrityCollectorEventHandler implements EventHandler {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The model where the integrity data is stored.*/
    private final IntegrityModel store;
    /** The amount of milliseconds before the results are required.*/
    private final long timeout;
    
    /** The queue used to store the received operation events. */
    private final BlockingQueue<OperationEvent> finalEventQueue = new LinkedBlockingQueue<>();
    /** The integrity contributors, keeps track of who have failed, are active or finished */
    private final IntegrityContributors integrityContributors;
    
    /**
     * Constructor.
     * @param model The integrity model, where the results of GetChecksums or GetFileIDs are to be delivered.
     * @param timeout The maximum amount of millisecond to wait for an result.
     * @param integrityContributors the integrity contributors
     */
    public IntegrityCollectorEventHandler(IntegrityModel model, long timeout, 
            IntegrityContributors integrityContributors) {
        this.store = model;
        this.timeout = timeout;
        this.integrityContributors = integrityContributors;
    }
    
    @Override
    public void handleEvent(OperationEvent event) {
        if(event.getEventType() == OperationEventType.COMPONENT_COMPLETE) {
            log.debug("Component complete: " + event.toString());
            handleResult(event);
        } else if(event.getEventType() == OperationEventType.COMPLETE) {
            log.debug("Complete: " + event.toString());
            finalEventQueue.add(event);
        } else if(event.getEventType() == OperationEventType.FAILED) {
            log.warn("Failure: " + event.toString());
            finalEventQueue.add(event);
        } else if(event.getEventType() == OperationEventType.COMPONENT_FAILED) {
            ContributorFailedEvent cfe = (ContributorFailedEvent) event;
            log.warn("Component failure for '" + cfe.getContributorID() + "'.");
            integrityContributors.failContributor(cfe.getContributorID());
        } else {
            log.debug("Received event: " + event.toString());
        }
    }
    
    /**
     * Retrieves the final event when the operation finishes. The final event is awaited for 'timeout' amount 
     * of milliseconds. If no final events has occurred, then an InterruptedException is thrown.
     * @return The final event.
     * @throws InterruptedException If it timeouts before the final event.
     */
    public OperationEvent getFinish() throws InterruptedException {
        return finalEventQueue.poll(timeout, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Handle the results of the GetChecksums operation at a single pillar.
     * @param event The event for the completion of a GetChecksums for a single pillar.
     */
    private void handleResult(OperationEvent event) {
        if(event instanceof ChecksumsCompletePillarEvent) {
            ChecksumsCompletePillarEvent checksumEvent = (ChecksumsCompletePillarEvent) event;
            log.trace("Receiving GetChecksums result: {}", 
                    checksumEvent.getChecksums().getChecksumDataItems().toString());
            store.addChecksums(checksumEvent.getChecksums().getChecksumDataItems(), checksumEvent.getContributorID(), 
                    checksumEvent.getCollectionID());
            if(checksumEvent.isPartialResult()) {
                integrityContributors.succeedContributor(checksumEvent.getContributorID());
            } else {
                integrityContributors.finishContributor(checksumEvent.getContributorID());
            }
        } else if(event instanceof FileIDsCompletePillarEvent) {
            FileIDsCompletePillarEvent fileidEvent = (FileIDsCompletePillarEvent) event;
            log.trace("Receiving GetFileIDs result: {}", fileidEvent.getFileIDs().getFileIDsData().toString());
            store.addFileIDs(fileidEvent.getFileIDs().getFileIDsData(), fileidEvent.getContributorID(),
                    fileidEvent.getCollectionID());
            if(fileidEvent.isPartialResult()) {
                integrityContributors.succeedContributor(fileidEvent.getContributorID());
            } else {
                integrityContributors.finishContributor(fileidEvent.getContributorID());
            }
        } else {
            log.warn("Unexpected component complete event: " + event.toString());
        }
    }
}
