package org.bitrepository.protocol.message;

import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;

public class GetStatusContributorTestMessageFactory extends ContributorTestMessageFactory {
    public GetStatusContributorTestMessageFactory(String collectionID,
                                                  String contributorID,
                                                  String collectionDestination,
                                                  String clientID,
                                                  String clientDestination) {
        super(collectionID, contributorID, collectionDestination, clientID, clientDestination);
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
        response.setFrom(contributorID);
        response.setTo(clientID);
        response.setReplyTo(clientDestination);
        return response;
    }

    public GetStatusRequest createGetStatusRequest(String auditTrail, String contributorId, String correlationId,
                                                   String from, String replyTo, String toTopic) {
        GetStatusRequest res = new GetStatusRequest();

        return res;
    }

    public GetStatusProgressResponse createGetStatusProgressResponse(String contributorId, String correlationId,
                                                                     String replyTo, ResponseInfo responseInfo, String toTopic) {
        GetStatusProgressResponse res = new GetStatusProgressResponse();

        return res;
    }

    public GetStatusFinalResponse createGetStatusFinalResponse(String contributorId, String correlationId,
                                                               String replyTo, ResponseInfo responseInfo, ResultingStatus status, String toTopic) {
        GetStatusFinalResponse res = new GetStatusFinalResponse();

        return res;
    }
}
