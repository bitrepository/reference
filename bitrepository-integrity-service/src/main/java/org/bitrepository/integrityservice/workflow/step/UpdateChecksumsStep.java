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

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.bitrepository.client.eventhandler.OperationFailedEvent;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.SettingsUtils;
import org.bitrepository.integrityservice.alerter.IntegrityAlerter;
import org.bitrepository.integrityservice.cache.IntegrityModel;
import org.bitrepository.integrityservice.collector.IntegrityCollectorEventHandler;
import org.bitrepository.integrityservice.collector.IntegrityInformationCollector;
import org.bitrepository.integrityservice.workflow.IntegrityContributors;
import org.bitrepository.service.exception.WorkflowAbortedException;
import org.bitrepository.service.workflow.AbstractWorkFlowStep;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;

/**
 * The step for collecting the checksums of all files from all pillars.
 */
public abstract class UpdateChecksumsStep extends AbstractWorkFlowStep {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final IntegrityInformationCollector collector;
    protected final IntegrityModel store;
    private final ChecksumSpecTYPE checksumType;
    private final IntegrityAlerter alerter;
    private final Duration timeout;
    private final Integer maxNumberOfResultsPerConversation;
    protected final String collectionID;
    private boolean abortInCaseOfFailure = true;
    private final IntegrityContributors integrityContributors;

    /**
     * @param collector    The client for collecting the checksums.
     * @param store        The storage for the integrity data.
     * @param alerter      The alerter for sending failures.
     * @param checksumType The type of checksum to collect.
     */
    public UpdateChecksumsStep(IntegrityInformationCollector collector, IntegrityModel store, IntegrityAlerter alerter,
                               ChecksumSpecTYPE checksumType, Settings settings, String collectionID,
                               IntegrityContributors integrityContributors) {
        this.collector = collector;
        this.store = store;
        this.checksumType = checksumType;
        this.alerter = alerter;
        this.collectionID = collectionID;
        this.integrityContributors = integrityContributors;
        this.timeout = settings.getIdentificationTimeout().plus(settings.getOperationTimeout());
        this.maxNumberOfResultsPerConversation = SettingsUtils.getMaxClientPageSize();
        if (settings.getReferenceSettings().getIntegrityServiceSettings().isSetAbortOnFailedContributor()) {
            abortInCaseOfFailure = settings.getReferenceSettings().getIntegrityServiceSettings().isAbortOnFailedContributor();
        }
    }

    /**
     * Method to implement early/pre-performStep action
     */
    protected void initialStepAction() {}

    /**
     * Method to implement late/post-perform actions.
     */
    protected void finalStepAction() {}

    @Override
    public synchronized void performStep() throws WorkflowAbortedException {
        try {
            initialStepAction();

            Set<String> pillarsToCollectFrom = integrityContributors.getActiveContributors();
            log.debug("Collecting checksums from '" + pillarsToCollectFrom + "' for collection '" + collectionID + "'.");
            while (!pillarsToCollectFrom.isEmpty()) {
                IntegrityCollectorEventHandler eventHandler = new IntegrityCollectorEventHandler(store, timeout, integrityContributors);
                ContributorQuery[] queries = getQueries(pillarsToCollectFrom);
                collector.getChecksums(collectionID, pillarsToCollectFrom, checksumType, null, "IntegrityService: " + getName(), queries,
                        eventHandler);

                OperationEvent event = eventHandler.getFinish();
                if (event.getEventType() == OperationEventType.FAILED) {
                    handleFailureEvent(event);
                }
                log.debug("Collecting of checksums ids had the final event: " + event);
                pillarsToCollectFrom = integrityContributors.getActiveContributors();
            }

            finalStepAction();
        } catch (InterruptedException e) {
            log.warn("Interrupted while collecting checksums.", e);
        }
    }

    /**
     * Handle a failure event. This includes checking if any contributors have failed (if not just retry),
     * checking to see if the workflow should be aborted, and sending alarms if needed.
     */
    private void handleFailureEvent(OperationEvent event) throws WorkflowAbortedException {
        if (integrityContributors.getFailedContributors().isEmpty()) {
            log.info("Get failure event, but no contributors marked as failed, retrying");
        } else {
            OperationFailedEvent ofe = (OperationFailedEvent) event;
            if (abortInCaseOfFailure) {
                alerter.integrityFailed("Integrity check aborted while getting checksums due to failed contributors: " +
                        integrityContributors.getFailedContributors(), collectionID);
                throw new WorkflowAbortedException("Aborting workflow due to failure collecting checksums. " + "Cause: " + ofe.toString());
            } else {
                log.info("Failure occured collecting fileIDs, continuing collecting checksums. Failure {}", ofe.toString());
                alerter.integrityFailed("Failure while collecting checksums, the check will continue " +
                                "with the information available. The failed contributors were: " + integrityContributors.getFailedContributors(),
                        collectionID);
            }
        }
    }

    /**
     * Define the queries for the collection of FileIDs for the given pillars.
     *
     * @param pillars The pillars to collect from.
     * @return The queries for the pillars for collecting the file ids.
     */
    private ContributorQuery[] getQueries(Collection<String> pillars) {
        List<ContributorQuery> res = new ArrayList<>();
        for (String pillar : pillars) {
            Date latestChecksumEntry = store.getDateForNewestChecksumEntryForPillar(pillar, collectionID);
            res.add(new ContributorQuery(pillar, latestChecksumEntry, null, maxNumberOfResultsPerConversation));
        }

        return res.toArray(new ContributorQuery[pillars.size()]);
    }

}
