/*
 * #%L
 * Bitrepository Reference Pillar
 * 
 * $Id: PutFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/PutFileOnReferencePillarTest.java $
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
package org.bitrepository.pillar.checksumpillar;

import java.util.Date;
import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.bitrepository.settings.referencesettings.ChecksumPillarFileDownload;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class PutFileOnChecksumPillarTest extends ChecksumPillarTest {
    PutFileMessageFactory msgFactory;
    Long FILE_SIZE = 1L;

    public void initializeCUT() {
        super.initializeCUT();
        msgFactory = new PutFileMessageFactory(settingsForTestClient, getPillarID(), pillarDestinationId);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestSuccessCase() throws Exception {
        addDescription("Tests the put functionality of the checksum pillar for the successful scenario.");
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                DEFAULT_FILE_ID, FILE_SIZE);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response getPillarID() the pillar.", "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertNotNull(receivedIdentifyResponse);
        Assert.assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        Assert.assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getReplyTo(), pillarDestinationId);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Create and send the actual Put message to the pillar.", 
                "Should be received and handled by the pillar.");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(TestFileHelper.getDefaultFileChecksum(),
                TestFileHelper.getDefaultFileChecksum().getChecksumSpec(), DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE);
        putRequest.setCorrelationID(identifyRequest.getCorrelationID());
        messageBus.sendMessage(putRequest);
        
        addStep("Retrieve the ProgressResponse for the put request", "The put response should be sent by the pillar.");
        PutFileProgressResponse progressResponse = clientReceiver.waitForMessage(PutFileProgressResponse.class);
        Assert.assertNotNull(progressResponse);
        Assert.assertEquals(progressResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        Assert.assertEquals(progressResponse.getFileID(), DEFAULT_FILE_ID);
        Assert.assertEquals(progressResponse.getFrom(), getPillarID());
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertEquals(progressResponse.getReplyTo(), pillarDestinationId);
        Assert.assertEquals(progressResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_ACCEPTED_PROGRESS);
        
        addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertNotNull(finalResponse);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getCorrelationID(), putRequest.getCorrelationID());
        Assert.assertEquals(finalResponse.getFileID(), DEFAULT_FILE_ID);
        Assert.assertEquals(finalResponse.getFrom(), getPillarID());
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
        Assert.assertEquals(finalResponse.getReplyTo(), pillarDestinationId);
        
        // validating the checksum
        Assert.assertEquals(finalResponse.getFileID(), DEFAULT_FILE_ID, "The FileID of this test.");
        Assert.assertNotNull(finalResponse.getChecksumDataForNewFile(), "The results should contain a ");
        ChecksumDataForFileTYPE receivedChecksumData = finalResponse.getChecksumDataForNewFile();
        Assert.assertNotNull(receivedChecksumData.getChecksumSpec());
        Assert.assertEquals(receivedChecksumData.getChecksumSpec(), putRequest.getChecksumRequestForNewFile(), 
                "Should return the same type of checksum as requested.");
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestFailedDuplicateFileDuringIdentify() throws Exception {
        addDescription("Tests that the checksum pillar rejects putting a file, which already exists. During the identification fase");
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        initializeCacheWithMD5ChecksummedFile();
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                DEFAULT_FILE_ID, FILE_SIZE);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response getPillarID() the pillar.", "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertNotNull(receivedIdentifyResponse);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.DUPLICATE_FILE_FAILURE);
        Assert.assertNotNull(receivedIdentifyResponse.getChecksumDataForExistingFile());
        
        addStep("Validate the content of the cache", "Should contain the checksum of the file");
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID, collectionID), DEFAULT_MD5_CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestFailedDuplicateFileDuringOperation() throws Exception {
        addDescription("Tests that the checksum pillar rejects putting a file, which already exists. During the operation fase");
        initializeCacheWithMD5ChecksummedFile();
        addStep("Create and send the actual Put message to the pillar.", 
                "Should be received and handled by the pillar.");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(TestFileHelper.getDefaultFileChecksum(),
                csSpec, DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE);
        messageBus.sendMessage(putRequest);
        
        addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertNotNull(finalResponse);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.
                DUPLICATE_FILE_FAILURE);
        
        addStep("Validate the content of the cache", "Should contain the checksum of the file");
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID, collectionID), DEFAULT_MD5_CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestMissingChecksum() throws Exception {
        addDescription("Tests that the checksum pillar rejects putting a file, which already exists. During the operation fase");
        context.getSettings().getRepositorySettings().getProtocolSettings().setRequireChecksumForNewFileRequests(true);
        PutFileRequest putRequest = msgFactory.createPutFileRequest(null, 
                csSpec, DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE);
        messageBus.sendMessage(putRequest);
        
        addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertNotNull(finalResponse);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
        
        addStep("Validate the content of the cache", "Should contain the checksum of the file");
        Assert.assertFalse(cache.hasFile(DEFAULT_FILE_ID, collectionID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestBadChecksumSpecGiven() throws Exception {
        addDescription("Tests the file will not be put if a bad checksum is given.");
        Assert.assertTrue(context.getSettings().getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
        
        ChecksumSpecTYPE badCsType = new ChecksumSpecTYPE();
        badCsType.setChecksumType(ChecksumType.OTHER);
        badCsType.setOtherChecksumType("NOT-EXISTING-TYPE");
        ChecksumDataForFileTYPE badData = new ChecksumDataForFileTYPE();
        badData.setCalculationTimestamp(CalendarUtils.getEpoch());
        badData.setChecksumSpec(badCsType);
        badData.setChecksumValue(Base16Utils.encodeBase16("baabbbaaabba"));

        messageBus.sendMessage(msgFactory.createPutFileRequest(badData, 
                csSpec, DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.REQUEST_NOT_SUPPORTED);
        Assert.assertFalse(cache.hasFile(DEFAULT_FILE_ID, collectionID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestBadChecksumSpecRequested() throws Exception {
        addDescription("Tests whether the file will not be put if a bad checksum is given.");
        Assert.assertTrue(context.getSettings().getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
        
        ChecksumSpecTYPE badCsType = new ChecksumSpecTYPE();
        badCsType.setChecksumType(ChecksumType.OTHER);
        badCsType.setOtherChecksumType("NOT-EXISTING-TYPE");

        messageBus.sendMessage(msgFactory.createPutFileRequest(TestFileHelper.getDefaultFileChecksum(),
                badCsType, DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.REQUEST_NOT_SUPPORTED);
        Assert.assertFalse(cache.hasFile(DEFAULT_FILE_ID, collectionID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestNoChecksumAllowed() throws Exception {
        addDescription("Tests that it is possible to put without any checksums if the collection settings allows it.");
        context.getSettings().getRepositorySettings().getProtocolSettings().setRequireChecksumForDestructiveRequests(false);
        Assert.assertFalse(context.getSettings().getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());

        messageBus.sendMessage(msgFactory.createPutFileRequest(null, 
                null, DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_COMPLETED);
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID, collectionID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestBadURL() throws Exception {
        addDescription("Tests that the pillar handles a bad URL correct.");
        context.getSettings().getRepositorySettings().getProtocolSettings().setRequireChecksumForDestructiveRequests(false);
        Assert.assertFalse(context.getSettings().getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());

        String badUrl = DEFAULT_DOWNLOAD_FILE_ADDRESS + "-ERROR-" + new Date().getTime();
        messageBus.sendMessage(msgFactory.createPutFileRequest(null, 
                null, badUrl, DEFAULT_FILE_ID, FILE_SIZE));
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_TRANSFER_FAILURE);
        Assert.assertFalse(cache.hasFile(DEFAULT_FILE_ID, collectionID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestNoFileIdInIdentification() throws Exception {
        addDescription("Tests that it is possible to identify without the fileid or the filesize.");

        messageBus.sendMessage(msgFactory.createIdentifyPillarsForPutFileRequest(null, null));
        IdentifyPillarsForPutFileResponse identifyResponse = clientReceiver.waitForMessage(IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(identifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestBadChecksumArgumentWhenNotDownload() throws Exception {
        addDescription("Tests that the file will be put with a checksum different from the checksum of the file at "
                + "the URL, when the ChecksumPillar is set to only use the checksum from the message.");
        context.getSettings().getReferenceSettings().getPillarSettings().setChecksumPillarFileDownload(ChecksumPillarFileDownload.DOWNLOAD_WHEN_MISSING_FROM_MESSAGE);
        Assert.assertEquals(context.getSettings().getReferenceSettings().getPillarSettings().getChecksumPillarFileDownload(), ChecksumPillarFileDownload.DOWNLOAD_WHEN_MISSING_FROM_MESSAGE);
        Assert.assertTrue(context.getSettings().getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
        
        String badChecksum = "baabbbaaabba";
        ChecksumDataForFileTYPE badData = new ChecksumDataForFileTYPE();
        badData.setCalculationTimestamp(CalendarUtils.getEpoch());
        badData.setChecksumSpec(csSpec);
        badData.setChecksumValue(Base16Utils.encodeBase16(badChecksum));

        addStep("Send the message with a checksum differing from the one for the file at the address.", 
                "The incorrect checksum is stored.");
        messageBus.sendMessage(msgFactory.createPutFileRequest(badData, 
                csSpec, DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_COMPLETED);
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID, collectionID));
        Assert.assertEquals(cache.getChecksum(DEFAULT_FILE_ID, collectionID), badChecksum);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestBadChecksumArgumentCaughtWhenForcedDownload() throws Exception {
        addDescription("Tests that the ChecksumPillar, when forced to download the file, will catch a incorrect "
                + "checksum given as validation in the PutFileRequest message.");
        context.getSettings().getReferenceSettings().getPillarSettings().setChecksumPillarFileDownload(ChecksumPillarFileDownload.ALWAYS_DOWNLOAD);
        Assert.assertEquals(context.getSettings().getReferenceSettings().getPillarSettings().getChecksumPillarFileDownload(), ChecksumPillarFileDownload.ALWAYS_DOWNLOAD);
        Assert.assertTrue(context.getSettings().getRepositorySettings().getProtocolSettings().isRequireChecksumForDestructiveRequests());
        
        ChecksumDataForFileTYPE badData = new ChecksumDataForFileTYPE();
        badData.setCalculationTimestamp(CalendarUtils.getEpoch());
        badData.setChecksumSpec(csSpec);
        badData.setChecksumValue(Base16Utils.encodeBase16("baabbbaaabba"));

        addStep("Send the message with a checksum differing from the one for the file at the address.", 
                "Failure reported from the pillar and the incorrect checksum is not stored.");
        messageBus.sendMessage(msgFactory.createPutFileRequest(badData,
                csSpec, DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
        Assert.assertFalse(cache.hasFile(DEFAULT_FILE_ID, collectionID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestNoDownloadAndNoChecksum() throws Exception {
        addDescription("Test the scenario when the ChecksumPillar is forced to only use the checksum in the PutFileRequest "
                + "(It is set to not allowing downloading the file), and no checksum is given in the PutFileRequest.");
        context.getSettings().getReferenceSettings().getPillarSettings().setChecksumPillarFileDownload(ChecksumPillarFileDownload.NEVER_DOWNLOAD);
        Assert.assertEquals(context.getSettings().getReferenceSettings().getPillarSettings().getChecksumPillarFileDownload(), ChecksumPillarFileDownload.NEVER_DOWNLOAD);

        addStep("Send message without checksum.", 
                "Failure from the pillar.");
        messageBus.sendMessage(msgFactory.createPutFileRequest(null, 
                null, DEFAULT_DOWNLOAD_FILE_ADDRESS, DEFAULT_FILE_ID, FILE_SIZE));
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
        Assert.assertFalse(cache.hasFile(DEFAULT_FILE_ID, collectionID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarPutFileTestBadURLIgnored() throws Exception {
        addDescription("Tests that the pillar ignores a bad URL when the checksum is given and the ChecksumPillar"
                + " is not forced to download the file.");
        context.getSettings().getReferenceSettings().getPillarSettings().setChecksumPillarFileDownload(ChecksumPillarFileDownload.DOWNLOAD_WHEN_MISSING_FROM_MESSAGE);
        Assert.assertEquals(context.getSettings().getReferenceSettings().getPillarSettings().getChecksumPillarFileDownload(), ChecksumPillarFileDownload.DOWNLOAD_WHEN_MISSING_FROM_MESSAGE);

        String badUrl = DEFAULT_DOWNLOAD_FILE_ADDRESS + "-ERROR-" + new Date().getTime();
        messageBus.sendMessage(msgFactory.createPutFileRequest(TestFileHelper.getDefaultFileChecksum(), 
                null, badUrl, DEFAULT_FILE_ID, FILE_SIZE));
        PutFileFinalResponse finalResponse = clientReceiver.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_COMPLETED);
        Assert.assertTrue(cache.hasFile(DEFAULT_FILE_ID, collectionID));
    }
}
