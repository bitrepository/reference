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
package org.bitrepository.pillar.referencepillar;

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
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.messagefactories.ReplaceFileMessageFactory;
import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.bitrepository.protocol.utils.Base16Utils;
import org.bitrepository.protocol.utils.ChecksumUtils;
import org.bitrepository.service.contributor.ContributorContext;
import org.bitrepository.settings.referencesettings.AlarmLevel;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;

/**
 * Tests the ReplaceFile functionality on the ReferencePillar.
 */
public class ReplaceFileOnReferencePillarTest extends DefaultFixturePillarTest {
    ReplaceFileMessageFactory msgFactory;
    
    ReferenceArchive archive;
    ReferencePillarMediator mediator;
    MockAlarmDispatcher alarmDispatcher;
    MockAuditManager audits;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseGetChecksumsTests() throws Exception {
        msgFactory = new ReplaceFileMessageFactory(settings);
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        settings.getReferenceSettings().getPillarSettings().setAlarmLevel(AlarmLevel.WARNING);
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        addStep("Initialize the pillar.", "Should not be a problem.");
        archive = new ReferenceArchive(settings.getReferenceSettings().getPillarSettings().getFileDir());
        audits = new MockAuditManager();
        ContributorContext contributorContext = new ContributorContext(messageBus, settings, 
                settings.getReferenceSettings().getPillarSettings().getPillarID(), 
                settings.getReferenceSettings().getPillarSettings().getReceiverDestination());
        alarmDispatcher = new MockAlarmDispatcher(contributorContext);
        PillarContext context = new PillarContext(settings, messageBus, alarmDispatcher, audits);
        mediator = new ReferencePillarMediator(context, archive);
        mediator.start();
    }
    
