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
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.TimeMeasurementUtils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.utils.MessageUtils;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetFile operation.
 */
public class IdentifyPillarsForGetFileRequestHandler 
        extends IdentifyRequestHandler<IdentifyPillarsForGetFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param context The context for the message handling.
     * @param model The storage model for the pillar.
     */
    protected IdentifyPillarsForGetFileRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }
    
    @Override
    public Class<IdentifyPillarsForGetFileRequest> getRequestClass() {
        return IdentifyPillarsForGetFileRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForGetFileRequest message) {
        return createFinalResponse(message);
    }
    
    @Override
    protected void validateRequest(IdentifyPillarsForGetFileRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        if(getPillarModel().getChecksumPillarSpec() != null) {
            throw new InvalidMessageException(ResponseCode.REQUEST_NOT_SUPPORTED, "A ChecksumPillar cannot deliver "
                    + "actual files.", request.getCollectionID());
        }

        validateCollectionID(request);
        validateFileIDFormat(request.getFileID());
        
        checkThatFileIsAvailable(request);
    }

    @Override
    protected void sendPositiveResponse(IdentifyPillarsForGetFileRequest request, MessageContext requestContext) {
        IdentifyPillarsForGetFileResponse response = createFinalResponse(request);

        response.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
            getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        response.setResponseInfo(irInfo);

        dispatchResponse(response, request);
        log.debug(MessageUtils.createMessageIdentifier(request) + " Identified for performing a GetFile operation.");
    }
    
    /**
     * Validates that the requested file is within the archive. 
     * Otherwise an {@link IdentifyContributorException} with the appropriate errorcode is thrown.
     * @param message The request for the identification for the GetFileRequest operation.
     */
    private void checkThatFileIsAvailable(IdentifyPillarsForGetFileRequest message) 
            throws RequestHandlerException {
        validateFileIDFormat(message.getFileID());
        getPillarModel().verifyFileExists(message.getFileID(), message.getCollectionID());
    }
    
    /**
     * Creates a IdentifyPillarsForGetFileResponse based on a 
     * IdentifyPillarsForGetFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - AuditTrailInformation
     * <br/> - IdentifyResponseInfo
     * 
     * @param msg The IdentifyPillarsForGetFileRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForGetFileResponse createFinalResponse(IdentifyPillarsForGetFileRequest msg) {
        IdentifyPillarsForGetFileResponse res = new IdentifyPillarsForGetFileResponse();
        res.setFileID(msg.getFileID());
        res.setPillarID(getPillarModel().getPillarID());
        
        return res;
    }
}
