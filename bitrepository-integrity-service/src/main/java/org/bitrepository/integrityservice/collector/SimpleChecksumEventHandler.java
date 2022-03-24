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

import org.bitrepository.access.getchecksums.conversation.ChecksumsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Simple eventHandler for retrieving checksum results.
 * <p/>
 * Notifies the monitor
 */
public class SimpleChecksumEventHandler implements EventHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final long timeout;
    private final BlockingQueue<OperationEvent> finalEventQueue = new LinkedBlockingQueue<>();
    private final IntegrityContributors integrityContributors;
    private final Map<String, ResultingChecksums> checksumResults = new HashMap<>();

    /**
     * @param timeout               The maximum amount of millisecond to wait for a result.
     * @param integrityContributors the integrity contributors
     */
    public SimpleChecksumEventHandler(long timeout, IntegrityContributors integrityContributors) {
        this.timeout = timeout;
        this.integrityContributors = integrityContributors;
    }

    @Override
    public void handleEvent(OperationEvent event) {
        if (event.getEventType() == OperationEventType.COMPONENT_COMPLETE) {
            log.debug("Component complete: " + event);
            handleResult(event);
        } else if (event.getEventType() == OperationEventType.COMPLETE) {
            log.debug("Complete: " + event);
            finalEventQueue.add(event);
        } else if (event.getEventType() == OperationEventType.FAILED) {
            log.warn("Failure: " + event);
            finalEventQueue.add(event);
        } else if (event.getEventType() == OperationEventType.COMPONENT_FAILED) {
            ContributorFailedEvent cfe = (ContributorFailedEvent) event;
            log.warn("Component failure for '" + cfe.getContributorID() + "'.");
            integrityContributors.failContributor(cfe.getContributorID());
        } else {
            log.debug("Received event: " + event);
        }
    }

    /**
     * Retrieves the final event when the operation finishes. The final event is awaited for 'timeout' amount
     * of milliseconds. If no final events has occurred, then an InterruptedException is thrown.
     *
     * @return The final event.
     * @throws InterruptedException If it timeouts before the final event.
     */
    public OperationEvent getFinish() throws InterruptedException {
        return finalEventQueue.poll(timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * Handle the results of the GetChecksums operation at a single pillar.
     *
     * @param event The event for the completion of a GetChecksums for a single pillar.
     */
    private void handleResult(OperationEvent event) {
        if (event instanceof ChecksumsCompletePillarEvent) {
            ChecksumsCompletePillarEvent checksumEvent = (ChecksumsCompletePillarEvent) event;
            log.trace("Receiving GetChecksums result: {}", checksumEvent.getChecksums().getChecksumDataItems().toString());
            checksumResults.put(checksumEvent.getContributorID(), checksumEvent.getChecksums());
            if (checksumEvent.isPartialResult()) {
                integrityContributors.succeedContributor(checksumEvent.getContributorID());
            } else {
                integrityContributors.finishContributor(checksumEvent.getContributorID());
            }
        } else {
            log.warn("Unexpected component complete event: " + event.toString());
        }
    }

    /**
     * @return The map of the checksum results for each pillar.
     */
    public Map<String, ResultingChecksums> getResults() {
        return checksumResults;
    }
}
