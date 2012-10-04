/*
 * #%L
 * Bitrepository Reference Pillar
 * *
 * $Id: PutFileRequestHandler.java 687 2012-01-09 12:56:47Z ktc $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/main/java/org/bitrepository/pillar/messagehandler/PutFileRequestHandler.java $
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

import java.io.IOException;
import java.net.URL;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.FileAction;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.cache.ChecksumEntry;
import org.bitrepository.pillar.cache.ChecksumStore;
import org.bitrepository.pillar.common.MessageHandlerContext;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.service.exception.IllegalOperationException;
import org.bitrepository.service.exception.InvalidMessageException;
import org.bitrepository.service.exception.RequestHandlerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for performing the PutFile operation.
 * TODO handle error scenarios.
 */
public class PutFileRequestHandler extends ChecksumPillarMessageHandler<PutFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param context The context of the message handler.
     * @param refCache The cache for the checksum data.
     */
    public PutFileRequestHandler(MessageHandlerContext context, ChecksumStore refCache) {
        super(context, refCache);
    }
    
    @Override
    public Class<PutFileRequest> getRequestClass() {
        return PutFileRequest.class;
    }

    @Override
    public void processRequest(PutFileRequest message) throws RequestHandlerException {
        validateMessage(message);
        tellAboutProgress(message);
        retrieveChecksum(message);
        sendFinalResponse(message);
    }

    @Override
    public MessageResponse generateFailedResponse(PutFileRequest message) {
        return createFinalResponse(message);
    }
    
    /**
     * Validates the message.
     * @param message The message to validate.
     */
    private void validateMessage(PutFileRequest message) throws RequestHandlerException {
        validatePillarId(message.getPillarID());
        if(message.getChecksumDataForNewFile() != null) {
            validateChecksumSpec(message.getChecksumDataForNewFile().getChecksumSpec());
        } else if(getSettings().getCollectionSettings()
                .getProtocolSettings().isRequireChecksumForNewFileRequests()) {
            ResponseInfo responseInfo = new ResponseInfo();
            responseInfo.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
            responseInfo.setResponseText("According to the contract a checksum for creating a new file is required.");
            throw new IllegalOperationException(responseInfo);            
        }
        validateChecksumSpec(message.getChecksumRequestForNewFile());
        validateFileID(message.getFileID());
        
        // verify, that we already have the file
        if(getCache().hasFile(message.getFileID())) {
            log.warn("Cannot perform put for a file, '" + message.getFileID() 
                    + "', which we already have within the archive");
            // Then tell the mediator, that we failed.
            ResponseInfo fri = new ResponseInfo();
            fri.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
            fri.setResponseText("File is already within archive.");
            throw new InvalidMessageException(fri);
        }
    }
    
    /**
     * Method for sending a progress response.
     * @param message The message to base the response upon.
     */
    private void tellAboutProgress(PutFileRequest message) {
        log.info("Respond that we are starting to retrieve the file.");
        PutFileProgressResponse pResponse = createPutFileProgressResponse(message);
        
        // Needs to fill in: AuditTrailInformation, PillarChecksumSpec, ProgressResponseInfo
        pResponse.setPillarChecksumSpec(null);
        ResponseInfo prInfo = new ResponseInfo();
        prInfo.setResponseCode(ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        prInfo.setResponseText("Started to receive data.");  
        pResponse.setResponseInfo(prInfo);
        
        log.debug("Sending ProgressResponseInfo: " + prInfo);
        getMessageSender().sendMessage(pResponse);
    }
    
    /**
     * Retrieves the checksum for the file according to the configuration of the ChecksumPillarFileDownload setting.
     * @param message The PutFileRequest message.
     * @throws RequestHandlerException If the checksum cannot be retrieved.
     */
    private void retrieveChecksum(PutFileRequest message) throws RequestHandlerException {
        String calculatedChecksum;
        switch(getChecksumPillarFileDownload()) {
            case ALWAYS_DOWNLOAD:
                calculatedChecksum = retrieveFile(message);
                break;
            case NEVER_DOWNLOAD:
                calculatedChecksum = extractChecksumFromMessage(message);
                break;
            default:
                if(message.getChecksumDataForNewFile() != null) {
                    calculatedChecksum = extractChecksumFromMessage(message);
                } else {
                    calculatedChecksum = retrieveFile(message);
                }
                break;
        }
        
        getAuditManager().addAuditEvent(message.getFileID(), message.getFrom(), "Putting the checksum of the file "
                + "into archive.", message.getAuditTrailInformation(), FileAction.PUT_FILE);
        getCache().insertChecksumCalculation(message.getFileID(), calculatedChecksum, new Date());
    }
    
    /**
     * Extracts the checksum from PutFileRequest message. 
     * @param message The message to extract the checksum from.
     * @return The checksum from the message.
     * @throws RequestHandlerException If the message does not contain the checksum.
     */
    private String extractChecksumFromMessage(PutFileRequest message) throws RequestHandlerException {
        if(message.getChecksumDataForNewFile() != null) {
            return Base16Utils.decodeBase16(message.getChecksumDataForNewFile().getChecksumValue());
        } else {
            ResponseInfo fi = new ResponseInfo();
            fi.setResponseText("A PutFileRequest without the checksum cannot be handled.");
            fi.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
            throw new InvalidMessageException(fi);
        }
    }
    
    /**
     * Retrieves the actual data, validates it (if possible).
     * @param message The request to for the file to put.
     * @return The checksum of the retrieved file.
     * @throws RequestHandlerException If the operation fails.
     */
    @SuppressWarnings("deprecation")
    private String retrieveFile(PutFileRequest message) throws RequestHandlerException {
        log.debug("Retrieving the data to be stored from URL: '" + message.getFileAddress() + "'");
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange(getSettings());
        
        getAuditManager().addAuditEvent(message.getFileID(), message.getFrom(), "Calculating the validation "
                + "checksum for the file before putting it into the cache.", message.getAuditTrailInformation(), 
                FileAction.CHECKSUM_CALCULATED);
        String calculatedChecksum = null;
        try {
            calculatedChecksum = ChecksumUtils.generateChecksum(fe.downloadFromServer(new URL(message.getFileAddress())),
                    getChecksumType());
        } catch (IOException e) {
            String errMsg = "Could not retrieve the file from '" + message.getFileAddress() + "'";
            log.error(errMsg, e);
            ResponseInfo ri = new ResponseInfo();
            ri.setResponseCode(ResponseCode.FILE_TRANSFER_FAILURE);
            ri.setResponseText(errMsg);
            throw new InvalidMessageException(ri);
        }
        
        if(message.getChecksumDataForNewFile() != null) {
            ChecksumDataForFileTYPE csType = message.getChecksumDataForNewFile();
            String givenChecksum = Base16Utils.decodeBase16(csType.getChecksumValue());
            if(!calculatedChecksum.equals(givenChecksum)) {
                log.error("Wrong checksum! Expected: [" + givenChecksum 
                        + "], but calculated: [" + calculatedChecksum + "]");
                ResponseInfo ri = new ResponseInfo();
                ri.setResponseCode(ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
                ri.setResponseText("Expected checksums '" + givenChecksum + "' but the checksum was '" 
                        + calculatedChecksum + "'.");
                throw new IllegalOperationException(ri);
            }
        } else {
            log.debug("No checksums for validating the retrieved file.");
        }
        
        return calculatedChecksum;
    }
    
    /**
     * Method for sending the final response for the requested put operation.
     * @param message The message requesting the put operation.
     */
    private void sendFinalResponse(PutFileRequest message) {
        PutFileFinalResponse fResponse = createFinalResponse(message);
        
        // insert: AuditTrailInformation, ChecksumsDataForNewFile, FinalResponseInfo, PillarChecksumSpec
        ResponseInfo frInfo = new ResponseInfo();
        frInfo.setResponseCode(ResponseCode.OPERATION_COMPLETED);
        frInfo.setResponseText("The put has be finished.");
        fResponse.setResponseInfo(frInfo);
        fResponse.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR
        
        if(message.getChecksumRequestForNewFile() != null) {
            ChecksumDataForFileTYPE checksumForValidation = new ChecksumDataForFileTYPE();
            
            ChecksumEntry entry = getCache().getEntry(message.getFileID());
            checksumForValidation.setChecksumValue(Base16Utils.encodeBase16(entry.getChecksum()));
            checksumForValidation.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(
                    entry.getCalculationDate()));
            checksumForValidation.setChecksumSpec(message.getChecksumRequestForNewFile());
            log.debug("Requested checksum calculated: " + checksumForValidation);
            
            fResponse.setChecksumDataForNewFile(checksumForValidation);
        } else {
            // TODO is such a request required?
            log.info("No checksum validation requested.");
        }
        
        log.debug("Sending PutFileFinalResponse: " + fResponse);
        getContext().getDispatcher().sendMessage(fResponse);
    }
    
    /**
     * Creates a PutFileProgressResponse based on a PutFileRequest. Missing the 
     * following fields:
     * <br/> - AuditTrailInformation
     * <br/> - PillarChecksumSpec
     * <br/> - ProgressResponseInfo
     * 
     * @param msg The PutFileRequest to base the progress response on.
     * @return The PutFileProgressResponse based on the request.
     */
    private PutFileProgressResponse createPutFileProgressResponse(PutFileRequest msg) {
        PutFileProgressResponse res = new PutFileProgressResponse();
        populateResponse(msg, res);
        res.setFileAddress(msg.getFileAddress());
        res.setFileID(msg.getFileID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setPillarChecksumSpec(getChecksumType());
        
        return res;
    }
    
    /**
     * Creates a PutFileFinalResponse based on a PutFileRequest. Missing the
     * following fields:
     * <br/> - AuditTrailInformation
     * <br/> - ChecksumsDataForNewFile
     * <br/> - FinalResponseInfo
     * <br/> - PillarChecksumSpec
     * 
     * @param msg The PutFileRequest to base the final response message on.
     * @return The PutFileFinalResponse message based on the request.
     */
    private PutFileFinalResponse createFinalResponse(PutFileRequest msg) {
        PutFileFinalResponse res = new PutFileFinalResponse();
        populateResponse(msg, res);
        res.setFileAddress(msg.getFileAddress());
        res.setFileID(msg.getFileID());
        res.setPillarID(getSettings().getReferenceSettings().getPillarSettings().getPillarID());
        res.setPillarChecksumSpec(getChecksumType());
        
        return res;
    }
}
