/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: GetFileIDsRequestHandler.java 685 2012-01-06 16:35:17Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org/bitrepository/pillar/messagehandler/GetFileIDsRequestHandler.java $
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
package org.bitrepository.pillar.checksumpillar.messagehandler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.xml.bind.JAXBException;

import org.apache.activemq.util.ByteArrayInputStream;
import org.bitrepository.bitrepositorydata.GetFileIDsResults;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.checksumpillar.cache.ChecksumStore;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.exceptions.InvalidMessageException;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.SAXException;

/**
 * Class for handling requests for the GetFileIDs operation.
 */
public class GetFileIDsRequestHandler extends ChecksumPillarMessageHandler<GetFileIDsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param refCache The cache for the checksum data.
     */
    public GetFileIDsRequestHandler(PillarContext context, ChecksumStore refCache) {
        super(context,  refCache);
    }
    
    /**
     * Handles the requests for the GetFileIDs operation.
     * @param message The IdentifyPillarsForGetFileIDsRequest message to handle.
     */
    public void handleMessage(GetFileIDsRequest message) {
        ArgumentValidator.checkNotNull(message, "GetFileIDsRequest message");

        try {
            validateMessage(message);
            sendInitialProgressMessage(message);
            ResultingFileIDs results = performGetFileIDsOperation(message);
            sendFinalResponse(message, results);
        } catch (InvalidMessageException e) {
            sendFailedResponse(message, e.getResponseInfo());
        } catch (IllegalArgumentException e) {
            log.warn("Caught IllegalArgumentException. Message ", e);
            getAlarmDispatcher().handleIllegalArgumentException(e);
        } catch (RuntimeException e) {
            log.warn("Internal RuntimeException caught. Sending response for 'error at my end'.", e);
            getAuditManager().addAuditEvent(message.getFileIDs().getFileID(), message.getFrom(), 
                    "Failed getting file ids.", message.getAuditTrailInformation(), FileAction.FAILURE);
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FAILURE);
            fri.setResponseText("GetFileIDs operation failed with the exception: " + e.getMessage());
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
    private void validateMessage(GetFileIDsRequest message) {
        // Validate the message.
        validateBitrepositoryCollectionId(message.getCollectionID());
        validatePillarId(message.getPillarID());

        checkThatAllRequestedFilesAreAvailable(message);

        log.debug("Message '" + message.getCorrelationID() + "' validated and accepted.");
    }
    
    /**
     * Validates that all the requested files in the filelist are present. 
     * Otherwise an {@link InvalidMessageException} with the appropriate errorcode is thrown.
     * @param message The message containing the list files. An empty filelist is expected 
     * when "AllFiles" or the parameter option is used.
     */
    public void checkThatAllRequestedFilesAreAvailable(GetFileIDsRequest message) {
        FileIDs fileids = message.getFileIDs();
        if(fileids == null) {
            log.debug("No fileids are defined in the identification request ('" + message.getCorrelationID() + "').");
            return;
        }
        
        List<String> missingFiles = new ArrayList<String>();
        String fileID = fileids.getFileID();
        if(fileID != null && !fileID.isEmpty() && !getCache().hasFile(fileID)) {
            missingFiles.add(fileID);
        }
        
        // Throw exception if any files are missing.
        if(!missingFiles.isEmpty()) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            irInfo.setResponseText(missingFiles.size() + " missing files: '" + missingFiles + "'");
            
            throw new InvalidMessageException(irInfo);
        }
    }
    
    /**
     * Method for creating and sending the initial progress message for accepting the operation.
     * @param message The message to base the response upon.
     */
    private void sendInitialProgressMessage(GetFileIDsRequest message) {
        GetFileIDsProgressResponse pResponse = createProgressResponse(message);
        
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Operation accepted. Starting to locate files.");
        pResponse.setResponseInfo(prInfo);

        // Send the ProgressResponse
        getMessageBus().sendMessage(pResponse);
    }
    
    /**
     * Finds the requested FileIDs and either uploads them or puts them into the final response depending on 
     * the request. 
     * @param message The message requesting which fileids to be found.
     * @return The ResultingFileIDs with either the list of fileids or the final address for where the 
     * list of fileids is uploaded.
     */
    private ResultingFileIDs performGetFileIDsOperation(GetFileIDsRequest message) {
        ResultingFileIDs res = new ResultingFileIDs();
        FileIDsData data = retrieveFileIDsData(message.getFileIDs());
        
        getAuditManager().addAuditEvent(message.getFileIDs().getFileID(), message.getFrom(), "Getting the requested "
                + "file ids.", message.getAuditTrailInformation(), FileAction.GET_FILEID);
        
        String resultingAddress = message.getResultAddress();
        if(resultingAddress == null || resultingAddress.isEmpty()) {
            res.setFileIDsData(data);
        } else {
            try {
                File outputFile = makeTemporaryChecksumFile(message, data);
                uploadFile(outputFile, resultingAddress);
                res.setResultAddress(resultingAddress);
            } catch (Exception e) {
                ResponseInfo ir = new ResponseInfo();
                ir.setResponseCode(ResponseCode.FAILURE);
                ir.setResponseText(e.getMessage());
                throw new InvalidMessageException(ir, e);
            }
        }
        
        return res;
    }
    
    /**
     * Retrieves the requested FileIDs. Depending on which operation is requested, it will call the appropriate method.
     * 
     * @param fileIDs The requested FileIDs.
     * @return The resulting collection of FileIDs found.
     * file. 
     */
    private FileIDsData retrieveFileIDsData(FileIDs fileIDs) {
        
        FileIDsData res = new FileIDsData();
        FileIDsDataItems fileIDList = new FileIDsDataItems();
        for(Map.Entry<String, Date> fileDate : getCache().getLastModifiedDate(fileIDs).entrySet()) {
            FileIDsDataItem fileIDData = new FileIDsDataItem();
            fileIDData.setFileID(fileDate.getKey());
            fileIDData.setLastModificationTime(CalendarUtils.getXmlGregorianCalendar(fileDate.getValue()));

            fileIDList.getFileIDsDataItem().add(fileIDData);
        }
        res.setFileIDsDataItems(fileIDList);
        return res;
    }
    
    /**
     * Method for creating a file containing the list of calculated checksums.
     * 
     * @param message The GetChecksumMessage requesting the checksum calculations.
     * @param checksumList The list of checksums to put into the list.
     * @return A file containing all the checksums in the list.
     * @throws IOException If a problem occurs during accessing or handling the data.
     * @throws JAXBException If the resulting structure cannot be serialized or if it is invalid.
     */
    private File makeTemporaryChecksumFile(GetFileIDsRequest message, FileIDsData fileIDs) 
            throws IOException, JAXBException {
        // Create the temporary file.
        File checksumResultFile = File.createTempFile(message.getCorrelationID(), new Date().getTime() + ".id");
        log.debug("Writing the requested fileids to the file '" + checksumResultFile + "'");

        // Print all the checksums safely (close the streams!)
        OutputStream is = null;
        try {
            is = new FileOutputStream(checksumResultFile);
            GetFileIDsResults result = new GetFileIDsResults();
            result.setCollectionID(getSettings().getCollectionID());
            result.setMinVersion(MIN_VERSION);
            result.setVersion(VERSION);
            result.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
            result.setFileIDsData(fileIDs);
            
            JaxbHelper jaxbHelper = new JaxbHelper(XSD_CLASSPATH, XSD_BR_DATA);
            String file = jaxbHelper.serializeToXml(result);
            try {
                jaxbHelper.validate(new ByteArrayInputStream(file.getBytes()));
            } catch (SAXException e) {
                String errMsg = "The resulting XML for the GetFileIDsRequest does not validate. \n"
                        + file;
                log.error(errMsg, e);
                throw new JAXBException(errMsg, e);
            }
            is.write(file.getBytes());
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
     * Send a positive final response telling that the operation has successfully finished.
     * @param message The message to base the final response upon.
     * @param results The results to be put into the final response.
     */
    private void sendFinalResponse(GetFileIDsRequest message, ResultingFileIDs results) {
        GetFileIDsFinalResponse fResponse = createFinalResponse(message);
        
        ResponseInfo fri = new ResponseInfo();
        fri.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        fri.setResponseText("Finished locating the requested files.");
        fResponse.setResponseInfo(fri);
        fResponse.setResultingFileIDs(results);
        
        getMessageBus().sendMessage(fResponse);        
    }
    
    /**
     * Method for sending a response telling that the operation failed.
     * @param message The message to base the response upon.
     * @param fri The information about why the operation failed.
     */
    private void sendFailedResponse(GetFileIDsRequest message, ResponseInfo fri) {
        GetFileIDsFinalResponse fResponse = createFinalResponse(message);
        fResponse.setResponseInfo(fri);
        
        getMessageBus().sendMessage(fResponse);        
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
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCollectionID(getSettings().getCollectionID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());
        res.setCorrelationID(message.getCorrelationID());
        res.setFileIDs(message.getFileIDs());
        res.setFrom(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setResultAddress(message.getResultAddress());
        res.setTo(message.getReplyTo());

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
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCollectionID(getSettings().getCollectionID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setReplyTo(getSettings().getReferenceSettings().getPillarSettings().getReceiverDestination());
        res.setCorrelationID(message.getCorrelationID());
        res.setFileIDs(message.getFileIDs());
        res.setFrom(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setTo(message.getReplyTo());

        return res;
    }
}
