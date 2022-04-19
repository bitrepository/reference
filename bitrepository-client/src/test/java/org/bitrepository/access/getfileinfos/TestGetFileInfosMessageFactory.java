/*
 * #%L
 * Bitrepository Access
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
/*
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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

import org.bitrepository.bitrepositorymessages.GetFileInfosFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileInfosRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileInfosRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileInfosResponse;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

/**
 * Constructs the GetFileInfos specific messages.
 * <p>
 * ToDo based on example messages.
 */
public class TestGetFileInfosMessageFactory extends ClientTestMessageFactory {

    public TestGetFileInfosMessageFactory(String clientID) {
        super(clientID);
    }

    public IdentifyPillarsForGetFileInfosResponse createIdentifyPillarsForGetFileInfosResponse(
            IdentifyPillarsForGetFileInfosRequest receivedIdentifyRequestMessage, String pillarID, String pillarDestinationId) {
        IdentifyPillarsForGetFileInfosResponse identifyPillarsForGetFileInfosResponse = new IdentifyPillarsForGetFileInfosResponse();
        initializeMessageDetails(identifyPillarsForGetFileInfosResponse);
        identifyPillarsForGetFileInfosResponse.setDestination(receivedIdentifyRequestMessage.getReplyTo());
        identifyPillarsForGetFileInfosResponse.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        identifyPillarsForGetFileInfosResponse.setCollectionID(receivedIdentifyRequestMessage.getCollectionID());
        identifyPillarsForGetFileInfosResponse.setReplyTo(pillarDestinationId);
        identifyPillarsForGetFileInfosResponse.setPillarID(pillarID);
        identifyPillarsForGetFileInfosResponse.setFileIDs(receivedIdentifyRequestMessage.getFileIDs());
        identifyPillarsForGetFileInfosResponse.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        identifyPillarsForGetFileInfosResponse.setResponseInfo(createPositiveIdentificationResponseInfo());
        identifyPillarsForGetFileInfosResponse.setFrom(pillarID);
        return identifyPillarsForGetFileInfosResponse;
    }

    /**
     * @param receivedGetChecksumsRequest
     * @param pillarID
     * @param pillarDestinationId
     * @return
     */
    public GetFileInfosFinalResponse createGetFileInfosFinalResponse(GetFileInfosRequest receivedGetChecksumsRequest, String pillarID,
                                                                     String pillarDestinationId) {
        GetFileInfosFinalResponse getFileInfosFinalResponse = new GetFileInfosFinalResponse();
        initializeMessageDetails(getFileInfosFinalResponse);
        getFileInfosFinalResponse.setDestination(receivedGetChecksumsRequest.getReplyTo());
        getFileInfosFinalResponse.setCorrelationID(receivedGetChecksumsRequest.getCorrelationID());
        getFileInfosFinalResponse.setCollectionID(receivedGetChecksumsRequest.getCollectionID());
        getFileInfosFinalResponse.setReplyTo(pillarDestinationId);
        getFileInfosFinalResponse.setPillarID(pillarID);
        getFileInfosFinalResponse.setFrom(pillarID);
        getFileInfosFinalResponse.setResponseInfo(createCompleteResponseInfo());

        return getFileInfosFinalResponse;
    }
}