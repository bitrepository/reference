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
import java.util.Date;

import javax.xml.bind.JAXBException;

import org.apache.activemq.util.ByteArrayInputStream;
import org.bitrepository.bitrepositorydata.GetFileIDsResults;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.FileIDsData;
import org.bitrepository.bitrepositoryelements.FileIDsData.FileIDsDataItems;
import org.bitrepository.bitrepositoryelements.FileIDsDataItem;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositoryelements.ResultingFileIDs;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.JaxbHelper;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.pillar.cache.ChecksumEntry;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
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
    public GetFileIDsRequestHandler(MessageHandlerContext context, ChecksumStore refCache) {
        super(context,  refCache);
    }
    
    @Override
    public Class<GetFileIDsRequest> getRequestClass() {
        return GetFileIDsRequest.class;
    }

    @Override
    public void processRequest(GetFileIDsRequest message) throws RequestHandlerException {
        validateMessage(message);
        sendInitialProgressMessage(message);
        ResultingFileIDs results = performGetFileIDsOperation(message);
        sendFinalResponse(message, results);
    }

    @Override
    public MessageResponse generateFailedResponse(GetFileIDsRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Method for validating the content of the GetFileIDsRequest message. 
     * @param message The message to validate.
     * @throws RequestHandlerException If the requested operation is not possible to perform.
     */
    private void validateMessage(GetFileIDsRequest message) throws RequestHandlerException {
        validatePillarId(message.getPillarID());
        validateFileID(message.getFileIDs().getFileID());
        checkThatAllRequestedFilesAreAvailable(message);

        log.debug("Message '" + message.getCorrelationID() + "' validated and accepted.");
    }
    
    /**
     * Validates that all the requested files in the filelist are present. 
     * Otherwise an {@link InvalidMessageException} with the appropriate errorcode is thrown.
     * @param message The message containing the list files. An empty filelist is expected 
     * when "AllFiles" or the parameter option is used.
     * @throws InvalidMessageException If the requested file does not exist.
     */
    public void checkThatAllRequestedFilesAreAvailable(GetFileIDsRequest message) throws InvalidMessageException {
        FileIDs fileids = message.getFileIDs();
        if(fileids.isSetAllFileIDs()) {
            return ;
        }
        
        if(!getCache().hasFile(fileids.getFileID())) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            throw new InvalidMessageException(irInfo);
        }
    }
    
    /**
     * Method for creating and sending the initial progress response for accepting the operation.
     * @param request The request to base the response upon.
     */
    private void sendInitialProgressMessage(GetFileIDsRequest request) {
        GetFileIDsProgressResponse response = createProgressResponse(request);
        
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        response.setResponseInfo(prInfo);

        dispatchResponse(response, request);
    }
    
    /**
     * Finds the requested FileIDs and either uploads them or puts them into the final response depending on 
     * the request. 
     * @param message The message requesting which fileids to be found.
     * @return The ResultingFileIDs with either the list of fileids or the final address for where the 
     * list of fileids is uploaded.
     */
    private ResultingFileIDs performGetFileIDsOperation(GetFileIDsRequest message) throws RequestHandlerException {
        ResultingFileIDs res = new ResultingFileIDs();
        FileIDsData data = retrieveFileIDsData(message.getFileIDs());
        
        String resultingAddress = message.getResultAddress();
        if(resultingAddress == null) {
            res.setFileIDsData(data);
        } else {
            try {
                File outputFile = makeTemporaryChecksumFile(message, data);
                uploadFile(outputFile, resultingAddress);
                res.setResultAddress(resultingAddress);
            } catch (Exception e) {
                ResponseInfo ir = new ResponseInfo();
                ir.setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
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
        if(fileIDs.isSetFileID()) {
            ChecksumEntry entry = getCache().getEntry(fileIDs.getFileID());
            FileIDsDataItem fileIDData = new FileIDsDataItem();
            fileIDData.setFileID(entry.getFileId());
            fileIDData.setLastModificationTime(CalendarUtils.getXmlGregorianCalendar(entry.getCalculationDate()));
            fileIDList.getFileIDsDataItem().add(fileIDData);
        } else {
            for(ChecksumEntry entry : getCache().getAllEntries()) {
                FileIDsDataItem fileIDData = new FileIDsDataItem();
                fileIDData.setFileID(entry.getFileId());
                fileIDData.setLastModificationTime(CalendarUtils.getXmlGregorianCalendar(entry.getCalculationDate()));
                fileIDList.getFileIDsDataItem().add(fileIDData);
            }
        }
        
        res.setFileIDsDataItems(fileIDList);
        return res;
    }
    
    /**
     * Method for creating a file containing the list of calculated checksums.
     * 
     * @param message The GetChecksumMessage requesting the checksum calculations.
     * @param fileIDs The list of checksums to put into the list.
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
    private void uploadFile(File fileToUpload, String url) throws IOException {
        URL uploadUrl = new URL(url);
        
        // Upload the file.
        log.debug("Uploading file: " + fileToUpload.getName() + " to " + url);
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(getSettings());
        fe.uploadToServer(new FileInputStream(fileToUpload), uploadUrl);
    }
    
    /**
     * Send a positive final response telling that the operation has successfully finished.
     * @param request The request to base the final response upon.
     * @param results The results to be put into the final response.
     */
    private void sendFinalResponse(GetFileIDsRequest request, ResultingFileIDs results) {
        GetFileIDsFinalResponse response = createFinalResponse(request);
        
        ResponseInfo fri = new ResponseInfo();
        fri.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        response.setResponseInfo(fri);
        response.setResultingFileIDs(results);

        dispatchResponse(response, request);
    }
    
    /**
     * Create a generic final response for the GetFileIDs conversation.
     * Missing:
     * <br/> - ProgressResponseInfo
     * 
     * @param request The GetFileIDsRequest to base the response upon.
     * @return The GetFileIDsFinalResponse.
     */
    private GetFileIDsProgressResponse createProgressResponse(GetFileIDsRequest request) {
        GetFileIDsProgressResponse res = new GetFileIDsProgressResponse();
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setFileIDs(request.getFileIDs());
        res.setResultAddress(request.getResultAddress());

        return res;
    }
    
    /**
     * Create a generic final response for the GetFileIDs conversation.
     * Missing:
     * <br/> - FinalResponseInfo
     * <br/> - ResultingFileIDs
     * 
     * @param request The GetFileIDsRequest to base the response upon.
     * @return The GetFileIDsFinalResponse.
     */
    private GetFileIDsFinalResponse createFinalResponse(GetFileIDsRequest request) {
        GetFileIDsFinalResponse res = new GetFileIDsFinalResponse();
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setFileIDs(request.getFileIDs());

        return res;
    }
}
