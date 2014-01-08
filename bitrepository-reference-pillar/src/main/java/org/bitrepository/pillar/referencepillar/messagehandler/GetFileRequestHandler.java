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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.URL;

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.referencepillar.archive.ReferenceChecksumManager;
import org.bitrepository.protocol.*;
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
    
    /**
     * @param context The context for the pillar.
     * @param archivesManager The manager of the archives.
     * @param csManager The checksum manager for the pillar.
     */
    protected GetFileRequestHandler(MessageHandlerContext context, FileStore archivesManager, 
            ReferenceChecksumManager csManager) {
        super(context, archivesManager, csManager);
    }

    @Override
    public Class<GetFileRequest> getRequestClass() {
        return GetFileRequest.class;
    }

    @Override
    public void processRequest(GetFileRequest message, MessageContext messageContext) throws RequestHandlerException {
        validateMessage(message);
        sendProgressMessage(message);
        uploadToClient(message, messageContext);
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
        validateCollectionID(message);
        validatePillarId(message.getPillarID());
        validateFileID(message.getFileID());

        if(!getArchives().hasFile(message.getFileID(), message.getCollectionID())) {
            log.warn("The file '" + message.getFileID() + "' has been requested, but we do not have that file!");
            // Then tell the mediator, that we failed.
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            fri.setResponseText("The file '" + message.getFileID() + "' has been requested, but we do "
                    + "not have that file!");
            throw new InvalidMessageException(fri, message.getCollectionID());
        }
    }
    
    /**
     * The method for sending a progress response telling, that the operation is about to be performed.
     * @param message The request for the GetFile operation.
     */
    protected void sendProgressMessage(GetFileRequest message) {
        FileInfo requestedFi = getArchives().getFileInfo(message.getFileID(), message.getCollectionID());
        GetFileProgressResponse response = createGetFileProgressResponse(message);

        response.setFileSize(BigInteger.valueOf(requestedFi.getSize()));
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Started to retrieve data.");
        response.setResponseInfo(prInfo);
        getContext().getResponseDispatcher().dispatchResponse(response, message);
    } 

    /**
     * Method for uploading the file to the requested location.
     * @param message The message requesting the GetFile operation.
     * @throws InvalidMessageException If the upload of the file fails.
     */
    protected void uploadToClient(GetFileRequest message, MessageContext messageContext) throws InvalidMessageException {
        FileInfo requestedFile = getArchives().getFileInfo(message.getFileID(), message.getCollectionID());

        try {
            InputStream is;
            if(message.getFilePart() == null) {
                is = requestedFile.getInputstream();
            } else {
                is = extractFilePart(requestedFile, message.getFilePart());
            }

            log.info("Uploading file: " + requestedFile.getFileID() + " to " + message.getFileAddress());
            getAuditManager().addAuditEvent(message.getCollectionID(), message.getFileID(), message.getFrom(), 
                    "Failed identifying pillar.", message.getAuditTrailInformation(), FileAction.GET_FILE,
                    message.getCorrelationID(), messageContext.getCertificateFingerprint());
            FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(getSettings());
            fe.uploadToServer(is, new URL(message.getFileAddress()));
        } catch (IOException e) {
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
            fri.setResponseText("The file '" + message.getFileID() + "' could not be uploaded at '" 
                    + message.getFileAddress() + "'");
            throw new InvalidMessageException(fri, message.getCollectionID(), e);
        }
    }
    
    /**
     * Extracts a given file part 
     * @param fileInfo The requested file to extract the file part from.
     * @param filePart The defined interval for the file part.
     * @return A InputStream with the requested file part.
     * @throws IOException If anything goes wrong.
     */
    private InputStream extractFilePart(FileInfo fileInfo, FilePart filePart) throws IOException {
        int offset = filePart.getPartOffSet().intValue();
        int size = filePart.getPartLength().intValue();
        byte[] partOfFile = new byte[size];
        InputStream fis  = null;
        try {
            log.debug("Extracting " + size + " bytes with offset " + offset + " from " + fileInfo.getFileID());
            fis = fileInfo.getInputstream();
            
            fis.read(new byte[offset]);
            fis.read(partOfFile);
        } finally {
            if(fis != null) {
                fis.close();
            }
        }
        return new ByteArrayInputStream(partOfFile);
    }
    
    /**
     * Method for sending the final response.
     * @param request The request to respond to.
     */
    protected void sendFinalResponse(GetFileRequest request) {
        GetFileFinalResponse response = createFinalResponse(request);
        ResponseInfo frInfo = new ResponseInfo();
        frInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        response.setResponseInfo(frInfo);

        dispatchResponse(response, request);
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
        res.setFileAddress(msg.getFileAddress());
        res.setFileID(msg.getFileID());
        res.setFilePart(msg.getFilePart());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());

        return res;
    }
}
