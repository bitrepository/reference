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

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.referencepillar.ReferenceArchive;
import org.bitrepository.protocol.utils.TimeMeasurementUtils;
import org.bitrepository.service.exception.IdentifyContributorException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetFileIDs operation.
 */
public class IdentifyPillarsForGetFileIDsRequestHandler 
        extends ReferencePillarMessageHandler<IdentifyPillarsForGetFileIDsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param referenceArchive The archive for the data.
     */
    public IdentifyPillarsForGetFileIDsRequestHandler(PillarContext context, ReferenceArchive referenceArchive) {
        super(context, referenceArchive);
    }
    
    @Override
    public Class<IdentifyPillarsForGetFileIDsRequest> getRequestClass() {
        return IdentifyPillarsForGetFileIDsRequest.class;
    }

    @Override
    public void processRequest(IdentifyPillarsForGetFileIDsRequest message) throws RequestHandlerException {
        checkThatAllRequestedFilesAreAvailable(message);
        respondSuccesfullIdentification(message);
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForGetFileIDsRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Validates that all the requested files in the filelist are present. 
     * Otherwise an {@link IdentifyContributorException} with the appropriate errorcode is thrown.
     * @param message The message containing the list files. An empty filelist is expected 
     * when "AllFiles" or the parameter option is used.
     */
    public void checkThatAllRequestedFilesAreAvailable(IdentifyPillarsForGetFileIDsRequest message) 
            throws RequestHandlerException {
        FileIDs fileids = message.getFileIDs();
        if(fileids == null) {
            log.debug("No fileids are defined in the identification request ('" + message.getCorrelationID() + "').");
            return;
        }
        validateFileID(message.getFileIDs().getFileID());
        
        List<String> missingFiles = new ArrayList<String>();
        String fileID = fileids.getFileID();
        if(fileID != null && !fileID.isEmpty() && !getArchive().hasFile(fileID)) {
            missingFiles.add(fileID);
        }
        
        // Throw exception if any files are missing.
        if(!missingFiles.isEmpty()) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            irInfo.setResponseText(missingFiles.size() + " missing files: '" + missingFiles + "'");
            
            throw new IdentifyContributorException(irInfo);
        }
    }
    
    /**
     * Makes a response to the successful identification.
     * @param message The request message to respond to.
     */
    private void respondSuccesfullIdentification(IdentifyPillarsForGetFileIDsRequest message) {
        // Create the response.
        IdentifyPillarsForGetFileIDsResponse reply = createFinalResponse(message);
        
        reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        reply.setResponseInfo(irInfo);
        
        // Send resulting file.
        getMessageBus().sendMessage(reply);
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
        populateResponse(msg, res);
        res.setFileIDs(msg.getFileIDs());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        
        return res;
    }
}