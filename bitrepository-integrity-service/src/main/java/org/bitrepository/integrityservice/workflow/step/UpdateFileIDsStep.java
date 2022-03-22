/*
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

import java.util.*;

/**
 * The step for collecting of all file ids from all pillars.
 */
public abstract class UpdateFileIDsStep extends AbstractWorkFlowStep {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final IntegrityInformationCollector collector;
    protected final IntegrityModel store;
    private final IntegrityAlerter alerter;
    private final Long timeout;
    private final Integer maxNumberOfResultsPerConversation;
    protected final String collectionID;
    private boolean abortInCaseOfFailure = true;
    private final IntegrityContributors integrityContributors;

    /**
     * @param collector The client for collecting the checksums.
     * @param store     The storage for the integrity data.
     * @param alerter   The alerter for sending failures.
     * @param settings  The settings to use.
     */
    public UpdateFileIDsStep(IntegrityInformationCollector collector, IntegrityModel store, IntegrityAlerter alerter, Settings settings,
                             String collectionID, IntegrityContributors integrityContributors) {
        this.collector = collector;
        this.store = store;
        this.alerter = alerter;
        this.collectionID = collectionID;
        this.integrityContributors = integrityContributors;
        this.timeout = settings.getRepositorySettings().getClientSettings().getIdentificationTimeout().longValue() +
                settings.getRepositorySettings().getClientSettings().getOperationTimeout().longValue();
        this.maxNumberOfResultsPerConversation = SettingsUtils.getMaxClientPageSize();
        if (settings.getReferenceSettings().getIntegrityServiceSettings().isSetAbortOnFailedContributor()) {
            abortInCaseOfFailure = settings.getReferenceSettings().getIntegrityServiceSettings().isAbortOnFailedContributor();
        }
    }

    /**
     * Method to implement early/pre-performStep action
     */
    protected void initialStepAction() {}

    @Override
    public synchronized void performStep() throws WorkflowAbortedException {
        initialStepAction();

        try {
            Set<String> pillarsToCollectFrom = integrityContributors.getActiveContributors();
            log.debug("Collecting fileIDs from: " + pillarsToCollectFrom);
            while (!pillarsToCollectFrom.isEmpty()) {
                IntegrityCollectorEventHandler eventHandler = new IntegrityCollectorEventHandler(store, timeout, integrityContributors);
                ContributorQuery[] queries = getQueries(pillarsToCollectFrom);
                collector.getFileIDs(collectionID, pillarsToCollectFrom, "IntegrityService: " + getName(), queries, eventHandler);

                OperationEvent event = eventHandler.getFinish();
                if (event.getEventType() == OperationEventType.FAILED) {
                    handleFailureEvent(event);
                }
                log.debug("Collection of file ids had the final event: " + event);
                pillarsToCollectFrom = integrityContributors.getActiveContributors();
            }
        } catch (InterruptedException e) {
            log.warn("Interrupted while collecting file ids.", e);
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
                alerter.integrityFailed("Integrity check aborted while getting fileIDs due to failed contributors: " +
                        integrityContributors.getFailedContributors(), collectionID);
                throw new WorkflowAbortedException("Aborting workflow due to failure collecting fileIDs. " + "Cause: " + ofe.toString());
            } else {
                log.info("Failure occurred collecting fileIDs, continuing collecting fileIDs. Failure {}", ofe.toString());
                alerter.integrityFailed("Failure while collecting fileIDs, the check will continue " +
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
            Date latestFileIDEntry = store.getDateForNewestFileEntryForPillar(pillar, collectionID);
            res.add(new ContributorQuery(pillar, latestFileIDEntry, null, maxNumberOfResultsPerConversation));
        }

        return res.toArray(new ContributorQuery[pillars.size()]);
    }
}
