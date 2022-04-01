/*
 * #%L
 * BitRepository modify client
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
package org.bitrepository.access.getfileinfos;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.client.eventhandler.BlockingEventHandler;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.exceptions.NegativeResponseException;

import java.net.URL;
import java.util.List;

/**
 * Wrappes a <code>PutFileClient</code> to provide a blocking client. The client will block until the PutFileOperation
 * has finished.
 */
public class BlockingGetFileInfosClient {
    private final GetFileInfosClient client;

    public BlockingGetFileInfosClient(GetFileInfosClient client) {
        this.client = client;
    }

    /**
     * @param collectionID          The ID of the collection
     * @param contributorQueries    The queries for the contributors
     * @param fileID                The ID of the file that the request is about
     * @param checksumSpec          The checksum specification for the file
     * @param urlForResult          The URL to deliver results
     * @param eventHandler          The EventHandler to handle incoming events
     * @param auditTrailInformation The audit trail information for the components
     * @return The list of ContributorEvents containing the results
     * @throws NegativeResponseException in case of the operation fails.
     * @see GetFileInfosClient#getFileInfos
     */
    public List<ContributorEvent> getFileInfos(String collectionID, ContributorQuery[] contributorQueries,
                                               String fileID, ChecksumSpecTYPE checksumSpec, URL urlForResult, EventHandler eventHandler,
                                               String auditTrailInformation) throws NegativeResponseException {

        BlockingEventHandler blocker = new BlockingEventHandler(eventHandler);
        client.getFileInfos(collectionID, contributorQueries, fileID, checksumSpec, urlForResult, blocker,
                auditTrailInformation);
        OperationEvent finishEvent = blocker.awaitFinished();
        if (finishEvent.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
            return blocker.getResults();
        } else if (finishEvent.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
            throw new NegativeResponseException(finishEvent.getInfo(), null);
        } else throw new RuntimeException("Received unexpected event type" + finishEvent);
    }
}