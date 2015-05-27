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
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.utils.MessageUtils;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for performing the GetFile operation.
 */
public class GetFileRequestHandler extends PerformRequestHandler<GetFileRequest> {
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * @param context The context for the message handling.
     * @param model The storage model for the pillar.
     */
    protected GetFileRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }

    @Override
    public Class<GetFileRequest> getRequestClass() {
        return GetFileRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(GetFileRequest message) {
        return createFinalResponse(message);
    }

    @Override
    protected void validateRequest(GetFileRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        validateCollectionID(request);
        validatePillarId(request.getPillarID());
        validateFileIDFormat(request.getFileID());

        getPillarModel().verifyFileExists(request.getFileID(), request.getCollectionID());

    }

    @Override
    protected void sendProgressResponse(GetFileRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        FileInfo requestedFi = getPillarModel().getFileInfoForActualFile(request.getFileID(), 
                request.getCollectionID());
        GetFileProgressResponse response = createGetFileProgressResponse(request);

        response.setFileSize(BigInteger.valueOf(requestedFi.getSize()));
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Started to retrieve data.");
        response.setResponseInfo(prInfo);
        getContext().getResponseDispatcher().dispatchResponse(response, request);
    }

    @Override
    protected void performOperation(GetFileRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        log.info(MessageUtils.createMessageIdentifier(request) + " Performing GetFile for file " 
                + request.getFileID() + " on collection " + request.getCollectionID());
        uploadToClient(request);
        getAuditManager().addAuditEvent(request.getCollectionID(), request.getFileID(), request.getFrom(), 
                "Failed identifying pillar.", request.getAuditTrailInformation(), FileAction.GET_FILE,
                request.getCorrelationID(), requestContext.getCertificateFingerprint());
        sendFinalResponse(request);
    }

    /**
     * Method for uploading the file to the requested location.
     * @param message The message requesting the GetFile operation.
     * @throws InvalidMessageException If the upload of the file fails.
     */
    protected void uploadToClient(GetFileRequest message) 
            throws RequestHandlerException {
        FileInfo requestedFile = getPillarModel().getFileInfoForActualFile(message.getFileID(), 
                message.getCollectionID());

        try {
            InputStream is;
            if(message.getFilePart() == null) {
                is = requestedFile.getInputstream();
            } else {
                is = extractFilePart(requestedFile, message.getFilePart());
            }

            log.info("Uploading file: " + requestedFile.getFileID() + " to " + message.getFileAddress());
            context.getFileExchange().putFile(is, new URL(message.getFileAddress()));
        } catch (IOException e) {
            log.warn("The file '" + message.getFileID() + "' from collection '" + message.getCollectionID() 
                    + "' could not be uploaded at '" + message.getFileAddress() + "'");
            throw new InvalidMessageException(ResponseCode.FILE_TRANSFER_FAILURE, "Could not deliver file to address '" 
                    + message.getFileAddress() + "'", message.getCollectionID(), e);
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
     * Creates a GetFileProgressResponse based on a GetFileRequest. 
     * @param msg The GetFileRequest to base the progress response on.
     * @return The GetFileProgressResponse based on the request.
     */
    private GetFileProgressResponse createGetFileProgressResponse(GetFileRequest msg) {
        GetFileProgressResponse res = new GetFileProgressResponse();
        res.setFileAddress(msg.getFileAddress());
        res.setFileID(msg.getFileID());
        res.setFilePart(msg.getFilePart());
        res.setPillarID(getPillarModel().getPillarID());

        return res;
    }
    
    /**
     * Creates a GetFileFinalResponse based on a GetFileRequest. 
     * @param msg The GetFileRequest to base the final response on.
     * @return The GetFileFinalResponse based on the request.
     */
    private GetFileFinalResponse createFinalResponse(GetFileRequest msg) {
        GetFileFinalResponse res = new GetFileFinalResponse();
        res.setFileAddress(msg.getFileAddress());
        res.setFileID(msg.getFileID());
        res.setFilePart(msg.getFilePart());
        res.setPillarID(getPillarModel().getPillarID());

        return res;
    }
}
