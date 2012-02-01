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

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.pillar.exceptions.IdentifyPillarsException;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.time.TimeMeasurementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetChecksums operation.
 */
public class IdentifyPillarsForGetChecksumsRequestHandler 
        extends PillarMessageHandler<IdentifyPillarsForGetChecksumsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    public IdentifyPillarsForGetChecksumsRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
        super(settings, messageBus, alarmDispatcher, referenceArchive);
    }

    /**
     * Handles the identification messages for the GetChecksums operation.
     * @param message The IdentifyPillarsForGetChecksumsRequest message to handle.
     */
    public void handleMessage(IdentifyPillarsForGetChecksumsRequest message) {
        ArgumentValidator.checkNotNull(message, "IdentifyPillarsForGetChecksumsRequest message");

        try {
            validateBitrepositoryCollectionId(message.getCollectionID());
            checkThatAllRequestedFilesAreAvailable(message);
            checkThatTheChecksumFunctionIsAvailable(message);
            respondSuccesfullIdentification(message);
        } catch (IllegalArgumentException e) {
            alarmDispatcher.handleIllegalArgumentException(e);
        } catch (IdentifyPillarsException e) {
            log.warn("Unsuccessfull identification for the GetChecksums operation.", e);
            respondUnsuccessfulIdentification(message, e);
        } catch (RuntimeException e) {
            alarmDispatcher.handleRuntimeExceptions(e);
        }
    }
    
    /**
     * Validates that all the requested files in the filelist are present. 
     * Otherwise an {@link IdentifyPillarsException} with the appropriate errorcode is thrown.
     * @param message The message containing the list files. An empty filelist is expected 
     * when "AllFiles" or the parameter option is used.
     */
    public void checkThatAllRequestedFilesAreAvailable(IdentifyPillarsForGetChecksumsRequest message) {
        FileIDs fileids = message.getFileIDs();
        if(fileids == null) {
            log.debug("No fileids are defined in the identification request ('" + message.getCorrelationID() + "').");
            return;
        }
        
        List<String> missingFiles = new ArrayList<String>();
        String fileID = fileids.getFileID();
        if(fileID != null && !fileID.isEmpty() && !archive.hasFile(fileID)) {
            missingFiles.add(fileID);
        }
        
        // Throw exception if any files are missing.
        if(!missingFiles.isEmpty()) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
            irInfo.setResponseText(missingFiles.size() + " missing files: '" + missingFiles + "'");
            
            throw new IdentifyPillarsException(irInfo);
        }
    }
    
    /**
     * Validates that it is possible to instantiate the requested checksum algorithm.
     * Otherwise an {@link IdentifyPillarsException} with the appropriate errorcode is thrown.
     * @param message The message with the checksum algorithm to validate.
     */
    public void checkThatTheChecksumFunctionIsAvailable(IdentifyPillarsForGetChecksumsRequest message) {
        ChecksumSpecTYPE checksumSpec = message.getChecksumRequestForExistingFile();
        
        // validate that this non-mandatory field has been filled out.
        if(checksumSpec == null || checksumSpec.getChecksumType() == null) {
            log.debug("No checksumSpec in the identification. Thus no reason to expect, that we cannot handle it.");
            return;
        }
        
        try {
            ChecksumUtils.verifyAlgorithm(checksumSpec.getChecksumType());
        } catch (NoSuchAlgorithmException e) {
            log.warn("Could not instantiate the given messagedigester for calculating a checksum.", e);
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FAILURE);
            irInfo.setResponseText("The algorithm '" + checksumSpec.getChecksumType() 
                    + "' cannot be found. Exception: " + e.getLocalizedMessage());
            throw new IdentifyPillarsException(irInfo, e);
        }
    }
    
    /**
     * Method for making a successful response to the identification.
     * @param message The request message to respond to.
     */
    private void respondSuccesfullIdentification(IdentifyPillarsForGetChecksumsRequest message) {
        // Create the response.
        IdentifyPillarsForGetChecksumsResponse reply = createIdentifyPillarsForGetChecksumsResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, AuditTrailInformation, IdentifyResponseInfo
        reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                settings.getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        reply.setResponseInfo(irInfo);
        
        // Send resulting file.
        messagebus.sendMessage(reply);
    }
    
    /**
     * Sends a bad response with the given cause.
     * @param message The identification request to respond to.
     * @param cause The cause of the bad identification (e.g. which files are missing).
     */
    private void respondUnsuccessfulIdentification(IdentifyPillarsForGetChecksumsRequest message, 
            IdentifyPillarsException cause) {
        IdentifyPillarsForGetChecksumsResponse reply = createIdentifyPillarsForGetChecksumsResponse(message);
        
        reply.setTimeToDeliver(TimeMeasurementUtils.getMaximumTime());
        reply.setResponseInfo(cause.getResponseInfo());
        
        messagebus.sendMessage(reply);
    }
    
    /**
     * Creates a IdentifyPillarsForGetChecksumsResponse based on a 
     * IdentifyPillarsForGetFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - AuditTrailInformation
     * <br/> - IdentifyResponseInfo
     * <br/> - PillarChecksumSpec
     * 
     * @param msg The IdentifyPillarsForGetFileRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForGetChecksumsResponse createIdentifyPillarsForGetChecksumsResponse(
            IdentifyPillarsForGetChecksumsRequest msg) {
        IdentifyPillarsForGetChecksumsResponse res 
                = new IdentifyPillarsForGetChecksumsResponse();
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(msg.getCorrelationID());
        res.setFileIDs(msg.getFileIDs());
        res.setTo(msg.getReplyTo());
        res.setChecksumRequestForExistingFile(msg.getChecksumRequestForExistingFile());
        res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        res.setCollectionID(settings.getCollectionID());
        res.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        
        return res;
    }
}
