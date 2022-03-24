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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.TimeMeasurementUtils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.utils.MessageUtils;
import org.bitrepository.service.exception.IdentifyContributorException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the DeleteFile operation.
 */
public class IdentifyPillarsForDeleteFileRequestHandler
        extends IdentifyRequestHandler<IdentifyPillarsForDeleteFileRequest> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param context The context for the message handling.
     * @param model   The storage model for the pillar.
     */
    protected IdentifyPillarsForDeleteFileRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }

    @Override
    public Class<IdentifyPillarsForDeleteFileRequest> getRequestClass() {
        return IdentifyPillarsForDeleteFileRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(IdentifyPillarsForDeleteFileRequest message) {
        return createFinalResponse(message);
    }


    @Override
    protected void validateRequest(IdentifyPillarsForDeleteFileRequest request, MessageContext messageContext)
            throws RequestHandlerException {
        validateCollectionID(request);
        validateFileIDFormat(request.getFileID());
        checkThatRequestedFileIsAvailable(request);
    }

    @Override
    protected void sendPositiveResponse(IdentifyPillarsForDeleteFileRequest request, MessageContext messageContext) {
        IdentifyPillarsForDeleteFileResponse response = createFinalResponse(request);

        response.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMilliseconds(
                getSettings().getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));

        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        response.setResponseInfo(irInfo);

        dispatchResponse(response, request);
        log.debug(MessageUtils.createMessageIdentifier(request) + " Identified for performing a DeleteFile operation.");
    }

    /**
     * Validates that the requested files are present in the archive.
     * Otherwise, an {@link IdentifyContributorException} with the appropriate error-code is thrown.
     *
     * @param message The message containing the id of the file. If no file id is given, then a warning is logged,
     *                but the operation is accepted.
     */
    private void checkThatRequestedFileIsAvailable(IdentifyPillarsForDeleteFileRequest message)
            throws RequestHandlerException {
        if (!getPillarModel().hasFileID(message.getFileID(), message.getCollectionID())) {
            throw new IdentifyContributorException(ResponseCode.FILE_NOT_FOUND_FAILURE, "File not found.");
        }
    }

    /**
     * Creates a IdentifyPillarsForDeleteFileResponse based on a
     * IdentifyPillarsForDeleteFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - ResponseInfo
     * <br/> - PillarChecksumSpec
     *
     * @param msg The IdentifyPillarsForDeleteFileRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForDeleteFileResponse createFinalResponse(IdentifyPillarsForDeleteFileRequest msg) {
        IdentifyPillarsForDeleteFileResponse res = new IdentifyPillarsForDeleteFileResponse();
        res.setFileID(msg.getFileID());
        res.setPillarID(getPillarModel().getPillarID());
        res.setPillarChecksumSpec(getPillarModel().getChecksumPillarSpec());

        return res;
    }
}
