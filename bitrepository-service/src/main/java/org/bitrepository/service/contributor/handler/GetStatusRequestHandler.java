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
import org.bitrepository.protocol.MessageVersionValidator;
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
        if(!request.getContributor().equals(getContext().getSettings().getComponentID())) {
            throw new IllegalArgumentException("Illegal argument exception. Expected: " + getContext().getSettings().getComponentID()
                    + ", but it was " + request.getContributor());
        }
    }

    /**
     * Creates and sends the progress response.
     * @param request The request to base the response upon.
     */
    private void responseProgress(GetStatusRequest request) {
        GetStatusProgressResponse response = new GetStatusProgressResponse();
        response.setResponseInfo(ResponseInfoUtils.getInitialProgressResponse());
        response.setContributor(getContext().getSettings().getComponentID());
        getContext().getResponseDispatcher().dispatchResponse(response, request);
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

        getContext().getResponseDispatcher().dispatchResponse(response, request);
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
        response.setContributor(getContext().getSettings().getComponentID());
        return response;
    }

    /**
     * @return Positive status info for the contributor.
     */
    protected ResultingStatus getStatus() {
        ResultingStatus res = new ResultingStatus();
        
        StatusInfo status = new StatusInfo();
        status.setStatusCode(StatusCode.OK);
        status.setStatusText("Version: " + getClass().getPackage().getImplementationVersion() + 
                " MessageXML version: " + MessageVersionValidator.getProtocolVersion());
        
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
