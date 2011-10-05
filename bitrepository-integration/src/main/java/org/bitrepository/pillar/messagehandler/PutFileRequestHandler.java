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
import java.net.URL;
import java.util.Collection;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumsDataForNewFile;
import org.bitrepository.bitrepositoryelements.ErrorcodeFinalresponseType;
import org.bitrepository.bitrepositoryelements.ErrorcodeGeneralType;
import org.bitrepository.bitrepositoryelements.FinalResponseCodePositiveType;
import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositoryelements.ProgressResponseCodeType;
import org.bitrepository.bitrepositoryelements.ProgressResponseInfo;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.collection.settings.standardsettings.Settings;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.pillar.ReferenceArchive;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.messagebus.MessageBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for performing the PutFile operation.
 * TODO handle error scenarios.
 */
public class PutFileRequestHandler extends PillarMessageHandler<PutFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Constructor.
     * @param settings The settings for handling the message.
     * @param messageBus The bus for communication.
     * @param alarmDispatcher The dispatcher of alarms.
     * @param referenceArchive The archive for the data.
     */
    public PutFileRequestHandler(Settings settings, MessageBus messageBus,
            AlarmDispatcher alarmDispatcher, ReferenceArchive referenceArchive) {
        super(settings, messageBus, alarmDispatcher, referenceArchive);
    }

    /**
     * Handles the identification messages for the PutFile operation.
     * TODO perhaps synchronisation for all these methods?
     * @param message The IdentifyPillarsForPutFileRequest message to handle.
     */
    public void handleMessage(PutFileRequest message) {
        try {
            if(!validateMessage(message)) {
                return;
            }
            
            tellAboutProgress(message);
            retrieveFile(message);
            sendFinalResponse(message);
        } catch (IllegalArgumentException e) {
            log.warn("Caught IllegalArgumentException. Possible intruder -> Sending alarm! ", e);
            alarmDispatcher.alarmIllegalArgument(e);
        } catch (RuntimeException e) {
            log.warn("Internal RunTimeException caught. Sending response for 'error at my end'.", e);
            FinalResponseInfo fri = new FinalResponseInfo();
            fri.setFinalResponseCode(ErrorcodeFinalresponseType.OPERATION_FAILED.value().toString());
            fri.setFinalResponseText("Error: " + e.getMessage());
            sendFailedResponse(message, fri);
        }
    }
    
    /**
     * Validates the message.
     * @param message The message to validate.
     */
    private boolean validateMessage(PutFileRequest message) {
        // validate message
        validateBitrepositoryCollectionId(message.getBitRepositoryCollectionID());
        validatePillarId(message.getPillarID());

        // verify, that we already have the file
        if(archive.hasFile(message.getFileID())) {
            log.warn("Cannot perform put for a file, '" + message.getFileID() 
                    + "', which we already have within the archive");
            // Then tell the mediator, that we failed.
            FinalResponseInfo fri = new FinalResponseInfo();
            fri.setFinalResponseCode(ErrorcodeGeneralType.DUPLICATE_FILE.value().toString());
            fri.setFinalResponseText("File is already within archive.");
            sendFailedResponse(message, fri);

            return false;
        }
        return true;
    }
    
    /**
     * Method for sending a progress response.
     * @param message The message to base the response upon.
     */
    private void tellAboutProgress(PutFileRequest message) {
        log.info("Respond that we are starting to retrieve the file.");
        PutFileProgressResponse pResponse = createPutFileProgressResponse(message);

        // Needs to fill in: AuditTrailInformation, PillarChecksumSpec, ProgressResponseInfo
        pResponse.setAuditTrailInformation(null);
        pResponse.setPillarChecksumSpec(null);
        ProgressResponseInfo prInfo = new ProgressResponseInfo();
        prInfo.setProgressResponseCode(ProgressResponseCodeType.REQUEST_ACCEPTED);
        prInfo.setProgressResponseText("Started to receive date.");  
        pResponse.setProgressResponseInfo(prInfo);

        log.info("Sending ProgressResponseInfo: " + prInfo);
        messagebus.sendMessage(pResponse);
    }
    
    /**
     * Retrieves the actual data, validates it and stores it.
     * @param message The request to for the file to put.
     */
    private void retrieveFile(PutFileRequest message) {
        log.debug("Retrieving the data to be stored from URL: '" + message.getFileAddress() + "'");
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();

        File fileForValidation;
        try {
            fileForValidation = archive.downloadFileForValidation(message.getFileID(), 
                    fe.downloadFromServer(new URL(message.getFileAddress())));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
//        if(message.getChecksumsDataForNewFile() != null 
//                && message.getChecksumsDataForNewFile().getChecksumDataItems() != null
//                && message.getChecksumsDataForNewFile().getChecksumDataItems().getChecksumDataForFile() != null) {
//            Collection<ChecksumDataForFileTYPE> validationChecksums 
//                    = message.getChecksumsDataForNewFile().getChecksumDataItems().getChecksumDataForFile();
//            try {
//                for(ChecksumDataForFileTYPE csType : validationChecksums) {
//                    String checksum = ChecksumUtils.generateChecksum(fileForValidation, 
//                            csType.getChecksumSpec().getChecksumType(), 
//                            csType.getChecksumSpec().getChecksumSalt());
//                    if(!checksum.equals(csType.getChecksumValue())) {
//                        log.error("Expected checksums '" + csType.getChecksumValue() + "' but the checksum was '" 
//                                + checksum + "'.");
//                        throw new IllegalStateException("Wrong checksum! Expected: [" + csType.getChecksumValue() 
//                                + "], but calculated: [" + checksum + "]");
//                    }
//                }
//            } catch (Exception e) {
//                log.error("The retrieved file did not validate! Removing it from archive.", e);
//                throw new RuntimeException(e);
//            }
//        } else {
//            // TODO is such a checksum required?
//            log.warn("No checksums for validating the retrieved file.");
//        }

        try {
            // TODO verify that this operation is successful.
            archive.moveToArchive(message.getFileID());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Method for sending the final response for the requested put operation.
     * @param message The message requesting the put operation.
     */
    private void sendFinalResponse(PutFileRequest message) {

        File retrievedFile = archive.getFile(message.getFileID());

        PutFileFinalResponse fResponse = createPutFileFinalResponse(message);

        // insert: AuditTrailInformation, ChecksumsDataForNewFile, FinalResponseInfo, PillarChecksumSpec
        fResponse.setAuditTrailInformation(null);
        FinalResponseInfo frInfo = new FinalResponseInfo();
        frInfo.setFinalResponseCode(FinalResponseCodePositiveType.SUCCESS.value().toString());
        frInfo.setFinalResponseText("The put has be finished.");
        fResponse.setFinalResponseInfo(frInfo);
        fResponse.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR

        ChecksumsDataForNewFile checksumForValidation = new ChecksumsDataForNewFile();
//        if(message.getChecksumSpecs() != null && message.getChecksumSpecs().getNoOfItems().equals(BigInteger.ZERO)) {
//            // Calculate the requested checksum data.
//            checksumForValidation.setFileID(message.getFileID());
//            checksumForValidation.setNoOfItems(message.getChecksumSpecs().getNoOfItems());
//            Collection<ChecksumSpecTYPE> requestedChecksumToCalculate 
//                    = message.getChecksumSpecs().getChecksumSpecsItems().getChecksumSpecsItem();
//            ChecksumDataItems cdi = new ChecksumDataItems();
//            // Calculate each of the requested checksums and put them into the response message.
//            for(ChecksumSpecTYPE checksumToCalculate : requestedChecksumToCalculate) {
//                String checksum = ChecksumUtils.generateChecksum(retrievedFile, 
//                        checksumToCalculate.getChecksumType(),
//                        checksumToCalculate.getChecksumSalt());
//                ChecksumDataForFileTYPE resultingChecksum = new ChecksumDataForFileTYPE();
//                resultingChecksum.setChecksumSpec(checksumToCalculate);
//                resultingChecksum.setChecksumValue(checksum);
//                resultingChecksum.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
//                cdi.getChecksumDataForFile().add(resultingChecksum);
//            }
//            checksumForValidation.setChecksumDataItems(cdi);
//        } else {
//            // TODO is such a request required?
//            log.info("No checksum validation requested.");
//        }
        fResponse.setChecksumsDataForNewFile(checksumForValidation);

        // Finish by sending final response.
        log.info("Sending PutFileFinalResponse: " + fResponse);
        messagebus.sendMessage(fResponse);
    }
    
    /**
     * Method for sending a response telling, that the operation has failed.
     * @param message The message requesting the put operation.
     * @param frInfo The information about why it has failed.
     */
    private void sendFailedResponse(PutFileRequest message, FinalResponseInfo frInfo) {
        // send final response telling, that the file already exists!
        PutFileFinalResponse fResponse = createPutFileFinalResponse(message);
        fResponse.setFinalResponseInfo(frInfo);
        messagebus.sendMessage(fResponse);
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
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(msg.getCorrelationID());
        res.setFileAddress(msg.getFileAddress());
        res.setFileID(msg.getFileID());
        res.setTo(msg.getReplyTo());
        res.setPillarID(settings.getPillar().getPillarID());
        res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
        res.setReplyTo(settings.getProtocol().getLocalDestination());
        
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
    private PutFileFinalResponse createPutFileFinalResponse(PutFileRequest msg) {
        PutFileFinalResponse res = new PutFileFinalResponse();
        res.setMinVersion(MIN_VERSION);
        res.setVersion(VERSION);
        res.setCorrelationID(msg.getCorrelationID());
        res.setFileAddress(msg.getFileAddress());
        res.setFileID(msg.getFileID());
        res.setTo(msg.getReplyTo());
        res.setPillarID(settings.getPillar().getPillarID());
        res.setBitRepositoryCollectionID(settings.getBitRepositoryCollectionID());
        res.setReplyTo(settings.getProtocol().getLocalDestination());

        return res;
    }
}
