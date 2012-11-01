/*
 * #%L
 * Bitrepository Reference Pillar
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

import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyContributorsForGetAuditTrailsResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.utils.ResponseInfoUtils;
import org.bitrepository.service.contributor.ContributorContext;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetStatus operation.
 */
public class IdentifyContributorsForGetAuditTrailsRequestHandler 
        extends AbstractRequestHandler<IdentifyContributorsForGetAuditTrailsRequest> {
    /**
     * Constructor.
     * @param context The context of the message handler.
     */
    public IdentifyContributorsForGetAuditTrailsRequestHandler(ContributorContext context) {
        super(context);
    }

    @Override
    public Class<IdentifyContributorsForGetAuditTrailsRequest> getRequestClass() {
        return IdentifyContributorsForGetAuditTrailsRequest.class;
    }

    @Override
    public void processRequest(IdentifyContributorsForGetAuditTrailsRequest message) {
        ArgumentValidator.checkNotNull(message, "IdentifyContributorsForGetAuditTrailsRequest message");

        respondSuccessfulIdentification(message);
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyContributorsForGetAuditTrailsRequest request) {
        return createResponse();
    }
    
    /**
     * Makes a success response for the identification.
     * @param request The request to base the response upon.
     */
    protected void respondSuccessfulIdentification(IdentifyContributorsForGetAuditTrailsRequest request) {
        IdentifyContributorsForGetAuditTrailsResponse response = createResponse();
        response.setResponseInfo(ResponseInfoUtils.getPositiveIdentification());
        getContext().getResponseDispatcher().dispatchResponse(response, request);
    }
    
    /**
     * Sends a bad response with the given cause.
     * @param request The identification request to respond to.
     * @param responseInfo The cause of the bad identification (e.g. which file is missing).
     */
    protected void respondFailedIdentification(IdentifyContributorsForGetAuditTrailsRequest request,
            ResponseInfo responseInfo) {
        IdentifyContributorsForGetAuditTrailsResponse response = createResponse();
        
        response.setResponseInfo(responseInfo);
        getContext().getResponseDispatcher().dispatchResponse(response, request);
    }
    
    /**
     * Creates a IdentifyContributorsForGetAuditTrailsResponse based on a
     * IdentifyContributorsForGetAuditTrailsResponse. The following fields are not inserted:
     * <br/> - ResponseInfo
     *
     * @return The response to the request.
     */
    protected IdentifyContributorsForGetAuditTrailsResponse createResponse() {
        IdentifyContributorsForGetAuditTrailsResponse res = new IdentifyContributorsForGetAuditTrailsResponse();
        return res;
    }
}
