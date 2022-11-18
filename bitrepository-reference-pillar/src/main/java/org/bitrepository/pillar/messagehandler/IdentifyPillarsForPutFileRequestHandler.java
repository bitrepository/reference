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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.utils.MessageUtils;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;

/**
 * Class for handling the identification of this pillar for the purpose of performing the PutFile operation.
 */
public class IdentifyPillarsForPutFileRequestHandler extends IdentifyRequestHandler<IdentifyPillarsForPutFileRequest> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param context The context for the message handling.
     * @param model   The storage model for the pillar.
     */
    protected IdentifyPillarsForPutFileRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }

    @Override
    public Class<IdentifyPillarsForPutFileRequest> getRequestClass() {
        return IdentifyPillarsForPutFileRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForPutFileRequest message) {
        return createFinalResponse(message);
    }

    @Override
    public void validateRequest(IdentifyPillarsForPutFileRequest request, MessageContext messageContext) throws RequestHandlerException {
        validateCollectionID(request);
        validateFileIDFormat(request.getFileID());
    }

    @Override
    protected void sendPositiveResponse(IdentifyPillarsForPutFileRequest request, MessageContext requestContext)
            throws RequestHandlerException {
        if (checkThatTheFileDoesNotAlreadyExist(request)) {
            respondDuplicateFile(request);
        } else {
            checkSpaceForStoringNewFile(request);
            respondSuccessfulIdentification(request);
        }
    }

    /**
     * Validates that the file is not already within the archive.
     *
     * @param message The request with the filename to validate.
     * @return Whether the file already exists.
     */
    private boolean checkThatTheFileDoesNotAlreadyExist(IdentifyPillarsForPutFileRequest message) {
        if (message.getFileID() == null) {
            log.debug("No fileID given in the identification request.");
            return false;
        }

        return getPillarModel().hasFileID(message.getFileID(), message.getCollectionID());
    }

    /**
     * Validates that enough space exists is left in the archive.
     *
     * @param message The request with the size of the file.
     * @throws RequestHandlerException If there is not enough space free to store the file.
     */
    private void checkSpaceForStoringNewFile(IdentifyPillarsForPutFileRequest message) throws RequestHandlerException {
        BigInteger fileSize = message.getFileSize();
        if (fileSize == null) {
            log.debug("No file size given in the identification request." +
                    " Validating that the archive has any space left.");
            fileSize = BigInteger.ZERO;
        }

        getPillarModel().verifyEnoughFreeSpaceLeftForFile(fileSize.longValue(), message.getCollectionID());
    }

    /**
     * Method for sending a response for 'DUPLICATE_FILE_FAILURE'.
     *
     * @param message The request to base the response upon.
     * @throws RequestHandlerException If the checksum of the file from the request could not be verified.
     */
    private void respondDuplicateFile(IdentifyPillarsForPutFileRequest message) throws RequestHandlerException {
        IdentifyPillarsForPutFileResponse response = createFinalResponse(message);

        response.setReplyTo(getSettings().getReceiverDestinationID());
        response.setTimeToDeliver(getTimeToStartDeliver());
        response.setChecksumDataForExistingFile(getPillarModel().getChecksumDataForFile(message.getFileID(), message.getCollectionID(),
                ChecksumUtils.getDefault(getSettings())));


        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
        response.setResponseInfo(irInfo);

        dispatchResponse(response, message);
        log.debug("{} Duplicate file identification for PutFile operation",
                MessageUtils.createMessageIdentifier(message));
    }

    /**
     * Method for sending a positive response for putting this file.
     *
     * @param request The request to respond to.
     */
    private void respondSuccessfulIdentification(IdentifyPillarsForPutFileRequest request) {
        IdentifyPillarsForPutFileResponse response = createFinalResponse(request);

        // Needs to filled in: AuditTrailInformation, PillarChecksumSpec, ReplyTo, TimeToDeliver
        response.setReplyTo(getSettings().getContributorDestinationID());
        response.setTimeToDeliver(getTimeToStartDeliver());

        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        response.setResponseInfo(irInfo);

        dispatchResponse(response, request);
        log.debug("{} Identified for performing a PutFile operation.", MessageUtils.createMessageIdentifier(request));
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
        res.setPillarID(getPillarModel().getPillarID());
        res.setPillarChecksumSpec(getPillarModel().getChecksumPillarSpec());
        res.setFileID(msg.getFileID());

        return res;
    }
}
