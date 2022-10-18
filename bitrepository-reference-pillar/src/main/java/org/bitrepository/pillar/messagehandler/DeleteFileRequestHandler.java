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
package org.bitrepository.pillar.messagehandler;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
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

public class DeleteFileRequestHandler extends PerformRequestHandler<DeleteFileRequest> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param context The context for the message handling.
     * @param model   The storage model for the pillar.
     */
    protected DeleteFileRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }

    @Override
    public Class<DeleteFileRequest> getRequestClass() {
        return DeleteFileRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(DeleteFileRequest request) {
        return createFinalResponse(request);
    }

    @Override
    protected void validateRequest(DeleteFileRequest request, MessageContext messageContext)
            throws RequestHandlerException {
        validateCollectionID(request);
        validatePillarId(request.getPillarID());
        getPillarModel().verifyChecksumAlgorithm(request.getChecksumRequestForExistingFile());
        if (request.getChecksumDataForExistingFile() != null) {
            getPillarModel().verifyChecksumAlgorithm(request.getChecksumDataForExistingFile().getChecksumSpec());
        } else if (getSettings().getRepositorySettings().getProtocolSettings()
                .isRequireChecksumForDestructiveRequests()) {
            throw new IllegalOperationException(ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE, "No mandatory checksum "
                    + "for destructive operation was supplied.", request.getFileID());
        }

        validateFileIDFormat(request.getFileID());

        // Validate, that we have the requested file.
        if (!getPillarModel().hasFileID(request.getFileID(), request.getCollectionID())) {
            throw new InvalidMessageException(ResponseCode.FILE_NOT_FOUND_FAILURE, "File not found.");
        }

        // calculate and validate the checksum of the file.
        ChecksumDataForFileTYPE checksumData = request.getChecksumDataForExistingFile();
        if (checksumData != null) {
            ChecksumSpecTYPE checksumType = checksumData.getChecksumSpec();

            String calculatedChecksum = getPillarModel().getChecksumForFile(request.getFileID(),
                    request.getCollectionID(), checksumType);
            String requestedChecksum = Base16Utils.decodeBase16(checksumData.getChecksumValue());
            if (!calculatedChecksum.equals(requestedChecksum)) {
                // Log the different checksums, but do not send the right checksum back!
                log.warn("Failed to handle delete operation on file '{}' since the request had the checksum '{}'" +
                        " where our local file has the value '{}'. Sending alarm and respond failure.",
                        request.getFileID(), requestedChecksum, calculatedChecksum);

                throw new IllegalOperationException(ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE, "Cannot delete file "
                        + "due to inconsistency between checksums.", request.getFileID());
            }
        } else {
            log.debug("No checksum for validation of the existing file before deleting the file '{}'",
                    request.getFileID());
        }

        log.debug(MessageUtils.createMessageIdentifier(request) + "' validated and accepted.");
    }

    /**
     * The method for sending a progress response telling, that the operation is about to be performed.
     *
     * @param request The request for the DeleteFile operation.
     */
    @Override
    protected void sendProgressResponse(DeleteFileRequest request, MessageContext requestContext) {
        DeleteFileProgressResponse response = createDeleteFileProgressResponse(request);

        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        response.setResponseInfo(prInfo);

        dispatchResponse(response, request);
    }

    @Override
    protected void performOperation(DeleteFileRequest request, MessageContext requestContext)
            throws RequestHandlerException {
        log.info("{} Deleting file '{}' in collection '{}'", MessageUtils.createMessageIdentifier(request),
                request.getFileID(), request.getCollectionID());
        ChecksumDataForFileTYPE resultingChecksum = calculatedRequestedChecksum(request);
        deleteTheFile(request);
        getAuditManager().addAuditEvent(request.getCollectionID(), request.getFileID(), request.getFrom(),
                "Deleting the file.", request.getAuditTrailInformation(), FileAction.DELETE_FILE,
                request.getCorrelationID(), requestContext.getCertificateFingerprint());
        sendFinalResponse(request, resultingChecksum);
    }

    /**
     * Method for calculating the requested checksum.
     * If no checksum is requested to be delivered back a warning is logged.
     *
     * @param message The request for deleting the file. Contains the specs for calculating the checksum.
     * @return The requested checksum, or null if no such checksum is requested.
     * @throws RequestHandlerException If the requested checksum specification is not supported.
     */
    private ChecksumDataForFileTYPE calculatedRequestedChecksum(DeleteFileRequest message)
            throws RequestHandlerException {
        ChecksumSpecTYPE checksumType = message.getChecksumRequestForExistingFile();
        if (checksumType == null) {
            return null;
        }

        return getPillarModel().getChecksumDataForFile(message.getFileID(), message.getCollectionID(), checksumType);
    }

    /**
     * Performs the operation of deleting the file from the archive.
     *
     * @param message The message requesting the file to be deleted.
     */
    private void deleteTheFile(DeleteFileRequest message) {
        getPillarModel().deleteFile(message.getFileID(), message.getCollectionID());
    }

    /**
     * Method for sending the final response.
     *
     * @param request           The request to respond to.
     * @param requestedChecksum The results of the requested checksums
     */
    private void sendFinalResponse(DeleteFileRequest request, ChecksumDataForFileTYPE requestedChecksum) {
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
        res.setPillarID(getPillarModel().getPillarID());

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
        res.setPillarID(getPillarModel().getPillarID());

        return res;
    }
}
