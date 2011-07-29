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
import java.math.BigInteger;
import java.net.URL;
import java.util.Collection;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumsDataForNewFile;
import org.bitrepository.bitrepositoryelements.ChecksumsDataForNewFile.ChecksumDataItems;
import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositoryelements.ProgressResponseInfo;
import org.bitrepository.bitrepositoryelements.TimeMeasureTYPE;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for handling the messages concerning the modification of the content of the archive.
 * TODO it currently only handles Put. Should also have Delete and Replace!
 */
public class ModifyMessageHandler {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());
    /** The mediator to handle all the information.*/
    private final PillarMediator mediator;
    
    /**
     * Constructor.
     * @param pm The mediator.
     */
    public ModifyMessageHandler(PillarMediator pm) {
        this.mediator = pm;
    }

    /**
     * Method for handling the IdentifyPillarsForPutFileRequest messages.
     * @param msg The IdentifyPillarsForPutFileRequest to be handled.
     * @throws Exception If anything bad happens.
     */
    public void handleMessage(IdentifyPillarsForPutFileRequest msg) throws Exception {
        // validate message
        validateBitrepositoryCollectionId(msg.getBitRepositoryCollectionID());
        
        log.info("Creating reply for '" + msg + "'");
        IdentifyPillarsForPutFileResponse reply = mediator.msgFactory.createIdentifyPillarsForPutFileResponse(msg);

        // Needs to filled in: AuditTrailInformation, PillarChecksumSpec, ReplyTo, TimeToDeliver
        reply.setReplyTo(mediator.settings.getLocalQueue());
        TimeMeasureTYPE timeToDeliver = new TimeMeasureTYPE();
        timeToDeliver.setTimeMeasureValue(BigInteger.valueOf(mediator.settings.getTimeToDownloadValue()));
        timeToDeliver.setTimeMeasureUnit(mediator.settings.getTimeToDownloadMeasure());
        reply.setTimeToDeliver(timeToDeliver);
        reply.setAuditTrailInformation(null);
        reply.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR
        
        log.info("Sending IdentifyPillarsForPutfileResponse: " + reply);
        mediator.messagebus.sendMessage(reply);
    }

    /**
     * Method for handling the PutFileRequest messages.
     * If the file already exists, then a FinalResponse telling that is sent, and an exception is thrown.
     * @param msg The PutFileRequest to be handled.
     * @throws Exception If the file already exists, or ??
     */
    public void handleMessage(PutFileRequest msg) throws Exception {
        // validate message
        validateBitrepositoryCollectionId(msg.getBitRepositoryCollectionID());
        validatePillarId(msg.getPillarID());
        
        // verify, that we already have the file
        if(mediator.archive.hasFile(msg.getFileID())) {
            // send final response telling, that the file already exists!
            PutFileFinalResponse fResponse = mediator.msgFactory.createPutFileFinalResponse(msg);
            FinalResponseInfo frInfo = new FinalResponseInfo();
            frInfo.setFinalResponseCode("409"); // HTTP for conflict.
            frInfo.setFinalResponseText("Conflict: The file is already within the archive.");
            fResponse.setFinalResponseInfo(frInfo);
            mediator.messagebus.sendMessage(fResponse);
            
            // Then tell the mediator, that we failed.
            throw new IllegalArgumentException("Cannot perform put for a file, '" + msg.getFileID() 
                    + "', which we already have within the archive");
        }
        
        log.info("Respond that we are starting to retrieve the file.");
        PutFileProgressResponse pResponse = mediator.msgFactory.createPutFileProgressResponse(msg);
        
        // Needs to fill in: AuditTrailInformation, PillarChecksumSpec, ProgressResponseInfo
        pResponse.setAuditTrailInformation(null);
        pResponse.setPillarChecksumSpec(null);
        ProgressResponseInfo prInfo = new ProgressResponseInfo();
        prInfo.setProgressResponseCode("202"); // HTTP for accepted.
        prInfo.setProgressResponseText("Started to receive date.");  
        pResponse.setProgressResponseInfo(prInfo);

        log.info("Sending ProgressResponseInfo: " + prInfo);
        mediator.messagebus.sendMessage(pResponse);
        
        log.debug("Retrieving the data to be stored from URL: '" + msg.getFileAddress() + "'");
        FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();
        mediator.archive.downloadFileForValidation(msg.getFileID(), fe.downloadFromServer(new URL(msg.getFileAddress())));
        
        // validating the file. If invalid, then delete it again!
        File retrievedFile = mediator.archive.getFile(msg.getFileID());
        try {
            Collection<ChecksumDataForFileTYPE> validationChecksums 
                = msg.getChecksumsDataForNewFile().getChecksumDataItems().getChecksumDataForFile();
            for(ChecksumDataForFileTYPE csType : validationChecksums) {
                String checksum = ChecksumUtils.generateChecksum(retrievedFile, 
                        csType.getChecksumSpec().getChecksumType(), csType.getChecksumSpec().getChecksumSalt());
                if(!checksum.equals(csType.getChecksumValue())) {
                    throw new IllegalStateException("Wrong checksum! Expected: [" + csType.getChecksumValue() 
                            + "], but calculated: [" + checksum + "]");
                }
            }
        } catch (Exception e) {
            log.error("The retrieved file did not validate! Removing it from archive.", e);
        }
        // TODO verify that this operation is successful.
        mediator.archive.moveToArchive(msg.getFileID());
        
        PutFileFinalResponse fResponse = mediator.msgFactory.createPutFileFinalResponse(msg);

        // insert: AuditTrailInformation, ChecksumsDataForNewFile, FinalResponseInfo, PillarChecksumSpec
        fResponse.setAuditTrailInformation(null);
        FinalResponseInfo frInfo = new FinalResponseInfo();
        frInfo.setFinalResponseCode("200"); // HTTP for OK!
        frInfo.setFinalResponseText("The put has be finished.");
        fResponse.setFinalResponseInfo(frInfo);
        fResponse.setPillarChecksumSpec(null); // NOT A CHECKSUM PILLAR
        
        ChecksumsDataForNewFile checksumForValidation = new ChecksumsDataForNewFile();
        {
            // Calculate the requested checksum data.
            checksumForValidation.setFileID(msg.getFileID());
            checksumForValidation.setNoOfItems(msg.getChecksumSpecs().getNoOfItems());
            checksumForValidation.setParameterSpecification("NOT FOUND IN EXAMPLES!");
            Collection<ChecksumSpecTYPE> requestedChecksumToCalculate 
                    = msg.getChecksumSpecs().getChecksumSpecsItems().getChecksumSpecsItem();
            ChecksumDataItems cdi = new ChecksumDataItems();
            // Calculate each of the requested checksums and put them into the response message.
            for(ChecksumSpecTYPE checksumToCalculate : requestedChecksumToCalculate) {
                String checksum = ChecksumUtils.generateChecksum(retrievedFile, 
                        checksumToCalculate.getChecksumType(),
                        checksumToCalculate.getChecksumSalt());
                ChecksumDataForFileTYPE resultingChecksum = new ChecksumDataForFileTYPE();
                resultingChecksum.setChecksumSpec(checksumToCalculate);
                resultingChecksum.setChecksumValue(checksum);
                resultingChecksum.setCalculationTimestamp(CalendarUtils.getXmlGregorianCalendar(new Date()));
                cdi.getChecksumDataForFile().add(resultingChecksum);
            }
            checksumForValidation.setChecksumDataItems(cdi);
        }
        fResponse.setChecksumsDataForNewFile(checksumForValidation);

        // Finish by sending final response.
        log.info("Sending PutFileFinalResponse: " + fResponse);
        mediator.messagebus.sendMessage(fResponse);
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
