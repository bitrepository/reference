/*
 * #%L
 * Bitrepository Core
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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;

public class GetStatusContributorTestMessageFactory extends ContributorTestMessageFactory {
    public GetStatusContributorTestMessageFactory(String collectionID,
                                                  String collectionDestination,
                                                  String contributorID,
                                                  String contributorDestination,
                                                  String clientID,
                                                  String clientDestination) {
        super(collectionID, collectionDestination, contributorID, contributorDestination, clientID, clientDestination);
    }


    public IdentifyContributorsForGetStatusRequest createIdentifyContributorsForGetStatusRequest() {
        IdentifyContributorsForGetStatusRequest request = new IdentifyContributorsForGetStatusRequest();
        initializeIdentifyRequestDetails(request);
        return request;
    }

    public IdentifyContributorsForGetStatusResponse createExpectedIdentifyContributorsForGetStatusResponse(
            IdentifyContributorsForGetStatusResponse received) {
        IdentifyContributorsForGetStatusResponse response = new IdentifyContributorsForGetStatusResponse();
        initializeMessageDetails(response);
        response.setCorrelationID(received.getCorrelationID());
        response.setTimeToDeliver(received.getTimeToDeliver());
        response.setContributor(contributorID);
        response.setTo(clientDestination);
        response.setReplyTo(contributorDestination);
        response.setFrom(contributorID);
        ResponseInfo info = new ResponseInfo();
        info.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        info.setResponseText(received.getResponseInfo().getResponseText());
        response.setResponseInfo(info);
        return response;
    }

    public GetStatusFinalResponse createExpectedGetStatusFinalResponse(
            GetStatusFinalResponse received) {
        GetStatusFinalResponse response = new GetStatusFinalResponse();
        initializeMessageDetails(response);
        response.setCorrelationID(received.getCorrelationID());
        response.setContributor(contributorID);
        response.setTo(clientDestination);
        response.setReplyTo(contributorDestination);
        response.setFrom(contributorID);
        ResponseInfo info = new ResponseInfo();
        info.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        info.setResponseText(received.getResponseInfo().getResponseText());
        response.setResponseInfo(info);
        response.setResultingStatus(received.getResultingStatus());
        return response;
    }

    public GetStatusRequest createGetStatusRequest(String correlationID) {
        GetStatusRequest request = new GetStatusRequest();
        initializeRequestDetails(request, correlationID);
        return request;
    }

    public GetStatusProgressResponse createGetStatusProgressResponse(String contributorId, String correlationId,
                                                                     String replyTo, ResponseInfo responseInfo, String toTopic) {
        GetStatusProgressResponse res = new GetStatusProgressResponse();

        return res;
    }
}
