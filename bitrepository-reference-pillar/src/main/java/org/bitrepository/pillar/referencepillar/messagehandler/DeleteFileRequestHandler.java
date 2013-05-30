/*
 * #%L
 * Bitrepository Reference Pillar
 * 
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

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.referencepillar.archive.ReferenceChecksumManager;
import org.bitrepository.service.exception.IllegalOperationException;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the DeleteFile operation.
 */
public class DeleteFileRequestHandler extends ReferencePillarMessageHandler<DeleteFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * @param context The context for the pillar.
     * @param archivesManager The manager of the archives.
     * @param csManager The checksum manager for the pillar.
     */
    protected DeleteFileRequestHandler(MessageHandlerContext context, FileStore archivesManager, 
            ReferenceChecksumManager csManager) {
        super(context, archivesManager, csManager);
    }

    @Override
    public Class<DeleteFileRequest> getRequestClass() {
        return DeleteFileRequest.class;
    }

    @Override
    public void processRequest(DeleteFileRequest message) throws RequestHandlerException {
        validateMessage(message);
        sendProgressMessage(message);
        ChecksumDataForFileTYPE resultingChecksum = calculatedRequestedChecksum(message);
        deleteTheFile(message);
        sendFinalResponse(message, resultingChecksum);
    }

    @Override
    public MessageResponse generateFailedResponse(DeleteFileRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Method for validating the content of the message.
     * @param message The message requesting the operation, which should be validated.
     * @return Whether it was valid.
     */
    protected void validateMessage(DeleteFileRequest message) throws RequestHandlerException {
        validateCollectionID(message);
        validatePillarId(message.getPillarID());
        validateChecksumSpecification(message.getChecksumRequestForExistingFile());
        if(message.getChecksumDataForExistingFile() != null) {
            validateChecksumSpecification(message.getChecksumDataForExistingFile().getChecksumSpec());
        } else if(getSettings().getRepositorySettings().getProtocolSettings()
                .isRequireChecksumForDestructiveRequests()) {
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
            responseInfo.setResponseText("No checksum was supplied for the file to delete, even though according to "
                    + "the contract a checksum for file to be deleted during the deleting operation is required.");
            throw new IllegalOperationException(responseInfo);
        }
        
        validateFileID(message.getFileID());

        // Validate, that we have the requested file.
        if(!getArchives().hasFile(message.getFileID(), message.getCollectionID())) {
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            responseInfo.setResponseText("The file '" + message.getFileID() + "' has been requested, but we do "
                    + "not have that file!");
            throw new InvalidMessageException(responseInfo);
        }
        
        // calculate and validate the checksum of the file.
        ChecksumDataForFileTYPE checksumData = message.getChecksumDataForExistingFile();
        if(checksumData != null) {
            ChecksumSpecTYPE checksumType = checksumData.getChecksumSpec();

            getAuditManager().addAuditEvent(message.getCollectionID(), message.getFileID(), message.getFrom(), 
                    "Calculating the validation checksum on the file, which should be deleted.", 
                    message.getAuditTrailInformation(), FileAction.CHECKSUM_CALCULATED);
            String calculatedChecksum = getCsManager().getChecksumForFile(message.getFileID(), 
                    message.getCollectionID(), checksumType);
            String requestedChecksum = Base16Utils.decodeBase16(checksumData.getChecksumValue());
            if(!calculatedChecksum.equals(requestedChecksum)) {
                // Log the different checksums, but do not send the right checksum back!
                log.warn("Failed to handle delete operation on file '" + message.getFileID() + "' since the request "
                        + "had the checksum '" + requestedChecksum + "' where our local file has the value '" 
                        + calculatedChecksum + "'. Sending alarm and respond failure.");
                String errMsg = "Requested to delete file '" + message.getFileID() + "' with checksum '"
                + requestedChecksum + "', but our file had a different checksum.";

                ResponseInfo responseInfo = new ResponseInfo();
                responseInfo.setResponseCode(ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
                responseInfo.setResponseText(errMsg);
                throw new IllegalOperationException(responseInfo);
            }
        } else {
            log.debug("No checksum for validation of the existing file before delete the file '" + message.getFileID() 
                    + "'");
        }
    }

    /**
     * The method for sending a progress response telling, that the operation is about to be performed.
     * @param request The request for the DeleteFile operation.
     */
    protected void sendProgressMessage(DeleteFileRequest request) {
        DeleteFileProgressResponse response = createDeleteFileProgressResponse(request);

        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        response.setResponseInfo(prInfo);

        dispatchResponse(response, request);
    }
    
    /**
     * Method for calculating the requested checksum. 
     * If no checksum is requested to be delivered back a warning is logged.
     * @param message The request for deleting the file. Contains the specs for calculating the checksum.
     * @return The requested checksum, or null if no such checksum is requested.
     */
    protected ChecksumDataForFileTYPE calculatedRequestedChecksum(DeleteFileRequest message) {
        ChecksumSpecTYPE checksumType = message.getChecksumRequestForExistingFile();
        if(checksumType == null) {
            return null;
        }
        
        getAuditManager().addAuditEvent(message.getCollectionID(), message.getFileID(), message.getFrom(), 
                "Calculating the requested checksum on the file, which should be deleted.", 
                message.getAuditTrailInformation(), FileAction.CHECKSUM_CALCULATED);
        return getCsManager().getChecksumDataForFile(message.getFileID(), message.getCollectionID(), checksumType);
    }
    
    /**
     * Performs the operation of deleting the file from the archive.
     * @param message The message requesting the file to be deleted.
     */
    protected void deleteTheFile(DeleteFileRequest message) throws RequestHandlerException {
        getAuditManager().addAuditEvent(message.getCollectionID(), message.getFileID(), message.getFrom(), 
                "Deleting the file.", message.getAuditTrailInformation(), FileAction.DELETE_FILE);
        getArchives().deleteFile(message.getFileID(), message.getCollectionID());
        getCsManager().deleteEntry(message.getFileID(), message.getCollectionID());
    }

    /**
     * Method for sending the final response.
     * @param request The request to respond to.
     * @param requestedChecksum The results of the requested checksums
     */
    protected void sendFinalResponse(DeleteFileRequest request, ChecksumDataForFileTYPE requestedChecksum) {
        DeleteFileFinalResponse response = createFinalResponse(request);
        response.setChecksumDataForExistingFile(requestedChecksum);
        ResponseInfo frInfo = new ResponseInfo();
        frInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        response.setResponseInfo(frInfo);

        dispatchResponse(response, request);
    }
    
    /**
     * Creates a DeleteFileProgressResponse based on a DeleteFileRequest. Missing the 
     * following fields:
     * <br/> - ResponseInfo
     * 
     * @param request The DeleteFileRequest to base the progress response on.
     * @return The DeleteFileProgressResponse based on the request.
     */
    private DeleteFileProgressResponse createDeleteFileProgressResponse(DeleteFileRequest request) {
        DeleteFileProgressResponse res = new DeleteFileProgressResponse();
        res.setFileID(request.getFileID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        
        return res;
    }
    
    /**
     * Creates a DeleteFileFinalResponse based on a DeleteFileRequest. Missing the 
     * following fields:
     * <br/> - ResponseInfo
     * <br/> - ChecksumDataForFile
     * 
     * @param msg The DeleteFileRequest to base the final response on.
     * @return The DeleteFileFinalResponse based on the request.
     */
    private DeleteFileFinalResponse createFinalResponse(DeleteFileRequest msg) {
        DeleteFileFinalResponse res = new DeleteFileFinalResponse();
        res.setFileID(msg.getFileID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());

        return res;
    }
}
