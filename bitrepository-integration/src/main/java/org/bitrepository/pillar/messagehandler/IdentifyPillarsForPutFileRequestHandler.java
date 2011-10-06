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

import org.bitrepository.bitrepositoryelements.ErrorcodeGeneralType;
import org.bitrepository.bitrepositoryelements.IdentifyResponseCodePositiveType;
import org.bitrepository.bitrepositoryelements.IdentifyResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE.TimeMeasureUnit;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.time.TimeMeasurementUtils;
import org.bitrepository.settings.collectionsettings.AlarmLevel;
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
        try {
            // validate message
            validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());

            if(archive.hasFile(message.getFileID())) {
                sendDuplicateFileResponse(message);
            } else if(archive.sizeLeftInArchive() > message.getFileSize().longValue()) {
                // TODO perhaps have a minimum size added?
                sendNotEnoughSpaceResponse(message);
            } else {
                sendPositiveResponse(message);
            }
        } catch (IllegalArgumentException e) {
            if(settings.getCollectionSettings().getPillarSettings().getAlarmLevel() == AlarmLevel.WARNING) {
                alarmDispatcher.alarmIllegalArgument(e);
            } else {
                log.warn("Caught IllegalArgumentException", e);
            }
        }
    }
    
    /**
     * Sending a response telling, that the file is already in the archive.
     * @param message The message requesting the identification of the operation.
     */
    protected void sendDuplicateFileResponse(IdentifyPillarsForPutFileRequest message) {
        log.info("Creating 'duplicate file' reply for '" + message + "'");
        IdentifyPillarsForPutFileResponse reply = createIdentifyPillarsForPutFileResponse(message);

        // Needs to filled in: AuditTrailInformation, PillarChecksumSpec, ReplyTo, TimeToDeliver
        reply.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        TimeMeasureTYPE timeToStartDeliver = new TimeMeasureTYPE();
        timeToStartDeliver.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        timeToStartDeliver.setTimeMeasureValue(BigInteger.valueOf(Long.MAX_VALUE));
        reply.setTimeToDeliver(timeToStartDeliver);
        reply.setAuditTrailInformation(null);
        reply.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR
        
        IdentifyResponseInfo irInfo = new IdentifyResponseInfo();
        irInfo.setIdentifyResponseCode(ErrorcodeGeneralType.DUPLICATE_FILE.value().toString());
        irInfo.setIdentifyResponseText("The file is already in the archive.");
        reply.setIdentifyResponseInfo(irInfo);

        log.debug("Sending IdentifyPillarsForPutfileResponse: " + reply);
        messagebus.sendMessage(reply);
    }
    
    /**
     * Sending a response telling, that there is not enough space in the archive.
     * @param message The message requesting the identification of the operation.
     */
    protected void sendNotEnoughSpaceResponse(IdentifyPillarsForPutFileRequest message) {
        log.info("Creating 'not enough space' reply for '" + message + "'");
        IdentifyPillarsForPutFileResponse reply = createIdentifyPillarsForPutFileResponse(message);

        // Needs to filled in: AuditTrailInformation, PillarChecksumSpec, ReplyTo, TimeToDeliver
        reply.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        TimeMeasureTYPE timeToStartDeliver = new TimeMeasureTYPE();
        timeToStartDeliver.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        timeToStartDeliver.setTimeMeasureValue(BigInteger.valueOf(Long.MAX_VALUE));
        reply.setTimeToDeliver(timeToStartDeliver);
        reply.setAuditTrailInformation(null);
        reply.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR
        
        IdentifyResponseInfo irInfo = new IdentifyResponseInfo();
        irInfo.setIdentifyResponseCode(ErrorcodeGeneralType.FAILURE.value().toString());
        irInfo.setIdentifyResponseText("Not enough space left in this pillar.");
        reply.setIdentifyResponseInfo(irInfo);

        log.debug("Sending IdentifyPillarsForPutfileResponse: " + reply);
        messagebus.sendMessage(reply);
    }
    
    /**
     * Method for sending a positive response for putting this file.
     * @param message The message to respond to.
     */
    protected void sendPositiveResponse(IdentifyPillarsForPutFileRequest message)  {
        log.info("Creating positive reply for '" + message + "'");
        IdentifyPillarsForPutFileResponse reply = createIdentifyPillarsForPutFileResponse(message);

        // Needs to filled in: AuditTrailInformation, PillarChecksumSpec, ReplyTo, TimeToDeliver
        reply.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                settings.getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        reply.setAuditTrailInformation(null);
        reply.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR
        
        IdentifyResponseInfo irInfo = new IdentifyResponseInfo();
        irInfo.setIdentifyResponseCode(IdentifyResponseCodePositiveType.IDENTIFICATION_POSITIVE.value().toString());
        irInfo.setIdentifyResponseText("Operation acknowledged and accepted.");
        reply.setIdentifyResponseInfo(irInfo);

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
        res.setBitRepositoryCollectionID(settings.getCollectionID());
        res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        res.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        
        return res;
    }

}
