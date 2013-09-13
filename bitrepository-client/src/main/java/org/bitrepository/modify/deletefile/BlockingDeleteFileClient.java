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
package org.bitrepository.modify.deletefile;

import java.util.List;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.BlockingEventHandler;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.common.exceptions.OperationFailedException;

/**
 * Wrappes a <code>DeleteFileClient</code> to provide a blocking client. The client will block until the DeleteFileOperation
 * has finished.
 */
public class BlockingDeleteFileClient {
    private final DeleteFileClient client;

    public BlockingDeleteFileClient(DeleteFileClient client) {
        this.client = client;
    }
    /**
     * Method for performing a blocking delete file operation.
     *
     * @param fileId The id of the file.
     * @param pillarID The id of the pillar to delete the file on.
     * @param checksumForValidationAtPillar The checksum for validating at pillar side.
     * @param checksumRequestsForValidation The checksum for validating at client side.
     * @param eventHandler The EventHandler for the operation.
     * @param auditTrailInformation The audit trail information.
     */
    public List<ContributorEvent> deleteFile(
            String collectionID,
            String fileId,
            String pillarID,
            ChecksumDataForFileTYPE checksumForValidationAtPillar,
            ChecksumSpecTYPE checksumRequestsForValidation,
            EventHandler eventHandler,
            String auditTrailInformation)
            throws OperationFailedException {
        BlockingEventHandler blocker = new BlockingEventHandler(eventHandler);
        client.deleteFile(collectionID,  fileId, pillarID, checksumForValidationAtPillar,
                checksumRequestsForValidation,
                blocker, auditTrailInformation);
        OperationEvent finishEvent = blocker.awaitFinished();
        if(finishEvent.getEventType() == OperationEvent.OperationEventType.COMPLETE) {
            return blocker.getResults();
        } else if (finishEvent.getEventType() == OperationEvent.OperationEventType.FAILED) {
            throw new OperationFailedException(finishEvent.getInfo());
        } else throw new RuntimeException("Received unexpected event type" + finishEvent);
    }
}
