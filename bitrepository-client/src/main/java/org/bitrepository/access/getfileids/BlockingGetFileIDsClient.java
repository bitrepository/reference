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
package org.bitrepository.access.getfileids;

import java.net.URL;
import java.util.List;

import org.bitrepository.access.ContributorQuery;
import org.bitrepository.client.eventhandler.BlockingEventHandler;
import org.bitrepository.client.eventhandler.ContributorEvent;
import org.bitrepository.client.eventhandler.EventHandler;
import org.bitrepository.client.eventhandler.OperationEvent;
import org.bitrepository.client.exceptions.NegativeResponseException;

/**
 * Wrappes a <code>PutFileClient</code> to provide a blocking client. The client will block until the PutFileOperation
 * has finished.
 */
public class BlockingGetFileIDsClient {
    private final GetFileIDsClient client;

    public BlockingGetFileIDsClient(GetFileIDsClient client) {
        this.client = client;
    }

    /**
     * @see GetFileIDsClient#getFileIDs
     * @param collectionID The ID of the collection
     * @param contributorQueries The {@link ContributorQuery} for the GetFileIDs request
     * @param fileID The ID of the file that the request is about
     * @param addressForResult The address of where to deliver the result
     * @param eventHandler The event handler to handle incoming events
     * @return The list of {@link ContributorEvent}'s 
     * @throws NegativeResponseException if the operation failed
     */
    public List<ContributorEvent> getGetFileIDs(
            String collectionID,
            ContributorQuery[] contributorQueries,
            String fileID,
            URL addressForResult,
            EventHandler eventHandler)
            throws NegativeResponseException {
        BlockingEventHandler blocker = new BlockingEventHandler(eventHandler);
        client.getFileIDs(collectionID, contributorQueries, fileID, addressForResult, blocker);
        return getContributorEvents(blocker);
    }
}
