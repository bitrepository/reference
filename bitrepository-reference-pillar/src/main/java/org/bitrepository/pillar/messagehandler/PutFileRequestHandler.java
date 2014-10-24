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

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.FileInfoStore;
import org.bitrepository.pillar.store.filearchive.ReferenceChecksumManager;
import org.bitrepository.protocol.*;
import org.bitrepository.service.exception.IllegalOperationException;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.math.BigInteger;
import java.net.URL;

/**
 * Class for performing the PutFile operation.
 * TODO handle error scenarios.
 */
public class PutFileRequestHandler extends PillarMessageHandler<PutFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * @param context The context for the pillar.
     * @param archivesManager The manager of the archives.
     * @param csManager The checksum manager for the pillar.
     */
    protected PutFileRequestHandler(MessageHandlerContext context, FileInfoStore fileInfoStore) {
        super(context, fileInfoStore);
    }
    
    @Override
    public Class<PutFileRequest> getRequestClass() {
        return PutFileRequest.class;
    }

    @Override
    public void processRequest(PutFileRequest message, MessageContext messageContext) throws RequestHandlerException {
        validateMessage(message);
        try {
            dispatchInitialProgressResponse(message);
            retrieveFile(message, messageContext);
            sendFinalResponse(message);
        } finally {
            getArchives().ensureFileNotInTmpDir(message.getFileID(), message.getCollectionID());
        }
    }

    @Override
    public MessageResponse generateFailedResponse(PutFileRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Validates the message.
     * @param message The message to validate.
     */
    private void validateMessage(PutFileRequest message) throws RequestHandlerException {
        validateCollectionID(message);
        validatePillarId(message.getPillarID());
        if(message.getChecksumDataForNewFile() != null) {
            validateChecksumSpecification(message.getChecksumDataForNewFile().getChecksumSpec(), 
                    message.getCollectionID());
        } else if(getSettings().getRepositorySettings().getProtocolSettings().isRequireChecksumForNewFileRequests()) {
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
            responseInfo.setResponseText("According to the contract a checksum for creating a new file is required.");
            throw new IllegalOperationException(responseInfo, message.getCollectionID());
        }
        
        validateChecksumSpecification(message.getChecksumRequestForNewFile(), message.getCollectionID());
        validateFileID(message.getFileID());
        
        checkThatTheFileDoesNotAlreadyExist(message);
        checkSpaceForStoringNewFile(message);
    }
    
    /**
     * Validates that the file is not already within the archive. 
     * Otherwise an {@link InvalidMessageException} with the appropriate errorcode is thrown.
     * @param message The request with the filename to validate.
     */
    private void checkThatTheFileDoesNotAlreadyExist(PutFileRequest message) throws RequestHandlerException {
        if(getArchives().hasFileID(message.getFileID(), message.getCollectionID())) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
            irInfo.setResponseText("The file '" + message.getFileID() 
                    + "' already exists within the archive.");
            
            throw new InvalidMessageException(irInfo, message.getCollectionID());
        }
    }
    
    /**
     * Validates that enough space exists is left in the archive.
     * Otherwise an {@link InvalidMessageException} with the appropriate errorcode is thrown.
     * If the no size is defined in the message, then it is not checked.
     * @param message The request with the size of the file.
     */
    private void checkSpaceForStoringNewFile(PutFileRequest message) throws RequestHandlerException {
        if(message.getFileSize() == null) {
            log.debug("No size for the file to be put.");
            return;
        }
        
        BigInteger fileSize = message.getFileSize();
        
        long useableSizeLeft = getArchives().sizeLeftInArchive(message.getCollectionID()) 
                - getSettings().getReferenceSettings().getPillarSettings().getMinimumSizeLeft();
        if(useableSizeLeft < fileSize.longValue()) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FAILURE);
            irInfo.setResponseText("Not enough space left in this pillar. Requires '" 
                    + fileSize.longValue() + "' but has only '" + useableSizeLeft + "'");
            
            throw new InvalidMessageException(irInfo, message.getCollectionID());
        }
    }
    
    /**
     * Method for sending a progress response.
     * @param request The request to base the response upon.
     */
    private void dispatchInitialProgressResponse(PutFileRequest request) {
        PutFileProgressResponse response = createPutFileProgressResponse(request);

        response.setPillarChecksumSpec(null);
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Started to receive data.");  
        response.setResponseInfo(prInfo);

        dispatchResponse(response, request);
    }
    
    /**
     * Retrieves the actual data, validates it and stores it.
     * @param message The request to for the file to put.
     * @throws RequestHandlerException If the retrival of the file fails.
     */
    private void retrieveFile(PutFileRequest message, MessageContext messageContext) throws RequestHandlerException {
        log.debug("Retrieving the data to be stored from URL: '" + message.getFileAddress() + "'");
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(getSettings());
        
        try {
            getArchives().downloadFileForValidation(message.getFileID(), message.getCollectionID(),
                    fe.downloadFromServer(new URL(message.getFileAddress())));
        } catch (IOException e) {
            String errMsg = "Could not retrieve the file from '" + message.getFileAddress() + "'";
            log.error(errMsg, e);
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
            ri.setResponseText(errMsg);
            throw new InvalidMessageException(ri, message.getCollectionID());
        }
        
        if(message.getChecksumDataForNewFile() != null) {
            ChecksumDataForFileTYPE csType = message.getChecksumDataForNewFile();
            String calculatedChecksum = getCsManager().getChecksumForTempFile(message.getFileID(), 
                    message.getCollectionID(), csType.getChecksumSpec());
            String expectedChecksum = Base16Utils.decodeBase16(csType.getChecksumValue());
            if(!calculatedChecksum.equals(expectedChecksum)) {
                ResponseInfo responseInfo = new ResponseInfo();
                responseInfo.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
                responseInfo.setResponseText("Wrong checksum! Expected: [" + expectedChecksum 
                        + "], but calculated: [" + calculatedChecksum + "]");
                throw new IllegalOperationException(responseInfo, message.getCollectionID());
            }
        } else {
            // TODO is such a checksum required?
            log.warn("No checksums for validating the retrieved file.");
        }
        getAuditManager().addAuditEvent(message.getCollectionID(), message.getFileID(), message.getFrom(), 
                "Add file to archive.", message.getAuditTrailInformation(), FileAction.PUT_FILE,
                message.getCorrelationID(), messageContext.getCertificateFingerprint());
        getArchives().moveToArchive(message.getFileID(), message.getCollectionID());
        getCsManager().recalculateChecksum(message.getFileID(), message.getCollectionID());
    }
    
    /**
     * Method for sending the final response for the requested put operation.
     * @param message The request requesting the put operation.
     */
    private void sendFinalResponse(PutFileRequest message) {
        PutFileFinalResponse response = createFinalResponse(message);

        ResponseInfo frInfo = new ResponseInfo();
        frInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        response.setResponseInfo(frInfo);
        response.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR
        
        if(message.getChecksumRequestForNewFile() != null) {
            response.setChecksumDataForNewFile(getCsManager().getChecksumDataForFile(message.getFileID(),
                    message.getCollectionID(), message.getChecksumRequestForNewFile()));
        } else {
            // TODO is such a request required?
            log.info("No checksum validation requested.");
        }

        dispatchResponse(response, message);
    }
    
    
    /**
     * Creates a PutFileProgressResponse based on a PutFileRequest. Missing the 
     * following fields:
     * <br/> - AuditTrailInformation
     * <br/> - PillarChecksumSpec
     * <br/> - ProgressResponseInfo
     * 
     * @param request The PutFileRequest to base the progress response on.
     * @return The PutFileProgressResponse based on the request.
     */
    private PutFileProgressResponse createPutFileProgressResponse(PutFileRequest request) {
        PutFileProgressResponse res = new PutFileProgressResponse();
        res.setFileAddress(request.getFileAddress());
        res.setFileID(request.getFileID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        
        return res;
    }
    
    /**
     * Creates a PutFileFinalResponse based on a PutFileRequest. Missing the
     * following fields:
     * <br/> - AuditTrailInformation
     * <br/> - ChecksumsDataForNewFile
     * <br/> - FinalResponseInfo
     * <br/> - PillarChecksumSpec
     * 
     * @param request The PutFileRequest to base the final response message on.
     * @return The PutFileFinalResponse message based on the request.
     */
    private PutFileFinalResponse createFinalResponse(PutFileRequest request) {
        PutFileFinalResponse res = new PutFileFinalResponse();
        res.setFileAddress(request.getFileAddress());
        res.setFileID(request.getFileID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        
        return res;
    }
}
