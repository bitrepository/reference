///*
// * #%L
// * Bitrepository Reference Pillar
// * 
// * $Id$
// * $HeadURL$
// * %%
// * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
// * %%
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as 
// * published by the Free Software Foundation, either version 2.1 of the 
// * License, or (at your option) any later version.
// * 
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU General Lesser Public License for more details.
// * 
// * You should have received a copy of the GNU General Lesser Public 
// * License along with this program.  If not, see
// * <http://www.gnu.org/licenses/lgpl-2.1.html>.
// * #L%
// */
//package org.bitrepository.pillar.checksumpillar.messagehandler;
//
//import org.bitrepository.bitrepositoryelements.ResponseCode;
//import org.bitrepository.bitrepositoryelements.ResponseInfo;
//import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
//import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
//import org.bitrepository.common.ArgumentValidator;
//import org.bitrepository.common.settings.Settings;
//import org.bitrepository.pillar.exceptions.IdentifyPillarsException;
//import org.bitrepository.pillar.referencepillar.ReferenceArchive;
//import org.bitrepository.protocol.messagebus.MessageBus;
//import org.bitrepository.protocol.time.TimeMeasurementUtils;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
///**
// * Class for handling the identification of this pillar for the purpose of performing the ReplaceFile operation.
// */
//public class IdentifyPillarsForReplaceFileRequestHandler 
//        extends ChecksumPillarMessageHandler<IdentifyPillarsForReplaceFileRequest> {
//    /** The log.*/
//    private Logger log = LoggerFactory.getLogger(getClass());
//
//    /**
//     * Constructor.
//     * @param settings The settings for handling the message.
//     * @param messageBus The bus for communication.
//     * @param alarmDispatcher The dispatcher of alarms.
//     * @param referenceArchive The archive for the data.
//     */
//    protected IdentifyPillarsForReplaceFileRequestHandler(Settings settings, MessageBus messageBus,
//            AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
//        super(settings, messageBus, alarmDispatcher, referenceArchive);
//    }
//
//    @Override
//    void handleMessage(IdentifyPillarsForReplaceFileRequest message) {
//        ArgumentValidator.checkNotNull(message, "IdentifyPillarsForReplaceFileRequest message");
//
//        try {
//            validateBitrepositoryCollectionId(message.getCollectionID());
//            checkThatRequestedFileIsAvailable(message);
//            respondSuccessfulIdentification(message);
//        } catch (IllegalArgumentException e) {
//            alarmDispatcher.handleIllegalArgumentException(e);
//        } catch (IdentifyPillarsException e) {
//            log.warn("Unsuccessful identification for the ReplaceFile operation.", e);
//            respondUnsuccessfulIdentification(message, e);
//        } catch (RuntimeException e) {
//            alarmDispatcher.handleRuntimeExceptions(e);
//        }
//    }
//    
//    /**
//     * Validates that the requested files are present in the archive. 
//     * Otherwise an {@link IdentifyPillarsException} with the appropriate errorcode is thrown.
//     * @param message The message containing the id of the file. 
//     */
//    public void checkThatRequestedFileIsAvailable(IdentifyPillarsForReplaceFileRequest message) {
//        if(!archive.hasFile(message.getFileID())) {
//            ResponseInfo irInfo = new ResponseInfo();
//            irInfo.setResponseCode(ResponseCode.FILE_NOT_FOUND_FAILURE);
//            irInfo.setResponseText("Could not find the requested file to delete.");
//            throw new IdentifyPillarsException(irInfo);
//        }
//    }
//
//    /**
//     * Method for making a successful response to the identification.
//     * @param message The request message to respond to.
//     */
//    private void respondSuccessfulIdentification(IdentifyPillarsForReplaceFileRequest message) {
//        // Create the response.
//        IdentifyPillarsForReplaceFileResponse reply = createIdentifyPillarsForReplaceFileResponse(message);
//        
//        // set the missing variables in the reply:
//        // TimeToDeliver, IdentifyResponseInfo (ignore PillarChecksumSpec)
//        reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
//                settings.getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
//        
//        ResponseInfo irInfo = new ResponseInfo();
//        irInfo.setResponseCode(ResponseCode.IDENTIFICATION_POSITIVE);
//        irInfo.setResponseText(RESPONSE_FOR_POSITIVE_IDENTIFICATION);
//        reply.setResponseInfo(irInfo);
//        
//        messagebus.sendMessage(reply);
//    }
//    
//    /**
//     * Sends a bad response with the given cause.
//     * @param message The identification request to respond to.
//     * @param cause The cause of the bad identification (e.g. which file is missing).
//     */
//    private void respondUnsuccessfulIdentification(IdentifyPillarsForReplaceFileRequest message, 
//            IdentifyPillarsException cause) {
//        IdentifyPillarsForReplaceFileResponse reply = createIdentifyPillarsForReplaceFileResponse(message);
//        
//        reply.setResponseInfo(cause.getResponseInfo());
//        
//        messagebus.sendMessage(reply);
//    }
//    
//    /**
//     * Creates a IdentifyPillarsForGetChecksumsResponse based on a 
//     * IdentifyPillarsForReplaceFileRequest. The following fields are not inserted:
//     * <br/> - TimeToDeliver
//     * <br/> - ResponseInfo
//     * <br/> - PillarChecksumSpec
//     * 
//     * @param msg The IdentifyPillarsForReplaceFileRequest to base the response on.
//     * @return The response to the request.
//     */
//    private IdentifyPillarsForReplaceFileResponse createIdentifyPillarsForReplaceFileResponse(
//            IdentifyPillarsForReplaceFileRequest msg) {
//        IdentifyPillarsForReplaceFileResponse res = new IdentifyPillarsForReplaceFileResponse();
//        res.setMinVersion(MIN_VERSION);
//        res.setVersion(VERSION);
//        res.setCorrelationID(msg.getCorrelationID());
//        res.setFileID(msg.getFileID());
//        res.setTo(msg.getReplyTo());
//        res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
//        res.setCollectionID(settings.getCollectionID());
//        res.setReplyTo(settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
//        
//        return res;
//    }
//}
