/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: GetFileIDsRequestHandler.java 685 2012-01-06 16:35:17Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org
 * /bitrepository/pillar/messagehandler/GetFileIDsRequestHandler.java $
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

import org.apache.activemq.util.ByteArrayInputStream;
import org.bitrepository.bitrepositorydata.GetFileIDsResults;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedFileIDsResultSet;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.utils.MessageUtils;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

import javax.xml.bind.JAXBException;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

public class GetFileIDsRequestHandler extends PerformRequestHandler<GetFileIDsRequest> {
    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param context The context for the message handling.
     * @param model   The storage model for the pillar.
     */
    protected GetFileIDsRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }

    @Override
    public Class<GetFileIDsRequest> getRequestClass() {
        return GetFileIDsRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(GetFileIDsRequest request) {
        return createFinalResponse(request);
    }

    @Override
    protected void validateRequest(GetFileIDsRequest request, MessageContext requestContext)
            throws RequestHandlerException {
        validateCollectionID(request);
        validatePillarID(request.getPillarID());
        if (request.getFileIDs() != null && request.getFileIDs().getFileID() != null) {
            validateFileIDFormat(request.getFileIDs().getFileID());
            verifyFileIDExistence(request.getFileIDs(), request.getCollectionID());
        }

        log.debug("{} validated and accepted.", MessageUtils.createMessageIdentifier(request));
    }

    @Override
    protected void sendProgressResponse(GetFileIDsRequest request, MessageContext requestContext) {
        GetFileIDsProgressResponse response = createProgressResponse(request);

        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Starting to locate files.");
        response.setResponseInfo(prInfo);

        dispatchResponse(response, request);
    }

    @Override
    protected void performOperation(GetFileIDsRequest request, MessageContext requestContext)
            throws RequestHandlerException {
        log.debug("{} Performing GetFileIDs for file(s) {} on collection '{}'",
                MessageUtils.createMessageIdentifier(request), request.getFileIDs(), request.getCollectionID());
        ExtractedFileIDsResultSet extractedFileIDs = retrieveFileIDsData(request);
        ResultingFileIDs results = new ResultingFileIDs();
        if (request.getResultAddress() == null) {
            results.setFileIDsData(extractedFileIDs.getEntries());
        } else {
            uploadResults(request, extractedFileIDs);
            results.setResultAddress(request.getResultAddress());
        }
        sendFinalResponse(request, results, extractedFileIDs);
    }

    /**
     * Retrieves the requested FileIDs. Depending on which operation is requested, it will call the appropriate method.
     *
     * @param message The requested for extracting the file ids.
     * @return The resulting collection of FileIDs found.
     */
    private ExtractedFileIDsResultSet retrieveFileIDsData(GetFileIDsRequest message) {
        Long maxResults = null;
        if (message.getMaxNumberOfResults() != null) {
            maxResults = message.getMaxNumberOfResults().longValue();
        }
        return getPillarModel().getFileIDsResultSet(message.getFileIDs().getFileID(), message.getMinTimestamp(),
                message.getMaxTimestamp(), maxResults, message.getCollectionID());
    }

    /**
     * Uploads the results to the URL in the request.
     *
     * @param request          The request.
     * @param extractedFileIDs The extracted file ids.
     * @throws RequestHandlerException If the file with the results could not be created, or could not be uploaded.
     */
    private void uploadResults(GetFileIDsRequest request, ExtractedFileIDsResultSet extractedFileIDs)
            throws RequestHandlerException {
        String resultingAddress = request.getResultAddress();
        try {
            File outputFile = makeTemporaryResultFile(request, extractedFileIDs.getEntries());
            uploadFile(outputFile, resultingAddress);
        } catch (Exception e) {
            throw new InvalidMessageException(ResponseCode.FILE_TRANSFER_FAILURE, "Could not deliver results.", e);
        }
    }

    /**
     * Method for creating a file containing the resulting list of file ids.
     *
     * @param request The GetFileIDsMessage requesting the checksum calculations.
     * @param fileIDs The file ids to be put into the result file.
     * @return A file containing all the checksums in the list.
     * @throws IOException   If a problem occurs during accessing or handling the data.
     * @throws JAXBException If the resulting structure cannot be serialized or if it is invalid.
     */
    private File makeTemporaryResultFile(GetFileIDsRequest request, FileIDsData fileIDs)
            throws IOException, JAXBException {
        // Create the temporary file.
        File checksumResultFile = File.createTempFile(request.getCorrelationID(), new Date().getTime() + ".id");
        log.info("Writing the requested fileIDs to the file '{}'", checksumResultFile);

        // Print all the file ids data safely (close the streams!)
        try (OutputStream is = new FileOutputStream(checksumResultFile)) {
            GetFileIDsResults result = new GetFileIDsResults();
            result.setCollectionID(request.getCollectionID());
            result.setMinVersion(MIN_VERSION);
            result.setVersion(VERSION);
            result.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
            result.setFileIDsData(fileIDs);

            JaxbHelper jaxbHelper = new JaxbHelper(XSD_CLASSPATH, XSD_BR_DATA);
            String file = jaxbHelper.serializeToXml(result);
            try {
                jaxbHelper.validate(new ByteArrayInputStream(file.getBytes(StandardCharsets.UTF_8)));
            } catch (SAXException e) {
                String errMsg = "The resulting XML for the GetFileIDsRequest does not validate. \n"
                        + file;
                log.error(errMsg, e);
                throw new JAXBException(errMsg, e);
            }
            is.write(file.getBytes(StandardCharsets.UTF_8));
            is.flush();
        }

        return checksumResultFile;
    }

    /**
     * Method for uploading a file to a given URL.
     *
     * @param fileToUpload The File to upload.
     * @param url          The location where the file should be uploaded.
     * @throws IOException If something goes wrong.
     */
    private void uploadFile(File fileToUpload, String url) throws IOException {
        URL uploadUrl = new URL(url);

        log.debug("Uploading file '{}' to {}", fileToUpload.getName(), url);
        try (InputStream in = new BufferedInputStream(new FileInputStream(fileToUpload))) {
            context.getFileExchange().putFile(in, uploadUrl);
        }
    }

    /**
     * Send a positive final response telling that the operation has successfully finished.
     *
     * @param request          The request to base the final response upon.
     * @param results          The results to be put into the final response.
     * @param extractedFileIDs The extracted file ids. Contains whether more results can be found.
     */
    private void sendFinalResponse(GetFileIDsRequest request, ResultingFileIDs results,
                                   ExtractedFileIDsResultSet extractedFileIDs) {
        GetFileIDsFinalResponse response = createFinalResponse(request);

        if (extractedFileIDs.hasMoreEntries()) {
            response.setPartialResult(true);
        }

        ResponseInfo fri = new ResponseInfo();
        fri.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        response.setResponseInfo(fri);
        response.setResultingFileIDs(results);

        getContext().getResponseDispatcher().dispatchResponse(response, request);
    }

    /**
     * Create a generic final response message for the GetFileIDs conversation.
     * Missing:
     * <br/> - ProgressResponseInfo
     *
     * @param message The GetFileIDsRequest to base the response upon.
     * @return The GetFileIDsFinalResponse.
     */
    private GetFileIDsProgressResponse createProgressResponse(GetFileIDsRequest message) {
        GetFileIDsProgressResponse res = new GetFileIDsProgressResponse();
        res.setPillarID(getPillarModel().getPillarID());
        res.setFileIDs(message.getFileIDs());
        res.setResultAddress(message.getResultAddress());

        return res;
    }

    /**
     * Create a generic final response message for the GetFileIDs conversation.
     * Missing:
     * <br/> - FinalResponseInfo
     * <br/> - ResultingFileIDs
     *
     * @param message The GetFileIDsRequest to base the response upon.
     * @return The GetFileIDsFinalResponse.
     */
    private GetFileIDsFinalResponse createFinalResponse(GetFileIDsRequest message) {
        GetFileIDsFinalResponse res = new GetFileIDsFinalResponse();
        res.setPillarID(getPillarModel().getPillarID());
        res.setFileIDs(message.getFileIDs());

        return res;
    }
}
