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

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.common.FileIDValidator;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.referencepillar.ReferenceArchive;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for performing the GetFile operation.
 */
public class GetFileRequestHandler extends ReferencePillarMessageHandler<GetFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The file id validator for validating the file id.*/
    private final FileIDValidator fileIdValidator;
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param referenceArchive The archive for the data.
     */
    public GetFileRequestHandler(PillarContext context, ReferenceArchive referenceArchive) {
        super(context, referenceArchive);
        this.fileIdValidator = new FileIDValidator(context);
    }
    
    @Override
    public Class<GetFileRequest> getRequestClass() {
        return GetFileRequest.class;
    }

    @Override
    public void processRequest(GetFileRequest message) throws RequestHandlerException {
        validateMessage(message);
        sendProgressMessage(message);
        uploadToClient(message);
        sendFinalResponse(message);
    }

    @Override
    public MessageResponse generateFailedResponse(GetFileRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Method for validating the content of the message.
     * @param message The message requesting the operation, which should be validated.
     * @return Whether it was valid.
     */
    protected void validateMessage(GetFileRequest message) throws RequestHandlerException {
        // Validate the message.
        validatePillarId(message.getPillarID());
        fileIdValidator.validateFileID(message.getFileID());
        
        // Validate, that we have the requested file.
        if(!getArchive().hasFile(message.getFileID())) {
            log.warn("The file '" + message.getFileID() + "' has been requested, but we do not have that file!");
            // Then tell the mediator, that we failed.
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            fri.setResponseText("The file '" + message.getFileID() + "' has been requested, but we do "
                    + "not have that file!");
            throw new InvalidMessageException(fri);
        }
    }
    
    /**
     * The method for sending a progress response telling, that the operation is about to be performed.
     * @param message The request for the GetFile operation.
     */
    protected void sendProgressMessage(GetFileRequest message) {
        File requestedFile = getArchive().getFile(message.getFileID());
        
        // make ProgressResponse to tell that we are handling this.
        GetFileProgressResponse pResponse = createGetFileProgressResponse(message);
        
        // set missing variables in the message:
        // AuditTrailInformation, ChecksumsDataForBitRepositoryFile, FileSize, ProgressResponseInfo
        pResponse.setFileSize(BigInteger.valueOf(requestedFile.length()));
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Started to retrieve data.");
        pResponse.setResponseInfo(prInfo);

        // Send the ProgressResponse
        log.info("Sending GetFileProgressResponse: " + pResponse);
        getMessageBus().sendMessage(pResponse);
    } 

    /**
     * Method for uploading the file to the requested location.
     * @param message The message requesting the GetFile operation.
     * @throws InvalidMessageException If the upload of the file fails.
     */
    @SuppressWarnings("deprecation")
    protected void uploadToClient(GetFileRequest message) throws InvalidMessageException {
        File requestedFile = getArchive().getFile(message.getFileID());

        try {
            // Upload the file.
            log.info("Uploading file: " + requestedFile.getName() + " to " + message.getFileAddress());
            getAuditManager().addAuditEvent(message.getFileID(), message.getFrom(), "Failed identifying pillar.", 
                    message.getAuditTrailInformation(), FileAction.GET_FILE);
            FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();
            fe.uploadToServer(new FileInputStream(requestedFile), new URL(message.getFileAddress()));
        } catch (IOException e) {
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
            fri.setResponseText("The file '" + message.getFileID() + "' could not be uploaded at '" 
                    + message.getFileAddress() + "'");
            throw new InvalidMessageException(fri, e);
        }
    }
    
    /**
     * Method for sending the final response.
     * @param message The message to respond to.
     */
    protected void sendFinalResponse(GetFileRequest message) {
        // make ProgressResponse to tell that we are handling this.
        GetFileFinalResponse fResponse = createFinalResponse(message);
        ResponseInfo frInfo = new ResponseInfo();
        frInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        frInfo.setResponseText("Data delivered.");
        fResponse.setResponseInfo(frInfo);

        // send the FinalResponse.
        log.info("Sending GetFileFinalResponse: " + fResponse);
        getMessageBus().sendMessage(fResponse);
    }
    
    /**
     * Creates a GetFileResponse based on a GetFileRequest. Missing the 
     * following fields:
     * <br/> - AuditTrailInformation
     * <br/> - ChecksumsDataForBitRepositoryFile
     * <br/> - FileSize
     * <br/> - ProgressResponseInfo
     * 
     * @param msg The GetFileRequest to base the progress response on.
     * @return The GetFileProgressResponse based on the request.
     */
    private GetFileProgressResponse createGetFileProgressResponse(GetFileRequest msg) {
        GetFileProgressResponse res = new GetFileProgressResponse();
        populateResponse(msg, res);
        res.setFileAddress(msg.getFileAddress());
        res.setFileID(msg.getFileID());
        res.setFilePart(msg.getFilePart());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());

        return res;
    }
    
    /**
     * Creates a GetFileFinalResponse based on a GetFileRequest. Missing the 
     * following fields:
     * <br/> - AuditTrailInformation
     * <br/> - FinalResponseInfo
     * 
     * @param msg The GetFileRequest to base the final response on.
     * @return The GetFileFinalResponse based on the request.
     */
    private GetFileFinalResponse createFinalResponse(GetFileRequest msg) {
        GetFileFinalResponse res = new GetFileFinalResponse();
        populateResponse(msg, res);
        res.setFileAddress(msg.getFileAddress());
        res.setFileID(msg.getFileID());
        res.setFilePart(msg.getFilePart());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());

        return res;
    }
}
