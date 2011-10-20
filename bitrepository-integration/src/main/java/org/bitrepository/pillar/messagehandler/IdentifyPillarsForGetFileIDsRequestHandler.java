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
import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ErrorcodeGeneralType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.IdentifyResponseCodePositiveType;
import org.bitrepository.bitrepositoryelements.IdentifyResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE.TimeMeasureUnit;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.time.TimeMeasurementUtils;
import org.bitrepository.settings.collectionsettings.AlarmLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the identification of this pillar for the purpose of performing the GetFileIDs operation.
 */
public class IdentifyPillarsForGetFileIDsRequestHandler extends PillarMessageHandler<IdentifyPillarsForGetFileIDsRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    
    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    public IdentifyPillarsForGetFileIDsRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
        super(settings, messageBus, alarmDispatcher, referenceArchive);
    }
    
    /**
     * Handles the identification messages for the GetFileIDs operation.
     * TODO perhaps synchronisation?
     * @param message The IdentifyPillarsForGetFileIDsRequest message to handle.
     */
    public void handleMessage(IdentifyPillarsForGetFileIDsRequest message) {
        try {
            // Validate the message.
            validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());

            if(verifyFiles(message)) {
                respondSuccesfullIdentification(message);
            }
        } catch (IllegalArgumentException e) {
            // Only send this message if the alarm level is 'WARNING'.
            if(settings.getCollectionSettings().getPillarSettings().getAlarmLevel() == AlarmLevel.WARNING) {
                alarmDispatcher.alarmIllegalArgument(e);
            } else {
                log.warn("Caught illegal argument exception", e);
            }
        }
    }
    
    /**
     * Method for validating that all the requested files in the filelist are present. 
     * An empty filelist is expected when "AllFiles" or the parameter option is used.
     * If this is not the case, then a bad response message with the appropriate error is sent. 
     * @param message The message containing the list files.
     * @return Whether all the files are present or not.
     */
    public boolean verifyFiles(IdentifyPillarsForGetFileIDsRequest message) {
        FileIDs fileids = message.getFileIDs();

        // go through all the files and find any missing
        List<String> missingFiles = new ArrayList<String>();
        for(String fileID : fileids.getFileID()) {
            if(!archive.hasFile(fileID)) {
                missingFiles.add(fileID);
            }
        }
        
        // if not missing, then all files have been found!
        if(missingFiles.isEmpty()) {
            return true;
        }
        
        // report on the missing files
        respondMissingFileIdentification(message, missingFiles.size() + " missing files: '" + missingFiles + "'");
        return false;
    }
    
    /**
     * Method for making a successful response to the identification.
     * @param message The request message to respond to.
     */
    private void respondSuccesfullIdentification(IdentifyPillarsForGetFileIDsRequest message) {
        // Create the response.
        IdentifyPillarsForGetFileIDsResponse reply = createIdentifyPillarsForGetFileIDsResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, IdentifyResponseInfo
        reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                settings.getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        
        IdentifyResponseInfo irInfo = new IdentifyResponseInfo();
        irInfo.setIdentifyResponseCode(IdentifyResponseCodePositiveType.IDENTIFICATION_POSITIVE.value().toString());
        irInfo.setIdentifyResponseText("Operation acknowledged and accepted.");
        reply.setIdentifyResponseInfo(irInfo);
        
        // Send resulting file.
        messagebus.sendMessage(reply);
    }
    
    /**
     * Method for sending a bad response.
     * @param message The identification request to respond to.
     * @param cause The cause of the bad identification (e.g. which files are missing).
     */
    private void respondMissingFileIdentification(IdentifyPillarsForGetFileIDsRequest message, String cause) {
        // Create the response.
        IdentifyPillarsForGetFileIDsResponse reply = createIdentifyPillarsForGetFileIDsResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, IdentifyResponseInfo
        TimeMeasureTYPE timeToStartDeliver = new TimeMeasureTYPE();
        timeToStartDeliver.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        timeToStartDeliver.setTimeMeasureValue(BigInteger.valueOf(Long.MAX_VALUE));
        reply.setTimeToDeliver(timeToStartDeliver);
        
        IdentifyResponseInfo irInfo = new IdentifyResponseInfo();
        irInfo.setIdentifyResponseCode(ErrorcodeGeneralType.FILE_NOT_FOUND.value().toString());
        irInfo.setIdentifyResponseText(cause);
        reply.setIdentifyResponseInfo(irInfo);
        
        // Send resulting file.
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
        res.setAuditTrailInformation(msg.getAuditTrailInformation());
        res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        res.setBitRepositoryCollectionID(settings.getCollectionID());
        res.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        
        return res;
    }
}
