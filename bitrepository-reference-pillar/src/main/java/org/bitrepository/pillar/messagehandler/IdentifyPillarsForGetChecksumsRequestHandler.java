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

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.TimeMeasurementUtils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.utils.MessageUtils;
import org.bitrepository.service.exception.IdentifyContributorException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetChecksums operation.
 */
public class IdentifyPillarsForGetChecksumsRequestHandler 
        extends IdentifyRequestHandler<IdentifyPillarsForGetChecksumsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param context The context for the message handling.
     * @param model The storage model for the pillar.
     */
    protected IdentifyPillarsForGetChecksumsRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }
    
    @Override
    public Class<IdentifyPillarsForGetChecksumsRequest> getRequestClass() {
        return IdentifyPillarsForGetChecksumsRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForGetChecksumsRequest message) {
        return createFinalResponse(message);
    }
    
    @Override
    protected void validateRequest(IdentifyPillarsForGetChecksumsRequest request, MessageContext messageContext)
            throws RequestHandlerException {
        validateCollectionID(request);
        getPillarModel().verifyChecksumAlgorithm(request.getChecksumRequestForExistingFile(),
                request.getCollectionID());
        if (request.getFileIDs() != null && request.getFileIDs().getFileID() != null) {
            validateFileIDFormat(request.getFileIDs().getFileID());
            checkThatAllRequestedFilesAreAvailable(request);
        }
    }

    @Override
    protected void sendPositiveResponse(IdentifyPillarsForGetChecksumsRequest request, MessageContext requestContext) {
        IdentifyPillarsForGetChecksumsResponse response = createFinalResponse(request);

        response.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
            getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));

        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        response.setResponseInfo(irInfo);
        
        dispatchResponse(response, request);
        log.debug(MessageUtils.createMessageIdentifier(request) 
                + " Identified for performing a GetChecksums operation.");
    }

    /**
     * Validates that all the requested files in the filelist are present. 
     * Otherwise an {@link IdentifyContributorException} with the appropriate errorcode is thrown.
     * @param message The message containing the list files. An empty filelist is expected 
     * when "AllFiles" or the parameter option is used.
     */
    private void checkThatAllRequestedFilesAreAvailable(IdentifyPillarsForGetChecksumsRequest message) 
            throws RequestHandlerException {
        FileIDs fileids = message.getFileIDs();
        
        if(fileids.getFileID() != null && !getPillarModel().hasFileID(fileids.getFileID(), message.getCollectionID())) {
            throw new IdentifyContributorException(ResponseCode.FILE_NOT_FOUND_FAILURE, "File not found.");
        }
    }
    
    /**
     * Creates a IdentifyPillarsForGetChecksumsResponse based on a 
     * IdentifyPillarsForGetFileRequest. The following fields are not inserted:
     * <br/> - AuditTrailInformation
     * <br/> - ResponseInfo
     * <br/> - PillarChecksumSpec
     * 
     * @param msg The IdentifyPillarsForGetFileRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForGetChecksumsResponse createFinalResponse(IdentifyPillarsForGetChecksumsRequest msg) {
        IdentifyPillarsForGetChecksumsResponse res = new IdentifyPillarsForGetChecksumsResponse();
        res.setFileIDs(msg.getFileIDs());
        res.setChecksumRequestForExistingFile(msg.getChecksumRequestForExistingFile());
        res.setPillarID(getPillarModel().getPillarID());
        res.setPillarChecksumSpec(getPillarModel().getChecksumPillarSpec());
        
        return res;
    }
}
