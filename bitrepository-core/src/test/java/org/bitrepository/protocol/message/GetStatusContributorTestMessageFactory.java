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
