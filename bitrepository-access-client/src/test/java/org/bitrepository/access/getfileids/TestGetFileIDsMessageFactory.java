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
package org.bitrepository.access.getfileids;

import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

/**
 * Generates test messages for use in GetFileIDsClientTest.
 */
public class TestGetFileIDsMessageFactory extends ClientTestMessageFactory {

    public TestGetFileIDsMessageFactory(String collectionID) {
        super(collectionID);
    }

    public IdentifyPillarsForGetFileIDsRequest createIdentifyPillarsForGetFileIDsRequest() {
        IdentifyPillarsForGetFileIDsRequest identifyPillarsForGetFileIDsRequest = new IdentifyPillarsForGetFileIDsRequest();
        identifyPillarsForGetFileIDsRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        initializeMessageDetails(identifyPillarsForGetFileIDsRequest);
        
        return identifyPillarsForGetFileIDsRequest;
    }
    
    // TODO queue in all methods?
    /**
     * Generate IdentifyPillarsForGetFileIDsRequest test message with specified values.
     * @return test message
     */
    public IdentifyPillarsForGetFileIDsRequest createIdentifyPillarsForGetFileIDsRequest(
            IdentifyPillarsForGetFileIDsRequest receivedMessage, String toTopic) {
        IdentifyPillarsForGetFileIDsRequest request = createIdentifyPillarsForGetFileIDsRequest();
        request.setCorrelationID(receivedMessage.getCorrelationID());
        request.setReplyTo(receivedMessage.getReplyTo());
        request.setTo(toTopic);
        request.setFrom(receivedMessage.getFrom());
        
        request.setAuditTrailInformation(receivedMessage.getAuditTrailInformation());
        request.setFileIDs(receivedMessage.getFileIDs());
        
        return request;
    }
    
    public IdentifyPillarsForGetFileIDsResponse createIdentifyPillarsForGetFileIDsResponse(
            IdentifyPillarsForGetFileIDsRequest receivedIdentifyRequestMessage,
            String pillarId, String pillarDestinationId) {
        IdentifyPillarsForGetFileIDsResponse identifyPillarsForGetFileIdsResponse = new IdentifyPillarsForGetFileIDsResponse();
        initializeMessageDetails(identifyPillarsForGetFileIdsResponse);
        setResponseDetails(identifyPillarsForGetFileIdsResponse, receivedIdentifyRequestMessage, pillarId, pillarDestinationId);
        identifyPillarsForGetFileIdsResponse.setPillarID(pillarId);
        identifyPillarsForGetFileIdsResponse.setFileIDs(receivedIdentifyRequestMessage.getFileIDs());
        identifyPillarsForGetFileIdsResponse.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        return identifyPillarsForGetFileIdsResponse;
    }
    
    public GetFileIDsRequest createGetFileIDsRequest(String pillarId, String toTopic, String from) {
        GetFileIDsRequest getFileIDsRequest = new GetFileIDsRequest();
        getFileIDsRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        initializeMessageDetails(getFileIDsRequest);
        getFileIDsRequest.setPillarID(pillarId);
        getFileIDsRequest.setTo(toTopic);
        getFileIDsRequest.setFrom(from);
        
        return getFileIDsRequest;
    }
    public GetFileIDsRequest createGetFileIDsRequest(GetFileIDsRequest receivedGetFileIDsRequest,
            String pillarId, String toTopic) {
        GetFileIDsRequest getFileIDsRequest = createGetFileIDsRequest(pillarId, toTopic, 
                receivedGetFileIDsRequest.getFrom());
        getFileIDsRequest.setCorrelationID(receivedGetFileIDsRequest.getCorrelationID());
        getFileIDsRequest.setReplyTo(receivedGetFileIDsRequest.getReplyTo());
        getFileIDsRequest.setFileIDs(receivedGetFileIDsRequest.getFileIDs());
        getFileIDsRequest.setResultAddress(receivedGetFileIDsRequest.getResultAddress());
        getFileIDsRequest.setAuditTrailInformation(receivedGetFileIDsRequest.getAuditTrailInformation());
        return getFileIDsRequest;
    }

    public GetFileIDsProgressResponse createGetFileIDsProgressResponse(
            GetFileIDsRequest receivedMessage, String pillarId, String pillarDestination) {
        GetFileIDsProgressResponse getFileIDsProgressResponse = new GetFileIDsProgressResponse();
        initializeMessageDetails(getFileIDsProgressResponse);
        getFileIDsProgressResponse.setTo(receivedMessage.getReplyTo());
        getFileIDsProgressResponse.setCorrelationID(receivedMessage.getCorrelationID());
        getFileIDsProgressResponse.setCollectionID(receivedMessage.getCollectionID());
        getFileIDsProgressResponse.setReplyTo(pillarDestination);
        getFileIDsProgressResponse.setPillarID(pillarId);
        getFileIDsProgressResponse.setFileIDs(receivedMessage.getFileIDs());
        getFileIDsProgressResponse.setResponseInfo(PROGRESS_INFO_DEFAULT);
        getFileIDsProgressResponse.setFrom(pillarId);
        getFileIDsProgressResponse.setResultAddress(receivedMessage.getResultAddress());
        
        return getFileIDsProgressResponse;
    }

    /**
     * MISSING:
     * 
     * - getFileIDsFinalResponse.setAuditTrailInformation(null);
     * - getFileIDsFinalResponse.setResultingFileIDs(null);
     *  
     * @param receivedGetFileIDsRequest
     * @param pillarId
     * @param pillarDestinationId
     * @return
     */
    public GetFileIDsFinalResponse createGetFileIDsFinalResponse(
            GetFileIDsRequest receivedGetFileIDsRequest, String pillarId, String pillarDestinationId) {
        GetFileIDsFinalResponse getFileIDsFinalResponse = new GetFileIDsFinalResponse();
        initializeMessageDetails(getFileIDsFinalResponse);
        getFileIDsFinalResponse.setTo(receivedGetFileIDsRequest.getReplyTo());
        getFileIDsFinalResponse.setCorrelationID(receivedGetFileIDsRequest.getCorrelationID());
        getFileIDsFinalResponse.setCollectionID(receivedGetFileIDsRequest.getCollectionID());
        getFileIDsFinalResponse.setReplyTo(pillarDestinationId);
        getFileIDsFinalResponse.setPillarID(pillarId);
        getFileIDsFinalResponse.setFileIDs(receivedGetFileIDsRequest.getFileIDs());
        getFileIDsFinalResponse.setResponseInfo(createCompleteResponseInfo());
        getFileIDsFinalResponse.setFrom(pillarId);

        return getFileIDsFinalResponse;
    }
}
