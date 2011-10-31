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
import java.io.OutputStream;
import java.math.BigInteger;
import java.net.URL;
import java.util.Date;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ErrorcodeFinalresponseType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.FinalResponseCodePositiveType;
import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositoryelements.ProgressResponseCodeType;
import org.bitrepository.bitrepositoryelements.ProgressResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetFileIDs operation.
 */
public class GetFileIDsRequestHandler extends PillarMessageHandler<GetFileIDsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    public GetFileIDsRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
        super(settings, messageBus, alarmDispatcher, referenceArchive);
    }
    
    /**
     * Handles the identification messages for the GetFileIDs operation.
     * @param message The IdentifyPillarsForGetFileIDsRequest message to handle.
     */
    public void handleMessage(GetFileIDsRequest message) {
        try {
            if(!validateMessage(message)) {
                return;
            }
            
            sendInitialProgressMessage(message);
            ResultingFileIDs results = performOperation(message);
            sendFinalResponse(message, results);
        } catch (IllegalArgumentException e) {
            log.warn("Caught IllegalArgumentException. Message ", e);
            alarmDispatcher.alarmIllegalArgument(e);
        } catch (RuntimeException e) {
            log.warn("Internal RunTimeException caught. Sending response for 'error at my end'.", e);
            FinalResponseInfo fri = new FinalResponseInfo();
            fri.setFinalResponseCode(ErrorcodeFinalresponseType.OPERATION_FAILED.value().toString());
            fri.setFinalResponseText("Error: " + e.getMessage());
            sendFailedResponse(message, fri);
        }
    }
    
    /**
     * Method for validating the content of the GetFileIDsRequest message. 
     * Is it possible to perform the operation?
     * 
     * @param message The message to validate.
     * @return Whether it is valid.
     */
    private boolean validateMessage(GetFileIDsRequest message) {
        // Validate the message.
        validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());
        validatePillarId(message.getPillarID());
        
        log.debug("Message '" + message.getCorrelationID() + "' validated and accepted.");
        
        return true;
    }
    
    /**
     * Method for creating and sending the initial progress message for accepting the operation.
     * @param message The message to base the response upon.
     */
    private void sendInitialProgressMessage(GetFileIDsRequest message) {
        GetFileIDsProgressResponse pResponse = createProgressResponse(message);
        
        ProgressResponseInfo prInfo = new ProgressResponseInfo();
        prInfo.setProgressResponseCode(ProgressResponseCodeType.REQUEST_ACCEPTED);
        prInfo.setProgressResponseText("Operation accepted. Starting to locate files.");
        pResponse.setProgressResponseInfo(prInfo);

        // Send the ProgressResponse
        log.info("Sending GetFileProgressResponse: " + pResponse);
        messagebus.sendMessage(pResponse);
    }
    
    /**
     * Method for finding the requested FileIDs. 
     * @param message The message requesting which fileids to be found.
     * @return The ResultingFileIDs with either the list of fileids or the final address for where the 
     * list of fileids is uploaded.
     */
    private ResultingFileIDs performOperation(GetFileIDsRequest message) {
        // First find the requested files.

        FileIDsData data = retrieveFileIDsData(message.getFileIDs());

        ResultingFileIDs res = new ResultingFileIDs();
        
        // Either upload the files or put them into a 
        String resultingAddress = message.getResultAddress();
        if(resultingAddress == null || resultingAddress.isEmpty()) {
            res.setFileIDsData(data);
        } else {
            try {
                File outputFile = makeTemporaryChecksumFile(message, data);
                uploadFile(outputFile, resultingAddress);
                res.setResultAddress(resultingAddress);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return res;
    }
    
    /**
     * Method for retrieving the requested FileIDs. Depending on which operation is requested, it will call 
     * appropriate methods.
     * 
     * @param fileIDs The requested FileIDs.
     * @return The resulting collection of FileIDs found.
     */
    private FileIDsData retrieveFileIDsData(FileIDs fileIDs) {
        if(fileIDs.isSetAllFileIDs()) {
            log.debug("Retrieving the id for all the files.");
            return retrieveAllFileIDs();
        }
        if(fileIDs.isSetParameterAddress()) {
            log.debug("Retrieves the file ids for parameter: " + fileIDs.getParameterAddress());
            return retrieveParameterFileIDs(fileIDs.getParameterAddress());
        }
        
        log.debug("Retrieve the specified fileIDs: " + fileIDs.getFileID());
        return retrieveSpecifiedFileIDs(fileIDs.getFileID());
    }
    
    /**
     * Retrieves all the fileIDs.
     * @return The list of the ids of all the files in the archive, wrapped in the requested datastructure.
     */
    private FileIDsData retrieveAllFileIDs() {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems fileIDList = new FileIDsDataItems();
        for(String fileID : archive.getAllFileIds()) {
            // validate that the file exists.
            if(archive.getFile(fileID).isFile()) {
                FileIDsDataItem fileIDData = new FileIDsDataItem();
                fileIDData.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
                fileIDData.setFileID(fileID);
                fileIDList.getFileIDsDataItem().add(fileIDData);
            } else {
                log.warn("The requested file '" + fileID + "' is not a real file (does not exist or is a directory).");
            }
        }
        res.setNoOfItems(BigInteger.valueOf(fileIDList.getFileIDsDataItem().size()));
        res.setFileIDsDataItems(fileIDList);
        return res;
    }
    
    /**
     * Retrieves all the fileIDs matching a given parameter address.
     * TODO implement.
     * @return The list of the ids of the requested files in the archive, wrapped in the requested datastructure.
     */
    private FileIDsData retrieveParameterFileIDs(String parameterAddress) {
        throw new IllegalStateException("Implement me!");
    }
    
    /**
     * Retrieves specified fileIDs and whether the files exists as a proper file.
     * @param fileIDs The requested fileIDs to find and validate their existence..
     * @return The list of the ids of the requested files in the archive, wrapped in the requested datastructure.
     */
    private FileIDsData retrieveSpecifiedFileIDs(List<String> fileIDs) {
        FileIDsData res = new FileIDsData();
        FileIDsDataItems fileIDList = new FileIDsDataItems();
        for(String fileID : fileIDs) {
            // validate that the file exists.
            if(archive.getFile(fileID).isFile()) {
                FileIDsDataItem fileIDData = new FileIDsDataItem();
                fileIDData.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
                fileIDData.setFileID(fileID);
                fileIDList.getFileIDsDataItem().add(fileIDData);
            } else {
                log.warn("The requested file '" + fileID + "' is not a real file (does not exist or is a directory).");
            }
        }
        res.setNoOfItems(BigInteger.valueOf(fileIDList.getFileIDsDataItem().size()));
        res.setFileIDsDataItems(fileIDList);
        return res;
    }
    
    /**
     * Method for creating a file containing the list of calculated checksums.
     * 
     * @param message The GetChecksumMessage requesting the checksum calculations.
     * @param checksumList The list of checksums to put into the list.
     * @return A file containing all the checksums in the list.
     * @throws Exception If something goes wrong, e.g. IOException or JAXBException.
     */
    private File makeTemporaryChecksumFile(GetFileIDsRequest message, FileIDsData fileIDs) throws Exception {
        // Create the temporary file.
        File checksumResultFile = File.createTempFile(message.getCorrelationID(), new Date().getTime() + ".id");
        log.debug("Writing the requested fileids to the file '" + checksumResultFile + "'");

        // Print all the checksums safely (close the streams!)
        OutputStream is = null;
        try {
            is = new FileOutputStream(checksumResultFile);
            for(FileIDsDataItem fileid : fileIDs.getFileIDsDataItems().getFileIDsDataItem()) {
                // TODO write this correct format. Which???
                is.write(new String(fileid.getFileID() + "##" + fileid.getCalculationTimestamp() 
                        + "\n").getBytes());
            }
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
    private void uploadFile(File fileToUpload, String url) throws Exception {
        URL uploadUrl = new URL(url);
        
        // Upload the file.
        log.debug("Uploading file: " + fileToUpload.getName() + " to " + url);
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();
        fe.uploadToServer(new FileInputStream(fileToUpload), uploadUrl);
    }
    
    /**
     * Send a positive final response telling that the operation has successfully finished.
     * @param message The message to base the final response upon.
     * @param results The results to be put into the final response.
     */
    private void sendFinalResponse(GetFileIDsRequest message, ResultingFileIDs results) {
        GetFileIDsFinalResponse fResponse = createFinalResponse(message);
        
        FinalResponseInfo fri = new FinalResponseInfo();
        fri.setFinalResponseCode(FinalResponseCodePositiveType.SUCCESS.value().toString());
        fri.setFinalResponseText("Finished locating the requested files.");
        fResponse.setFinalResponseInfo(fri);
        
        fResponse.setResultingFileIDs(results);
        
        // Send the FinalResponse
        log.info("Sending GetFileIDsFinalResponse: " + fResponse);
        messagebus.sendMessage(fResponse);        
    }
    
    /**
     * Method for sending a response telling that the operation failed.
     * @param message The message to base the response upon.
     * @param fri The information about why the operation failed.
     */
    private void sendFailedResponse(GetFileIDsRequest message, FinalResponseInfo fri) {
        GetFileIDsFinalResponse fResponse = createFinalResponse(message);
        fResponse.setFinalResponseInfo(fri);
        
        // Send the FinalResponse
        log.info("Sending GetFileIDsFinalResponse: " + fResponse);
        messagebus.sendMessage(fResponse);        
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
        GetFileIDsProgressResponse response = new GetFileIDsProgressResponse();
        response.setMinVersion(MIN_VERSION);
        response.setVersion(VERSION);
        response.setBitRepositoryCollectionID(settings.getCollectionID());
        response.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        response.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        response.setAuditTrailInformation(message.getAuditTrailInformation());
        response.setCorrelationID(message.getCorrelationID());
        response.setFileIDs(message.getFileIDs());
        response.setResultAddress(message.getResultAddress());
        response.setTo(message.getReplyTo());

        return response;
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
        GetFileIDsFinalResponse response = new GetFileIDsFinalResponse();
        response.setMinVersion(MIN_VERSION);
        response.setVersion(VERSION);
        response.setBitRepositoryCollectionID(settings.getCollectionID());
        response.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        response.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        response.setAuditTrailInformation(message.getAuditTrailInformation());
        response.setCorrelationID(message.getCorrelationID());
        response.setFileIDs(message.getFileIDs());
        response.setTo(message.getReplyTo());

        return response;
    }
}
