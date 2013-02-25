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
package org.bitrepository.pillar.referencepillar.messagehandler;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.TimeMeasurementUtils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.referencepillar.archive.CollectionArchiveManager;
import org.bitrepository.pillar.referencepillar.archive.ReferenceChecksumManager;
import org.bitrepository.service.exception.IdentifyContributorException;
import org.bitrepository.service.exception.RequestHandlerException;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetFile operation.
 */
public class IdentifyPillarsForGetFileRequestHandler 
        extends ReferencePillarMessageHandler<IdentifyPillarsForGetFileRequest> {
    /**
     * @param context The context for the pillar.
     * @param archivesManager The manager of the archives.
     * @param csManager The checksum manager for the pillar.
     */
    protected IdentifyPillarsForGetFileRequestHandler(MessageHandlerContext context, 
            CollectionArchiveManager archivesManager, ReferenceChecksumManager csManager) {
        super(context, archivesManager, csManager);
    }
    
    @Override
    public Class<IdentifyPillarsForGetFileRequest> getRequestClass() {
        return IdentifyPillarsForGetFileRequest.class;
    }

    @Override
    public void processRequest(IdentifyPillarsForGetFileRequest message) throws RequestHandlerException {
        validateCollectionID(message);
        checkThatFileIsAvailable(message);
        respondSuccesfullIdentification(message);
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForGetFileRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Validates that the requested file is within the archive. 
     * Otherwise an {@link IdentifyContributorException} with the appropriate errorcode is thrown.
     * @param message The request for the identification for the GetFileRequest operation.
     */
    private void checkThatFileIsAvailable(IdentifyPillarsForGetFileRequest message) 
            throws RequestHandlerException {
        validateFileID(message.getFileID());
        if(!getArchives().hasFile(message.getFileID(), message.getCollectionID())) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            irInfo.setResponseText("The file '" + message.getFileID() 
                    + "' does not exist within the archive.");
            
            throw new IdentifyContributorException(irInfo);
        }
    }
    
    /**
     * Method for making a successful response to the identification.
     * @param request The request to respond to.
     */
    private void respondSuccesfullIdentification(IdentifyPillarsForGetFileRequest request) {
        IdentifyPillarsForGetFileResponse response = createFinalResponse(request);

        response.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
            getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        response.setResponseInfo(irInfo);

        dispatchResponse(response, request);
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
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        
        return res;
    }
}
