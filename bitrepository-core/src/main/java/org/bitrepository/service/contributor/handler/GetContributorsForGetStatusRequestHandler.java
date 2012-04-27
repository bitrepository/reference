package org.bitrepository.service.contributor.handler;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.StatusCode;
import org.bitrepository.bitrepositoryelements.StatusInfo;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.service.contributor.ContributorContext;

public class GetContributorsForGetStatusRequestHandler extends AbstractRequestHandler {
    private final ContributorContext context;

    public GetContributorsForGetStatusRequestHandler(ContributorContext context) {
        this.context = context;
    }

    @Override
    public Class getRequestClass() {
        return IdentifyContributorsForGetStatusRequest.class;
    }

    @Override
    public void processRequest(MessageRequest request) {
        IdentifyContributorsForGetStatusResponse response = new IdentifyContributorsForGetStatusResponse();

        ResponseInfo info = new ResponseInfo();
        info.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        info.setResponseText("Ready to service status request");
        response.setResponseInfo(info);
        response.setContributor(context.getComponentID());

        dispatchResponse(request, response);
    }

    @Override
    public MessageResponse generateFailedResponse() {
        return new GetStatusFinalResponse();
    }

    protected StatusInfo getStatus() {
        StatusInfo status = new StatusInfo();
        status.setStatusCode(StatusCode.OK);
        status.setStatusText("Ok");
        return status;
    }

    @Override
    protected ContributorContext getContext() {
        return context;
    }
}