    @AfterMethod (alwaysRun=true) 
    public void closeArchive() {
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        if(mediator != null) {
            mediator.close();
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestSuccessCase() throws Exception {
        addDescription("Tests the replace functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        ChecksumSpecTYPE csSpecPillar = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        Long FILE_SIZE = replaceFile.length();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
        csSpecExisting.setChecksumType(ChecksumType.MD5);
        ChecksumSpecTYPE csSpecNew = new ChecksumSpecTYPE();
        csSpecNew.setChecksumType(ChecksumType.SHA1);
        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpecExisting);
        ChecksumDataForFileTYPE checksumDataForNewFile = new ChecksumDataForFileTYPE();
        checksumDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        checksumDataForNewFile.setChecksumSpec(csSpecExisting);
        checksumDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        Date startDate = new Date();
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(replaceFile), 
                new URL(FILE_ADDRESS));
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        File oldFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(oldFile.isFile(), "The test file does not exist at '" + oldFile.getAbsolutePath() + "'.");

        ChecksumDataForFileTYPE csDataDelete = new ChecksumDataForFileTYPE();
        csDataDelete.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataDelete.setChecksumSpec(csSpecExisting);
        csDataDelete.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(oldFile, csSpecExisting)));
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(oldFile, new File(dir, FILE_ID));
        
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
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Create and send the actual Replace message to the pillar.", 
                "Should be received and handled by the pillar.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(auditTrail, csDataDelete, 
                checksumDataForNewFile, csSpecNew, csSpecExisting, receivedIdentifyResponse.getCorrelationID(), 
                FILE_ADDRESS, FILE_ID, FILE_SIZE, FROM, pillarId, clientDestinationId, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the ProgressResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileProgressResponse progressResponse = clientTopic.waitForMessage(ReplaceFileProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createReplaceFileProgressResponse(
                        identifyRequest.getCorrelationID(), 
                        progressResponse.getFileAddress(), 
                        progressResponse.getFileID(),
                        csSpecPillar,
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
                        identifyRequest.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
        
        // validating the checksum
        Assert.assertEquals(finalResponse.getFileID(), FILE_ID, "The FileID of this test.");
        Assert.assertNotNull(finalResponse.getChecksumDataForNewFile(), "The results should contain a ");
        ChecksumDataForFileTYPE receivedChecksumData = finalResponse.getChecksumDataForNewFile();
        Assert.assertNotNull(receivedChecksumData.getChecksumSpec());
        Assert.assertEquals(receivedChecksumData.getChecksumSpec(), replaceRequest.getChecksumRequestForNewFile(), 
                "Should return the same type of checksum as requested.");
        Assert.assertEquals(Base16Utils.decodeBase16(receivedChecksumData.getChecksumValue()), 
                REPLACE_FILE_CHECKSUM_MD5);
        Assert.assertTrue(receivedChecksumData.getCalculationTimestamp().toGregorianCalendar().getTimeInMillis() > startDate.getTime(), 
                "The received timestamp should be after the start of this test '" + startDate + "', but was "
                        + receivedChecksumData.getCalculationTimestamp().toGregorianCalendar().getTime() + "'");
        
        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 0, "Should not have send any alarms.");
        Assert.assertEquals(audits.getCallsForAuditEvent(), 5, "Should deliver 5 audits: One for the operation, and "
                + "four for calculating checksums (one for each validation and one for the each request)");
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestSuccessCaseMinimumChecksums() throws Exception {
        addDescription("Tests the replace functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        ChecksumSpecTYPE csSpecPillar = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        Long FILE_SIZE = replaceFile.length();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
        csSpecExisting.setChecksumType(ChecksumType.MD5);
        ChecksumDataForFileTYPE checksumDataForNewFile = null;
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(replaceFile), 
                new URL(FILE_ADDRESS));
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        File oldFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(oldFile.isFile(), "The test file does not exist at '" + oldFile.getAbsolutePath() + "'.");

        ChecksumDataForFileTYPE csDataDelete = new ChecksumDataForFileTYPE();
        csDataDelete.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataDelete.setChecksumSpec(csSpecExisting);
        csDataDelete.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(oldFile, csSpecExisting)));
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(oldFile, new File(dir, FILE_ID));
        
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
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Create and send the actual Replace message to the pillar.", 
                "Should be received and handled by the pillar.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(auditTrail, csDataDelete, 
                checksumDataForNewFile, null, null, receivedIdentifyResponse.getCorrelationID(), 
                FILE_ADDRESS, FILE_ID, FILE_SIZE, FROM, pillarId, clientDestinationId, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the ProgressResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileProgressResponse progressResponse = clientTopic.waitForMessage(ReplaceFileProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createReplaceFileProgressResponse(
                        identifyRequest.getCorrelationID(), 
                        progressResponse.getFileAddress(), 
                        progressResponse.getFileID(),
                        csSpecPillar,
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
                        identifyRequest.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
        
        // validating the checksum
        Assert.assertEquals(finalResponse.getFileID(), FILE_ID, "The FileID of this test.");
        Assert.assertNull(finalResponse.getChecksumDataForNewFile(), "The results should contain a ");
        
        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 0, "Should not have send any alarms.");
        Assert.assertEquals(audits.getCallsForAuditEvent(), 2, "Should deliver 2 audits: One for the operation, and "
                + "one for the calculating checksums (only one required checksum calculation)");
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestMissingFile() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject replace requests when there is no file to replace.");
        addDescription("Tests the replace functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        ChecksumSpecTYPE csSpecPillar = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        Long FILE_SIZE = 0L;
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
        csSpecExisting.setChecksumType(ChecksumType.MD5);
        ChecksumSpecTYPE csSpecNew = new ChecksumSpecTYPE();
        csSpecNew.setChecksumType(ChecksumType.SHA1);
        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpecExisting);
        ChecksumDataForFileTYPE checksumDataForNewFile = new ChecksumDataForFileTYPE();
        checksumDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        checksumDataForNewFile.setChecksumSpec(csSpecExisting);
        checksumDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(replaceFile), 
                new URL(FILE_ADDRESS));
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        File oldFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(oldFile.isFile(), "The test file does not exist at '" + oldFile.getAbsolutePath() + "'.");

