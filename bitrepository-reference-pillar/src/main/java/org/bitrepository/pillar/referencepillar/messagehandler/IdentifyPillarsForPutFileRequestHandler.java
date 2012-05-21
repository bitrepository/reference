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

import java.math.BigInteger;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.referencepillar.ReferenceArchive;
import org.bitrepository.protocol.utils.TimeMeasurementUtils;
import org.bitrepository.service.exception.IdentifyContributorException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the PutFile operation.
 */
public class IdentifyPillarsForPutFileRequestHandler 
        extends ReferencePillarMessageHandler<IdentifyPillarsForPutFileRequest> {
    
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param referenceArchive The archive for the data.
     */
    public IdentifyPillarsForPutFileRequestHandler(PillarContext context, ReferenceArchive referenceArchive) {
        super(context, referenceArchive);
    }
    
    @Override
    public Class<IdentifyPillarsForPutFileRequest> getRequestClass() {
        return IdentifyPillarsForPutFileRequest.class;
    }
    
    @Override
    public void processRequest(IdentifyPillarsForPutFileRequest message) throws RequestHandlerException {
        validateFileID(message.getFileID());
        checkThatTheFileDoesNotAlreadyExist(message);
        checkSpaceForStoringNewFile(message);
        respondSuccesfullIdentification(message);
    }
    
    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForPutFileRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Validates that the file is not already within the archive. 
     * Otherwise an {@link IdentifyContributorException} with the appropriate errorcode is thrown.
     * @param message The request with the filename to validate.
     */
    private void checkThatTheFileDoesNotAlreadyExist(IdentifyPillarsForPutFileRequest message) 
            throws RequestHandlerException {
        if(message.getFileID() == null) {
            log.debug("No fileid given in the identification request.");
            return;
        }
        
        if(getArchive().hasFile(message.getFileID())) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
            irInfo.setResponseText("The file '" + message.getFileID() 
                    + "' already exists within the archive.");
            
            throw new IdentifyContributorException(irInfo);
        }
    }
    
    /**
     * Validates that enough space exists is left in the archive.
     * Otherwise an {@link IdentifyContributorException} with the appropriate errorcode is thrown.
     * @param message The request with the size of the file.
     */
    private void checkSpaceForStoringNewFile(IdentifyPillarsForPutFileRequest message) 
            throws RequestHandlerException {
        BigInteger fileSize = message.getFileSize();
        if(fileSize == null) {
            log.debug("No file size given in the identification request. "
                    + "Validating that the archive has any space left.");
            fileSize = BigInteger.ZERO;
        }
        
        long useableSizeLeft = getArchive().sizeLeftInArchive() 
                - getSettings().getReferenceSettings().getPillarSettings().getMinimumSizeLeft();
        if(useableSizeLeft < fileSize.longValue()) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FAILURE);
            irInfo.setResponseText("Not enough space left in this pillar. Requires '" 
                    + fileSize.longValue() + "' but has only '" + useableSizeLeft + "'");
            
            throw new IdentifyContributorException(irInfo);
        }
    }
    
    /**
     * Method for sending a positive response for putting this file.
     * @param message The message to respond to.
     */
    protected void respondSuccesfullIdentification(IdentifyPillarsForPutFileRequest message)  {
        log.info("Creating positive reply for '" + message + "'");
        IdentifyPillarsForPutFileResponse reply = createFinalResponse(message);

        // Needs to filled in: AuditTrailInformation, PillarChecksumSpec, ReplyTo, TimeToDeliver
        reply.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());
        reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        reply.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        reply.setResponseInfo(irInfo);

        log.debug("Sending IdentifyPillarsForPutfileResponse: " + reply);
        getMessageBus().sendMessage(reply);
    }
    
    /**
     * Creates a IdentifyPillarsForPutFileResponse based on a 
     * IdentifyPillarsForPutFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - AuditTrailInformation
     * <br/> - PillarChecksumSpec
     * <br/> - IdentifyResponseInfo
     * 
     * @param msg The IdentifyPillarsForPutFileRequest to base the response on.
     * @return A IdentifyPillarsForPutFileResponse from the request.
     */
    private IdentifyPillarsForPutFileResponse createFinalResponse(IdentifyPillarsForPutFileRequest msg) {
        IdentifyPillarsForPutFileResponse res = new IdentifyPillarsForPutFileResponse();
        populateResponse(msg, res);
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        
        return res;
    }
}
