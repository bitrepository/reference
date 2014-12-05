/*
 * #%L
 * Bitrepository Reference Pillar
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.common.utils.Base16Utils;
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
 * Class for handling the ReplaceFile operation.
 * In the positive scenario: <ul> 
 * <li> It starts by validating the message </li> 
 * <li> Afterwards it sends a positive progress to tell that the new file is being downloaded </li>
 * <li> Then the new file is downloaded to 'tempDir' </li>
 * <li> A progress message is then send to tell that the new file is replacing the old one.</li>
 * <li> Then it performs the replacement of the file by deleting the old one (moving it from 'fileDir' to 'retainDir') 
 * and then moves the new file from 'tempDir' to 'fileDir'</li>
 * <li> Finally the complete message is send.</li>
 * </ul>
 * If anything goes wrong a FinalResponse is send telling about the failure.
 */
public class ReplaceFileRequestHandler extends PerformRequestHandler<ReplaceFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * @param context The context for the message handling.
     * @param model The storage model for the pillar.
     */
    protected ReplaceFileRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }

    @Override
    public Class<ReplaceFileRequest> getRequestClass() {
        return ReplaceFileRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(ReplaceFileRequest message) {
        return createFinalResponse(message);
    }
    
    @Override
    protected void validateRequest(ReplaceFileRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        validateCollectionID(request);
        validatePillarId(request.getPillarID());
        
        if(!getPillarModel().hasFileID(request.getFileID(), request.getCollectionID())) {
            throw new InvalidMessageException(ResponseCode.FILE_NOT_FOUND_FAILURE, "The file '" + request.getFileID() 
                    + "' has been requested, but we do not have that file!", request.getCollectionID());
        }

        getPillarModel().verifyEnoughFreeSpaceLeftForFile(request.getFileSize().longValue(), 
                request.getCollectionID());

        validateChecksumAlgorithms(request);
        validateExistingChecksum(request);
    }

    @Override
    protected void sendProgressResponse(ReplaceFileRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        ReplaceFileProgressResponse response = createProgressResponse(request);
        String responseText = "Performing the ReplaceFileRequest for file '" + request.getFileID() + "' at '"
                + request.getCollectionID() + "'.";
        log.debug(responseText);
        
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        responseInfo.setResponseText(responseText);
        response.setResponseInfo(responseInfo);

        dispatchResponse(response, request);
    }

    @Override
    protected void performOperation(ReplaceFileRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        log.info(MessageUtils.createMessageIdentifier(request) + " Performing ReplaceFile for file " 
                + request.getFileID() + " on collection " + request.getCollectionID());
        try {
            ChecksumDataForFileTYPE requestedOldChecksum = calculateChecksumOnOldFile(request);
            replaceFile(request);
            getAuditManager().addAuditEvent(request.getCollectionID(), request.getFileID(), request.getFrom(), 
                    "Replacing the file.", request.getAuditTrailInformation(), FileAction.REPLACE_FILE,
                    request.getCorrelationID(), requestContext.getCertificateFingerprint());

            ChecksumDataForFileTYPE requestedNewChecksum = calculateChecksumOnNewFile(request);
            sendFinalResponse(request, requestedOldChecksum, requestedNewChecksum);
        } finally {
            getPillarModel().ensureFileNotInTmpDir(request.getFileID(), request.getCollectionID());
        }
    }
    
    /**
     * Validates that the checksum algorithms in the request are supported, and that the request contains the required
     * checksums for destructive and new file requests.
     * @param request The request.
     * @throws RequestHandlerException If one (or more) of the algorithms are not supported, or if the required 
     * checksums are not given. 
     */
    private void validateChecksumAlgorithms(ReplaceFileRequest request) throws RequestHandlerException {
        getPillarModel().verifyChecksumAlgorithm(request.getChecksumRequestForExistingFile(), 
                request.getCollectionID());
        getPillarModel().verifyChecksumAlgorithm(request.getChecksumRequestForNewFile(), request.getCollectionID());
        if(request.getChecksumDataForExistingFile() != null) {
            getPillarModel().verifyChecksumAlgorithm(request.getChecksumDataForExistingFile().getChecksumSpec(), 
                    request.getCollectionID());
        } else if(getSettings().getRepositorySettings().getProtocolSettings()
                .isRequireChecksumForDestructiveRequests()) {
            throw new IllegalOperationException(ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE, "According to the "
                    + "contract a checksum for file to be deleted during the replacing operation is required.", 
                    request.getCollectionID(), request.getFileID());
        }
        if(request.getChecksumDataForNewFile() != null) {
            getPillarModel().verifyChecksumAlgorithm(request.getChecksumDataForNewFile().getChecksumSpec(), 
                    request.getCollectionID());
        } else if(getSettings().getRepositorySettings().getProtocolSettings()
                .isRequireChecksumForNewFileRequests()) {
            throw new IllegalOperationException(ResponseCode.NEW_FILE_CHECKSUM_FAILURE, "According to the contract a "
                    + "checksum for new file in the replacing operation is required.",
                    request.getCollectionID(), request.getFileID());
        }
    }
    
    /**
     * Validates the the existing file has the expected checksum.
     * @param request The request containing info about which file and which checksum is expected.
     * @throws RequestHandlerException If the file does not have the expected checksum.
     */
    private void validateExistingChecksum(ReplaceFileRequest request) throws RequestHandlerException {
        ChecksumDataForFileTYPE checksumData = request.getChecksumDataForExistingFile();
        if(checksumData != null) {
            ChecksumSpecTYPE checksumType = checksumData.getChecksumSpec();
            String calculatedChecksum = getPillarModel().getChecksumForFile(request.getFileID(), 
                    request.getCollectionID(), checksumType);
            String requestedChecksum = Base16Utils.decodeBase16(checksumData.getChecksumValue());
            if(!calculatedChecksum.equals(requestedChecksum)) {
                // Log the different checksums, but do not send the right checksum back!
                log.warn("Failed to handle replace operation on file '" + request.getFileID() + "' since the request had "
                        + "the checksum '" + requestedChecksum + "' where our local file has the value '" 
                        + calculatedChecksum + "'. Sending alarm and respond failure.");
                String errMsg = "Requested to replace the file '" + request.getFileID() + "' with checksum '"
                        + requestedChecksum + "', but our file had a different checksum.";
                
                throw new IllegalOperationException(ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE, errMsg, 
                        request.getCollectionID(), request.getFileID());
            }
        } else {
            log.debug("No checksum for validation of the existing file before replace.");
        }
    }
    
    /**
     * Calculates the checksum for the old file.
     * ONLY USE BEFORE THE REPLACE OPERATION HAS BEEN PERFORMED!
     * @param message The message containing the request for the checksum to be calculated, along with the 
     * specification about which algorithm and salt to use.
     * @return The checksum data for the old file in the archive.
     * If no checksum specification for the 'old' file has been defined, then an null is returned.
     * @throws RequestHandlerException If the checksum of the requested type cannot be retrieved.
     */
    private ChecksumDataForFileTYPE calculateChecksumOnOldFile(ReplaceFileRequest message) throws RequestHandlerException {
        ChecksumSpecTYPE csType = message.getChecksumRequestForExistingFile();
        if(csType != null) {
            return getPillarModel().getChecksumDataForFile(message.getFileID(), message.getCollectionID(), csType);
        }
        
        return null;
    }
    
    /**
     * Perform the replaceFile operation.
     * 
     * Downloading the new file to replace the old one. 
     * Also validates against the given checksum. Will log a warning, if no checksum for validation is in the request.
     * @param message The request containing the location of the file and the checksum of it.
     */
    private void replaceFile(ReplaceFileRequest message) throws RequestHandlerException {
        log.info("Replacing the file '" + message.getFileID() + "' in the archive with the one in the "
                + "temporary area.");
        getPillarModel().replaceFile(message.getFileID(), message.getCollectionID(), 
                message.getFileAddress(), message.getChecksumDataForNewFile());
    }

    /**
     * Calculates the checksum for the new file.
     * ONLY USE WHEN THE REPLACE OPERATION HAS BEEN PERFORMED!
     * @param message The message containing the request for the checksum to be calculated, along with the 
     * specification about which algorithm and salt to use.
     * @return The checksum data for the new file in the archive.
     * If no checksum specification for the 'old' file has been defined, then an null is returned.
     * @throws RequestHandlerException If the checksum of the requested type cannot be retrieved.
     */
    private ChecksumDataForFileTYPE calculateChecksumOnNewFile(ReplaceFileRequest message) 
            throws RequestHandlerException {
        ChecksumSpecTYPE csType = message.getChecksumRequestForNewFile();
        if(csType != null) {
            return getPillarModel().getChecksumDataForFile(message.getFileID(), message.getCollectionID(), csType);
        }
        
        return null;
    }
    
    /**
     * Sends the final response for a successful finish of the operation.
     * @param request The request to base the response upon.
     * @param requestedOldChecksum The requested checksum for the old file, or null if no checksum for the old file.
     * @param requestedNewChecksum The requested checksum for the new file, or null if no checksum for the new file.
     */
    private void sendFinalResponse(ReplaceFileRequest request, ChecksumDataForFileTYPE requestedOldChecksum,
            ChecksumDataForFileTYPE requestedNewChecksum) {
        ReplaceFileFinalResponse response = createFinalResponse(request);
        
        ResponseInfo ri = new ResponseInfo();
        ri.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        response.setResponseInfo(ri);
        
        response.setChecksumDataForNewFile(requestedNewChecksum);
        response.setChecksumDataForExistingFile(requestedOldChecksum);

        dispatchResponse(response, request);
    }
    
    /**
     * Creates the generic ReplaceFileFinalResponse based on the request.
     * @param request The ReplaceFileRequest to base the response upon.
     * @return The ReplaceFileFinalResponse based on the request.
     */
    private ReplaceFileProgressResponse createProgressResponse(ReplaceFileRequest request) {
        ReplaceFileProgressResponse res = new ReplaceFileProgressResponse();
        res.setFileAddress(request.getFileAddress());
        res.setFileID(request.getFileID());
        res.setPillarID(getPillarModel().getPillarID());
        res.setPillarChecksumSpec(getPillarModel().getChecksumPillarSpec());

        return res;
    }
    
    /**
     * Creates the generic ReplaceFileFinalResponse based on the request.
     * @param request The ReplaceFileRequest to base the response upon.
     * @return The ReplaceFileFinalResponse based on the request.
     */
    private ReplaceFileFinalResponse createFinalResponse(ReplaceFileRequest request) {
        ReplaceFileFinalResponse res = new ReplaceFileFinalResponse();
        res.setFileAddress(request.getFileAddress());
        res.setFileID(request.getFileID());
        res.setPillarID(getPillarModel().getPillarID());
        res.setPillarChecksumSpec(getPillarModel().getChecksumPillarSpec());

        return res;
    }
}
