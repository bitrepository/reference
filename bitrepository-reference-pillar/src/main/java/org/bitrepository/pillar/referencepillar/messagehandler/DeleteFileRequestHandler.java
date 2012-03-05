/*
 * #%L
 * Bitrepository Reference Pillar
 * 
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

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.exceptions.InvalidMessageException;
import org.bitrepository.pillar.referencepillar.ReferenceArchive;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the DeleteFile operation.
 */
public class DeleteFileRequestHandler extends PillarMessageHandler<DeleteFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    protected DeleteFileRequestHandler(Settings settings, MessageBus messageBus, AlarmDispatcher alarmDispatcher,
            ReferenceArchive referenceArchive) {
        super(settings, messageBus, alarmDispatcher, referenceArchive);
    }

    @Override
    void handleMessage(DeleteFileRequest message) {
        ArgumentValidator.checkNotNull(message, "DeleteFileRequest message");

        try {
            validateMessage(message);
            sendProgressMessage(message);
            ChecksumDataForFileTYPE resultingChecksum = calculatedRequestedChecksum(message);
            deleteTheFile(message);
            sendFinalResponse(message, resultingChecksum);
        } catch (InvalidMessageException e) {
            sendFailedResponse(message, e.getResponseInfo());
        } catch (IllegalArgumentException e) {
            log.warn("Caught IllegalArgumentException. Message ", e);
            alarmDispatcher.handleIllegalArgumentException(e);
        } catch (RuntimeException e) {
            log.warn("Internal RunTimeException caught. Sending response for 'error at my end'.", e);
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.FAILURE);
            fri.setResponseText("Error: " + e.getMessage());
            sendFailedResponse(message, fri);
        }
    }
    
    /**
     * Method for validating the content of the message.
     * @param message The message requesting the operation, which should be validated.
     * @return Whether it was valid.
     */
    protected void validateMessage(DeleteFileRequest message) {
        // Validate the message.
        validateBitrepositoryCollectionId(message.getCollectionID());
        validatePillarId(message.getPillarID());

        // Validate, that we have the requested file.
        if(!archive.hasFile(message.getFileID())) {
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            responseInfo.setResponseText("The file '" + message.getFileID() + "' has been requested, but we do "
                    + "not have that file!");
            throw new InvalidMessageException(responseInfo);
        }
        
        // calculate and validate the checksum of the file.
        ChecksumDataForFileTYPE checksumData = message.getChecksumDataForExistingFile();
        ChecksumSpecTYPE checksumType = checksumData.getChecksumSpec();
        if(checksumType == null) {
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.FAILURE);
            responseInfo.setResponseText("A checksum for deletion is required!");
            throw new InvalidMessageException(responseInfo);
        }
        
        String checksum = ChecksumUtils.generateChecksum(archive.getFile(message.getFileID()), 
                checksumType.getChecksumType().value(), checksumType.getChecksumSalt());
        if(!checksum.equals(new String(checksumData.getChecksumValue()))) {
            // Log the different checksums, but do not send the right checksum back!
            log.info("Failed to handle delete operation on file '" + message.getFileID() + "' since the request had "
                    + "the checksum '" + new String(checksumData.getChecksumValue()) + "' where our local file has "
                    + "the value '" + checksum + "'. Sending alarm and respond failure.");
            String errMsg = "Requested to delete file '" + message.getFileID() + "' with checksum '"
                    + new String(checksumData.getChecksumValue()) + "', but our file had a different checksum.";
            alarmDispatcher.sendInvalidChecksumAlarm(message, message.getFileID(), errMsg);
            
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.FAILURE);
            responseInfo.setResponseText(errMsg);
            throw new InvalidMessageException(responseInfo);
        }
    }

    /**
     * The method for sending a progress response telling, that the operation is about to be performed.
     * @param message The request for the GetFile operation.
     */
    protected void sendProgressMessage(DeleteFileRequest message) {
        // make ProgressResponse to tell that we are handling the requested operation.
        DeleteFileProgressResponse pResponse = createDeleteFileProgressResponse(message);
        
        // set missing variables in the message: ResponseInfo
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Starting to delete the file.");
        pResponse.setResponseInfo(prInfo);

        // Send the ProgressResponse
        messagebus.sendMessage(pResponse);
    }
    
    /**
     * Method for calculating the requested checksum. 
     * If no checksum is requested to be delivered back a warning is logged.
     * @param message The request for deleting the file. Contains the specs for calculating the checksum.
     * @return The requested checksum, or null if no such checksum is requested.
     */
    protected ChecksumDataForFileTYPE calculatedRequestedChecksum(DeleteFileRequest message) {
        ChecksumDataForFileTYPE res = new ChecksumDataForFileTYPE();
        ChecksumSpecTYPE checksumType = message.getChecksumRequestForExistingFile();
        
        if(checksumType == null) {
            log.warn("No checksum requested for the file about to be deleted.");
            return null;
        }
        String checksum = ChecksumUtils.generateChecksum(archive.getFile(message.getFileID()), 
                checksumType.getChecksumType().value(), checksumType.getChecksumSalt());
        
        res.setChecksumSpec(checksumType);
        res.setCalculationTimestamp(CalendarUtils.getNow());
        res.setChecksumValue(checksum.getBytes());
        
        return res;
    }
    
    /**
     * Performs the operation of deleting the file from the archive.
     * @param message The message requesting the file to be deleted.
     */
    protected void deleteTheFile(DeleteFileRequest message) {
        try {
            archive.deleteFile(message.getFileID());
        } catch (Exception e) {
            ResponseInfo ir = new ResponseInfo();
            ir.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            ir.setResponseText("Could not delete the file from the archive: " + e.getMessage());
            throw new InvalidMessageException(ir, e);
        }
    }

    /**
     * Method for sending the final response.
     * @param message The message to respond to.
     * @param requestedChecksum The results of the requested checksums
     */
    protected void sendFinalResponse(DeleteFileRequest message, ChecksumDataForFileTYPE requestedChecksum) {
        // make ProgressResponse to tell that we are handling this.
        DeleteFileFinalResponse fResponse = createDeleteFileFinalResponse(message);
        fResponse.setChecksumDataForExistingFile(requestedChecksum);
        ResponseInfo frInfo = new ResponseInfo();
        frInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        frInfo.setResponseText("Data delivered.");
        fResponse.setResponseInfo(frInfo);

        // send the FinalResponse.
        messagebus.sendMessage(fResponse);
    }
    
    /**
     * Method for sending a response telling that the operation has failed.
     * @param message The message requesting the operation.
     * @param frInfo The information about what went wrong.
     */
    protected void sendFailedResponse(DeleteFileRequest message, ResponseInfo frInfo) {
        log.info("Sending bad DeleteFileFinalResponse: " + frInfo);
        DeleteFileFinalResponse fResponse = createDeleteFileFinalResponse(message);
        fResponse.setResponseInfo(frInfo);
        messagebus.sendMessage(fResponse);
    }
    
    /**
     * Creates a DeleteFileProgressResponse based on a DeleteFileRequest. Missing the 
     * following fields:
     * <br/> - ResponseInfo
     * 
     * @param msg The DeleteFileRequest to base the progress response on.
     * @return The DeleteFileProgressResponse based on the request.
     */
    private DeleteFileProgressResponse createDeleteFileProgressResponse(DeleteFileRequest message) {
        DeleteFileProgressResponse res = new DeleteFileProgressResponse();
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(message.getCorrelationID());
        res.setFileID(message.getFileID());
        res.setTo(message.getReplyTo());
        res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        res.setCollectionID(settings.getCollectionID());
        res.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        
        return res;
    }
    
    /**
     * Creates a DeleteFileFinalResponse based on a DeleteFileRequest. Missing the 
     * following fields:
     * <br/> - ResponseInfo
     * <br/> - ChecksumDataForFile
     * 
     * @param msg The DeleteFileRequest to base the final response on.
     * @return The DeleteFileFinalResponse based on the request.
     */
    private DeleteFileFinalResponse createDeleteFileFinalResponse(DeleteFileRequest msg) {
        DeleteFileFinalResponse res = new DeleteFileFinalResponse();
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(msg.getCorrelationID());
        res.setFileID(msg.getFileID());
        res.setTo(msg.getReplyTo());
        res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        res.setCollectionID(settings.getCollectionID());
        res.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());

        return res;
    }
}
