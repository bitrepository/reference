/*
 * #%L
 * Bitrepository Reference Pillar
 * 
 * $Id$
 * $HeadURL$
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.cache.ChecksumEntry;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.service.exception.IllegalOperationException;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the ReplaceFile operation.
 * In the positive scenario: <ul> 
 * <li> It starts by validating the message </li> 
 * <li> Afterwards it sends a positive progress to tell that the new file is being downloaded </li>
 * <li> Then the new file is downloaded to 'tempDir' </li>
 * <li> A progress message is then send to tell that the new file is replacing the old one.</li>
 * <li> Then it performs the replacement of the file by deleting the old one (moving it from 'fileDir' to 'retainDir') 
 * and then moves the new file from 'tempDir' to 'fileDir'</li>
 * <li> Finally the complete message is send.</li>
 * </ul>
 * If anything goes wrong a FinalResponse is send telling about the failure.
 */
public class ReplaceFileRequestHandler extends ChecksumPillarMessageHandler<ReplaceFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param refCache The cache for the checksum data.
     */
    public ReplaceFileRequestHandler(PillarContext context, ChecksumStore refCache) {
        super(context, refCache);
    }

    @Override
    public Class<ReplaceFileRequest> getRequestClass() {
        return ReplaceFileRequest.class;
    }

    @Override
    public void processRequest(ReplaceFileRequest message) throws RequestHandlerException {
        validateMessage(message);
        sendProgressMessageDownloadNewFile(message);
        String newChecksum = downloadTheNewFile(message);
        sendProgressMessageDeleteOldFile(message);
        ChecksumDataForFileTYPE requestedOldChecksum = calculateChecksumOnOldFile(message);
        replaceTheEntry(message, newChecksum);
        ChecksumDataForFileTYPE requestedNewChecksum = calculateChecksumOnNewFile(message);
        sendFinalResponse(message, requestedOldChecksum, requestedNewChecksum);
    }

    @Override
    public MessageResponse generateFailedResponse(ReplaceFileRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Method for validating the content of the message.
     * @param message The message requesting the operation, which should be validated.
     * @return Whether it was valid.
     */
    protected void validateMessage(ReplaceFileRequest message) throws RequestHandlerException {
        validatePillarId(message.getPillarID());
        validateChecksumSpec(message.getChecksumRequestForExistingFile());
        validateChecksumSpec(message.getChecksumRequestForNewFile());
        if(message.getChecksumDataForExistingFile() != null) {
            validateChecksumSpec(message.getChecksumDataForExistingFile().getChecksumSpec());
        } else if(getSettings().getCollectionSettings().getProtocolSettings()
                .isRequireChecksumForDestructiveRequests()) {
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
            responseInfo.setResponseText("According to the contract a checksum for file to be deleted during the "
                    + "replacing operation is required.");
            throw new IllegalOperationException(responseInfo);
        }
        if(message.getChecksumDataForNewFile() != null) {
            validateChecksumSpec(message.getChecksumDataForNewFile().getChecksumSpec());
        } else if(getSettings().getCollectionSettings().getProtocolSettings()
                .isRequireChecksumForNewFileRequests()) {
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
            responseInfo.setResponseText("According to the contract a checksum for new file in the "
                    + "replacing operation is required.");
            throw new IllegalOperationException(responseInfo);
        }
        
        validateFileID(message.getFileID());
        
        // Validate, that we have the requested file.
        if(!getCache().hasFile(message.getFileID())) {
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            responseInfo.setResponseText("The file '" + message.getFileID() + "' has been requested, but we do "
                    + "not have that file!");
            throw new InvalidMessageException(responseInfo);
        }
        
        // calculate and validate the checksum of the file.
        ChecksumDataForFileTYPE checksumData = message.getChecksumDataForExistingFile();
        if(checksumData != null) { 
            String checksum = getCache().getChecksum(message.getFileID());
            String requestedChecksum = Base16Utils.decodeBase16(checksumData.getChecksumValue());
            if(!checksum.equals(requestedChecksum)) {
                // Log the different checksums, but do not send the right checksum back!
                log.info("Failed to handle replace operation on file '" + message.getFileID() + "' since the request had "
                        + "the checksum '" + requestedChecksum + "' where our local file has the value '" + checksum 
                        + "'. Sending alarm and respond failure.");
                String errMsg = "Requested to replace the file '" + message.getFileID() + "' with checksum '"
                        + requestedChecksum + "', but our file had a different checksum.";
                
                ResponseInfo responseInfo = new ResponseInfo();
                responseInfo.setResponseCode(ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
                responseInfo.setResponseText(errMsg);
                throw new IllegalOperationException(responseInfo);
            }
        } else {
            log.debug("No checksum for validation of the existing file before replace.");
        }
    }
    
    /**
     * Sends a progress message to tell that the replacement is happening.
     * @param message The request to base the progress response upon.
     */
    private void sendProgressMessageDownloadNewFile(ReplaceFileRequest message) {       
        ReplaceFileProgressResponse response = createProgressResponse(message);
        String responseText = "Progress: downloading the new file from: '" + message.getFileAddress() + "'";
        log.debug(responseText);
        
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        responseInfo.setResponseText(responseText);
        
        response.setResponseInfo(responseInfo);
        getMessageBus().sendMessage(response);
    }
    
    /**
     * Downloading the new file to replace the old one. 
     * Also validates against the given checksum. Will log a warning, if no checksum for validation is in the request.
     * @param message The request containing the location of the file and the checksum of it.
     */
    @SuppressWarnings("deprecation")
    private String downloadTheNewFile(ReplaceFileRequest message) throws RequestHandlerException {
        log.debug("Retrieving the data to be stored from URL: '" + message.getFileAddress() + "'");
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();

        getAuditManager().addAuditEvent(message.getFileID(), message.getFrom(), "Calculating the checksum of the "
                + "downloaded file for the replace operation.", message.getAuditTrailInformation(), 
                FileAction.CHECKSUM_CALCULATED);
        String checksum = null;
        try {
            checksum = ChecksumUtils.generateChecksum(fe.downloadFromServer(new URL(message.getFileAddress())), 
                    getChecksumType());
        } catch (IOException e) {
            String errMsg = "Could not retrieve the file from '" + message.getFileAddress() + "'";
            log.error(errMsg, e);
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
            ri.setResponseText(errMsg);
            throw new InvalidMessageException(ri);
        }
        
        ChecksumDataForFileTYPE csType = message.getChecksumDataForNewFile();
        if(csType != null) {
            String requestedChecksum = Base16Utils.decodeBase16(csType.getChecksumValue());
            if(!checksum.equals(requestedChecksum)) {
                ResponseInfo responseInfo = new ResponseInfo();
                responseInfo.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
                responseInfo.setResponseText("Wrong checksum! Expected: [" + requestedChecksum 
                        + "], but calculated: [" + checksum + "]");
                throw new IllegalOperationException(responseInfo);
            }
        } else {
            log.warn("No checksum for validating the new file.");
        }
        
        return checksum;
    }
    
    /**
     * Replaces the old entry with the new one.
     * @param message The request for replacing the entry.
     * @param newChecksum The new checksum to replace the old one with.
     */
    private void replaceTheEntry(ReplaceFileRequest message, String newChecksum) {
        getAuditManager().addAuditEvent(message.getFileID(), message.getFrom(), "Replacing the file.", 
                message.getAuditTrailInformation(), FileAction.REPLACE_FILE); 
        getCache().insertChecksumCalculation(message.getFileID(), newChecksum, new Date());
    }

    /**
     * Sends a progress message to tell that the replacement is happening.
     * @param message The request to base the progerss response upon.
     */
    private void sendProgressMessageDeleteOldFile(ReplaceFileRequest message) {       
        ReplaceFileProgressResponse response = createProgressResponse(message);
        String responseText = "Progress: deleting the old file '" + message.getFileID() + "' and replacing it with '"
                + message.getFileAddress() + "'";
        log.debug(responseText);
        
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        responseInfo.setResponseText(responseText);
        
        response.setResponseInfo(responseInfo);
        getMessageBus().sendMessage(response);
    }
    
    /**
     * Calculates the checksum for the old file.
     * ONLY USE BEFORE THE FILE HAS BEEN REMOVED FROM THE ARCHIVE!
     * @param message The message containing the request for the checksum to be calculated, along with the 
     * specification about which algorithm and salt to use.
     * @return The checksum data for the old file in the archive.
     * If no checksum specification for the 'old' file has been defined, then an null is returned.
     */
    private ChecksumDataForFileTYPE calculateChecksumOnOldFile(ReplaceFileRequest message) {
        ChecksumSpecTYPE csType = message.getChecksumRequestForExistingFile();
        if(csType != null) {
            return calculatedChecksumForFile(csType, message.getFileID());
        }
        
        return null;
    }

    /**
     * Calculates the checksum for the new file.
     * ONLY USE WHEN THE FILE HAS BEEN MOVED TO THE ARCHIVE!
     * @param message The message containing the request for the checksum to be calculated, along with the 
     * specification about which algorithm and salt to use.
     * @return The checksum data for the new file in the archive.
     * If no checksum specification for the 'old' file has been defined, then an null is returned.
     */
    private ChecksumDataForFileTYPE calculateChecksumOnNewFile(ReplaceFileRequest message) {
        ChecksumSpecTYPE csType = message.getChecksumRequestForNewFile();
        if(csType != null) {
            return calculatedChecksumForFile(csType, message.getFileID());
        }
        
        return null;
    }

    /**
     * Calculates the specified checksum on the file in the archive with the given file id.
     * @param checksumType The specification about which type of checksum to calculate.
     * @param fileId The id of the file to calculate the checksum upon. 
     * @return The checksum for the given file.
     */
    private ChecksumDataForFileTYPE calculatedChecksumForFile(ChecksumSpecTYPE checksumType, String fileId) {
        ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
        
        ChecksumEntry entry = getCache().getEntry(fileId);
        
        res.setChecksumSpec(checksumType);
        res.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(entry.getCalculationDate()));
        res.setChecksumValue(Base16Utils.encodeBase16(entry.getChecksum()));
        
        return res;
    }
    
    /**
     * Sends the final response for a successful finish of the operation.
     * @param message The message to base the response upon.
     * @param requestedOldChecksum [OPTIONAL] The requested checksum for the old file.
     * @param requestedNewChecksum [OPTIONAL] The requested checksum for the new file.
     */
    private void sendFinalResponse(ReplaceFileRequest message, ChecksumDataForFileTYPE requestedOldChecksum, 
            ChecksumDataForFileTYPE requestedNewChecksum) {
        ReplaceFileFinalResponse response = createFinalResponse(message);
        
        ResponseInfo ri = new ResponseInfo();
        ri.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        ri.setResponseText("Successfully replaced the file '" + message.getFileID() + "' as requested!");
        response.setResponseInfo(ri);
        
        response.setChecksumDataForNewFile(requestedNewChecksum);
        response.setChecksumDataForExistingFile(requestedOldChecksum);
        
        getMessageBus().sendMessage(response);
    }
    
    /**
     * Creates the generic ReplaceFileFinalResponse based on the request message.
     * Missing fields:
     * <br/> ResponseInfo
     * <br/> PillarChecksumSpec
     * @param message The ReplaceFileRequest to base the response upon.
     * @return The ReplaceFileFinalResponse based on the request.
     */
    private ReplaceFileProgressResponse createProgressResponse(ReplaceFileRequest message) {
        ReplaceFileProgressResponse res = new ReplaceFileProgressResponse();
        populateResponse(message, res);
        res.setFileAddress(message.getFileAddress());
        res.setFileID(message.getFileID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setPillarChecksumSpec(getChecksumType());

        return res;
    }
    
    /**
     * Creates the generic ReplaceFileFinalResponse based on the request message.
     * Missing fields:
     * <br/> ResponseInfo
     * <br/> ChecksumDataForFile
     * <br/> PillarChecksumSpec
     * @param message The ReplaceFileRequest to base the response upon.
     * @return The ReplaceFileFinalResponse based on the request.
     */
    private ReplaceFileFinalResponse createFinalResponse(ReplaceFileRequest message) {
        ReplaceFileFinalResponse res = new ReplaceFileFinalResponse();
        populateResponse(message, res);
        res.setFileAddress(message.getFileAddress());
        res.setFileID(message.getFileID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setPillarChecksumSpec(getChecksumType());

        return res;
    }
}
