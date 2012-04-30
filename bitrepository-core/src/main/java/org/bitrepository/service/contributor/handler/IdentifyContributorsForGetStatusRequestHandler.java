package org.bitrepository.service.contributor.handler;

import org.bitrepository.bitrepositoryelements.StatusCode;
import org.bitrepository.bitrepositoryelements.StatusInfo;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetStatusResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.ResponseInfoUtils;
import org.bitrepository.service.contributor.ContributorContext;

/**
 * Handler for the IdentifyContributorsForGetStatusRequest.
 */
public class IdentifyContributorsForGetStatusRequestHandler 
        extends AbstractRequestHandler<IdentifyContributorsForGetStatusRequest> {
    
    /**
     * Constructor.
     * @param context The context for the contributor.
     */
    public IdentifyContributorsForGetStatusRequestHandler(ContributorContext context) {
        super(context);
    }

    @Override
    public Class<IdentifyContributorsForGetStatusRequest> getRequestClass() {
        return IdentifyContributorsForGetStatusRequest.class;
    }

    @Override
    public void processRequest(IdentifyContributorsForGetStatusRequest request) {
        IdentifyContributorsForGetStatusResponse response = new IdentifyContributorsForGetStatusResponse();
        populateResponse(request, response);
        response.setContributor(getContext().getComponentID());
        response.setResponseInfo(ResponseInfoUtils.getPositiveIdentification());

        getContext().getDispatcher().sendMessage(response);
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyContributorsForGetStatusRequest request) {
        GetStatusFinalResponse response = new GetStatusFinalResponse();
        populateResponse(request, response);
        return response;
    }

    /**
     * Creates the default status info for the response.
     * @return The status info for the response.
     */
    protected StatusInfo getStatus() {
        StatusInfo status = new StatusInfo();
        status.setStatusCode(StatusCode.OK);
        status.setStatusText("Ok");
        return status;
    }
}
