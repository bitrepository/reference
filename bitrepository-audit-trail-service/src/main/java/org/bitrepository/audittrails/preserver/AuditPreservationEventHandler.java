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
package org.bitrepository.audittrails.preserver;

import org.bitrepository.audittrails.store.AuditTrailStore;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.eventhandler.OperationEvent.OperationEventType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * The event handler for the preservation of audit trail data.
 * When the PutFile operation has completed, then the store will be updated with the results.
 * <p>
 * It is not necessary to wait until all the components are complete. Just the first.
 * This EventHandler can only be used for handling the PutFile operation of a single AuditTrail package.
 */
public class AuditPreservationEventHandler implements EventHandler {
    private final Logger log = LoggerFactory.getLogger(getClass());
    private final Map<String, Long> seqNumbers;
    private final AuditTrailStore store;
    private final String collectionID;

    /**
     * @param contributorSequenceNumbers The map between the contributor ids and their respective sequence number.
     * @param store                      The store which should be updated with these sequence numbers.
     * @param collectionID               The ID of the collection that needs to have its sequence number updated.
     */
    public AuditPreservationEventHandler(Map<String, Long> contributorSequenceNumbers, AuditTrailStore store,
                                         String collectionID) {
        this.seqNumbers = contributorSequenceNumbers;
        this.store = store;
        this.collectionID = collectionID;
    }

    @Override
    public void handleEvent(OperationEvent event) {
        if (event.getEventType() == OperationEventType.COMPLETE) {
            updateStoreWithResults();

        } else {
            log.debug("Event for preservation of audit trails: {}", event);
        }
    }

    /**
     * Update the store with the results.
     */
    private void updateStoreWithResults() {
        for (Map.Entry<String, Long> entry : seqNumbers.entrySet()) {
            String contributorID = entry.getKey();
            if (store.hasPreservationKey(contributorID, collectionID)) {
                store.setPreservationSequenceNumber(contributorID, collectionID, entry.getValue());
            } else {
                log.debug("Preservation key for contributor: {} in collection: {} is not known by the database.",
                        contributorID, collectionID);
            }
        }
    }
}
