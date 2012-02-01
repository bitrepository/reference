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

import java.math.BigInteger;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.common.ArgumentValidator;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.pillar.exceptions.IdentifyPillarsException;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.time.TimeMeasurementUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the PutFile operation.
 */
public class IdentifyPillarsForPutFileRequestHandler extends PillarMessageHandler<IdentifyPillarsForPutFileRequest> {

    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    public IdentifyPillarsForPutFileRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
        super(settings, messageBus, alarmDispatcher, referenceArchive);
    }

    /**
     * Handles the identification messages for the PutFile operation.
     * @param message The IdentifyPillarsForPutFileRequest message to handle.
     */
    public void handleMessage(IdentifyPillarsForPutFileRequest message) {
        ArgumentValidator.checkNotNull(message, "IdentifyPillarsForPutFileRequest message");

        try {
            validateBitrepositoryCollectionId(message.getCollectionID());
            checkThatTheFileDoesNotAlreadyExist(message);
            checkSpaceForStoringNewFile(message);
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
     * Validates that the file is not already within the archive. 
     * Otherwise an {@link IdentifyPillarsException} with the appropriate errorcode is thrown.
     * @param message The request with the filename to validate.
     */
    private void checkThatTheFileDoesNotAlreadyExist(IdentifyPillarsForPutFileRequest message) {
        if(message.getFileID() == null) {
            log.debug("No fileid given in the identification request.");
            return;
        }
        
        if(archive.hasFile(message.getFileID())) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.DUPLICATE_FILE_FAILURE);
            irInfo.setResponseText("The file '" + message.getFileID() 
                    + "' already exists within the archive.");
            
            throw new IdentifyPillarsException(irInfo);
        }
    }
    
    /**
     * Validates that enough space exists is left in the archive.
     * Otherwise an {@link IdentifyPillarsException} with the appropriate errorcode is thrown.
     * @param message The request with the size of the file.
     */
    private void checkSpaceForStoringNewFile(IdentifyPillarsForPutFileRequest message) {
        BigInteger fileSize = message.getFileSize();
        if(fileSize == null) {
            log.debug("No file size given in the identification request. "
                    + "Validating that the archive has any space left.");
            fileSize = BigInteger.ZERO;
        }
        
        long useableSizeLeft = archive.sizeLeftInArchive() 
                - settings.getReferenceSettings().getPillarSettings().getMinimumSizeLeft();
        if(useableSizeLeft < fileSize.longValue()) {
            ResponseInfo irInfo = new ResponseInfo();
            irInfo.setResponseCode(ResponseCode.FAILURE);
            irInfo.setResponseText("Not enough space left in this pillar. Requires '" 
                    + fileSize.longValue() + "' but has only '" + useableSizeLeft + "'");
            
            throw new IdentifyPillarsException(irInfo);
        }
    }
    
    /**
     * Sending a response telling, that the file is already in the archive.
     * @param message The message requesting the identification of the operation.
     * @param cause The cause of the bad identification (e.g. that the file already exists).
     */
    protected void respondUnsuccessfulIdentification(IdentifyPillarsForPutFileRequest message,
            IdentifyPillarsException cause) {
        log.info("Creating 'duplicate file' reply for '" + message + "'");
        IdentifyPillarsForPutFileResponse reply = createIdentifyPillarsForPutFileResponse(message);
        
        reply.setTimeToDeliver(TimeMeasurementUtils.getMaximumTime());
        reply.setResponseInfo(cause.getResponseInfo());
        
        messagebus.sendMessage(reply);
    }
    
    /**
     * Method for sending a positive response for putting this file.
     * @param message The message to respond to.
     */
    protected void respondSuccesfullIdentification(IdentifyPillarsForPutFileRequest message)  {
        log.info("Creating positive reply for '" + message + "'");
        IdentifyPillarsForPutFileResponse reply = createIdentifyPillarsForPutFileResponse(message);

        // Needs to filled in: AuditTrailInformation, PillarChecksumSpec, ReplyTo, TimeToDeliver
        reply.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                settings.getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        reply.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR
        
        ResponseInfo irInfo = new ResponseInfo();
        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
        reply.setResponseInfo(irInfo);

        log.debug("Sending IdentifyPillarsForPutfileResponse: " + reply);
        messagebus.sendMessage(reply);
    }
    
    /**
     * Creates a IdentifyPillarsForPutFileResponse based on a 
     * IdentifyPillarsForPutFileRequest. The following fields are not inserted:
     * <br/> - TimeToDeliver
     * <br/> - AuditTrailInformation
     * <br/> - PillarChecksumSpec
     * <br/> - IdentifyResponseInfo
     * 
     * @param msg The IdentifyPillarsForPutFileRequest to base the response on.
     * @return A IdentifyPillarsForPutFileResponse from the request.
     */
    private IdentifyPillarsForPutFileResponse createIdentifyPillarsForPutFileResponse(
            IdentifyPillarsForPutFileRequest msg) {
        IdentifyPillarsForPutFileResponse res
                = new IdentifyPillarsForPutFileResponse();
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(msg.getCorrelationID());
        res.setTo(msg.getReplyTo());
        res.setCollectionID(settings.getCollectionID());
        res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        res.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        
        return res;
    }

}
