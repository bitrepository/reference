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
package org.bitrepository.pillar.referencepillar.messagehandler;

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
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.common.filestore.FileStore;
import org.bitrepository.pillar.cache.database.ExtractedChecksumResultSet;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.pillar.referencepillar.archive.ReferenceChecksumManager;
import org.bitrepository.protocol.FileExchange;
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
public class GetChecksumsRequestHandler extends ReferencePillarMessageHandler<GetChecksumsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * @param context The context for the pillar.
     * @param archivesManager The manager of the archives.
     * @param csManager The checksum manager for the pillar.
     */
    protected GetChecksumsRequestHandler(MessageHandlerContext context, FileStore archivesManager, 
            ReferenceChecksumManager csManager) {
        super(context, archivesManager, csManager);
    }
    
    @Override
    public Class<GetChecksumsRequest> getRequestClass() {
        return GetChecksumsRequest.class;
    }

    @Override
    public void processRequest(GetChecksumsRequest message) throws RequestHandlerException {
        validateMessage(message);
        sendInitialProgressMessage(message);
        ExtractedChecksumResultSet extractedChecksums = extractChecksumResults(message);
        ResultingChecksums compiledResults = performPostProcessing(message, extractedChecksums);
        sendFinalResponse(message, compiledResults, extractedChecksums);
    }

    @Override
    public MessageResponse generateFailedResponse(GetChecksumsRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Method for validating the content of the GetChecksumsRequest message. 
     * Is it possible to perform the operation?
     * 
     * @param message The message to validate.
     */
    private void validateMessage(GetChecksumsRequest message) throws RequestHandlerException {
        validateCollectionID(message);
        validatePillarId(message.getPillarID());
        validateChecksumSpecification(message.getChecksumRequestForExistingFile(), message.getCollectionID());
        validateFileIDs(message);
        
        log.debug(MessageUtils.createMessageIdentifier(message) + "' validated and accepted.");
    }
    
    /**
     * Method for validating the FileIDs in the GetChecksumsRequest message.
     * Do we have the requested files?
     * This does only concern the specified files. Not 'AllFiles' or the parameter stuff.
     *  
     * @param message The message to validate the FileIDs of.
     */
    private void validateFileIDs(GetChecksumsRequest message) throws RequestHandlerException {
        // Validate the requested files
        FileIDs fileids = message.getFileIDs();

        // go through all the files and find any missing
        String fileID = fileids.getFileID();
        if(fileID == null) {
            return;
        }
        validateFileID(fileID);
        
        // if not missing, then all files have been found!
        if(!getArchives().hasFile(fileID, message.getCollectionID())) {
            // report on the missing files
            String errText = "The following file is missing: '" + fileID + "'";
            log.warn(errText);
            // Then tell the mediator, that we failed.
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            fri.setResponseText(errText);
            throw new InvalidMessageException(fri, message.getCollectionID());
        }
    }
    
    /**
     * Method for creating and sending the initial progress response for accepting the operation.
     * @param request The request to base the response upon.
     */
    private void sendInitialProgressMessage(GetChecksumsRequest request) {
        GetChecksumsProgressResponse response = createProgressResponse(request);
        
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Starting to calculate checksums.");
        response.setResponseInfo(prInfo);

        dispatchResponse(response, request);
    }
    
    /**
     * Method for calculating the checksum results requested.
     * @param message The message with the checksum request.
     * @return The extracted results for the requested checksum.
     */
    private ExtractedChecksumResultSet extractChecksumResults(GetChecksumsRequest message) {
        log.debug("Starting to extracting the checksum of the requested files.");
        
        if(message.getFileIDs().isSetFileID()) {
            ExtractedChecksumResultSet res = new ExtractedChecksumResultSet();
            ChecksumDataForChecksumSpecTYPE data = getCsManager().getChecksumDataForChecksumSpec(
                    message.getFileIDs().getFileID(), message.getCollectionID(),
                    message.getChecksumRequestForExistingFile());
            res.insertChecksumEntry(data);
            return res;
        } else {
            Long maxResults = null;
            if(message.getMaxNumberOfResults() != null) {
                maxResults = message.getMaxNumberOfResults().longValue();
            }
            return getCsManager().getEntries(message.getMinTimestamp(), message.getMaxTimestamp(), maxResults,
                    message.getCollectionID(), message.getChecksumRequestForExistingFile());
        }
    }
    
    /**
     * Performs the needed operations to complete the operation.
     * If the results should be uploaded to a given URL, then the results are packed into a file and uploaded, 
     * otherwise the results a put into the result structure.
     * 
     * @param message The message requesting the calculation of the checksums.
     * @param checksumResultSet The list of requested checksums.
     * @return The result structure.
     */
    private ResultingChecksums performPostProcessing(GetChecksumsRequest message, 
            ExtractedChecksumResultSet checksumResultSet) throws RequestHandlerException {
        ResultingChecksums res = new ResultingChecksums();
        
        String url = message.getResultAddress();
        if(url != null) {
            try {
                File fileToUpload = makeTemporaryChecksumFile(message, checksumResultSet);
                uploadFile(fileToUpload, url);
            } catch (Exception e) {
                ResponseInfo ir = new ResponseInfo();
                ir.setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
                ir.setResponseText("Could not handle the creation and upload of the results due to: " + e.getMessage());
                throw new InvalidMessageException(ir, message.getCollectionID(), e);
            }
            
            res.setResultAddress(url);
        } else {
            // Put the checksums into the result structure.
            for(ChecksumDataForChecksumSpecTYPE cs : checksumResultSet.getEntries()) {
                res.getChecksumDataItems().add(cs);
            }
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
     * @param extractedChecksums The extracted checksum entries. Contains whether more results can be found.
     */
    private void sendFinalResponse(GetChecksumsRequest request, ResultingChecksums results, 
            ExtractedChecksumResultSet extractedChecksums) {
        GetChecksumsFinalResponse response = createFinalResponse(request);

        if(extractedChecksums.hasMoreEntries()) {
            response.setPartialResult(true);
        }

        ResponseInfo fri = new ResponseInfo();
        fri.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        response.setResponseInfo(fri);
        response.setResultingChecksums(results);

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
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());

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
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        
        return res;
    }
}
