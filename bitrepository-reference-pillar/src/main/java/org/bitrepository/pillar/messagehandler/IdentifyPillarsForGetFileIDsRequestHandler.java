/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2011 The State and University Library, The Royal Library and The State Archives, Denmark
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
package org.bitrepository.pillar.messagehandler;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.TimeMeasurementUtils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.utils.MessageUtils;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetFileIDs operation.
 */
public class IdentifyPillarsForGetFileIDsRequestHandler 
        extends IdentifyRequestHandler<IdentifyPillarsForGetFileIDsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * @param context The context for the message handling.
     * @param model The storage model for the pillar.
     */
    protected IdentifyPillarsForGetFileIDsRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }
    
    @Override
    public Class<IdentifyPillarsForGetFileIDsRequest> getRequestClass() {
        return IdentifyPillarsForGetFileIDsRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForGetFileIDsRequest message) {
        return createFinalResponse(message);
    }
    
    @Override
    protected void validateRequest(IdentifyPillarsForGetFileIDsRequest request, MessageContext messageContext) 
            throws RequestHandlerException {
        validateCollectionID(request);
        if (request.getFileIDs() != null && request.getFileIDs().getFileID() != null) {
            validateFileIDFormat(request.getFileIDs().getFileID());
            verifyFileIDExistence(request.getFileIDs(), request.getCollectionID());
        }
    }
    
    @Override
    protected void sendPositiveResponse(IdentifyPillarsForGetFileIDsRequest request, MessageContext requestContext) {
        IdentifyPillarsForGetFileIDsResponse response = createFinalResponse(request);
        response.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
            getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        response.setResponseInfo(irInfo);

        dispatchResponse(response, request);
        log.debug(MessageUtils.createMessageIdentifier(request) + " Identified for performing a GetFileIDs operation.");
    }
    
    /**
     * Creates a IdentifyPillarsForGetFileIDsResponse based on a 
     * IdentifyPillarsForGetFileIDsRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - IdentifyResponseInfo
     * 
     * @param msg The IdentifyPillarsForGetFileIDsRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForGetFileIDsResponse createFinalResponse(IdentifyPillarsForGetFileIDsRequest msg) {
        IdentifyPillarsForGetFileIDsResponse res = new IdentifyPillarsForGetFileIDsResponse();
        res.setFileIDs(msg.getFileIDs());
        res.setPillarID(getPillarModel().getPillarID());
        
        return res;
    }
}
