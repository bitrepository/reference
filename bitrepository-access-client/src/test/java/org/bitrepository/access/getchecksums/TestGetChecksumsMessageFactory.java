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
package org.bitrepository.access.getchecksums;

import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.protocol.TestMessageFactory;

/**
 * Constructs the GetFile specific messages.
 * 
 * ToDo based on example messages.
 */
public class TestGetChecksumsMessageFactory extends TestMessageFactory {
    protected final String slaID;

    public TestGetChecksumsMessageFactory(String slaID) {
        super();
        this.slaID = slaID;
    }

    public IdentifyPillarsForGetChecksumsRequest createIdentifyPillarsForGetChecksumsRequest() {
        IdentifyPillarsForGetChecksumsRequest identifyPillarsForGetChecksumsRequest = new IdentifyPillarsForGetChecksumsRequest();
        identifyPillarsForGetChecksumsRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        identifyPillarsForGetChecksumsRequest.setMinVersion(VERSION_DEFAULT);
        identifyPillarsForGetChecksumsRequest.setCollectionID(slaID);
        identifyPillarsForGetChecksumsRequest.setVersion(VERSION_DEFAULT);
        
        return identifyPillarsForGetChecksumsRequest;
    }
    
    /**
     * Creates a reference <code>IdentifyPillarsForGetFileRequest</code> message for comparing against a received 
     * request.  
     * @param receivedIdentifyRequestMessage The request to compare against. Any attributes which can't be determined 
     * prior to receiving the request are copied from the supplied request to the returned message. Attributes copied 
     * include <code>correlationId</code>.
     * @return A reference <code>IdentifyPillarsForGetFileRequest</code> message.
     */
    public IdentifyPillarsForGetChecksumsRequest createIdentifyPillarsForGetChecksumsRequest(
            IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage, String toTopic) {
        IdentifyPillarsForGetChecksumsRequest identifyPillarsForGetChecksumsRequest = createIdentifyPillarsForGetChecksumsRequest();
        identifyPillarsForGetChecksumsRequest.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        identifyPillarsForGetChecksumsRequest.setReplyTo(receivedIdentifyRequestMessage.getReplyTo());
        identifyPillarsForGetChecksumsRequest.setTo(toTopic);
        
        identifyPillarsForGetChecksumsRequest.setAuditTrailInformation(receivedIdentifyRequestMessage.getAuditTrailInformation());
        identifyPillarsForGetChecksumsRequest.setChecksumRequestForExistingFile(
        		receivedIdentifyRequestMessage.getChecksumRequestForExistingFile());
        identifyPillarsForGetChecksumsRequest.setFileIDs(receivedIdentifyRequestMessage.getFileIDs());
        
        return identifyPillarsForGetChecksumsRequest;
    }

    public IdentifyPillarsForGetChecksumsResponse createIdentifyPillarsForGetChecksumsResponse(
            IdentifyPillarsForGetChecksumsRequest receivedIdentifyRequestMessage,
            String pillarId, String pillarDestinationId) {
        IdentifyPillarsForGetChecksumsResponse identifyPillarsForGetChecksumsRequest = new IdentifyPillarsForGetChecksumsResponse();
        identifyPillarsForGetChecksumsRequest.setTo(receivedIdentifyRequestMessage.getReplyTo());
        identifyPillarsForGetChecksumsRequest.setCorrelationID(receivedIdentifyRequestMessage.getCorrelationID());
        identifyPillarsForGetChecksumsRequest.setCollectionID(
                receivedIdentifyRequestMessage.getCollectionID());
        identifyPillarsForGetChecksumsRequest.setReplyTo(pillarDestinationId);
        identifyPillarsForGetChecksumsRequest.setPillarID(pillarId);
        identifyPillarsForGetChecksumsRequest.setFileIDs(receivedIdentifyRequestMessage.getFileIDs());
        identifyPillarsForGetChecksumsRequest.setTimeToDeliver(TIME_TO_DELIVER_DEFAULT);
        identifyPillarsForGetChecksumsRequest.setVersion(VERSION_DEFAULT);
        identifyPillarsForGetChecksumsRequest.setMinVersion(VERSION_DEFAULT);
        identifyPillarsForGetChecksumsRequest.setResponseInfo(IDENTIFY_INFO_DEFAULT);
        return identifyPillarsForGetChecksumsRequest;
    }
  
