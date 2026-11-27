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

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.input.BoundedInputStream;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.FilePart;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.utils.FileExchangeResolver;
import org.bitrepository.protocol.utils.MessageUtils;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

public class GetFileRequestHandler extends PerformRequestHandler<GetFileRequest> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param context The context for the message handling.
     * @param model   The storage model for the pillar.
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
        validatePillarID(request.getPillarID());
        validateFileIDFormat(request.getFileID());
        // Parsed here only to reject an invalid address before the progress response is sent.
        toUploadUrl(request.getFileAddress());

        getPillarModel().verifyFileExists(request.getFileID(), request.getCollectionID());
        if (request.getFilePart() != null) {
            FileInfo requestedFi = getPillarModel().getFileInfoForActualFile(request.getFileID(),
                    request.getCollectionID());
            validateFilePart(request.getFilePart(), request.getFileID(), requestedFi.getSize());
        }
    }

    /**
     * Validates that the file part lies within the file, so the part can be delivered in full.
     *
     * @param filePart The requested file part.
     * @param fileID   The id of the requested file.
     * @param fileSize The size of the requested file.
     * @throws InvalidMessageException If the file part is not within the file.
     */
    private void validateFilePart(FilePart filePart, String fileID, long fileSize) throws InvalidMessageException {
        BigInteger offset = filePart.getPartOffSet();
        BigInteger length = filePart.getPartLength();
        if (offset.signum() < 0 || length.signum() < 0
                || offset.add(length).compareTo(BigInteger.valueOf(fileSize)) > 0) {
            throw new InvalidMessageException(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE,
                    "The file part with offset " + offset + " and length " + length + " is not within the file '"
                            + fileID + "' of " + fileSize + " bytes.");
        }
    }

    /**
     * Parses the address to deliver the file to, and checks that there is a file exchange for its protocol.
     *
     * @param fileAddress The address to deliver the file to.
     * @return The address as a URL.
     * @throws InvalidMessageException If the address is not a valid absolute URL with a supported protocol.
     */
    private static URL toUploadUrl(String fileAddress) throws InvalidMessageException {
        try {
            URL uploadUrl = new URI(fileAddress).toURL();
            FileExchangeResolver.getBasicFileExchangeFromURL(uploadUrl);
            return uploadUrl;
        } catch (URISyntaxException | MalformedURLException | IllegalArgumentException e) {
            throw new InvalidMessageException(ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE,
                    "Invalid file address '" + fileAddress + "', cause: " + e.getMessage(), e);
        }
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
        log.info("{} Performing GetFile for file '{}' on collection '{}'",
                MessageUtils.createMessageIdentifier(request), request.getFileID(), request.getCollectionID());
        uploadToClient(request);
        getAuditManager().addAuditEvent(request.getCollectionID(), request.getFileID(), request.getFrom(),
                "Failed identifying pillar.", request.getAuditTrailInformation(), FileAction.GET_FILE,
                request.getCorrelationID(), requestContext.certificateFingerprint());
        sendFinalResponse(request);
    }

    /**
     * Method for uploading the file to the requested location.
     *
     * @param message The message requesting the GetFile operation.
     * @throws InvalidMessageException If the upload of the file fails.
     */
    protected void uploadToClient(GetFileRequest message) throws RequestHandlerException {
        FileInfo requestedFile = getPillarModel().getFileInfoForActualFile(message.getFileID(),
                message.getCollectionID());
        String fileAddress = message.getFileAddress();
        URL uploadUrl = toUploadUrl(fileAddress);
        FilePart filePart = message.getFilePart();
        if (filePart != null) {
            log.debug("Extracting {} bytes with offset {} from {}", filePart.getPartLength(),
                    filePart.getPartOffSet(), message.getFileID());
        }

        try (InputStream is = filePart == null
                ? requestedFile.getInputStream()
                : extractFilePart(requestedFile, filePart)) {
            log.info("Uploading file '{}' to {}", message.getFileID(), fileAddress);
            FileExchange fileExchange = FileExchangeResolver.getBasicFileExchangeFromURL(uploadUrl);
            fileExchange.putFile(is, uploadUrl);
        } catch (IOException e) {
            log.warn("The file '{}' from collection '{}' could not be uploaded at '{}' cause: '{}'",
                    message.getFileID(), message.getCollectionID(), fileAddress, e.getMessage());
            throw new InvalidMessageException(ResponseCode.FILE_TRANSFER_FAILURE,
                    "Could not deliver file to address '" + fileAddress + "', cause: " + e.getMessage(), e);
        }
    }

    /**
     * Extracts a given file part as a stream, so the part is not buffered in memory.
     *
     * @param fileInfo The requested file to extract the file part from.
     * @param filePart The defined interval for the file part. It must lie within the file.
     * @return A InputStream with the requested file part, which the caller must close. Reading it fails with an
     * {@link EOFException} if the file ends before the part does.
     * @throws IOException If the file cannot be read.
     */
    static InputStream extractFilePart(FileInfo fileInfo, FilePart filePart) throws IOException {
        long offset = filePart.getPartOffSet().longValueExact();
        long length = filePart.getPartLength().longValueExact();
        ArgumentValidator.checkNotNegative(offset, "long offset");
        ArgumentValidator.checkNotNegative(length, "long length");
        String endedEarlyMessage = "The stored file '" + fileInfo.getFileID() + "' ended before the end of the "
                + "requested part with offset " + offset + " and length " + length
                + ". It may have been replaced since the request was validated.";
        InputStream fis = fileInfo.getInputStream();
        try {
            fis.skipNBytes(offset);
            return BoundedInputStream.builder().setInputStream(fis).setMaxCount(length)
                    .setAfterRead(n -> {
                        if (n == IOUtils.EOF) {
                            throw new EOFException(endedEarlyMessage);
                        }
                    })
                    .get();
        } catch (IOException e) {
            try {
                fis.close();
            } catch (IOException closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw e;
        }
    }

    /**
     * Method for sending the final response.
     *
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
     *
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
     *
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
