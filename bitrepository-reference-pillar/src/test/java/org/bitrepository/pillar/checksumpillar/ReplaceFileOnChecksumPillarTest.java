/*
 * #%L
 * Bitrepository Reference Pillar
 * 
 * $Id: ReplaceFileOnReferencePillarTest.java 589 2011-12-01 15:34:42Z jolf $
 * $HeadURL: https://sbforge.org/svn/bitrepository/bitrepository-reference/trunk/bitrepository-reference-pillar/src/test/java/org/bitrepository/pillar/ReplaceFileOnReferencePillarTest.java $
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

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileFinalResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileProgressResponse;
import org.bitrepository.bitrepositorymessages.ReplaceFileRequest;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.MockAlarmDispatcher;
import org.bitrepository.pillar.MockAuditManager;
import org.bitrepository.pillar.checksumpillar.messagehandler.ChecksumPillarMediator;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.messagefactories.ReplaceFileMessageFactory;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.utils.Base16Utils;
import org.bitrepository.protocol.utils.ChecksumUtils;
import org.bitrepository.service.contributor.ContributorContext;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the ReplaceFile functionality on the ReferencePillar.
 */
public class ReplaceFileOnChecksumPillarTest extends DefaultFixturePillarTest {
    ReplaceFileMessageFactory msgFactory;
    
