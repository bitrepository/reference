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
package org.bitrepository.pillar.checksumpillar.messagehandler;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.TimeMeasurementUtils;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.service.exception.IdentifyContributorException;
import org.bitrepository.service.exception.RequestHandlerException;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetChecksums operation.
 */
public class IdentifyPillarsForGetChecksumsRequestHandler 
        extends ChecksumPillarMessageHandler<IdentifyPillarsForGetChecksumsRequest> {
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param refCache The cache for the checksum data.
     */
    public IdentifyPillarsForGetChecksumsRequestHandler(MessageHandlerContext context, ChecksumStore refCache) {
        super(context, refCache);
    }

    @Override
    public Class<IdentifyPillarsForGetChecksumsRequest> getRequestClass() {
        return IdentifyPillarsForGetChecksumsRequest.class;
    }

    @Override
    public void processRequest(IdentifyPillarsForGetChecksumsRequest message) throws RequestHandlerException {
        validateMessage(message);
        checkThatAllRequestedFilesAreAvailable(message);
        respondSuccesfullIdentification(message);
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForGetChecksumsRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Validates the identification message.
     * @param message The identify message to validate.
     */
    private void validateMessage(IdentifyPillarsForGetChecksumsRequest message) throws RequestHandlerException {
        validateChecksumSpec(message.getChecksumRequestForExistingFile());        
    }
    
    /**
     * Validates that all the requested files in the filelist are present. 
     * Otherwise an {@link IdentifyContributorException} with the appropriate errorcode is thrown.
     * @param message The message containing the list files. An empty filelist is expected 
     * when "AllFiles" or the parameter option is used.
     */
    public void checkThatAllRequestedFilesAreAvailable(IdentifyPillarsForGetChecksumsRequest message) 
            throws RequestHandlerException {
        FileIDs fileids = message.getFileIDs();
        validateFileID(message.getFileIDs().getFileID());
        
        List<String> missingFiles = new ArrayList<String>();
        String fileID = fileids.getFileID();
        if(fileID != null && !getCache().hasFile(fileID, message.getCollectionID())) {
            missingFiles.add(fileID);
        }
        
        // Throw exception if any files are missing.
        if(!missingFiles.isEmpty()) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            
            throw new IdentifyContributorException(irInfo);
        }
    }
    
    /**
     * Method for making a successful response to the identification.
     * @param request The request to respond to.
     */
    private void respondSuccesfullIdentification(IdentifyPillarsForGetChecksumsRequest request) {
        IdentifyPillarsForGetChecksumsResponse response = createFinalResponse(request);

        response.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
            getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        response.setResponseInfo(irInfo);

        dispatchResponse(response, request);
    }
    
    /**
     * Creates a IdentifyPillarsForGetChecksumsResponse based on a 
     * IdentifyPillarsForGetFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - AuditTrailInformation
     * <br/> - IdentifyResponseInfo
     * <br/> - PillarChecksumSpec
     * 
     * @param msg The IdentifyPillarsForGetFileRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForGetChecksumsResponse createFinalResponse(
            IdentifyPillarsForGetChecksumsRequest msg) {
        IdentifyPillarsForGetChecksumsResponse res 
                = new IdentifyPillarsForGetChecksumsResponse();
        res.setFileIDs(msg.getFileIDs());
        res.setChecksumRequestForExistingFile(msg.getChecksumRequestForExistingFile());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setPillarChecksumSpec(getChecksumType());
        
        return res;
    }
}
