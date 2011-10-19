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
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ErrorcodeGeneralType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.IdentifyResponseCodePositiveType;
import org.bitrepository.bitrepositoryelements.IdentifyResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE.TimeMeasureUnit;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.common.settings.Settings;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.bitrepository.protocol.time.TimeMeasurementUtils;
import org.bitrepository.settings.collectionsettings.AlarmLevel;
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
        try {
            // Validate the message.
            validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());

            if(verifyChecksumFunction(message) && verifyFiles(message)) {
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
     * Method for validating that it is possible to instantiate the requested checksum algorithm.
     * If this is not the case, then a bad response message with the appropriate error is sent. 
     * Though this is not a mandatory field for the identification message, thus this method returns true, 
     * if the field or the algorithm is missing.
     * 
     * @param message The message with the checksum algorithm to validate.
     * @return Whether it the algorithm can be instantiated.  
     */
    public boolean verifyChecksumFunction(IdentifyPillarsForGetChecksumsRequest message) {
        ChecksumSpecTYPE checksumSpec = message.getFileChecksumSpec();
        
        // validate that this non-mandatory field has been filled out.
        if(checksumSpec == null || checksumSpec.getChecksumType() == null) {
            log.debug("No checksumSpec in the identification. Thus no reason to expect, that we cannot handle it.");
            return true;
        }
        
        try {
            MessageDigest.getInstance(checksumSpec.getChecksumType());
        } catch (NoSuchAlgorithmException e) {
            log.warn("Could not instantiate the given messagedigester for calculating a checksum.", e);
            respondNoSuchAlgorithm(message, "The algorithm '" + checksumSpec.getChecksumType() 
                    + "' cannot be found. Exception: " + e.getLocalizedMessage());
            return false;
        }

        return true;
    }
    
    /**
     * Method for validating that all the requested files in the filelist are present. 
     * An empty filelist is expected when "AllFiles" or the parameter option is used.
     * If this is not the case, then a bad response message with the appropriate error is sent. 
     * @param message The message containing the list files.
     * @return Whether all the files are present or not.
     */
    public boolean verifyFiles(IdentifyPillarsForGetChecksumsRequest message) {
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
    private void respondSuccesfullIdentification(IdentifyPillarsForGetChecksumsRequest message) {
        // Create the response.
        IdentifyPillarsForGetChecksumsResponse reply = createIdentifyPillarsForGetChecksumsResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, AuditTrailInformation, IdentifyResponseInfo
        reply.setTimeToDeliver(TimeMeasurementUtils.getTimeMeasurementFromMiliseconds(
                settings.getReferenceSettings().getPillarSettings().getTimeToStartDeliver()));
        reply.setAuditTrailInformation(null);
        
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
    private void respondMissingFileIdentification(IdentifyPillarsForGetChecksumsRequest message, String cause) {
        // Create the response.
        IdentifyPillarsForGetChecksumsResponse reply = createIdentifyPillarsForGetChecksumsResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, AuditTrailInformation, IdentifyResponseInfo
        TimeMeasureTYPE timeToStartDeliver = new TimeMeasureTYPE();
        timeToStartDeliver.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        timeToStartDeliver.setTimeMeasureValue(BigInteger.valueOf(Long.MAX_VALUE));
        reply.setTimeToDeliver(timeToStartDeliver);
        reply.setAuditTrailInformation(null);
        
        IdentifyResponseInfo irInfo = new IdentifyResponseInfo();
        irInfo.setIdentifyResponseCode(ErrorcodeGeneralType.FILE_NOT_FOUND.value().toString());
        irInfo.setIdentifyResponseText(cause);
        reply.setIdentifyResponseInfo(irInfo);
        
        // Send resulting file.
        messagebus.sendMessage(reply);
    }
    
    /**
     * Method for sending a bad response in the case of a unknown algorithm.
     * @param message The message to respond to.
     * @param cause The cause of the 'no such algorithm'.
     */
    private void respondNoSuchAlgorithm(IdentifyPillarsForGetChecksumsRequest message, String cause) {
        // Create the response.
        IdentifyPillarsForGetChecksumsResponse reply = createIdentifyPillarsForGetChecksumsResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, AuditTrailInformation, IdentifyResponseInfo
        TimeMeasureTYPE timeToStartDeliver = new TimeMeasureTYPE();
        timeToStartDeliver.setTimeMeasureUnit(TimeMeasureUnit.HOURS);
        timeToStartDeliver.setTimeMeasureValue(BigInteger.valueOf(Long.MAX_VALUE));
        reply.setTimeToDeliver(timeToStartDeliver);
        reply.setAuditTrailInformation(null);
        
        IdentifyResponseInfo irInfo = new IdentifyResponseInfo();
        // TODO make an errorcode for the 'NoSuchAlgorithm'?
        irInfo.setIdentifyResponseCode(ErrorcodeGeneralType.FAILURE.value().toString());
        irInfo.setIdentifyResponseText(cause);
        reply.setIdentifyResponseInfo(irInfo);
        
        // Send resulting file.
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
        res.setFileChecksumSpec(msg.getFileChecksumSpec());
        res.setPillarID(settings.getReferenceSettings().getPillarSettings().getPillarID());
        res.setBitRepositoryCollectionID(settings.getCollectionID());
        res.setReplyTo(settings.getReferenceSettings().getClientSettings().getReceiverDestination());
        
        return res;
    }
}