        ChecksumDataForFileTYPE csDataDelete = new ChecksumDataForFileTYPE();
        csDataDelete.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataDelete.setChecksumSpec(csSpecExisting);
        csDataDelete.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(oldFile, csSpecExisting)));
        
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
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestMissingFileInOperation() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject replace requests when there is no file to replace.");
        addDescription("Tests the replace functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        Long FILE_SIZE = 0L;
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
        csSpecExisting.setChecksumType(ChecksumType.MD5);
        ChecksumSpecTYPE csSpecNew = new ChecksumSpecTYPE();
        csSpecNew.setChecksumType(ChecksumType.SHA1);
        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpecExisting);
        ChecksumDataForFileTYPE checksumDataForNewFile = new ChecksumDataForFileTYPE();
        checksumDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        checksumDataForNewFile.setChecksumSpec(csSpecExisting);
        checksumDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(replaceFile), 
                new URL(FILE_ADDRESS));
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        File oldFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(oldFile.isFile(), "The test file does not exist at '" + oldFile.getAbsolutePath() + "'.");

        ChecksumDataForFileTYPE csDataDelete = new ChecksumDataForFileTYPE();
        csDataDelete.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataDelete.setChecksumSpec(csSpecExisting);
        csDataDelete.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(oldFile, csSpecExisting)));

        addStep("Create and send the actual Replace message to the pillar.", 
                "Should be received and handled by the pillar.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(auditTrail, csDataDelete, 
                checksumDataForNewFile, null, null, msgFactory.getNewCorrelationID(), 
                FILE_ADDRESS, FILE_ID, FILE_SIZE, FROM, pillarId, clientDestinationId, pillarDestinationId);
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        replaceRequest.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestFailedFileSize() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject replace requests for too large files.");
        addDescription("Tests the replace functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        ChecksumSpecTYPE csSpecPillar = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        Long FILE_SIZE = Long.MAX_VALUE;
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
        csSpecExisting.setChecksumType(ChecksumType.MD5);
        ChecksumSpecTYPE csSpecNew = new ChecksumSpecTYPE();
        csSpecNew.setChecksumType(ChecksumType.SHA1);
        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpecExisting);
        ChecksumDataForFileTYPE checksumDataForNewFile = new ChecksumDataForFileTYPE();
        checksumDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        checksumDataForNewFile.setChecksumSpec(csSpecExisting);
        checksumDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(replaceFile), 
                new URL(FILE_ADDRESS));
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        File oldFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(oldFile.isFile(), "The test file does not exist at '" + oldFile.getAbsolutePath() + "'.");

        ChecksumDataForFileTYPE csDataDelete = new ChecksumDataForFileTYPE();
        csDataDelete.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataDelete.setChecksumSpec(csSpecExisting);
        csDataDelete.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(oldFile, csSpecExisting)));
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(oldFile, new File(dir, FILE_ID));
        
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
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.FAILURE);
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestFailedFileSizeInOperation() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject replace requests for too large files.");
        addDescription("Tests the replace functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        Long FILE_SIZE = Long.MAX_VALUE;
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
        csSpecExisting.setChecksumType(ChecksumType.MD5);
        ChecksumSpecTYPE csSpecNew = new ChecksumSpecTYPE();
        csSpecNew.setChecksumType(ChecksumType.SHA1);
        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpecExisting);
        ChecksumDataForFileTYPE checksumDataForNewFile = new ChecksumDataForFileTYPE();
        checksumDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        checksumDataForNewFile.setChecksumSpec(csSpecExisting);
        checksumDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(replaceFile), 
                new URL(FILE_ADDRESS));
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        File oldFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(oldFile.isFile(), "The test file does not exist at '" + oldFile.getAbsolutePath() + "'.");

        ChecksumDataForFileTYPE csDataDelete = new ChecksumDataForFileTYPE();
        csDataDelete.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataDelete.setChecksumSpec(csSpecExisting);
        csDataDelete.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(oldFile, csSpecExisting)));
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(oldFile, new File(dir, FILE_ID));
        
        addStep("Create and send the actual Replace message to the pillar.", 
                "Should be received and handled by the pillar.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(auditTrail, csDataDelete, 
                checksumDataForNewFile, null, null, msgFactory.getNewCorrelationID(), 
                FILE_ADDRESS, FILE_ID, FILE_SIZE, FROM, pillarId, clientDestinationId, pillarDestinationId);
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        replaceRequest.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestNewChecksumCase() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject replace requests, "
                + "when it gives a wrong checksum for the new file.");
        addDescription("Tests the replace functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        ChecksumSpecTYPE csSpecPillar = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        Long FILE_SIZE = replaceFile.length();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
        csSpecExisting.setChecksumType(ChecksumType.MD5);
        ChecksumSpecTYPE csSpecNew = new ChecksumSpecTYPE();
        csSpecNew.setChecksumType(ChecksumType.SHA1);
        String REPLACE_FILE_CHECKSUM_MD5 = "Bad-checksum";
        ChecksumDataForFileTYPE checksumDataForNewFile = new ChecksumDataForFileTYPE();
        checksumDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        checksumDataForNewFile.setChecksumSpec(csSpecExisting);
        checksumDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(replaceFile), 
                new URL(FILE_ADDRESS));
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        File oldFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(oldFile.isFile(), "The test file does not exist at '" + oldFile.getAbsolutePath() + "'.");

        ChecksumDataForFileTYPE csDataDelete = new ChecksumDataForFileTYPE();
        csDataDelete.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataDelete.setChecksumSpec(csSpecExisting);
        csDataDelete.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(oldFile, csSpecExisting)));
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(oldFile, new File(dir, FILE_ID));
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForReplaceFileRequest identifyRequest = msgFactory.createIdentifyPillarsForReplaceFileRequest(
                auditTrail, FILE_ID, null, FROM, clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", "The pillar should make a response.");
        IdentifyPillarsForReplaceFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForReplaceFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForReplaceFileResponse(
                        identifyRequest.getCorrelationID(),
                        FILE_ID,
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Create and send the actual Replace message to the pillar.", 
                "Should be received and handled by the pillar.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(auditTrail, csDataDelete, 
                checksumDataForNewFile, csSpecNew, csSpecExisting, receivedIdentifyResponse.getCorrelationID(), 
                FILE_ADDRESS, FILE_ID, FILE_SIZE, FROM, pillarId, clientDestinationId, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        identifyRequest.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
    }
    
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarReplaceTestExistingChecksumCase() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject replace requests, "
                + "when it gives a wrong checksum for the old file to be replaced.");
        addDescription("Tests the replace functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        ChecksumSpecTYPE csSpecPillar = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File replaceFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(replaceFile.isFile(), "The test file does not exist at '" + replaceFile.getAbsolutePath() + "'.");
        Long FILE_SIZE = replaceFile.length();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
        csSpecExisting.setChecksumType(ChecksumType.MD5);
        ChecksumSpecTYPE csSpecNew = new ChecksumSpecTYPE();
        csSpecNew.setChecksumType(ChecksumType.SHA1);
        String REPLACE_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(replaceFile, csSpecExisting);
        ChecksumDataForFileTYPE checksumDataForNewFile = new ChecksumDataForFileTYPE();
        checksumDataForNewFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        checksumDataForNewFile.setChecksumSpec(csSpecExisting);
        checksumDataForNewFile.setChecksumValue(Base16Utils.encodeBase16(REPLACE_FILE_CHECKSUM_MD5));
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(replaceFile), 
                new URL(FILE_ADDRESS));
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        File oldFile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(oldFile.isFile(), "The test file does not exist at '" + oldFile.getAbsolutePath() + "'.");

        ChecksumDataForFileTYPE csDataDelete = new ChecksumDataForFileTYPE();
        csDataDelete.setCalculationTimestamp(CalendarUtils.getEpoch());
        csDataDelete.setChecksumSpec(csSpecExisting);
        csDataDelete.setChecksumValue(Base16Utils.encodeBase16("bad-checksum"));
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(oldFile, new File(dir, FILE_ID));
        
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
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Create and send the actual Replace message to the pillar.", 
                "Should be received and handled by the pillar.");
        ReplaceFileRequest replaceRequest = msgFactory.createReplaceFileRequest(auditTrail, csDataDelete, 
                checksumDataForNewFile, csSpecNew, csSpecExisting, receivedIdentifyResponse.getCorrelationID(), 
                FILE_ADDRESS, FILE_ID, FILE_SIZE, FROM, pillarId, clientDestinationId, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(replaceRequest);
        
        addStep("Retrieve the FinalResponse for the replace request", "The replace response should be sent by the pillar.");
        ReplaceFileFinalResponse finalResponse = clientTopic.waitForMessage(ReplaceFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createReplaceFileFinalResponse(
                        finalResponse.getChecksumDataForExistingFile(),
                        finalResponse.getChecksumDataForNewFile(),
                        identifyRequest.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
    }
}
