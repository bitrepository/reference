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

import java.io.File;
import java.io.FileInputStream;
import java.math.BigInteger;
import java.net.URL;
import java.security.MessageDigest;
import java.util.Date;

import org.apache.commons.lang.NotImplementedException;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumsDataForBitRepositoryFile;
import org.bitrepository.bitrepositoryelements.ChecksumsDataForBitRepositoryFile.ChecksumDataItems;
import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositoryelements.ProgressResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the messages regarding access (e.g. Get operations).
 */
public class AccessMessageHandler {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The mediator to handle all the information.*/
    private final PillarMediator mediator;
    /**
     * TODO use a setting, or something else.
     */
    private static Boolean USE_CHECKSUM = false;
    private static String CHECKSUM_DIGESTER = "MD5";
    
    /**
     * Constructor.
     * @param pm The mediator.
     */
    public AccessMessageHandler(PillarMediator pm) {
        this.mediator = pm;
    }
    
    /**
     * Method for handling IdentifyPillarsForGetChecksumsRequest messages.
     * @param message The IdentifyPillarsForGetChecksumsRequest message to be handled.
     */
    public void handleMessage(IdentifyPillarsForGetChecksumsRequest message) {
        // Validate the message.
        validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());
        
//        IdentifyPillarsForGetChecksumsResponse reply = mediator.msgFactory.
        
