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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.activemq.util.ByteArrayInputStream;
import org.bitrepository.bitrepositorydata.GetChecksumsResults;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.MessageContext;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.utils.MessageUtils;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Class for performing the GetChecksums operation for this pillar.
 */
public class GetChecksumsRequestHandler extends PerformRequestHandler<GetChecksumsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param context The context for the message handling.
     * @param model The storage model for the pillar.
     */
    protected GetChecksumsRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }

    @Override
    public Class<GetChecksumsRequest> getRequestClass() {
        return GetChecksumsRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(GetChecksumsRequest request) {
        return createFinalResponse(request);
    }

    @Override
    protected void validateRequest(GetChecksumsRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        validateCollectionID(request);
        validatePillarId(request.getPillarID());
        getPillarModel().verifyChecksumAlgorithm(request.getChecksumRequestForExistingFile(), 
                request.getCollectionID());
        validateFileIDFormat(request.getFileIDs().getFileID());
        verifyFileIDExistence(request.getFileIDs(), request.getCollectionID());

        log.debug(MessageUtils.createMessageIdentifier(request) + "' validated and accepted.");
    }

    @Override
    protected void sendProgressResponse(GetChecksumsRequest request, MessageContext requestContext) {
        GetChecksumsProgressResponse response = createProgressResponse(request);

        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Starting to calculate checksums.");
        response.setResponseInfo(prInfo);

        dispatchResponse(response, request);
    }

    @Override
    protected void performOperation(GetChecksumsRequest request, MessageContext requestContext) 
            throws RequestHandlerException {
        log.debug(MessageUtils.createMessageIdentifier(request) + " Performing GetChecksums for file(s) " 
                + request.getFileIDs() + " on collection " + request.getCollectionID());
        ExtractedChecksumResultSet extractedChecksums = extractChecksumResults(request);
        ResultingChecksums checksumResults;
        if(request.getResultAddress() == null) {
            checksumResults = compileResultsForMessage(request, extractedChecksums);
        } else {
            checksumResults = createAndUploadResults(request, extractedChecksums);
        }
        sendFinalResponse(request, checksumResults, extractedChecksums.hasMoreEntries());
    }

    /**
     * Method for calculating the checksum results requested.
     * @param request The message with the checksum request.
     * @return The extracted results for the requested checksum.
     * @throws RequestHandlerException If the requested checksum specification is not supported.
     */
    private ExtractedChecksumResultSet extractChecksumResults(GetChecksumsRequest request) throws RequestHandlerException {
        log.debug("Starting to extracting the checksum of the requested files.");

        if(request.getFileIDs().isSetFileID()) {
            return getPillarModel().getSingleChecksumResultSet(request.getFileIDs().getFileID(), 
                    request.getCollectionID(), request.getMinTimestamp(), request.getMaxTimestamp(), 
                    request.getChecksumRequestForExistingFile());
        } else {
            Long maxResults = null;
            if(request.getMaxNumberOfResults() != null) {
                maxResults = request.getMaxNumberOfResults().longValue();
            }
            return getPillarModel().getChecksumResultSet(request.getMinTimestamp(), request.getMaxTimestamp(), maxResults,
                    request.getCollectionID(), request.getChecksumRequestForExistingFile());
        }
    }

    /**
     * Uploads the extracted checksum results to the given URL, and creates the ResultingChecksums object for 
     * the final response message.
     * 
     * @param request The message requesting the calculation of the checksums.
     * @param checksumResultSet List containing the requested checksums.
     * @return The ResultingChecksums containing the URL.
     */
    private ResultingChecksums createAndUploadResults(GetChecksumsRequest request, 
            ExtractedChecksumResultSet checksumResultSet) throws RequestHandlerException {
        ResultingChecksums res = new ResultingChecksums();

        String url = request.getResultAddress();
        try {
            File fileToUpload = makeTemporaryChecksumFile(request, checksumResultSet);
            uploadFile(fileToUpload, url);
        } catch (Exception e) {
            throw new InvalidMessageException(ResponseCode.FILE_TRANSFER_FAILURE, "Could not handle the creation "
                    + "and upload of the results due to: " + e.getMessage(), request.getCollectionID(), e);
        }

        res.setResultAddress(url);
        return res;
    }
    
    /**
     * Compiles the extracted checksum results into the message format.
     * @param request The GetChecksumsRequest.
     * @param checksumResultSet The checksum results extracted from the database.
     * @return The extracted results in the ResultingChecksums format.
     */
    private ResultingChecksums compileResultsForMessage(GetChecksumsRequest request, 
            ExtractedChecksumResultSet checksumResultSet) {
        ResultingChecksums res = new ResultingChecksums();
        
        for(ChecksumDataForChecksumSpecTYPE cs : checksumResultSet.getEntries()) {
            res.getChecksumDataItems().add(cs);
        }

        return res;
    }

    /**
     * Method for creating a file containing the list of calculated checksums.
     * 
     * @param request The GetChecksumMessage requesting the checksum calculations.
     * @param checksumResultSet The list of checksums to put into the list.
     * @return A file containing all the checksums in the list.
     * @throws IOException If something goes wrong in the upload.
     * @throws JAXBException If the resulting structure cannot be serialized.
     * @throws SAXException If the results does not validate against the XSD.
     */
    private File makeTemporaryChecksumFile(GetChecksumsRequest request,
            ExtractedChecksumResultSet checksumResultSet) throws IOException, JAXBException, SAXException {
        // Create the temporary file.
        File checksumResultFile = File.createTempFile(request.getCorrelationID(), new Date().getTime() + ".cs");
        log.debug("Writing the list of checksums to the file '" + checksumResultFile + "'");

        // Create data format 
        GetChecksumsResults results = new GetChecksumsResults();
        results.setVersion(VERSION);
        results.setMinVersion(MIN_VERSION);
        results.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        results.setCollectionID(request.getCollectionID());
        for(ChecksumDataForChecksumSpecTYPE cs : checksumResultSet.getEntries()) {
            results.getChecksumDataItems().add(cs);
        }

        // Print all the checksums safely (close the streams!)
        OutputStream is = null;
        try {
            is = new FileOutputStream(checksumResultFile);
            JaxbHelper jaxb = new JaxbHelper(XSD_CLASSPATH, XSD_BR_DATA);
            String xmlMessage = jaxb.serializeToXml(results);
            jaxb.validate(new ByteArrayInputStream(xmlMessage.getBytes()));
            is.write(xmlMessage.getBytes());
            is.flush();
        } finally {
            if(is != null) {
                is.close();
            }
        }

        return checksumResultFile;
    }

    /**
     * Method for uploading a file to a given URL.
     * 
     * @param fileToUpload The File to upload.
     * @param url The location where the file should be uploaded.
     * @throws Exception If something goes wrong.
     */
    private void uploadFile(File fileToUpload, String url) throws IOException {
        URL uploadUrl = new URL(url);

        // Upload the file.
        log.debug("Uploading file: " + fileToUpload.getName() + " to " + url);
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(getSettings());
        fe.uploadToServer(new FileInputStream(fileToUpload), uploadUrl);
    }

    /**
     * Method for sending a final response reporting the success.
     * @param request The GetChecksumRequest to base the response upon.
     * @param results The results of the checksum calculations.
     * @param hasMoreEntries Whether more results can be found.
     */
    private void sendFinalResponse(GetChecksumsRequest request, ResultingChecksums results, 
            boolean hasMoreEntries) {
        GetChecksumsFinalResponse response = createFinalResponse(request);

        ResponseInfo fri = new ResponseInfo();
        fri.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        response.setResponseInfo(fri);
        response.setResultingChecksums(results);
        response.setPartialResult(hasMoreEntries);

        dispatchResponse(response, request);
    }

    /**
     * Method for creating a GetChecksumProgressResponse based on a request.
     * Missing arguments:
     * <br/> AuditTrailInformation
     * 
     * @param message The GetChecksumsRequest to base the results upon.
     * @return The GetChecksumsProgressResponse based on the GetChecksumsRequest.
     */
    private GetChecksumsProgressResponse createProgressResponse(GetChecksumsRequest message) {
        GetChecksumsProgressResponse res = new GetChecksumsProgressResponse();
        res.setChecksumRequestForExistingFile(message.getChecksumRequestForExistingFile());
        res.setFileIDs(message.getFileIDs());
        res.setResultAddress(message.getResultAddress());
        res.setPillarID(getPillarModel().getPillarID());

        return res;
    }

    /**
     * Method for creating the generic GetChecksumsFinalResponse based on a GetChecksumRequest.
     * Missing fields:
     * <br/> - AuditTrailInformation
     * <br/> - FinalResponseInfo
     * <br/> - ResultingChecksums
     * 
     * @param message The message to base the response upon and respond to.
     * @return The response for the generic GetChecksumRequest.
     */
    private GetChecksumsFinalResponse createFinalResponse(GetChecksumsRequest message) {
        GetChecksumsFinalResponse res = new GetChecksumsFinalResponse();
        res.setChecksumRequestForExistingFile(message.getChecksumRequestForExistingFile());
        res.setPillarID(getPillarModel().getPillarID());

        return res;
    }
}
