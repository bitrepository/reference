package org.bitrepository.service.contributor.handler;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingStatus;
import org.bitrepository.bitrepositoryelements.StatusCode;
import org.bitrepository.bitrepositoryelements.StatusInfo;
import org.bitrepository.bitrepositorymessages.GetStatusFinalResponse;
import org.bitrepository.bitrepositorymessages.GetStatusProgressResponse;
import org.bitrepository.bitrepositorymessages.GetStatusRequest;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ResponseInfoUtils;
import org.bitrepository.service.contributor.ContributorContext;

/**
 * Class for handling the GetStatusRequest messages.
 */
public class GetStatusRequestHandler extends AbstractRequestHandler<GetStatusRequest> {
    /**
     * Constructor.
     * @param context The context for the contributor.
     */
    public GetStatusRequestHandler(ContributorContext context) {
        super(context);
    }

    @Override
    public Class<GetStatusRequest> getRequestClass() {
        return GetStatusRequest.class;
    }

    @Override
    public void processRequest(GetStatusRequest request) {
        validateMessage(request);
        responseProgress(request);
        sendFinalSuccess(request);
    }
    
    @Override
    public GetStatusFinalResponse generateFailedResponse(GetStatusRequest request) {
        GetStatusFinalResponse response = createFinalResponse(request);
        response.setResultingStatus(failedStatus());
        return response;
    }
    
    /**
     * Validates the content of the message.
     * @param request The message to validate.
     */
    private void validateMessage(GetStatusRequest request) {
        if(!request.getContributor().equals(getContext().getComponentID())) {
            throw new IllegalArgumentException("Illegal argument exception. Expected: " + getContext().getComponentID()
                    + ", but it was " + request.getContributor());
        }
    }

    /**
     * Creates and sends the progress response.
     * @param request The request to base the response upon.
     */
    private void responseProgress(GetStatusRequest request) {
        GetStatusProgressResponse response = new GetStatusProgressResponse();
        populateResponse(request, response);
        response.setResponseInfo(ResponseInfoUtils.getInitialProgressResponse());
        response.setContributor(getContext().getComponentID());
        getContext().getDispatcher().sendMessage(response);
    }
    
    /**
     * Creates and sends the final response telling about success.
     * @param request The request to base the response upon.
     */
    private void sendFinalSuccess(GetStatusRequest request) {
        GetStatusFinalResponse response = createFinalResponse(request);
        response.setResultingStatus(getStatus());

        ResponseInfo info = new ResponseInfo();
        info.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        info.setResponseText("Returning status");
        response.setResponseInfo(info);

        getContext().getDispatcher().sendMessage(response);
    }
    
    /**
     * Creates the final response for the status message.
     * Missing:
     * <br/> ResponseInfo
     * <br/> ResultingStatus
     * 
     * @param request The request to base the response upon.
     * @return The final response.
     */
    private GetStatusFinalResponse createFinalResponse(GetStatusRequest request) {
        GetStatusFinalResponse response = new GetStatusFinalResponse();
        populateResponse(request, response);
        response.setContributor(getContext().getComponentID());

        return response;
    }

    /**
     * @return Positive status info for the contributor.
     */
    protected ResultingStatus getStatus() {
        ResultingStatus res = new ResultingStatus();
        
        StatusInfo status = new StatusInfo();
        status.setStatusCode(StatusCode.OK);
        status.setStatusText("Ok");
        
        res.setStatusInfo(status);
        res.setStatusTimestamp(CalendarUtils.getNow());
        return res;
    }
    
    /**
     * @return Negative status info for the contributor.
     */
    protected ResultingStatus failedStatus() {
        ResultingStatus res = new ResultingStatus();
        
        StatusInfo status = new StatusInfo();
        status.setStatusCode(StatusCode.ERROR);
        status.setStatusText("Unexpected error.");
        
        res.setStatusInfo(status);
        res.setStatusTimestamp(CalendarUtils.getNow());
        return res;
    }
}
