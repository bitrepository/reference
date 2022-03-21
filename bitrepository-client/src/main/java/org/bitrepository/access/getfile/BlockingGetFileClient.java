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
package org.bitrepository.access.getfile;

import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.client.eventhandler.BlockingEventHandler;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.exceptions.NegativeResponseException;

import java.net.URL;

/**
 * Wraps a {@link org.bitrepository.modify.putfile.PutFileClient} to provide a blocking client. The client will block until the
 * PutFileOperation has finished.
 */
public class BlockingGetFileClient {
    private final GetFileClient client;

    public BlockingGetFileClient(GetFileClient client) {
        this.client = client;
    }

    /**
     * @param collectionID          The ID of the collection
     * @param fileID                The ID of the file to get
     * @param filePart              The FilePart specification if only requesting parts of a file
     * @param uploadUrl             The URL to upload the file to
     * @param eventHandler          The EventHandler to handle incoming events
     * @param auditTrailInformation The auditTrail information for the contributors
     * @throws NegativeResponseException in case the operation fails
     * @see GetFileClient#getFileFromFastestPillar
     */
    public void getFileFromFastestPillar(String collectionID, String fileID, FilePart filePart, URL uploadUrl, EventHandler eventHandler,
                                         String auditTrailInformation) throws NegativeResponseException {
        BlockingEventHandler blocker = new BlockingEventHandler(eventHandler);
        client.getFileFromFastestPillar(collectionID, fileID, filePart, uploadUrl, blocker, auditTrailInformation);
        OperationEvent finishEvent = blocker.awaitFinished();
        if (finishEvent.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
            throw new NegativeResponseException(finishEvent.getInfo(), null);
        } else if (!finishEvent.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
            throw new RuntimeException("Received unexpected event type" + finishEvent);
        }
    }

    /**
     * @param collectionID          The ID of the collection
     * @param fileID                The ID of the file to get
     * @param filePart              The FilePart specification if only requesting parts of a file
     * @param uploadUrl             The URL to upload the file to
     * @param pillarID              The ID of the specific pillar to get the file from
     * @param eventHandler          The EventHandler to handle incoming events
     * @param auditTrailInformation The auditTrail information for the contributors
     * @throws NegativeResponseException in case the operation fails
     * @see GetFileClient#getFileFromSpecificPillar
     */
    public void getFileFromSpecificPillar(String collectionID, String fileID, FilePart filePart, URL uploadUrl, String pillarID,
                                          EventHandler eventHandler, String auditTrailInformation) throws NegativeResponseException {
        BlockingEventHandler blocker = new BlockingEventHandler(eventHandler);
        client.getFileFromSpecificPillar(collectionID, fileID, filePart, uploadUrl, pillarID, blocker, auditTrailInformation);
        OperationEvent finishEvent = blocker.awaitFinished();
        if (finishEvent.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
            throw new NegativeResponseException(finishEvent.getInfo(), null);
        } else if (!finishEvent.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
            throw new RuntimeException("Received unexpected event type" + finishEvent);
        }
    }
}
