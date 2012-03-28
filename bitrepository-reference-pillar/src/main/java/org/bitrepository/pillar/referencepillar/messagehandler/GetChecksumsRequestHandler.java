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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.xml.bind.JAXBException;

import org.apache.activemq.util.ByteArrayInputStream;
import org.bitrepository.bitrepositorydata.GetChecksumsResults;
import org.bitrepository.bitrepositoryelements.ChecksumDataForChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingChecksums;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.AlarmDispatcher;
import org.bitrepository.pillar.exceptions.InvalidMessageException;
import org.bitrepository.pillar.referencepillar.ReferenceArchive;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.utils.Base16Utils;
import org.bitrepository.protocol.utils.ChecksumUtils;
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
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    public GetChecksumsRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
        super(settings, messageBus, alarmDispatcher, referenceArchive);
    }
    
    /**
     * Handles the messages for the GetChecksums operation.
     * @param message The GetChecksumsRequest message to handle.
     */
    public void handleMessage(GetChecksumsRequest message) {
        ArgumentValidator.checkNotNull(message, "GetChecksumsRequest message");

        try {
            validateMessage(message);
            sendInitialProgressMessage(message);
            List<ChecksumDataForChecksumSpecTYPE> checksumList = calculateChecksumResults(message);
            ResultingChecksums compiledResults = performPostProcessing(message, checksumList);
            sendFinalResponse(message, compiledResults);
        } catch (InvalidMessageException e) {
            sendFailedResponse(message, e.getResponseInfo());
        } catch (IllegalArgumentException e) {
            log.warn("Caught IllegalArgumentException. Message ", e);
            getAlarmDispatcher().handleIllegalArgumentException(e);
        } catch (RuntimeException e) {
            log.warn("Internal RunTimeException caught. Sending response for 'error at my end'.", e);
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FAILURE);
            fri.setResponseText("Error at my end: " + e.getMessage());
            sendFailedResponse(message, fri);
        }
    }
    
    /**
     * Method for validating the content of the GetChecksumsRequest message. 
     * Is it possible to perform the operation?
     * 
     * @param message The message to validate.
     */
    private void validateMessage(GetChecksumsRequest message) {
        // Validate the message.
        validateBitrepositoryCollectionId(message.getCollectionID());
        validatePillarId(message.getPillarID());
        validateChecksumSpecification(message.getChecksumRequestForExistingFile());
        
        validateFileIDs(message);
        
        log.debug("Message '" + message.getCorrelationID() + "' validated and accepted.");
    }
    
    /**
     * Method for validating the FileIDs in the GetChecksumsRequest message.
     * Do we have the requested files?
     * This does only concern the specified files. Not 'AllFiles' or the parameter stuff.
     *  
     * @param message The message to validate the FileIDs of.
     */
    private void validateFileIDs(GetChecksumsRequest message) {
        // Validate the requested files
        FileIDs fileids = message.getFileIDs();

        // go through all the files and find any missing
        String fileID = fileids.getFileID();
        if(fileID == null || fileID.isEmpty()) {
            return;
        }
        
        // if not missing, then all files have been found!
        if(!getArchive().hasFile(fileID)) {
            // report on the missing files
            String errText = "The following file is missing: '" + fileID + "'";
            log.warn(errText);
            // Then tell the mediator, that we failed.
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            fri.setResponseText(errText);
            throw new InvalidMessageException(fri);
        }
    }
    
    /**
     * Method for creating and sending the initial progress message for accepting the operation.
     * @param message The message to base the response upon.
     */
    private void sendInitialProgressMessage(GetChecksumsRequest message) {
        GetChecksumsProgressResponse pResponse = createProgressResponse(message);
        
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Operation accepted. Starting to calculate checksums.");
        pResponse.setResponseInfo(prInfo);

        // Send the ProgressResponse
        log.info("Sending GetFileProgressResponse: " + pResponse);
        getMessageBus().sendMessage(pResponse);
    }
    
    /**
     * Method for calculating the checksum results requested.
     * @param message The message with the checksum request.
     * @return The list of results for the requested checksum.
     */
    private List<ChecksumDataForChecksumSpecTYPE> calculateChecksumResults(GetChecksumsRequest message) {
        log.debug("Starting to calculate the checksum of the requested files.");
        
        FileIDs fileids = message.getFileIDs();
        
        if(fileids.isSetAllFileIDs()) {
            log.debug("Calculating the checksum for all the files.");
            return calculateChecksumForAllFiles(message.getChecksumRequestForExistingFile());
        }
        
        log.debug("Calculating the checksum for specified files: " + fileids.getFileID());
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        res.add(calculateSingleChecksum(fileids.getFileID(), message.getChecksumRequestForExistingFile()));
        
        return res;
    }
    
    /**
     * Method for calculating the checksum on all the files in the archive.
     * @param algorithm The algorithm for calculating the checksums.
     * @param salt The salt of the checksum.
     * @return The list of checksums for requested files. 
     */
    private List<ChecksumDataForChecksumSpecTYPE> calculateChecksumForAllFiles(
            ChecksumSpecTYPE csType) {
        List<ChecksumDataForChecksumSpecTYPE> res = new ArrayList<ChecksumDataForChecksumSpecTYPE>();
        
        // Go through every file in the archive, calculate the checksum and put it into the results.
        for(String fileid : getArchive().getAllFileIds()) {
            res.add(calculateSingleChecksum(fileid, csType));
        }

        return res;
    }
    
    /**
     * Performs the actual checksum calculation of a file. And Base64 encodes the results.
     * 
     * @param fileId The id of the file.
     * @param csType The specifications for the calculation.
     * @return The calculated checksum for the given file, calculated with the given checksum specification.
     */
    private ChecksumDataForChecksumSpecTYPE calculateSingleChecksum(String fileId, ChecksumSpecTYPE csType) {
        File file = getArchive().getFile(fileId);
        ChecksumDataForChecksumSpecTYPE singleFileResult = new ChecksumDataForChecksumSpecTYPE();
        String checksum = ChecksumUtils.generateChecksum(file, csType);
        singleFileResult.setCalculationTimestamp(CalendarUtils.getNow());
        singleFileResult.setFileID(fileId);
        singleFileResult.setChecksumValue(Base16Utils.encodeBase16(checksum));
        
        return singleFileResult;
    }
    
    /**
     * Performs the needed operations to complete the operation.
     * If the results should be uploaded to a given URL, then the results are packed into a file and uploaded, 
     * otherwise the results a put into the result structure.
     * 
     * @param message The message requesting the calculation of the checksums.
     * @param checksumList The list of requested checksums.
     * @return The result structure.
     */
    private ResultingChecksums performPostProcessing(GetChecksumsRequest message, 
            List<ChecksumDataForChecksumSpecTYPE> checksumList) {
        ResultingChecksums res = new ResultingChecksums();
        
        String url = message.getResultAddress();
        if(url != null && !url.isEmpty()) {
            try {
                File fileToUpload = makeTemporaryChecksumFile(message, checksumList);
                uploadFile(fileToUpload, url);
            } catch (Exception e) {
                ResponseInfo ir = new ResponseInfo();
                ir.setResponseCode(ResponseCode.FAILURE);
                ir.setResponseText("Could not handle the creation and upload of the results due to: " + e.getMessage());
                throw new InvalidMessageException(ir, e);
            }
            
            res.setResultAddress(url);
        } else {
            // Put the checksums into the result structure.
            for(ChecksumDataForChecksumSpecTYPE cs : checksumList) {
                res.getChecksumDataItems().add(cs);
            }
        }
        
        return res;
    }
    
    /**
     * Method for creating a file containing the list of calculated checksums.
     * 
     * @param message The GetChecksumMessage requesting the checksum calculations.
     * @param checksumList The list of checksums to put into the list.
     * @return A file containing all the checksums in the list.
     * @throws IOException If something goes wrong in the upload.
     * @throws JAXBException If the resulting structure cannot be serialized.
     * @throws SAXException If the results does not validate against the XSD.
     */
    private File makeTemporaryChecksumFile(GetChecksumsRequest message, 
            List<ChecksumDataForChecksumSpecTYPE> checksumList) throws IOException, JAXBException, SAXException {
        // Create the temporary file.
        File checksumResultFile = File.createTempFile(message.getCorrelationID(), new Date().getTime() + ".cs");
        log.debug("Writing the list of checksums to the file '" + checksumResultFile + "'");
        
        // Create data format 
        GetChecksumsResults results = new GetChecksumsResults();
        results.setVersion(VERSION);
        results.setMinVersion(MIN_VERSION);
        results.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        results.setCollectionID(getSettings().getCollectionID());
        for(ChecksumDataForChecksumSpecTYPE cs : checksumList) {
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
    @SuppressWarnings("deprecation")
    private void uploadFile(File fileToUpload, String url) throws IOException {
        URL uploadUrl = new URL(url);
        
        // Upload the file.
        log.debug("Uploading file: " + fileToUpload.getName() + " to " + url);
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();
        fe.uploadToServer(new FileInputStream(fileToUpload), uploadUrl);
    }
    
    /**
     * Method for sending a final response reporting the success.
     * @param message The GetChecksumRequest to base the response upon.
     * @param results The results of the checksum calculations.
     */
    private void sendFinalResponse(GetChecksumsRequest message, ResultingChecksums results) {
        GetChecksumsFinalResponse fResponse = createFinalResponse(message);
        
        ResponseInfo fri = new ResponseInfo();
        fri.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        fri.setResponseText("Successfully calculated the requested checksums.");
        fResponse.setResponseInfo(fri);
        fResponse.setResultingChecksums(results);
        
        // Send the FinalResponse
        log.info("Sending successful GetFileFinalResponse: " + fResponse);
        getMessageBus().sendMessage(fResponse);        
    }
    
    /**
     * Method for sending a failed response based on a given request message and a given response info.
     * @param message The request message to base the response upon.
     * @param fri The response info for the failed response.
     */
    private void sendFailedResponse(GetChecksumsRequest message, ResponseInfo fri) {
        GetChecksumsFinalResponse fResponse = createFinalResponse(message);
        fResponse.setResponseInfo(fri);
        
        log.info("Sending failed GetFileFinalResponse: " + fResponse);
        getMessageBus().sendMessage(fResponse);        
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
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(message.getCorrelationID());
        res.setChecksumRequestForExistingFile(message.getChecksumRequestForExistingFile());
        res.setFileIDs(message.getFileIDs());
        res.setResultAddress(message.getResultAddress());
        res.setTo(message.getReplyTo());
        res.setCollectionID(getSettings().getCollectionID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());

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
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(message.getCorrelationID());
        res.setChecksumRequestForExistingFile(message.getChecksumRequestForExistingFile());
        res.setTo(message.getReplyTo());
        res.setCollectionID(getSettings().getCollectionID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());
        
        return res;
    }
}
