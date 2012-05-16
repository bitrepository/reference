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
package org.bitrepository.protocol.message;

import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;

public class TestGetStatusMessageFactory extends ClientTestMessageFactory {
    public TestGetStatusMessageFactory(String collectionID) {
        super(collectionID);
    }

    public IdentifyContributorsForGetStatusRequest createIdentifyContributorsForGetStatusRequest(
            IdentifyContributorsForGetStatusRequest request, String componentID,
            String toDestination) {
        IdentifyContributorsForGetStatusRequest message = new IdentifyContributorsForGetStatusRequest();
        initializeMessageDetails(message);
        message.setCorrelationID(request.getCorrelationID());
        message.setReplyTo(request.getReplyTo());
        message.setTo(toDestination);
        message.setFrom(componentID);
        return message;
    }

    public IdentifyContributorsForGetStatusResponse createIdentifyContributorsForGetStatusResponse(
            IdentifyContributorsForGetStatusRequest request, String componentID, String replyTo) {
        IdentifyContributorsForGetStatusResponse response = new IdentifyContributorsForGetStatusResponse();
        setResponseDetails(response, request, componentID,  replyTo);
        response.setResponseInfo(IDENTIFY_INFO_DEFAULT);
        response.setContributor(componentID);
        return response;
    }
    
    public GetStatusFinalResponse createGetStatusFinalResponse(GetStatusRequest request,
            String componentID, String replyTo, ResultingStatus status) {
        GetStatusFinalResponse response = new GetStatusFinalResponse();
        setResponseDetails(response, request, componentID, replyTo);
        response.setResponseInfo(FINAL_INFO_DEFAULT);
        response.setContributor(componentID);
        response.setResultingStatus(status);
        return response;        
    }
    
    public GetStatusRequest createGetStatusRequest(GetStatusRequest request, String componentID, 
            String toDestination, String from) {
        GetStatusRequest message = new GetStatusRequest();
        initializeMessageDetails(message);
        message.setCorrelationID(request.getCorrelationID());
        message.setReplyTo(request.getReplyTo());
        message.setTo(toDestination);
        message.setContributor(componentID);
        message.setFrom(from);
        message.setAuditTrailInformation("");
        return message;
    }
    

}
