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
package org.bitrepository.access.getchecksums;

import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

/**
 * Constructs the GetFile specific messages.
 * 
 * ToDo based on example messages.
 */
public class TestGetChecksumsMessageFactory extends ClientTestMessageFactory {

    public TestGetChecksumsMessageFactory(String collectionID) {
        super(collectionID);
    }

    public IdentifyPillarsForGetChecksumsResponse createIdentifyPillarsForGetChecksumsResponse(
            IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage,
            String pillarId, String pillarDestinationId) {
        IdentifyPillarsForGetChecksumsResponse identifyPillarsForGetChecksumsResponse = new IdentifyPillarsForGetChecksumsResponse();
        initializeMessageDetails(identifyPillarsForGetChecksumsResponse);
        identifyPillarsForGetChecksumsResponse.setDestination(receivedIdentifyRequestMessage.getReplyTo());
        identifyPillarsForGetChecksumsResponse.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        identifyPillarsForGetChecksumsResponse.setCollectionID(
                receivedIdentifyRequestMessage.getCollectionID());
        identifyPillarsForGetChecksumsResponse.setReplyTo(pillarDestinationId);
        identifyPillarsForGetChecksumsResponse.setPillarID(pillarId);
        identifyPillarsForGetChecksumsResponse.setFileIDs(receivedIdentifyRequestMessage.getFileIDs());
        identifyPillarsForGetChecksumsResponse.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        identifyPillarsForGetChecksumsResponse.setResponseInfo(createPositiveIdentificationResponseInfo());
        identifyPillarsForGetChecksumsResponse.setFrom(pillarId);
        return identifyPillarsForGetChecksumsResponse;
    }

    /**
     * @param receivedGetChecksumsRequest
     * @param pillarId
     * @param pillarDestinationId
     * @return
     */
    public GetChecksumsFinalResponse createGetChecksumsFinalResponse(
            GetChecksumsRequest receivedGetChecksumsRequest, String pillarId, String pillarDestinationId) {
        GetChecksumsFinalResponse getChecksumsFinalResponse = new GetChecksumsFinalResponse();
        initializeMessageDetails(getChecksumsFinalResponse);
        getChecksumsFinalResponse.setDestination(receivedGetChecksumsRequest.getReplyTo());
        getChecksumsFinalResponse.setCorrelationID(receivedGetChecksumsRequest.getCorrelationID());
        getChecksumsFinalResponse.setCollectionID(receivedGetChecksumsRequest.getCollectionID());
        getChecksumsFinalResponse.setReplyTo(pillarDestinationId);
        getChecksumsFinalResponse.setPillarID(pillarId);
        getChecksumsFinalResponse.setFrom(pillarId);
        getChecksumsFinalResponse.setResponseInfo(createCompleteResponseInfo());
        
        return getChecksumsFinalResponse;
    }
}
