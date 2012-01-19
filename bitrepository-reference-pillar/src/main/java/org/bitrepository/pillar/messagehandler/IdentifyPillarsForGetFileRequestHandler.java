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

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.pillar.exceptions.IdentifyPillarsException;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.time.TimeMeasurementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetFile operation.
 */
public class IdentifyPillarsForGetFileRequestHandler extends PillarMessageHandler<IdentifyPillarsForGetFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    public IdentifyPillarsForGetFileRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
        super(settings, messageBus, alarmDispatcher, referenceArchive);
    }
    
    /**
     * Handles the identification messages for the GetFile operation.
     * @param message The IdentifyPillarsForGetFileRequest message to handle.
     */
    public void handleMessage(IdentifyPillarsForGetFileRequest message) {
        ArgumentValidator.checkNotNull(message, "IdentifyPillarsForGetFileRequest message");

        try {
            validateBitrepositoryCollectionId(message.getCollectionID());
            checkThatFileIsAvailable(message);
            respondSuccesfullIdentification(message);
        } catch (IllegalArgumentException e) {
            alarmDispatcher.handleIllegalArgumentException(e);
        } catch (IdentifyPillarsException e) {
            log.warn("Unsuccessfull identification for the GetFile operation.", e);
            respondUnsuccessfulIdentification(message, e);
        } catch (RuntimeException e) {
            alarmDispatcher.handleRuntimeExceptions(e);
        }
    }
    
    /**
     * Validates that the requested file is within the archive. 
     * Otherwise an {@link IdentifyPillarsException} with the appropriate errorcode is thrown.
     * @param message The request for the identification for the GetFileRequest operation.
     */
    private void checkThatFileIsAvailable(IdentifyPillarsForGetFileRequest message) {
        if(!archive.hasFile(message.getFileID())) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND);
            irInfo.setResponseText("The file '" + message.getFileID() 
                    + "' does not exist within the archive.");
            
            throw new IdentifyPillarsException(irInfo);
        }
    }
    
    /**
     * Method for making a successful response to the identification.
     * @param message The request message to respond to.
     */
    private void respondSuccesfullIdentification(IdentifyPillarsForGetFileRequest message) {
        // Create the response.
        IdentifyPillarsForGetFileResponse reply = createIdentifyPillarsForGetFileResponse(message);
        
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
     * Method for sending a bad response.
     * @param message The identification request to respond to.
     */
    private void respondUnsuccessfulIdentification(IdentifyPillarsForGetFileRequest message, 
            IdentifyPillarsException cause) {
        // Create the response.
        IdentifyPillarsForGetFileResponse reply = createIdentifyPillarsForGetFileResponse(message);
        
        reply.setTimeToDeliver(TimeMeasurementUtils.getMaximumTime());
        reply.setResponseInfo(cause.getResponseInfo());
        
        // Send resulting file.
        messagebus.sendMessage(reply);
    }
    
    /**
     * Creates a IdentifyPillarsForGetFileResponse based on a 
     * IdentifyPillarsForGetFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - AuditTrailInformation
     * <br/> - IdentifyResponseInfo
     * 
     * @param msg The IdentifyPillarsForGetFileRequest to base the response on.
     * @return The response to the request.
     */
    private IdentifyPillarsForGetFileResponse createIdentifyPillarsForGetFileResponse(
            IdentifyPillarsForGetFileRequest msg) {
        IdentifyPillarsForGetFileResponse res 
                = new IdentifyPillarsForGetFileResponse();
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