    MemoryCache cache;
    ChecksumPillarMediator mediator;
    MockAlarmDispatcher alarmDispatcher;
    MockAuditManager audits;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseDeleteFileTests() throws Exception {
        msgFactory = new ReplaceFileMessageFactory(settings);
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        addStep("Initialize the pillar.", "Should not be a problem.");
        cache = new MemoryCache();
        audits = new MockAuditManager();
        ContributorContext contributorContext = new ContributorContext(messageBus, settings, 
                settings.getReferenceSettings().getPillarSettings().getPillarID(), 
                settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        alarmDispatcher = new MockAlarmDispatcher(contributorContext);
        PillarContext context = new PillarContext(settings, messageBus, alarmDispatcher, audits);
        mediator = new ChecksumPillarMediator(context, cache);
        mediator.start();
    }
    
    @AfterMethod (alwaysRun=true) 
    public void closeArchive() {
        if(cache != null) {
            cache.cleanUp();
        }
        if(mediator != null) {
            mediator.close();
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestSuccessCase() throws Exception {
        addDescription("Tests the replace functionality of the reference pillar for the successful scenario.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        long FILE_SIZE = 1L;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        
        ChecksumDataForFileTYPE csDataExistingFile = new ChecksumDataForFileTYPE();
        csDataExistingFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataExistingFile.setChecksumSpec(csSpec);
        csDataExistingFile.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));

        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpec);
        
        ChecksumDataForFileTYPE csDataForNewFile = new ChecksumDataForFileTYPE();
        csDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataForNewFile.setChecksumSpec(csSpec);
        csDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(replaceFile), 
                new URL(FILE_ADDRESS));
        
        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForReplaceFileRequest identifyRequest = msgFactory.createIdentifyPillarsForReplaceFileRequest(
                auditTrail, FILE_ID, FILE_SIZE, FROM, clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", "The pillar should make a response.");
        IdentifyPillarsForReplaceFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForReplaceFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForReplaceFileResponse(
                        identifyRequest.getCorrelationID(),
                        FILE_ID,
                        csSpec,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Create and send the actual Replace message to the pillar.", 
                "Should be received and handled by the pillar.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                auditTrail, 
                csDataExistingFile, 
                csDataForNewFile, 
                csSpec, 
                csSpec, 
                receivedIdentifyResponse.getCorrelationID(), 
                FILE_ADDRESS, 
                FILE_ID, 
                FILE_SIZE, 
                FROM, 
                pillarId, 
                clientDestinationId, 
                receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the ProgressResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileProgressResponse progressResponse = clientTopic.waitForMessage(ReplaceFileProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createReplaceFileProgressResponse(
                        identifyRequest.getCorrelationID(), 
                        progressResponse.getFileAddress(), 
                        progressResponse.getFileID(),
                        csSpec,
                        pillarId, 
                        progressResponse.getReplyTo(), 
                        progressResponse.getResponseInfo(), 
                        progressResponse.getTo()));
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.
                OPERATION_COMPLETED);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        replaceRequest.getCorrelationID(), 
                        FILE_ADDRESS, 
                        FILE_ID, 
                        csSpec, 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
        
        // validating the checksum
        Assert.assertEquals(finalResponse.getFileID(), FILE_ID, "The FileID of this test.");
        Assert.assertNotNull(finalResponse.getChecksumDataForNewFile(), "The results should contain a ");
        ChecksumDataForFileTYPE responseChecksumDataForOldFile = finalResponse.getChecksumDataForExistingFile();
        Assert.assertNotNull(responseChecksumDataForOldFile.getChecksumSpec());
        Assert.assertEquals(responseChecksumDataForOldFile.getChecksumSpec(), replaceRequest.getChecksumRequestForNewFile(), 
                "Should return the same type of checksum as requested.");
        Assert.assertEquals(Base16Utils.decodeBase16(responseChecksumDataForOldFile.getChecksumValue()), 
                CHECKSUM);

        ChecksumDataForFileTYPE receivedChecksumData = finalResponse.getChecksumDataForNewFile();
        Assert.assertNotNull(receivedChecksumData.getChecksumSpec());
        Assert.assertEquals(receivedChecksumData.getChecksumSpec(), replaceRequest.getChecksumRequestForNewFile(), 
                "Should return the same type of checksum as requested.");
        Assert.assertEquals(Base16Utils.decodeBase16(receivedChecksumData.getChecksumValue()), 
                REPLACE_FILE_CHECKSUM_MD5);

        addStep("Check the cached checksum.", "Should have been replaced by the new checksum.");
        Assert.assertEquals(cache.getChecksum(FILE_ID), REPLACE_FILE_CHECKSUM_MD5);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestMissingFileInIdentification() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject replace requests when there is no file to replace.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        long FILE_SIZE = 1L;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        
        ChecksumDataForFileTYPE csDataExistingFile = new ChecksumDataForFileTYPE();
        csDataExistingFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataExistingFile.setChecksumSpec(csSpec);
        csDataExistingFile.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));

        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpec);
        
        ChecksumDataForFileTYPE csDataForNewFile = new ChecksumDataForFileTYPE();
        csDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataForNewFile.setChecksumSpec(csSpec);
        csDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForReplaceFileRequest identifyRequest = msgFactory.createIdentifyPillarsForReplaceFileRequest(
                auditTrail, FILE_ID, FILE_SIZE, FROM, clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", "The pillar should make a response.");
        IdentifyPillarsForReplaceFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForReplaceFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForReplaceFileResponse(
                        identifyRequest.getCorrelationID(),
                        FILE_ID,
                        csSpec,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        
        addStep("Check the content of the cache", "Should not have any checksum for the file.");
        Assert.assertNull(cache.getChecksum(FILE_ID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestMissingFileInOperation() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject replace requests when there is no file to "
                + "replace during the operation.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        long FILE_SIZE = 1L;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        
        ChecksumDataForFileTYPE csDataExistingFile = new ChecksumDataForFileTYPE();
        csDataExistingFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataExistingFile.setChecksumSpec(csSpec);
        csDataExistingFile.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));

        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpec);
        
        ChecksumDataForFileTYPE csDataForNewFile = new ChecksumDataForFileTYPE();
        csDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataForNewFile.setChecksumSpec(csSpec);
        csDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        
        addStep("Create the replace operation file request and send it to the checksum pillar.", 
                "The checksum pillar should handle the request and send an 'FILE_NOT_FOUND' response.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                auditTrail, 
                csDataExistingFile, 
                csDataForNewFile, 
                csSpec, 
                csSpec, 
                msgFactory.getNewCorrelationID(), 
                FILE_ADDRESS, 
                FILE_ID, 
                FILE_SIZE, 
                FROM, 
                pillarId, 
                clientDestinationId, 
                pillarDestinationId);
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        replaceRequest.getCorrelationID(), 
                        FILE_ADDRESS, 
                        FILE_ID, 
                        csSpec, 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
        
        addStep("Check the content of the cache", "Should not have any checksum for the file.");
        Assert.assertNull(cache.getChecksum(FILE_ID));
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestNewChecksumCase() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject replace requests, "
                + "when it gives a wrong checksum for the new file.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String BAD_CHECKSUM = "cccc43211234cccc";
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        long FILE_SIZE = 1L;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        
        ChecksumDataForFileTYPE csDataExistingFile = new ChecksumDataForFileTYPE();
        csDataExistingFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataExistingFile.setChecksumSpec(csSpec);
        csDataExistingFile.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));

        ChecksumDataForFileTYPE csDataForNewFile = new ChecksumDataForFileTYPE();
        csDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataForNewFile.setChecksumSpec(csSpec);
        csDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(BAD_CHECKSUM));
        
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(replaceFile), 
                new URL(FILE_ADDRESS));
        
        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, CHECKSUM);
                
        addStep("Create the replace operation file request and send it to the checksum pillar.", 
                "The checksum pillar should handle the request and send an 'FILE_NOT_FOUND' response.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                auditTrail, 
                csDataExistingFile, 
                csDataForNewFile, 
                csSpec, 
                csSpec, 
                msgFactory.getNewCorrelationID(), 
                FILE_ADDRESS, 
                FILE_ID, 
                FILE_SIZE, 
                FROM, 
                pillarId, 
                clientDestinationId, 
                pillarDestinationId);
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        replaceRequest.getCorrelationID(), 
                        FILE_ADDRESS, 
                        FILE_ID, 
                        csSpec, 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
        
        addStep("Check the content of the cache", "Should still have the original checksum for the file.");
        Assert.assertEquals(cache.getChecksum(FILE_ID), CHECKSUM);
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestExistingChecksumCase() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject replace requests, "
                + "when it gives a wrong checksum for the old file to be replaced.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String BAD_CHECKSUM = "cccc43211234cccc";
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        long FILE_SIZE = 1L;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        
        ChecksumDataForFileTYPE csDataExistingFile = new ChecksumDataForFileTYPE();
        csDataExistingFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataExistingFile.setChecksumSpec(csSpec);
        csDataExistingFile.setChecksumValue(Base16Utils.encodeBase16(BAD_CHECKSUM));
        
        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpec);

        ChecksumDataForFileTYPE csDataForNewFile = new ChecksumDataForFileTYPE();
        csDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataForNewFile.setChecksumSpec(csSpec);
        csDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(replaceFile), 
                new URL(FILE_ADDRESS));
        
        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, CHECKSUM);
                
        addStep("Create the replace operation file request and send it to the checksum pillar.", 
                "The checksum pillar should handle the request and send an 'FILE_NOT_FOUND' response.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                auditTrail, 
                csDataExistingFile, 
                csDataForNewFile, 
                csSpec, 
                csSpec, 
                msgFactory.getNewCorrelationID(), 
                FILE_ADDRESS, 
                FILE_ID, 
                FILE_SIZE, 
                FROM, 
                pillarId, 
                clientDestinationId, 
                pillarDestinationId);
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        replaceRequest.getCorrelationID(), 
                        FILE_ADDRESS, 
                        FILE_ID, 
                        csSpec, 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
        
        addStep("Check the content of the cache", "Should still have the original checksum for the file.");
        Assert.assertEquals(cache.getChecksum(FILE_ID), CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestBadNewFileChecksumSpec() throws Exception {
        addDescription("Tests that the ChecksumPillar is able to reject replace requests when it contains a different"
                + " checksum specification than the one for the checksum pillar.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        long FILE_SIZE = 1L;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        
        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        ChecksumSpecTYPE badCsSpec = new ChecksumSpecTYPE();
        badCsSpec.setChecksumSalt(null);
        badCsSpec.setChecksumType(ChecksumType.OTHER);
        badCsSpec.setOtherChecksumType("INVALID CHECKSUM SPEC");
        
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        
        ChecksumDataForFileTYPE csDataExistingFile = new ChecksumDataForFileTYPE();
        csDataExistingFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataExistingFile.setChecksumSpec(badCsSpec);
        csDataExistingFile.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));
        
        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpec);
        
        ChecksumDataForFileTYPE csDataForNewFile = new ChecksumDataForFileTYPE();
        csDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataForNewFile.setChecksumSpec(csSpec);
        csDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        
        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        addStep("Create the replace operation file request and send it to the checksum pillar.", 
                "The checksum pillar should handle the request and send an 'FILE_NOT_FOUND' response.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                auditTrail, 
                csDataExistingFile, 
                csDataForNewFile, 
                csSpec, 
                csSpec, 
                msgFactory.getNewCorrelationID(), 
                FILE_ADDRESS, 
                FILE_ID, 
                FILE_SIZE, 
                FROM, 
                pillarId, 
                clientDestinationId, 
                pillarDestinationId);
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        replaceRequest.getCorrelationID(), 
                        FILE_ADDRESS, 
                        FILE_ID, 
                        csSpec, 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
        addStep("Check the content of the cache", "Should still have the original checksum for the file.");
        Assert.assertEquals(cache.getChecksum(FILE_ID), CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestBadNewFileChecksumSpecRequest() throws Exception {
        addDescription("Tests that the ChecksumPillar is able to reject replace requests when it contains a different"
                + " checksum specification than the one for the checksum pillar.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        long FILE_SIZE = 1L;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        
        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        ChecksumSpecTYPE badCsSpec = new ChecksumSpecTYPE();
        badCsSpec.setChecksumSalt(null);
        badCsSpec.setChecksumType(ChecksumType.OTHER);
        badCsSpec.setOtherChecksumType("INVALID CHECKSUM SPEC");
        
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        
        ChecksumDataForFileTYPE csDataExistingFile = new ChecksumDataForFileTYPE();
        csDataExistingFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataExistingFile.setChecksumSpec(csSpec);
        csDataExistingFile.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));
        
        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpec);
        
        ChecksumDataForFileTYPE csDataForNewFile = new ChecksumDataForFileTYPE();
        csDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataForNewFile.setChecksumSpec(csSpec);
        csDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        
        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        addStep("Create the replace operation file request and send it to the checksum pillar.", 
                "The checksum pillar should handle the request and send an 'FILE_NOT_FOUND' response.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                auditTrail, 
                csDataExistingFile, 
                csDataForNewFile, 
                csSpec, 
                badCsSpec, 
                msgFactory.getNewCorrelationID(), 
                FILE_ADDRESS, 
                FILE_ID, 
                FILE_SIZE, 
                FROM, 
                pillarId, 
                clientDestinationId, 
                pillarDestinationId);
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        replaceRequest.getCorrelationID(), 
                        FILE_ADDRESS, 
                        FILE_ID, 
                        csSpec, 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));

        addStep("Check the content of the cache", "Should still have the original checksum for the file.");
        Assert.assertEquals(cache.getChecksum(FILE_ID), CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestBadExistingFileChecksumSpec() throws Exception {
        addDescription("Tests that the ChecksumPillar is able to reject replace requests when it contains a different"
                + " checksum specification than the one for the checksum pillar.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        long FILE_SIZE = 1L;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        
        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        ChecksumSpecTYPE badCsSpec = new ChecksumSpecTYPE();
        badCsSpec.setChecksumSalt(null);
        badCsSpec.setChecksumType(ChecksumType.OTHER);
        badCsSpec.setOtherChecksumType("INVALID CHECKSUM SPEC");
        
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        
        ChecksumDataForFileTYPE csDataExistingFile = new ChecksumDataForFileTYPE();
        csDataExistingFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataExistingFile.setChecksumSpec(csSpec);
        csDataExistingFile.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));
        
        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpec);
        
        ChecksumDataForFileTYPE csDataForNewFile = new ChecksumDataForFileTYPE();
        csDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataForNewFile.setChecksumSpec(badCsSpec);
        csDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        
        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        addStep("Create the replace operation file request and send it to the checksum pillar.", 
                "The checksum pillar should handle the request and send an 'FILE_NOT_FOUND' response.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                auditTrail, 
                csDataExistingFile, 
                csDataForNewFile, 
                csSpec, 
                csSpec, 
                msgFactory.getNewCorrelationID(), 
                FILE_ADDRESS, 
                FILE_ID, 
                FILE_SIZE, 
                FROM, 
                pillarId, 
                clientDestinationId, 
                pillarDestinationId);
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        replaceRequest.getCorrelationID(), 
                        FILE_ADDRESS, 
                        FILE_ID, 
                        csSpec, 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
        
        addStep("Check the content of the cache", "Should still have the original checksum for the file.");
        Assert.assertEquals(cache.getChecksum(FILE_ID), CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestBadExistingFileChecksumSpecRequest() throws Exception {
        addDescription("Tests that the ChecksumPillar is able to reject replace requests when it contains a different"
                + " checksum specification than the one for the checksum pillar.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        long FILE_SIZE = 1L;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        
        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        ChecksumSpecTYPE badCsSpec = new ChecksumSpecTYPE();
        badCsSpec.setChecksumSalt(null);
        badCsSpec.setChecksumType(ChecksumType.OTHER);
        badCsSpec.setOtherChecksumType("INVALID CHECKSUM SPEC");
        
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        
        ChecksumDataForFileTYPE csDataExistingFile = new ChecksumDataForFileTYPE();
        csDataExistingFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataExistingFile.setChecksumSpec(csSpec);
        csDataExistingFile.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));
        
        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpec);
        
        ChecksumDataForFileTYPE csDataForNewFile = new ChecksumDataForFileTYPE();
        csDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataForNewFile.setChecksumSpec(csSpec);
        csDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        
        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        addStep("Create the replace operation file request and send it to the checksum pillar.", 
                "The checksum pillar should handle the request and send an 'FILE_NOT_FOUND' response.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                auditTrail, 
                csDataExistingFile, 
                csDataForNewFile, 
                badCsSpec, 
                csSpec, 
                msgFactory.getNewCorrelationID(), 
                FILE_ADDRESS, 
                FILE_ID, 
                FILE_SIZE, 
                FROM, 
                pillarId, 
                clientDestinationId, 
                pillarDestinationId);
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        replaceRequest.getCorrelationID(), 
                        FILE_ADDRESS, 
                        FILE_ID, 
                        csSpec, 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
        
        addStep("Check the content of the cache", "Should still have the original checksum for the file.");
        Assert.assertEquals(cache.getChecksum(FILE_ID), CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestSuccessCaseWithFewChecksums() throws Exception {
        addDescription("Tests the replace functionality of the reference pillar for the successful scenario, where as "
                + "few checksums are requested as possible.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        long FILE_SIZE = 1L;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        
        ChecksumDataForFileTYPE csDataExistingFile = new ChecksumDataForFileTYPE();
        csDataExistingFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataExistingFile.setChecksumSpec(csSpec);
        csDataExistingFile.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));

        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForReplaceFileRequest identifyRequest = msgFactory.createIdentifyPillarsForReplaceFileRequest(
                auditTrail, FILE_ID, FILE_SIZE, FROM, clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", "The pillar should make a response.");
        IdentifyPillarsForReplaceFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForReplaceFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForReplaceFileResponse(
                        identifyRequest.getCorrelationID(),
                        FILE_ID,
                        csSpec,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Create and send the actual Replace message to the pillar.", 
                "Should be received and handled by the pillar.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(
                auditTrail, 
                csDataExistingFile, 
                null, 
                null, 
                null, 
                receivedIdentifyResponse.getCorrelationID(), 
                FILE_ADDRESS, 
                FILE_ID, 
                FILE_SIZE, 
                FROM, 
                pillarId, 
                clientDestinationId, 
                receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the ProgressResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileProgressResponse progressResponse = clientTopic.waitForMessage(ReplaceFileProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createReplaceFileProgressResponse(
                        identifyRequest.getCorrelationID(), 
                        progressResponse.getFileAddress(), 
                        progressResponse.getFileID(),
                        csSpec,
                        pillarId, 
                        progressResponse.getReplyTo(), 
                        progressResponse.getResponseInfo(), 
                        progressResponse.getTo()));
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.
                OPERATION_COMPLETED);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        replaceRequest.getCorrelationID(), 
                        FILE_ADDRESS, 
                        FILE_ID, 
                        csSpec, 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
        
        // validating the checksum
        Assert.assertEquals(finalResponse.getFileID(), FILE_ID, "The FileID of this test.");
        Assert.assertNull(finalResponse.getChecksumDataForExistingFile());
        Assert.assertNull(finalResponse.getChecksumDataForNewFile());
    }
}