        // TODO implement this!
        throw new NotImplementedException("TODO implement this!");
    }

    /**
     * Method for handling GetChecksumsRequest messages.
     * @param message The GetChecksumsRequest message to be handled.
     */
    public void handleMessage(GetChecksumsRequest message) {
        // Validate the message.
        validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());
        validatePillarId(message.getPillarID());
        
        // TODO implement this!
        throw new NotImplementedException("TODO implement this!");
    }
    
    /**
     * Method for handling IdentifyPillarsForGetFileIDsRequest messages.
     * @param message The IdentifyPillarsForGetFileIDsRequest message to be handled.
     */
    public void handleMessage(IdentifyPillarsForGetFileIDsRequest message) {
        // Validate the message.
        validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());

        // TODO implement this!
        throw new NotImplementedException("TODO implement this!");
    }
    
    /**
     * Method for handling GetFileIDsRequest messages.
     * @param message The GetFileIDsRequest message to be handled.
     */
    public void handleMessage(GetFileIDsRequest message) {
        // Validate the message.
        validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());
        validatePillarId(message.getPillarID());

        // TODO implement this!
        throw new NotImplementedException("TODO implement this!");
    }

    /**
     * Method for handling IdentifyPillarsForGetFileRequest messages.
     * @param message The IdentifyPillarsForGetFileRequest message to be handled.
     */
    public void handleMessage(IdentifyPillarsForGetFileRequest message) {
        // Validate the message.
        validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());
        
        // Validate, that we have the requested file.
        File requestedFile = mediator.archive.getFile(message.getFileID());
        if(!requestedFile.isFile()) {
            // HANDLE?
            throw new IllegalStateException("The file '" + message.getFileID() + "' has been requested, but we do "
                    + "not have that file!");
        }

        // Create the response.
        IdentifyPillarsForGetFileResponse reply = mediator.msgFactory.createIdentifyPillarsForGetFileResponse(message);
        
        // set the missing variables in the reply:
        // TimeToDeliver, AuditTrailInformation
        TimeMeasureTYPE timeToDeliver = new TimeMeasureTYPE();
        timeToDeliver.setTimeMeasureUnit(mediator.settings.getTimeToUploadMeasure());
        timeToDeliver.setTimeMeasureValue(BigInteger.valueOf(mediator.settings.getTimeToUploadValue()));
        reply.setTimeToDeliver(timeToDeliver);
        reply.setAuditTrailInformation(null);
        
        // Send resulting file.
        mediator.messagebus.sendMessage(reply);
    }

    /**
     * Method for handling GetFileRequest messages.
     * @param message The GetFileRequest message to be handled.
     */
    public void handleMessage(GetFileRequest message) throws Exception {
        // Validate the message.
        validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());
        validatePillarId(message.getPillarID());

        // Validate, that we have the requested file.
        File requestedFile = mediator.archive.getFile(message.getFileID());
        if(!requestedFile.isFile()) {
            // HANDLE?
            throw new IllegalStateException("The file '" + message.getFileID() + "' has been requested, but we do "
                    + "not have that file!");
        }
        
        // make ProgressResponse to tell that we are handling this.
        GetFileProgressResponse pResponse = mediator.msgFactory.createGetFileProgressResponse(message);
        
        // set missing variables in the message:
        // AuditTrailInformation, ChecksumsDataForBitRepositoryFile, FileSize, ProgressResponseInfo
        pResponse.setFileSize(BigInteger.valueOf(requestedFile.length()));
        ProgressResponseInfo prInfo = new ProgressResponseInfo();
        prInfo.setProgressResponseCode("202"); // HTTP for accepted!
        prInfo.setProgressResponseText("Started to retrieve data.");
        pResponse.setProgressResponseInfo(prInfo);
        pResponse.setAuditTrailInformation(null);
        if(USE_CHECKSUM) {
            log.debug("generating checksum data for '" + requestedFile.getName() + "'.");
            ChecksumsDataForBitRepositoryFile checksumCollection = new ChecksumsDataForBitRepositoryFile();
            ChecksumDataItems checksumData = new ChecksumDataItems();
            // Retrieve the checksum using default digester without any salt.
            checksumData.getChecksumDataForFile().add(calculateChecksum(requestedFile, null, CHECKSUM_DIGESTER));
            checksumCollection.setChecksumDataItems(checksumData);
            checksumCollection.setFileID(message.getFileID());
            checksumCollection.setNoOfItems(BigInteger.valueOf(1L));
            pResponse.setChecksumsDataForBitRepositoryFile(checksumCollection);
        }

        // Send the ProgressResponse
        log.info("Sending GetFileProgressResponse: " + pResponse);
        mediator.messagebus.sendMessage(pResponse);

        // Upload the file.
        log.info("Uploading file: " + requestedFile.getName() + " to " + message.getFileAddress());
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();
        fe.uploadToServer(new FileInputStream(requestedFile), new URL(message.getFileAddress()));

        // make ProgressResponse to tell that we are handling this.
        GetFileFinalResponse fResponse = mediator.msgFactory.createGetFileFinalResponse(message);
        // set missing variables in the message: AuditTrailInformation, FinalResponseInfo
        fResponse.setAuditTrailInformation(null);
        FinalResponseInfo frInfo = new FinalResponseInfo();
        frInfo.setFinalResponseCode("200"); // HTTP for OK!
        frInfo.setFinalResponseText("Data delivered.");
        fResponse.setFinalResponseInfo(frInfo);

        // send the FinalResponse.
        log.info("Sending GetFileFinalResponse: " + fResponse);
        mediator.messagebus.sendMessage(fResponse);
    }
    
    /**
     * Method for calculating the checksum for a given file. The checksum is calculated with 
     * @param file The file to calculate the checksum for.
     * @param salt The salt of for the checksum.
     * @param digester The name of the digester for calculating the checksum (e.g. MD5 or SHA1).
     * @return The requested ChecksumDataForFileTYPE, or null if any bad stuff happens.
     */
    private ChecksumDataForFileTYPE calculateChecksum(File file, String salt, String digester) {
        try {
            ChecksumDataForFileTYPE checksumType = new ChecksumDataForFileTYPE();
            ChecksumSpecTYPE csType = new ChecksumSpecTYPE();
            csType.setChecksumSalt(salt);
            csType.setChecksumType(digester);
            checksumType.setChecksumSpec(csType);
            checksumType.setChecksumValue(ChecksumUtils.generateChecksum(file, 
                    MessageDigest.getInstance(digester), salt));
            checksumType.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));

            return checksumType;
        } catch (Exception e) {
            log.warn("Could not calculate the checksum of the requested file.", e);
            return null;
        }
    }

    /**
     * Validates that it is the correct BitrepositoryCollectionId.
     * @param bitrepositoryCollectionId The collection id to validate.
     */
    private void validateBitrepositoryCollectionId(String bitrepositoryCollectionId) {
        if(!bitrepositoryCollectionId.equals(mediator.settings.getBitRepositoryCollectionID())) {
            throw new IllegalArgumentException("The message had a wrong BitRepositoryIdCollection: "
                    + "Expected '" + mediator.settings.getBitRepositoryCollectionID() + "' but was '" 
                    + bitrepositoryCollectionId + "'.");
        }
    }

    /**
     * Validates that it is the correct pillar id.
     * @param pillarId The pillar id.
     */
    private void validatePillarId(String pillarId) {
        if(!pillarId.equals(mediator.settings.getPillarId())) {
            throw new IllegalArgumentException("The message had a wrong PillarId: "
                    + "Expected '" + mediator.settings.getPillarId() + "' but was '" 
                    + pillarId + "'.");
        }
    }
}
