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

import org.apache.activemq.util.ByteArrayInputStream;
import org.bitrepository.bitrepositorydata.GetFileInfosResults;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileInfosData;
import org.bitrepository.bitrepositoryelements.FileInfosData.FileInfosDataItems;
import org.bitrepository.bitrepositoryelements.FileInfosDataItem;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingFileInfos;
import org.bitrepository.bitrepositorymessages.GetFileInfosFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileInfosProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileInfosRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.common.filestore.FileInfo;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.store.StorageModel;
import org.bitrepository.pillar.store.checksumdatabase.ExtractedChecksumResultSet;
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
import java.math.BigInteger;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Date;

/**
 * Class for performing the GetFileInfos operation for this pillar.
 */
public class GetFileInfosRequestHandler extends PerformRequestHandler<GetFileInfosRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param context The context for the message handling.
     * @param model The storage model for the pillar.
     */
    protected GetFileInfosRequestHandler(MessageHandlerContext context, StorageModel model) {
        super(context, model);
    }

    @Override
    protected void validateRequest(GetFileInfosRequest request, MessageContext requestContext)
            throws RequestHandlerException {
        validateCollectionID(request);
        validatePillarId(request.getPillarID());
        getPillarModel().verifyChecksumAlgorithm(request.getChecksumRequestForExistingFile(),
                request.getCollectionID());
        if (request.getFileIDs() != null && request.getFileIDs().getFileID() != null) {
            validateFileIDFormat(request.getFileIDs().getFileID());
            verifyFileIDExistence(request.getFileIDs(), request.getCollectionID());
        }

        log.debug(MessageUtils.createMessageIdentifier(request) + "' validated and accepted.");
    }

    @Override
    protected void performOperation(GetFileInfosRequest request, MessageContext requestContext)
            throws RequestHandlerException {
        log.debug(MessageUtils.createMessageIdentifier(request) + " Performing GetFileInfos for file(s) "
                + request.getFileIDs() + " on collection " + request.getCollectionID());
        ExtractedChecksumResultSet extractedChecksums = extractChecksumPart(request);
        ResultingFileInfos fileInfosResults;
        if(request.getResultAddress() == null) {
            fileInfosResults = compileResultsForMessage(extractedChecksums, request);
        } else {
            fileInfosResults = createAndUploadResults(request, extractedChecksums);
        }
        sendFinalResponse(request, fileInfosResults, extractedChecksums.hasMoreEntries());
    }

    @Override
    public Class<GetFileInfosRequest> getRequestClass() {
        return GetFileInfosRequest.class;
    }

    @Override
    public MessageResponse generateFailedResponse(GetFileInfosRequest request) {
        return createFinalResponse(request);
    }

    @Override
    protected void sendProgressResponse(GetFileInfosRequest request, MessageContext requestContext) {
        GetFileInfosProgressResponse response = createProgressResponse(request);

        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Starting to extracting file infos.");
        response.setResponseInfo(prInfo);

        dispatchResponse(response, request);
    }

    /**
     * Method for calculating the checksum part of the requested file infos.
     * @param request The message with the file info request.
     * @return The extracted results for the requested file infos.
     * @throws RequestHandlerException If the requested file infos specification is not supported.
     */
    private ExtractedChecksumResultSet extractChecksumPart(GetFileInfosRequest request)
            throws RequestHandlerException {
        log.debug("Starting to extracting the checksum part of the requested file infos.");

        if(request.getFileIDs().isSetFileID()) {
            return getPillarModel().getSingleChecksumResultSet(request.getFileIDs().getFileID(), 
                    request.getCollectionID(), request.getMinChecksumTimestamp(), request.getMaxChecksumTimestamp(),
                    request.getChecksumRequestForExistingFile());
        } else {
            Long maxResults = null;
            if(request.getMaxNumberOfResults() != null) {
                maxResults = request.getMaxNumberOfResults().longValue();
            }
            return getPillarModel().getChecksumResultSet(request.getMinChecksumTimestamp(),
                    request.getMaxChecksumTimestamp(), maxResults, request.getCollectionID(),
                    request.getChecksumRequestForExistingFile());
        }
    }

    /**
     * Uploads the extracted file info results to the given URL, and creates the ResultingFileInfos object for
     * the final response message.
     * 
     * @param request The message requesting the calculation of the file infos.
     * @param checksumResultSet List containing the requested file infos.
     * @return The ResultingFileInfos containing the URL.
     */
    private ResultingFileInfos createAndUploadResults(GetFileInfosRequest request,
            ExtractedChecksumResultSet checksumResultSet) throws RequestHandlerException {
        ResultingFileInfos res = new ResultingFileInfos();

        String url = request.getResultAddress();
        try {
            File fileToUpload = makeTemporaryFileInfosFile(request, checksumResultSet);
            uploadFile(fileToUpload, url);
        } catch (Exception e) {
            throw new InvalidMessageException(ResponseCode.FILE_TRANSFER_FAILURE, "Could not handle the creation "
                    + "and upload of the results due to: " + e.getMessage(), e);
        }

        res.setResultAddress(url);
        return res;
    }
    
    /**
     * Compiles the extracted checksum results into the message format.
     * @param checksumResultSet The checksum results extracted from the database.
     * @return The extracted results in the ResultingChecksums format.
     */
    private ResultingFileInfos compileResultsForMessage(ExtractedChecksumResultSet checksumResultSet,
            GetFileInfosRequest request) throws RequestHandlerException {
        ResultingFileInfos res = new ResultingFileInfos();
        res.setFileInfosData(new FileInfosData());
        res.getFileInfosData().setFileInfosDataItems(new FileInfosDataItems());
        
        for(ChecksumDataForChecksumSpecTYPE cs : checksumResultSet.getEntries()) {
            FileInfosDataItem info = getFileInfoDataItem(cs, request);
            if(info != null) {
                res.getFileInfosData().getFileInfosDataItems().getFileInfosDataItem().add(info);
            }
        }

        return res;
    }

    /**
     * Creates a FileInfoDataItem from a checksum extraction.
     * If the last modified date for the file is not between the file dates, then a null is returned.
     * @param cs The checksum results from the database.
     * @param request The GetFileInfosRequest.
     * @return The FileInfoDataItem corresponding to the checksum result. Or null if it does not
     * @throws RequestHandlerException If it fails to extract the data for the file.
     */
    protected FileInfosDataItem getFileInfoDataItem(ChecksumDataForChecksumSpecTYPE cs, GetFileInfosRequest request)
            throws RequestHandlerException {
        FileInfo fileData = null;
        
        try {
            fileData = getPillarModel().getFileInfoForActualFile(cs.getFileID(), request.getCollectionID());
        } catch(RequestHandlerException e) {
            log.trace("Unable to obtain fileData as it is a checksumpillar", e);
        }
        
        if(fileData != null) {
            Long fileDate = fileData.getLastModifiedDate();
            if(!CalendarUtils.isDateBetween(fileDate, request.getMinFileTimestamp(), request.getMaxFileTimestamp())) {
                log.debug("Ignoring " + fileData.getFileID() + ", since it does not have a ");
                return null;
            }
        }

        FileInfosDataItem res = new FileInfosDataItem();
        res.setCalculationTimestamp(cs.getCalculationTimestamp());
        res.setChecksumValue(cs.getChecksumValue());
        res.setFileID(cs.getFileID());

        if(fileData == null) {
            // from Checksum Replica
            res.setLastModificationTime(cs.getCalculationTimestamp());
            res.setFileSize(BigInteger.valueOf(-1L));
        } else {
            res.setLastModificationTime(CalendarUtils.getFromMillis(fileData.getLastModifiedDate()));
            res.setFileSize(BigInteger.valueOf(fileData.getSize()));
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
    private File makeTemporaryFileInfosFile(GetFileInfosRequest request,
            ExtractedChecksumResultSet checksumResultSet) throws IOException, JAXBException, SAXException,
            RequestHandlerException {
        // Create the temporary file.
        File fileInfosResultFile = File.createTempFile(request.getCorrelationID(), new Date().getTime() + ".fi");
        log.debug("Writing the list of file infos to the file '" + fileInfosResultFile + "'");

        // Create data format 
        GetFileInfosResults results = new GetFileInfosResults();
        results.setVersion(VERSION);
        results.setMinVersion(MIN_VERSION);
        results.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        results.setCollectionID(request.getCollectionID());
        for(ChecksumDataForChecksumSpecTYPE cs : checksumResultSet.getEntries()) {
            FileInfosDataItem dataItem = getFileInfoDataItem(cs, request);
            if(dataItem != null) {
                results.getFileInfosDataItem().add(dataItem);
            }
        }

        // Print all the file infos safely to the file (autoclose the streams!)
        try (OutputStream is = new FileOutputStream(fileInfosResultFile)){
            JaxbHelper jaxb = new JaxbHelper(XSD_CLASSPATH, XSD_BR_DATA);
            String xmlMessage = jaxb.serializeToXml(results);
            jaxb.validate(new ByteArrayInputStream(xmlMessage.getBytes(StandardCharsets.UTF_8)));
            is.write(xmlMessage.getBytes(StandardCharsets.UTF_8));
            is.flush();
        }

        return fileInfosResultFile;
    }

    /**
     * Method for uploading a file to a given URL.
     * 
     * @param fileToUpload The File to upload.
     * @param url The location where the file should be uploaded.
     * @throws IOException If something goes wrong.
     */
    private void uploadFile(File fileToUpload, String url) throws IOException {
        URL uploadUrl = new URL(url);

        // Upload the file.
        log.debug("Uploading file: " + fileToUpload.getName() + " to " + url);
        try (InputStream in = new BufferedInputStream(new FileInputStream(fileToUpload))) {
            context.getFileExchange().putFile(in, uploadUrl);
        }
    }

    /**
     * Method for sending a final response reporting the success.
     * @param request The GetFileInfosRequest to base the response upon.
     * @param results The results of the file infos.
     * @param hasMoreEntries Whether more results can be found.
     */
    private void sendFinalResponse(GetFileInfosRequest request, ResultingFileInfos results,
            boolean hasMoreEntries) {
        GetFileInfosFinalResponse response = createFinalResponse(request);

        ResponseInfo fri = new ResponseInfo();
        fri.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        response.setResponseInfo(fri);
        response.setResultingFileInfos(results);
        response.setPartialResult(hasMoreEntries);

        dispatchResponse(response, request);
    }

    /**
     * Method for creating a GetFileInfosProgressResponse based on a request.
     * Missing arguments:
     * <br/> AuditTrailInformation
     * 
     * @param message The GetFileInfosRequest to base the results upon.
     * @return The GetFileInfosProgressResponse based on the GetFileInfosRequest.
     */
    private GetFileInfosProgressResponse createProgressResponse(GetFileInfosRequest message) {
        GetFileInfosProgressResponse res = new GetFileInfosProgressResponse();
        res.setChecksumRequestForExistingFile(message.getChecksumRequestForExistingFile());
        res.setFileIDs(message.getFileIDs());
        res.setResultAddress(message.getResultAddress());
        res.setPillarID(getPillarModel().getPillarID());

        return res;
    }

    /**
     * Method for creating the generic GetFileInfosFinalResponse based on a GetFileInfosRequest.
     * Missing fields:
     * <br/> - AuditTrailInformation
     * <br/> - FinalResponseInfo
     * <br/> - ResultingFileInfos
     * 
     * @param message The message to base the response upon and respond to.
     * @return The response for the generic GetFileInfosRequest.
     */
    private GetFileInfosFinalResponse createFinalResponse(GetFileInfosRequest message) {
        GetFileInfosFinalResponse res = new GetFileInfosFinalResponse();
        res.setChecksumRequestForExistingFile(message.getChecksumRequestForExistingFile());
        res.setPillarID(getPillarModel().getPillarID());
        res.setFileIDs(message.getFileIDs());

        return res;
    }
}
