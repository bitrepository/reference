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
import org.bitrepository.access.getfileids.conversation.FileIDsCompletePillarEvent;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.client.eventhandler.ContributorFailedEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.common.utils.CountAndTimeUnit;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * The EventHandler for the integrity collector.
 * <p>
 * Notifies the monitor
 */
public class IntegrityCollectorEventHandler implements EventHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final IntegrityModel store;
    private final Duration timeout;
    private final BlockingQueue<OperationEvent> finalEventQueue = new LinkedBlockingQueue<>();
    private final IntegrityContributors integrityContributors;

    /**
     * @param model                 The integrity model, where the results of GetChecksums or GetFileIDs are to be delivered.
     * @param timeout               The maximum duration to wait for a result.
     * @param integrityContributors the integrity contributors
     */
    public IntegrityCollectorEventHandler(IntegrityModel model, Duration timeout, IntegrityContributors integrityContributors) {
        this.store = model;
        this.timeout = Objects.requireNonNull(timeout, "timeout");
        this.integrityContributors = integrityContributors;
    }

    @Override
    public void handleEvent(OperationEvent event) {
        if (event.getEventType() == OperationEventType.COMPONENT_COMPLETE) {
            log.debug("Component complete: {}", event);
            handleResult(event);
        } else if (event.getEventType() == OperationEventType.COMPLETE) {
            log.debug("Complete: {}", event);
            finalEventQueue.add(event);
        } else if (event.getEventType() == OperationEventType.FAILED) {
            log.warn("Failure: {}", event);
            finalEventQueue.add(event);
        } else if (event.getEventType() == OperationEventType.COMPONENT_FAILED) {
            ContributorFailedEvent cfe = (ContributorFailedEvent) event;
            log.warn("Component failure for '{}'", cfe.getContributorID());
            integrityContributors.failContributor(cfe.getContributorID());
        } else {
            log.debug("Received event: {}", event);
        }
    }

    /**
     * Retrieves the final event when the operation finishes.
     * The final event is awaited for 'timeout'. If no final events has occurred, then null is returned.
     *
     * @return The final event or null if no final events has occurred.
     * @throws InterruptedException If interrupted while waiting.
     */
    public OperationEvent getFinish() throws InterruptedException {
        CountAndTimeUnit pollTimeout = TimeUtils.durationToCountAndTimeUnit(timeout);
        return finalEventQueue.poll(pollTimeout.getCount(), pollTimeout.getUnit());
    }

    /**
     * Handle the results of the GetChecksums operation at a single pillar.
     *
     * @param event The event for the completion of a GetChecksums for a single pillar.
     */
    private void handleResult(OperationEvent event) {
        if (event instanceof ChecksumsCompletePillarEvent) {
            ChecksumsCompletePillarEvent checksumEvent = (ChecksumsCompletePillarEvent) event;
            List<ChecksumDataForChecksumSpecTYPE> checksumsData = checksumEvent.getChecksums()
                    .getChecksumDataItems();
            String contributorID = checksumEvent.getContributorID();
            log.trace("Receiving GetChecksums result: {}", checksumsData.toString());
            store.addChecksums(checksumsData, contributorID, checksumEvent.getCollectionID());

            if (checksumEvent.isPartialResult()) {
                integrityContributors.succeedContributor(contributorID);
            } else {
                integrityContributors.finishContributor(contributorID);
            }
        } else if (event instanceof FileIDsCompletePillarEvent) {
            FileIDsCompletePillarEvent fileIDEvent = (FileIDsCompletePillarEvent) event;
            FileIDsData fileIDsData = fileIDEvent.getFileIDs().getFileIDsData();
            String contributorID = fileIDEvent.getContributorID();
            log.trace("Receiving GetFileIDs result: {}", fileIDsData.toString());
            store.addFileIDs(fileIDsData, contributorID, fileIDEvent.getCollectionID());

            if (fileIDEvent.isPartialResult()) {
                integrityContributors.succeedContributor(contributorID);
            } else {
                integrityContributors.finishContributor(contributorID);
            }
        } else {
            log.warn("Unexpected component complete event: {}", event.toString());
        }
    }
}
