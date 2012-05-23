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
package org.bitrepository.access.getfile;

import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.protocol.message.ClientTestMessageFactory;

/**
 * Constructs the GetFile specific messages.
 * 
 * ToDo based on example messages.
 */
public class TestGetFileMessageFactory extends ClientTestMessageFactory {

    public TestGetFileMessageFactory(String collectionID) {
        super(collectionID);
    }

    public IdentifyPillarsForGetFileRequest createIdentifyPillarsForGetFileRequest() {
        IdentifyPillarsForGetFileRequest identifyPillarsForGetFileRequest = new IdentifyPillarsForGetFileRequest();
        identifyPillarsForGetFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyPillarsForGetFileRequest.setFileID(FILE_ID_DEFAULT);
        identifyPillarsForGetFileRequest.setMinVersion(VERSION_DEFAULT);
        identifyPillarsForGetFileRequest.setCollectionID(collectionID);
        identifyPillarsForGetFileRequest.setVersion(VERSION_DEFAULT);
        return identifyPillarsForGetFileRequest;
    }
    
    /**
     * Creates a reference <code>IdentifyPillarsForGetFileRequest</code> message for comparing against a received 
     * request.  
     * @param receivedIdentifyRequestMessage The request to compare against. Any attributes which can't be determined 
     * prior to receiving the request are copied from the supplied request to the returned message. Attributes copied 
     * include <code>correlationId</code>.
     * @return A reference <code>IdentifyPillarsForGetFileRequest</code> message.
     */
    public IdentifyPillarsForGetFileRequest createIdentifyPillarsForGetFileRequest(
            IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage, String toTopic, String from) {
        IdentifyPillarsForGetFileRequest identifyPillarsForGetFileRequest = createIdentifyPillarsForGetFileRequest();
        identifyPillarsForGetFileRequest.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        identifyPillarsForGetFileRequest.setReplyTo(receivedIdentifyRequestMessage.getReplyTo());
        identifyPillarsForGetFileRequest.setTo(toTopic);
        identifyPillarsForGetFileRequest.setFrom(from);
        return identifyPillarsForGetFileRequest;
    }

    public IdentifyPillarsForGetFileResponse createIdentifyPillarsForGetFileResponse(
            IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage,
            String pillarId, 
            String pillarDestinationId) {
        IdentifyPillarsForGetFileResponse identifyPillarsForGetFileResponse = new IdentifyPillarsForGetFileResponse();
        identifyPillarsForGetFileResponse.setTo(receivedIdentifyRequestMessage.getReplyTo());
        identifyPillarsForGetFileResponse.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        identifyPillarsForGetFileResponse.setCollectionID(
        		receivedIdentifyRequestMessage.getCollectionID());
        identifyPillarsForGetFileResponse.setReplyTo(pillarDestinationId);
        identifyPillarsForGetFileResponse.setPillarID(pillarId);
        identifyPillarsForGetFileResponse.setFileID(receivedIdentifyRequestMessage.getFileID());
        identifyPillarsForGetFileResponse.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        identifyPillarsForGetFileResponse.setVersion(VERSION_DEFAULT);
        identifyPillarsForGetFileResponse.setMinVersion(VERSION_DEFAULT);
        identifyPillarsForGetFileResponse.setFrom(pillarId);
        ResponseInfo identifyResponseInfo = new ResponseInfo();
        identifyResponseInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE); 
        identifyResponseInfo.setResponseText("Pillar " + pillarId + " will be able to begin devlivery of file " 
        + receivedIdentifyRequestMessage.getFileID() + " in " + TIME_TO_DELIVER_DEFAULT);
        identifyPillarsForGetFileResponse.setResponseInfo(identifyResponseInfo);
        return identifyPillarsForGetFileResponse;
    }
  
    public GetFileRequest createGetFileRequest(String pillarId, String toTopic, String from) {
        GetFileRequest getFileRequest = new GetFileRequest();
        getFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        getFileRequest.setFileID(FILE_ID_DEFAULT);
        getFileRequest.setMinVersion(VERSION_DEFAULT);
        getFileRequest.setVersion(VERSION_DEFAULT);
        getFileRequest.setPillarID(pillarId);
        getFileRequest.setCollectionID(collectionID);
        getFileRequest.setTo(toTopic);
        getFileRequest.setFrom(from);
        return getFileRequest;
    }
    public GetFileRequest createGetFileRequest(GetFileRequest receivedGetFileRequest,
            FilePart filePart, String pillarId, String toTopic, String from) {
        GetFileRequest getFileRequest = createGetFileRequest(pillarId, toTopic, from);
        getFileRequest.setCorrelationID(receivedGetFileRequest.getCorrelationID());
        getFileRequest.setFileAddress(receivedGetFileRequest.getFileAddress());
        getFileRequest.setReplyTo(receivedGetFileRequest.getReplyTo());
        getFileRequest.setFilePart(filePart);
        return getFileRequest;
    }

    public GetFileProgressResponse createGetFileProgressResponse(
            GetFileRequest receivedGetFileRequest, String pillarId, String pillarDestinationId) {
        GetFileProgressResponse getFileProgressResponse = new GetFileProgressResponse();
        getFileProgressResponse.setTo(receivedGetFileRequest.getReplyTo());
        getFileProgressResponse.setCorrelationID(receivedGetFileRequest.getCorrelationID());
        getFileProgressResponse.setCollectionID(receivedGetFileRequest.getCollectionID());
        getFileProgressResponse.setReplyTo(pillarDestinationId);
        getFileProgressResponse.setPillarID(pillarId);
        getFileProgressResponse.setFileID(receivedGetFileRequest.getFileID());
        getFileProgressResponse.setResponseInfo(PROGRESS_INFO_DEFAULT);
        getFileProgressResponse.setVersion(VERSION_DEFAULT);
        getFileProgressResponse.setMinVersion(VERSION_DEFAULT);
        getFileProgressResponse.setFrom(pillarId);
        getFileProgressResponse.setFileAddress(receivedGetFileRequest.getFileAddress());
        return getFileProgressResponse;
    }

    public GetFileFinalResponse createGetFileFinalResponse(
            GetFileRequest receivedGetFileRequest, String pillarId, String pillarDestinationId) {
        GetFileFinalResponse getFileFinalResponse = new GetFileFinalResponse();
        getFileFinalResponse.setTo(receivedGetFileRequest.getReplyTo());
        getFileFinalResponse.setCorrelationID(receivedGetFileRequest.getCorrelationID());
        getFileFinalResponse.setCollectionID(receivedGetFileRequest.getCollectionID());
        getFileFinalResponse.setReplyTo(pillarDestinationId);
        getFileFinalResponse.setPillarID(pillarId);
        getFileFinalResponse.setFileID(receivedGetFileRequest.getFileID());
        getFileFinalResponse.setResponseInfo(FINAL_INFO_DEFAULT);
        getFileFinalResponse.setVersion(VERSION_DEFAULT);
        getFileFinalResponse.setMinVersion(VERSION_DEFAULT);
        getFileFinalResponse.setFrom(pillarId);
        getFileFinalResponse.setFileAddress(receivedGetFileRequest.getFileAddress());
        return getFileFinalResponse;
    }
}
