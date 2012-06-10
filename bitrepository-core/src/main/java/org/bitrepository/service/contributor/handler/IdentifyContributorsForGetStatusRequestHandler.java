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
        response.setContributor(getContext().getSettings().getComponentID());
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
