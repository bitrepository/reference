/*
 * #%L
 * bitrepository-access-client
 * *
 * $Id: AccessMessageHandler.java 249 2011-08-02 11:05:51Z mss $
 * $HeadURL: https://sbforge.org/svn/bitrepository/trunk/bitrepository-integration/src/main/java/org/bitrepository/pillar/messagehandler/AccessMessageHandler.java $
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

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumsDataForBitRepositoryFile;
import org.bitrepository.bitrepositoryelements.ChecksumsDataForBitRepositoryFile.ChecksumDataItems;
import org.bitrepository.bitrepositoryelements.FinalResponseInfo;
import org.bitrepository.bitrepositoryelements.ProgressResponseInfo;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.protocol.FileExchange;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for performing the GetFile operation.
 * TODO handle error scenarios.
 */
public class GetFileMessageHandler extends PillarMessageHandler<GetFileRequest> {
    /** The log.*/
    private Logger log = LoggerFactory.getLogger(getClass());

    /** Constant for identifying this.*/
    private static boolean USE_CHECKSUM = false;
    private static String CHECKSUM_DIGESTER = "MD5";

    
    /**
     * Constructor.
     * @param mediator The mediator for this pillar.
     */
    public GetFileMessageHandler(PillarMediator mediator) {
        super(mediator);
    }
    
    /**
     * Performs the GetFile operation.
     * TODO perhaps synchronisation?
     * @param message The GetFileRequest message to handle.
     */
    @Override
    public void handleMessage(GetFileRequest message) {
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

        // TODO handle!!!!
        try {
            // Upload the file.
            log.info("Uploading file: " + requestedFile.getName() + " to " + message.getFileAddress());
            FileExchange fe = ProtocolComponentFactory.getInstance().getFileExchange();
            fe.uploadToServer(new FileInputStream(requestedFile), new URL(message.getFileAddress()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

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
}
