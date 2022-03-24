/*
 * #%L
 * Bitmagasin modify client
 *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.modify.putfile;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.BlockingEventHandler;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.exceptions.OperationFailedException;

import java.net.URL;
import java.util.List;

/**
 * Wraps a <code>PutFileClient</code> to provide a blocking client. The client will block until the PutFileOperation
 * has finished.
 */
public class BlockingPutFileClient {
    private final PutFileClient client;

    public BlockingPutFileClient(PutFileClient client) {
        this.client = client;
    }

    /**
     * Method for performing a blocking put file operation. Wraps the asynchronous {@link PutFileClient#putFile
     * (String, java.net.URL, String, long, org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE,
     * org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE, org.bitrepository.client.eventhandler.EventHandler,
     * String)} method.
     *
     * @param collectionID                  The ID of the collection
     * @param url                           The url to where the file being put can be retrieved
     * @param fileID                        The ID of the file
     * @param sizeOfFile                    [OPTIONAL] the size of the file
     * @param checksumForValidationAtPillar The checksum data of the file for pillar side validation
     * @param checksumRequestsForValidation The checksum request for the pillar to deliver
     * @param eventHandler                  The {@link EventHandler} to handle incoming events
     * @param auditTrailInformation         The auditTrail information for the pillars
     * @return The list of {@link ContributorEvent}s received during the operation
     * @throws OperationFailedException The operation didn't complete successfully.
     */
    public List<ContributorEvent> putFile(
            String collectionID,
            URL url,
            String fileID,
            long sizeOfFile,
            ChecksumDataForFileTYPE checksumForValidationAtPillar,
            ChecksumSpecTYPE checksumRequestsForValidation,
            EventHandler eventHandler,
            String auditTrailInformation)
            throws OperationFailedException {
        BlockingEventHandler blocker = new BlockingEventHandler(eventHandler);
        client.putFile(collectionID, url, fileID, sizeOfFile, checksumForValidationAtPillar,
                checksumRequestsForValidation,
                blocker, auditTrailInformation);
        OperationEvent finishEvent = blocker.awaitFinished();
        if (finishEvent.getEventType() == OperationEvent.OperationEventType.COMPLETE) {
            return blocker.getResults();
        } else if (finishEvent.getEventType() == OperationEvent.OperationEventType.FAILED) {
            throw new OperationFailedException(finishEvent.getInfo());
        } else throw new RuntimeException("Received unexpected event type" + finishEvent);
    }
}
