/*
 * #%L
 * Bitrepository Reference Pillar
 * 
 * $Id$
 * $HeadURL$
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
package org.bitrepository.pillar.checksumpillar.messagehandler;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.TimeMeasurementUtils;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.service.exception.IdentifyContributorException;
import org.bitrepository.service.exception.RequestHandlerException;

/**
 * Class for handling the identification of this pillar for the purpose of performing the ReplaceFile operation.
 */
public class IdentifyPillarsForReplaceFileRequestHandler 
        extends ChecksumPillarMessageHandler<IdentifyPillarsForReplaceFileRequest> {
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param refCache The cache for the checksum data.
     */
    public IdentifyPillarsForReplaceFileRequestHandler(MessageHandlerContext context, ChecksumStore refCache) {
        super(context, refCache);
    }

    @Override
    public Class<IdentifyPillarsForReplaceFileRequest> getRequestClass() {
        return IdentifyPillarsForReplaceFileRequest.class;
    }

    @Override
    public void processRequest(IdentifyPillarsForReplaceFileRequest message) throws RequestHandlerException {
        validateFileID(message.getFileID());
        checkThatRequestedFileIsAvailable(message);
        respondSuccessfulIdentification(message);
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForReplaceFileRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Validates that the requested files are present in the archive. 
     * Otherwise an {@link IdentifyContributorException} with the appropriate errorcode is thrown.
     * @param message The message containing the id of the file. 
     */
    public void checkThatRequestedFileIsAvailable(IdentifyPillarsForReplaceFileRequest message) 
            throws RequestHandlerException {
        if(!getCache().hasFile(message.getFileID())) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            throw new IdentifyContributorException(irInfo);
        }
    }

    /**
     * Method for making a successful response to the identification.
     * @param request The request request to respond to.
     */
    private void respondSuccessfulIdentification(IdentifyPillarsForReplaceFileRequest request) {
        IdentifyPillarsForReplaceFileResponse response = createFinalResponse(request);

        response.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
            getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        response.setResponseInfo(irInfo);

        dispatchResponse(response, request);
    }
    
    /**
     * Creates a IdentifyPillarsForGetChecksumsResponse based on a 
     * IdentifyPillarsForReplaceFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - ResponseInfo
     * <br/> - PillarChecksumSpec
     * 
     * @param request The IdentifyPillarsForReplaceFileRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForReplaceFileResponse createFinalResponse(IdentifyPillarsForReplaceFileRequest request) {
        IdentifyPillarsForReplaceFileResponse res = new IdentifyPillarsForReplaceFileResponse();
        res.setFileID(request.getFileID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setPillarChecksumSpec(getChecksumType());
        
        return res;
    }
}
