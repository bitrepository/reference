/*
 * #%L
 * Bitrepository Access Client
 * 
 * $Id$
 * $HeadURL$
 * %%
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
package org.bitrepository.access;

import org.bitrepository.bitrepositoryelements.ProgressResponseInfo;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.protocol.TestMessageFactory;

public class TestGetFileMessageFactory extends TestMessageFactory {
    protected final String defaultSlaID;
    protected final String defaultClientDestination;
    protected final String defaultSlaDestination;

    public TestGetFileMessageFactory(String defaultSlaID,
            String defaultClientDestination,
            String defaultSlaDestination) {
        super();
        this.defaultSlaID = defaultSlaID;
        this.defaultClientDestination = defaultClientDestination;
        this.defaultSlaDestination = defaultSlaDestination;
    }

    public IdentifyPillarsForGetFileRequest createIdentifyPillarsForGetFileIDsRequest(
            IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage) {
        IdentifyPillarsForGetFileRequest identifyPillarsForGetFileRequest = createIdentifyPillarsForGetFileRequest();
        identifyPillarsForGetFileRequest.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        return identifyPillarsForGetFileRequest;
    }

    public IdentifyPillarsForGetFileRequest createIdentifyPillarsForGetFileRequest() {
        IdentifyPillarsForGetFileRequest identifyPillarsForGetFileRequest = new IdentifyPillarsForGetFileRequest();
        identifyPillarsForGetFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyPillarsForGetFileRequest.setFileID(FILE_ID_DEFAULT);
        identifyPillarsForGetFileRequest.setMinVersion(VERSION_DEFAULT);
        identifyPillarsForGetFileRequest.setReplyTo(defaultClientDestination);
        identifyPillarsForGetFileRequest.setSlaID(defaultSlaID);
        identifyPillarsForGetFileRequest.setVersion(VERSION_DEFAULT);
        return identifyPillarsForGetFileRequest;
    }

    public IdentifyPillarsForGetFileResponse createIdentifyPillarsForGetFileResponse(
            IdentifyPillarsForGetFileRequest receivedIdentifyRequestMessage,
            String pillarId, 
            String pillarDestinationId) {
        IdentifyPillarsForGetFileResponse identifyPillarsForGetFileRequest = new IdentifyPillarsForGetFileResponse();
        identifyPillarsForGetFileRequest.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        identifyPillarsForGetFileRequest.setSlaID(receivedIdentifyRequestMessage.getSlaID());
        identifyPillarsForGetFileRequest.setReplyTo(pillarDestinationId);
        identifyPillarsForGetFileRequest.setPillarID(pillarId);
        identifyPillarsForGetFileRequest.setFileID(receivedIdentifyRequestMessage.getFileID());
        identifyPillarsForGetFileRequest.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        identifyPillarsForGetFileRequest.setVersion(VERSION_DEFAULT);
        identifyPillarsForGetFileRequest.setMinVersion(VERSION_DEFAULT);
        return identifyPillarsForGetFileRequest;
    }
  
    public GetFileRequest createGetFileRequest() {
        GetFileRequest getFileRequest = new GetFileRequest();
        getFileRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        getFileRequest.setFileID(FILE_ID_DEFAULT);
        getFileRequest.setMinVersion(VERSION_DEFAULT);
        getFileRequest.setReplyTo(defaultClientDestination);
        getFileRequest.setSlaID(defaultSlaID);
        getFileRequest.setVersion(VERSION_DEFAULT);
        return getFileRequest;
    }
    public GetFileRequest createGetFileRequest(GetFileRequest receivedGetFileRequest) {
        GetFileRequest getFileRequest = createGetFileRequest();
        getFileRequest.setCorrelationID(receivedGetFileRequest.getCorrelationID());
        return getFileRequest;
    }

    public GetFileProgressResponse createGetFileProgressResponse(
            GetFileRequest receivedGetFileRequest, String pillarId, String pillarDestinationId) {
        GetFileProgressResponse getFileProgressResponse = new GetFileProgressResponse();
        getFileProgressResponse.setCorrelationID(receivedGetFileRequest.getCorrelationID());
        getFileProgressResponse.setSlaID(receivedGetFileRequest.getSlaID());
        getFileProgressResponse.setReplyTo(pillarDestinationId);
        getFileProgressResponse.setPillarID(pillarId);
        getFileProgressResponse.setFileID(receivedGetFileRequest.getFileID());
        getFileProgressResponse.setProgressResponseInfo(PROGRESS_INFO_DEFAULT);
        getFileProgressResponse.setVersion(VERSION_DEFAULT);
        getFileProgressResponse.setMinVersion(VERSION_DEFAULT);
        return getFileProgressResponse;
    }

    public GetFileFinalResponse createGetFileFinalResponse(
            GetFileRequest receivedGetFileRequest, String pillarId, String pillarDestinationId) {
        GetFileFinalResponse getFileFinalResponse = new GetFileFinalResponse();
        getFileFinalResponse.setCorrelationID(receivedGetFileRequest.getCorrelationID());
        getFileFinalResponse.setSlaID(receivedGetFileRequest.getSlaID());
        getFileFinalResponse.setReplyTo(pillarDestinationId);
        getFileFinalResponse.setPillarID(pillarId);
        getFileFinalResponse.setFileID(receivedGetFileRequest.getFileID());
        getFileFinalResponse.setFinalResponseInfo(FINAL_INFO_DEFAULT);
        getFileFinalResponse.setVersion(VERSION_DEFAULT);
        getFileFinalResponse.setMinVersion(VERSION_DEFAULT);
        return getFileFinalResponse;
    }
}
