/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: PutFileRequestHandler.java 687 2012-01-09 12:56:47Z ktc $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org/bitrepository/pillar/messagehandler/PutFileRequestHandler.java $
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

import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.utils.MessageUtils;
import org.bitrepository.service.exception.IllegalOperationException;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for performing the PutFile operation.
 */
public class PutFileRequestHandler extends PerformRequestHandler<PutFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * @param context The context for the message handling.
     * @param model The storage model for the pillar.
     */
    protected PutFileRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }
    
    @Override
    public Class<PutFileRequest> getRequestClass() {
        return PutFileRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(PutFileRequest message) {
        return createFinalResponse(message);
    }
    
    @Override
    protected void validateRequest(PutFileRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        validateCollectionID(request);
        validatePillarId(request.getPillarID());
        if(request.getChecksumDataForNewFile() != null) {
            getPillarModel().verifyChecksumAlgorithm(request.getChecksumDataForNewFile().getChecksumSpec(), 
                    request.getCollectionID());
        } else if(getSettings().getRepositorySettings().getProtocolSettings().isRequireChecksumForNewFileRequests()) {
            throw new IllegalOperationException(ResponseCode.NEW_FILE_CHECKSUM_FAILURE, "A checksum is required for "
                    + "the PutFile operation to be performed.", request.getCollectionID(), request.getFileID());
        }
        
        getPillarModel().verifyChecksumAlgorithm(request.getChecksumRequestForNewFile(), request.getCollectionID());
        validateFileIDFormat(request.getFileID(), request.getCollectionID());
        
        checkThatTheFileDoesNotAlreadyExist(request);
        checkSpaceForStoringNewFile(request);
    }

    @Override
    protected void sendProgressResponse(PutFileRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        PutFileProgressResponse response = createPutFileProgressResponse(request);

        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Started to receive data.");  
        response.setResponseInfo(prInfo);

        dispatchResponse(response, request);
    }

    @Override
    protected void performOperation(PutFileRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        log.info(MessageUtils.createMessageIdentifier(request) + " Performing PutFile for file " 
                + request.getFileID() + " on collection " + request.getCollectionID());
        try {
            retrieveFile(request);
            getAuditManager().addAuditEvent(request.getCollectionID(), request.getFileID(), request.getFrom(), 
                    "Add file to archive.", request.getAuditTrailInformation(), FileAction.PUT_FILE,
                    request.getCorrelationID(), requestContext.getCertificateFingerprint());
            sendFinalResponse(request);
        } finally {
            getPillarModel().ensureFileNotInTmpDir(request.getFileID(), request.getCollectionID());
        }
    }
    
    /**
     * Validates that the file is not already within the archive. 
     * Otherwise an {@link InvalidMessageException} with the appropriate error code is thrown.
     * @param message The request with the filename to validate.
     */
    private void checkThatTheFileDoesNotAlreadyExist(PutFileRequest message) throws RequestHandlerException {
        if(getPillarModel().hasFileID(message.getFileID(), message.getCollectionID())) {
            throw new InvalidMessageException(ResponseCode.DUPLICATE_FILE_FAILURE, "We already have the file", 
                    message.getCollectionID());
        }
    }
    
    /**
     * Validates that enough space exists is left in the archive.
     * Otherwise an {@link InvalidMessageException} with the appropriate error code is thrown.
     * If the no size is defined in the message, then it is not checked.
     * @param message The request with the size of the file.
     */
    private void checkSpaceForStoringNewFile(PutFileRequest message) throws RequestHandlerException {
        long fileSize;
        if(message.getFileSize() == null) {
            log.debug("No size for the file to be put.");
            fileSize = 0L;
        } else {
            fileSize = message.getFileSize().longValue();
        }
        
        getPillarModel().verifyEnoughFreeSpaceLeftForFile(fileSize, message.getCollectionID());
    }
    
    /**
     * Retrieves the actual data, validates it and stores it.
     * @param message The request to for the file to put.
     * @throws RequestHandlerException If the retrival of the file fails.
     */
    private void retrieveFile(PutFileRequest message) throws RequestHandlerException {
        getPillarModel().putFile(message.getCollectionID(), message.getFileID(), message.getFileAddress(), 
                message.getChecksumDataForNewFile());
    }
    
    /**
     * Method for sending the final response for the requested put operation.
     * @param message The request requesting the put operation.
     * @throws RequestHandlerException If the requested checksum specification is not supported.
     */
    private void sendFinalResponse(PutFileRequest message) throws RequestHandlerException {
        PutFileFinalResponse response = createFinalResponse(message);

        ResponseInfo frInfo = new ResponseInfo();
        frInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        response.setResponseInfo(frInfo);
        
        if(message.getChecksumRequestForNewFile() != null) {
            response.setChecksumDataForNewFile(getPillarModel().getChecksumDataForFile(message.getFileID(),
                    message.getCollectionID(), message.getChecksumRequestForNewFile()));
        } else {
            log.debug("No checksum validation requested.");
        }

        dispatchResponse(response, message);
    }
    
    
    /**
     * Creates a PutFileProgressResponse based on a PutFileRequest. 
     * @param request The PutFileRequest to base the progress response on.
     * @return The PutFileProgressResponse based on the request.
     */
    private PutFileProgressResponse createPutFileProgressResponse(PutFileRequest request) {
        PutFileProgressResponse res = new PutFileProgressResponse();
        res.setFileAddress(request.getFileAddress());
        res.setFileID(request.getFileID());
        res.setPillarID(getPillarModel().getPillarID());
        res.setPillarChecksumSpec(getPillarModel().getChecksumPillarSpec());

        return res;
    }
    
    /**
     * Creates a PutFileFinalResponse based on a PutFileRequest. 
     *  
     * @param request The PutFileRequest to base the final response message on.
     * @return The PutFileFinalResponse message based on the request.
     */
    private PutFileFinalResponse createFinalResponse(PutFileRequest request) {
        PutFileFinalResponse res = new PutFileFinalResponse();
        res.setFileAddress(request.getFileAddress());
        res.setFileID(request.getFileID());
        res.setPillarID(getPillarModel().getPillarID());
        res.setPillarChecksumSpec(getPillarModel().getChecksumPillarSpec());

        return res;
    }
}
