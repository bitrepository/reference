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
package org.bitrepository.modify.replacefile;

import java.net.URL;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.BlockingEventHandler;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.exceptions.OperationFailedException;

/**
 * Wrappes a <code>ReplaceFileClient</code> to provide a blocking client. The client will block until the ReplaceFileOperation
 * has finished.
 */
public class BlockingReplaceFileClient {
    private final ReplaceFileClient client;

    public BlockingReplaceFileClient(ReplaceFileClient client) {
        this.client = client;
    }
    /**
     * Method for performing a blocking replace file operation. Wraps the asynchronous
     * {@link org.bitrepository.modify.replacefile.ReplaceFileClient#replaceFile
     * (String, String, String, ChecksumDataForFileTYPE, ChecksumSpecTYPE, URL, long, ChecksumDataForFileTYPE,
     * ChecksumSpecTYPE, EventHandler, String)} method.
     * @param collectionID The ID of the collection
     * @param fileID The ID of the file
     * @param pillarID The ID of the pillar
     * @param checksumForDeleteAtPillar The checksum data for pillar side verification of the existing file
     * @param checksumRequestedForDeletedFile The checksum request for the existing file
     * @param url The url of where to find the replacement file
     * @param sizeOfNewFile The size of the new file [OPTIONAL]
     * @param checksumForNewFileValidationAtPillar The checksum data for pillar side verification of the new file
     * @param checksumRequestsForNewFile The checksum request for the new file
     * @param eventHandler The {@link EventHandler} to handle incoming events
     * @param auditTrailInformation The audit trail information for the pillar
     * @return The list of received {@link ContributorEvent}'s
     * @throws OperationFailedException The operation didn't complete successfully.
     */
    public List<ContributorEvent> replaceFile(
            String collectionID, String fileID, String pillarID, ChecksumDataForFileTYPE checksumForDeleteAtPillar,
    ChecksumSpecTYPE checksumRequestedForDeletedFile, URL url, long sizeOfNewFile,
    ChecksumDataForFileTYPE checksumForNewFileValidationAtPillar, ChecksumSpecTYPE checksumRequestsForNewFile,
    EventHandler eventHandler, String auditTrailInformation)
            throws OperationFailedException {
        BlockingEventHandler blocker = new BlockingEventHandler(eventHandler);
        client.replaceFile(collectionID, fileID, pillarID, checksumForDeleteAtPillar,
                checksumRequestedForDeletedFile, url, sizeOfNewFile, checksumForNewFileValidationAtPillar,
                checksumRequestsForNewFile,
                blocker, auditTrailInformation);
        OperationEvent finishEvent = blocker.awaitFinished();
        if(finishEvent.getEventType() == OperationEvent.OperationEventType.COMPLETE) {
            return blocker.getResults();
        } else if (finishEvent.getEventType() == OperationEvent.OperationEventType.FAILED) {
            throw new OperationFailedException(finishEvent.getInfo());
        } else throw new RuntimeException("Received unexpected event type" + finishEvent);
    }
}