    public GetChecksumsRequest createGetChecksumsRequest(String pillarId, String toTopic) {
        GetChecksumsRequest getChecksumsRequest = new GetChecksumsRequest();
        getChecksumsRequest.setCorrelationID(CORRELATION_ID_DEFAULT);
        getChecksumsRequest.setMinVersion(VERSION_DEFAULT);
        getChecksumsRequest.setVersion(VERSION_DEFAULT);
        getChecksumsRequest.setPillarID(pillarId);
        getChecksumsRequest.setCollectionID(slaID);
        getChecksumsRequest.setTo(toTopic);
        return getChecksumsRequest;
    }
    public GetChecksumsRequest createGetChecksumsRequest(GetChecksumsRequest receivedGetChecksumsRequest,
            String pillarId, String toTopic) {
        GetChecksumsRequest getChecksumsRequest = createGetChecksumsRequest(pillarId, toTopic);
        getChecksumsRequest.setCorrelationID(receivedGetChecksumsRequest.getCorrelationID());
        getChecksumsRequest.setReplyTo(receivedGetChecksumsRequest.getReplyTo());
        getChecksumsRequest.setChecksumRequestForExistingFile(receivedGetChecksumsRequest.getChecksumRequestForExistingFile());
        getChecksumsRequest.setFileIDs(receivedGetChecksumsRequest.getFileIDs());
        getChecksumsRequest.setResultAddress(receivedGetChecksumsRequest.getResultAddress());
        getChecksumsRequest.setAuditTrailInformation(receivedGetChecksumsRequest.getAuditTrailInformation());
        return getChecksumsRequest;
    }

    public GetChecksumsProgressResponse createGetChecksumsProgressResponse(
            GetChecksumsRequest receivedGetChecksumsRequest, String pillarId, String pillarDestinationId) {
        GetChecksumsProgressResponse getChecksumsProgressResponse = new GetChecksumsProgressResponse();
        getChecksumsProgressResponse.setTo(receivedGetChecksumsRequest.getReplyTo());
        getChecksumsProgressResponse.setCorrelationID(receivedGetChecksumsRequest.getCorrelationID());
        getChecksumsProgressResponse.setCollectionID(receivedGetChecksumsRequest.getCollectionID());
        getChecksumsProgressResponse.setReplyTo(pillarDestinationId);
        getChecksumsProgressResponse.setPillarID(pillarId);
        getChecksumsProgressResponse.setFileIDs(receivedGetChecksumsRequest.getFileIDs());
        getChecksumsProgressResponse.setResponseInfo(PROGRESS_INFO_DEFAULT);
        getChecksumsProgressResponse.setVersion(VERSION_DEFAULT);
        getChecksumsProgressResponse.setMinVersion(VERSION_DEFAULT);
        getChecksumsProgressResponse.setResultAddress(receivedGetChecksumsRequest.getResultAddress());
        
        return getChecksumsProgressResponse;
    }

    /**
     * MISSING:
     * 
     * - getChecksumsFinalResponse.setAuditTrailInformation(null);
     * - getChecksumsFinalResponse.setFileChecksumSpec(null);
     * - getChecksumsFinalResponse.setResultingChecksums(null);
     *  
     * @param receivedGetChecksumsRequest
     * @param pillarId
     * @param pillarDestinationId
     * @return
     */
    public GetChecksumsFinalResponse createGetChecksumsFinalResponse(
            GetChecksumsRequest receivedGetChecksumsRequest, String pillarId, String pillarDestinationId) {
        GetChecksumsFinalResponse getChecksumsFinalResponse = new GetChecksumsFinalResponse();
        getChecksumsFinalResponse.setTo(receivedGetChecksumsRequest.getReplyTo());
        getChecksumsFinalResponse.setCorrelationID(receivedGetChecksumsRequest.getCorrelationID());
        getChecksumsFinalResponse.setCollectionID(receivedGetChecksumsRequest.getCollectionID());
        getChecksumsFinalResponse.setReplyTo(pillarDestinationId);
        getChecksumsFinalResponse.setPillarID(pillarId);
        //getChecksumsFinalResponse.setFileIDs(receivedGetChecksumsRequest.getFileIDs());
        getChecksumsFinalResponse.setResponseInfo(FINAL_INFO_DEFAULT);
        getChecksumsFinalResponse.setVersion(VERSION_DEFAULT);
        getChecksumsFinalResponse.setMinVersion(VERSION_DEFAULT);
        
        
        return getChecksumsFinalResponse;
    }
}
