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

import java.net.URL;

import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.client.eventhandler.BlockingEventHandler;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.exceptions.NegativeResponseException;

/**
 * Wrappes a <code>PutFileClient</code> to provide a blocking client. The client will block until the PutFileOperation
 * has finished.
 */
public class BlockingGetFileClient {
    private final GetFileClient client;

    public BlockingGetFileClient(GetFileClient client) {
        this.client = client;
    }

    /**
     * @see GetFileClient#getFileFromFastestPillar
     */
    public void getFileFromFastestPillar(
            String collectionId,
            String fileID,
            FilePart filePart,
            URL uploadUrl,
            EventHandler eventHandler,
            String auditTrailInformation)
            throws NegativeResponseException {
        BlockingEventHandler blocker = new BlockingEventHandler(eventHandler);
        client.getFileFromFastestPillar(collectionId, fileID, filePart, uploadUrl, blocker, auditTrailInformation);
        OperationEvent finishEvent = blocker.awaitFinished();
        if(finishEvent.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
            return;
        } else if (finishEvent.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
            throw new NegativeResponseException(finishEvent.getInfo(), null);
        } else throw new RuntimeException("Received unexpected event type" + finishEvent);
    }

    /**
     * @see GetFileClient#getFileFromFastestPillar
     */
    public void getFileFromSpecificPillar(
            String collectionId,
            String fileID,
            FilePart filePart,
            URL uploadUrl,
            String pillarId,
            EventHandler eventHandler,
            String auditTrailInformation)
            throws NegativeResponseException {
        BlockingEventHandler blocker = new BlockingEventHandler(eventHandler);
        client.getFileFromSpecificPillar(collectionId, fileID, filePart, uploadUrl, pillarId, blocker, auditTrailInformation);
        OperationEvent finishEvent = blocker.awaitFinished();
        if(finishEvent.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
            return;
        } else if (finishEvent.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
            throw new NegativeResponseException(finishEvent.getInfo(), null);
        } else throw new RuntimeException("Received unexpected event type" + finishEvent);
    }
}
