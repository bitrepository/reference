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
package org.bitrepository.pillar.checksumpillar.messagehandler;

import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.checksumpillar.cache.ChecksumCache;
import org.bitrepository.pillar.exceptions.IdentifyPillarsException;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.time.TimeMeasurementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetFileIDs operation.
 */
public class IdentifyPillarsForGetFileIDsRequestHandler 
        extends ChecksumPillarMessageHandler<IdentifyPillarsForGetFileIDsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceCache The cache for the data.
     */
    public IdentifyPillarsForGetFileIDsRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ChecksumCache referenceCache) {
        super(settings, messageBus, alarmDispatcher, referenceCache);
    }
    
    /**
     * Handles the identification messages for the GetFileIDs operation.
     * @param message The IdentifyPillarsForGetFileIDsRequest message to handle.
     */
    public void handleMessage(IdentifyPillarsForGetFileIDsRequest message) {
        ArgumentValidator.checkNotNull(message, "IdentifyPillarsForGetFileIDsRequest message");

        try {
            validateBitrepositoryCollectionId(message.getCollectionID());
            checkThatAllRequestedFilesAreAvailable(message);
            respondSuccesfullIdentification(message);
        } catch (IllegalArgumentException e) {
            alarmDispatcher.handleIllegalArgumentException(e);
        } catch (IdentifyPillarsException e) {
            log.warn("Unsuccessfull identification for the GetFileIDs.", e);
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
    public void checkThatAllRequestedFilesAreAvailable(IdentifyPillarsForGetFileIDsRequest message) {
        FileIDs fileids = message.getFileIDs();
        if(fileids == null) {
            log.debug("No fileids are defined in the identification request ('" + message.getCorrelationID() + "').");
            return;
        }
        
        List<String> missingFiles = new ArrayList<String>();
        String fileID = fileids.getFileID();
        if(fileID != null && !fileID.isEmpty() && !cache.hasFile(fileID)) {
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
     * Makes a response to the successful identification.
     * @param message The request message to respond to.
     */
    private void respondSuccesfullIdentification(IdentifyPillarsForGetFileIDsRequest message) {
        // Create the response.
        IdentifyPillarsForGetFileIDsResponse reply = createIdentifyPillarsForGetFileIDsResponse(message);
        
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
     * Sending a bad response with the 
     * @param message The identification request to respond to.
     * @param cause The cause of the unsuccessful identification (e.g. which files are missing).
     */
    private void respondUnsuccessfulIdentification(IdentifyPillarsForGetFileIDsRequest message, 
            IdentifyPillarsException cause) {
        IdentifyPillarsForGetFileIDsResponse reply = createIdentifyPillarsForGetFileIDsResponse(message);
        
        reply.setResponseInfo(cause.getResponseInfo());
        
        // Set to maximum time to indicate that it is a bad reply.
        reply.setTimeToDeliver(TimeMeasurementUtils.getMaximumTime());
        
        messagebus.sendMessage(reply);
    }
    
    /**
     * Creates a IdentifyPillarsForGetFileIDsResponse based on a 
     * IdentifyPillarsForGetFileIDsRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - IdentifyResponseInfo
     * 
     * @param msg The IdentifyPillarsForGetFileIDsRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForGetFileIDsResponse createIdentifyPillarsForGetFileIDsResponse(
            IdentifyPillarsForGetFileIDsRequest msg) {
        IdentifyPillarsForGetFileIDsResponse res 
                = new IdentifyPillarsForGetFileIDsResponse();
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(msg.getCorrelationID());
        res.setFileIDs(msg.getFileIDs());
        res.setTo(msg.getReplyTo());
        res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        res.setCollectionID(settings.getCollectionID());
        res.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        
        return res;
    }
}