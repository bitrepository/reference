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
     */
    public List<ContributorEvent> getGetFileIDs(
            String collectionId,
            ContributorQuery[] contributorQueries,
            String fileID,
            URL addressForResult,
            EventHandler eventHandler)
            throws NegativeResponseException {
        BlockingEventHandler blocker = new BlockingEventHandler(eventHandler);
        client.getFileIDs(collectionId, contributorQueries, fileID, addressForResult, blocker);
        OperationEvent finishEvent = blocker.awaitFinished();
        if(finishEvent.getEventType().equals(OperationEvent.OperationEventType.COMPLETE)) {
            return blocker.getResults();
        } else if (finishEvent.getEventType().equals(OperationEvent.OperationEventType.FAILED)) {
            throw new NegativeResponseException(finishEvent.getInfo(), null);
        } else throw new RuntimeException("Received unexpected event type" + finishEvent);
    }
}
