/*
 * #%L
 * Bitrepository Audit Trail Service
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
package org.bitrepository.audittrails.collector;

import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.access.getaudittrails.BlockingAuditTrailClient;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.bitrepositoryelements.Alarm;
import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositoryelements.AuditTrailEvents;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.exceptions.NegativeResponseException;
import org.bitrepository.common.utils.TimeUtils;
import org.bitrepository.service.AlarmDispatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Will perform a single collection of audit trails, potential through multiple sequential getAuditTrail calls if the
 * set of new audit trails is large.
 */
public class IncrementalCollector {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final String clientID;
    private final BlockingAuditTrailClient client;
    private final AuditTrailStore store;
    private final int maxNumberOfResults;
    private final AlarmDispatcher alarmDispatcher;

    /**
     * When no file id is wanted for the collecting of audit trails.
     */
    private static final String NO_FILE_ID = null;
    /**
     * When no delivery address is wanted for the collecting of audit trails.
     */
    private static final String NO_DELIVERY_URL = null;
    private final String collectionID;
    private long collectedAudits = 0;

    /**
     * @param collectionID       the collection ID
     * @param clientID           The clientID to use for the requests.
     * @param client             The client to use for the operations.
     * @param store              Where to persist the received results.
     * @param maxNumberOfResults A optional limit on the number of audit trail events to request. If not set, {}
     * @param alarmDispatcher    the alarm dispatcher
     */
    public IncrementalCollector(String collectionID, String clientID, AuditTrailClient client, AuditTrailStore store,
                                int maxNumberOfResults, AlarmDispatcher alarmDispatcher) {
        this.collectionID = collectionID;
        this.clientID = clientID;
        this.client = new BlockingAuditTrailClient(client);
        this.store = store;
        this.maxNumberOfResults = maxNumberOfResults;
        this.alarmDispatcher = alarmDispatcher;
    }

    /**
     * Method to get the ID of the collection to get audit trails from
     *
     * @return String The ID of the collection
     */
    public String getCollectionID() {
        return collectionID;
    }

    /**
     * Get the number of collected audit trails during this collection
     *
     * @return long The number of collected audit trails
     */
    public long getNumberOfCollectedAudits() {
        return collectedAudits;
    }

    /**
     * Setup and initiates the collection of audit trails through the client.
     * Adds one to the sequence number to request only newer audit trails.
     *
     * @param contributors the collection of IDs of contributor
     */
    public void performCollection(Collection<String> contributors) {
        collectedAudits = 0;
        long start = System.currentTimeMillis();

        log.debug("Starting collection of AuditTrails for collection '{}'", collectionID);
        Collection<String> activeContributors = contributors;
        while (!activeContributors.isEmpty()) {
            activeContributors = collect(activeContributors);
        }
        log.debug("Finished collecting AuditTrails for collection '{}', collected {} audit trails it took {}.", collectionID,
                collectedAudits, TimeUtils.millisecondsToHuman(System.currentTimeMillis() - start));
    }

    /**
     * Collect a page of audit trails from the active contributors
     *
     * @param contributors The contributors to collect from
     * @return Collection<String> the contributors that have more audits to collect
     */
    private Collection<String> collect(Collection<String> contributors) {
        List<AuditTrailQuery> queries = new ArrayList<>();

        for (String contributorId : contributors) {
            long seq = store.largestSequenceNumber(contributorId, collectionID);
            queries.add(new AuditTrailQuery(contributorId, seq + 1, null, maxNumberOfResults));
        }

        log.debug("Collecting of AuditTrails for '{}' with ContributorQueries: {}", collectionID, queries);

        AuditCollectorEventHandler handler = new AuditCollectorEventHandler();
        try {
            client.getAuditTrails(collectionID, queries.toArray(new AuditTrailQuery[0]), NO_FILE_ID, NO_DELIVERY_URL, handler,
                    clientID);

        } catch (NegativeResponseException e) {
            log.error("Problem in collecting AuditTrails, collection will not be complete for collection '{}'", collectionID, e);
            Alarm alarm = new Alarm();
            alarm.setAlarmCode(AlarmCode.COMPONENT_FAILURE);
            alarm.setAlarmText("Failed to collect audit trails. Error was: '" + e + "'");
            alarm.setCollectionID(collectionID);
            alarmDispatcher.error(alarm);
        }
        return handler.getContributorsWithPartialResults();
    }

    /**
     * Event handler for the audit trail collector. The results of an AuditTrail operation will be ingested into the
     * audit trail store.
     */
    private class AuditCollectorEventHandler implements EventHandler {
        List<String> contributorsWithPartialResults = new LinkedList<>();
        private final long startTime = System.currentTimeMillis();

        Collection<String> getContributorsWithPartialResults() {
            return contributorsWithPartialResults;
        }

        @Override
        public void handleEvent(OperationEvent event) {
            if (event instanceof AuditTrailResult) {
                AuditTrailResult auditResult = (AuditTrailResult) event;
                if (!auditResult.getCollectionID().equals(collectionID)) {
                    log.warn("Received bad collection id! Expected '{}', but got '{}'.", collectionID, auditResult.getCollectionID());
                    return;
                }
                if (auditResult.isPartialResult()) {
                    contributorsWithPartialResults.add(auditResult.getContributorID());
                }
                AuditTrailEvents events = auditResult.getAuditTrailEvents().getAuditTrailEvents();
                if (events != null && events.getAuditTrailEvent() != null && !events.getAuditTrailEvent().isEmpty()) {
                    store.addAuditTrails(events, collectionID, auditResult.getContributorID());
                    collectedAudits += events.getAuditTrailEvent().size();
                    log.debug("Collected and stored {} audit trail event(s) for '{}' from {} in {} (PartialResult={}).",
                            events.getAuditTrailEvent().size(), collectionID, auditResult.getContributorID(),
                            TimeUtils.millisecondsToHuman(System.currentTimeMillis() - startTime), auditResult.isPartialResult());
                }
            } else if (event.getEventType() == OperationEvent.OperationEventType.COMPONENT_FAILED ||
                    event.getEventType() == OperationEvent.OperationEventType.FAILED ||
                    event.getEventType() == OperationEvent.OperationEventType.IDENTIFY_TIMEOUT) {
                log.warn("Event: {}", event);
            } else {
                log.debug("Event: {}", event);
            }
        }
    }
}
