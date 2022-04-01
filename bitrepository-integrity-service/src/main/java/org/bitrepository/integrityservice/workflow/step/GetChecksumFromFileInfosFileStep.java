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

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResultingFileInfos;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.collector.SimpleFileInfosEventHandler;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;
import org.bitrepository.service.exception.WorkflowAbortedException;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * The step for collecting the checksums of a specific file from the pillars.
 */
public class GetChecksumFromFileInfosFileStep extends AbstractWorkFlowStep {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final IntegrityInformationCollector collector;
    private final ChecksumSpecTYPE checksumType;
    private final IntegrityAlerter alerter;
    private final Long timeout;
    protected final String fileID;
    protected final String collectionID;
    private final IntegrityContributors integrityContributors;
    private Map<String, ResultingFileInfos> checksumResults = null;

    /**
     * @param collector             The client for collecting the checksums.
     * @param alerter               The alerter for sending failures.
     * @param checksumType          The type of checksum to collect.
     * @param fileID                The ID of the file to request.
     * @param settings              The settings.
     * @param collectionID          The id of the collection.
     * @param integrityContributors The contributors, who should be asked for the checksum.
     */
    public GetChecksumFromFileInfosFileStep(IntegrityInformationCollector collector, IntegrityAlerter alerter,
                                            ChecksumSpecTYPE checksumType,
                                            String fileID, Settings settings, String collectionID,
                                            IntegrityContributors integrityContributors) {
        this.collector = collector;
        this.checksumType = checksumType;
        this.alerter = alerter;
        this.collectionID = collectionID;
        this.fileID = fileID;
        this.integrityContributors = integrityContributors;
        this.timeout = settings.getRepositorySettings().getClientSettings().getIdentificationTimeout().longValue() +
                settings.getRepositorySettings().getClientSettings().getOperationTimeout().longValue();
    }

    @Override
    public synchronized void performStep() throws WorkflowAbortedException {
        try {
            Set<String> pillarsToCollectFrom = integrityContributors.getActiveContributors();
            log.debug("Collecting checksums from '" + pillarsToCollectFrom + "' for collection '" + collectionID + "'.");
            SimpleFileInfosEventHandler eventHandler = new SimpleFileInfosEventHandler(timeout, integrityContributors);
            collector.getChecksums(collectionID, pillarsToCollectFrom, checksumType, fileID,
                    "Getting salted " + checksumType.getChecksumType() + " checksum for spot integrity check", null, eventHandler);

            OperationEvent event = eventHandler.getFinish();
            if (event.getEventType() == OperationEventType.FAILED) {
                handleFailureEvent(event);
            }
            log.debug("Collecting of checksums had the final event: " + event);
            checksumResults = eventHandler.getResults();
        } catch (InterruptedException e) {
            log.warn("Interrupted while collecting checksums.", e);
        }
    }

    /**
     * Handle a failure event. This includes checking if any contributors have failed (if not just retry),
     * checking to see if the workflow should be aborted, and sending alarms if needed.
     */
    private void handleFailureEvent(OperationEvent event) {
        if (integrityContributors.getFailedContributors().isEmpty()) {
            log.info("Get failure event, but no contributors marked as failed.");
        } else {
            OperationFailedEvent ofe = (OperationFailedEvent) event;
            log.info("Failure occurred collecting checksums. Failure {}", ofe.toString());
            alerter.integrityFailed("Failure while collecting checksums, the check will continue " +
                            "with the information available. The failed contributors were: " + integrityContributors.getFailedContributors(),
                    collectionID);
        }
    }

    /**
     * Extracts the results from the conversation.
     *
     * @return The map between pillars and their checksum for the file.
     */
    public Map<String, String> getResults() {
        if (checksumResults == null) {
            return null;
        }
        Map<String, String> res = new HashMap<>();
        for (Map.Entry<String, ResultingFileInfos> entry : checksumResults.entrySet()) {
            if (!validateResults(entry.getValue())) {
                log.warn("No or invalid checksum results from pillar '" + entry.getKey() + "': " + entry.getValue());
                continue;
            }
            String checksum = Base16Utils.decodeBase16(entry.getValue().getFileInfosDataItem().get(0).getChecksumValue());
            res.put(entry.getKey(), checksum);
        }
        return res;
    }

    /**
     * Check whether the entry exists and that it is for the right file.
     * (If more than one results is given, then the first must be the requested file).
     *
     * @param fileInfosData The resulting file infos data to validate.
     * @return Whether it is valid.
     */
    private boolean validateResults(ResultingFileInfos fileInfosData) {
        if (fileInfosData.getFileInfosDataItem().isEmpty()) {
            return false;
        }
        return fileInfosData.getFileInfosDataItem().get(0).getFileID().equalsIgnoreCase(fileID);
    }

    @Override
    public String getName() {
        return "Performing the GetChecksum for file '" + fileID + "' with the algorithm '" + checksumType.getChecksumType() +
                "' and salt '" + Base16Utils.decodeBase16(checksumType.getChecksumSalt()) + "'.";
    }
}
