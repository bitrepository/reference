package org.bitrepository.audittrails.collector;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import org.bitrepository.access.getaudittrails.AuditTrailClient;
import org.bitrepository.access.getaudittrails.AuditTrailQuery;
import org.bitrepository.access.getaudittrails.BlockingAuditTrailClient;
import org.bitrepository.access.getaudittrails.client.AuditTrailResult;
import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.exceptions.NegativeResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Will perform a single collection of audit trails, potential through multiple
 */
public class IncrementalCollector {
    private Logger log = LoggerFactory.getLogger(getClass());
    private final String clientID;
    private final BlockingAuditTrailClient client;
    private final AuditTrailStore store;
    private final int maxNumberOfResults;

    /** When no file id is wanted for the collecting of audit trails.*/
    private static final String NO_FILE_ID = null;
    /** When no delivery address is wanted for the collecting of audit trails.*/
    private static final String NO_DELIVERY_URL = null;
    /** */
    private static final int DEFAULT_MAX_NUMBER_OF_RESULTS = 10000;

    public IncrementalCollector(String clientID, AuditTrailClient client, AuditTrailStore store,
                                BigInteger maxNumberOfResults) {
        this.clientID = clientID;
        this.client = new BlockingAuditTrailClient(client);
        this.store = store;
        this.maxNumberOfResults = (maxNumberOfResults != null)?
            maxNumberOfResults.intValue() : DEFAULT_MAX_NUMBER_OF_RESULTS;
    }

    /**
         * Setup and initiates the collection of audit trails through the client.
         * Adds one to the sequence number to request only newer audit trails.
         */
        public void performCollection(Collection<String> contributors) {
            List<AuditTrailQuery> queries = new ArrayList<AuditTrailQuery>();

            for(String contributorId : contributors) {
                int seq = store.largestSequenceNumber(contributorId);
                queries.add(new AuditTrailQuery(contributorId, seq + 1, null, maxNumberOfResults));
            }

            AuditCollectorEventHandler handler = new AuditCollectorEventHandler();
            try {
                client.getAuditTrails(queries.toArray(new AuditTrailQuery[queries.size()]), NO_FILE_ID, NO_DELIVERY_URL,
                    handler, clientID);

            } catch (NegativeResponseException e) {
                log.error("Problem in collecting audit trails, collection will not be complete", e);
            }
            if (!handler.contributorsWithPartialResults.isEmpty()) {
                performCollection(handler.contributorsWithPartialResults);
            }
        }

        /**
         * Event handler for the audit trail collector. The results of an audit trail operation will be ingested into the
         * audit trail store.
         */
        private class AuditCollectorEventHandler implements EventHandler {
            List<String> contributorsWithPartialResults = new LinkedList<String>();

            @Override
            public void handleEvent(OperationEvent event) {
                if(event instanceof AuditTrailResult) {
                    AuditTrailResult auditEvent = (AuditTrailResult) event;
                    if (auditEvent.isPartialResult()) {
                        contributorsWithPartialResults.add(auditEvent.getContributorID());
                    }
                    store.addAuditTrails(auditEvent.getAuditTrailEvents().getAuditTrailEvents());
                } else if (event.getEventType() == OperationEvent.OperationEventType.COMPONENT_FAILED ||
                    event.getEventType() == OperationEvent.OperationEventType.FAILED ||
                    event.getEventType() == OperationEvent.OperationEventType.IDENTIFY_TIMEOUT) {
                    log.warn("Event: " + event.toString());
                }
                else {
                    log.debug("Event:" + event.toString());
                }
            }
        }
}
