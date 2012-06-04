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
package org.bitrepository.pillar.referencepillar;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.MockAlarmDispatcher;
import org.bitrepository.pillar.MockAuditManager;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.bitrepository.pillar.referencepillar.archive.ReferenceArchive;
import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
import org.bitrepository.protocol.ProtocolComponentFactory;
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
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class PutFileOnReferencePillarTest extends DefaultFixturePillarTest {
    PutFileMessageFactory msgFactory;
    
    ReferenceArchive archive;
    ReferencePillarMediator mediator;
    MockAlarmDispatcher alarmDispatcher;
    MockAuditManager audits;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseGetChecksumsTests() throws Exception {
        msgFactory = new PutFileMessageFactory(settings);
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
    public void pillarPutTestSuccessCase() throws Exception {
        addDescription("Tests the put functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        ChecksumSpecTYPE csSpecPillar = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
        Long FILE_SIZE = testfile.length();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        ChecksumSpecTYPE csMD5 = new ChecksumSpecTYPE();
        csMD5.setChecksumType(ChecksumType.MD5);
        ChecksumSpecTYPE csSHA1 = new ChecksumSpecTYPE();
        csSHA1.setChecksumType(ChecksumType.SHA1);
        String FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(testfile, csMD5);
        String FILE_CHECKSUM_SHA1 = ChecksumUtils.generateChecksum(testfile, csSHA1);
        ChecksumDataForFileTYPE checksumDataForFile = new ChecksumDataForFileTYPE();
        checksumDataForFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        checksumDataForFile.setChecksumSpec(csMD5);
        checksumDataForFile.setChecksumValue(Base16Utils.encodeBase16(FILE_CHECKSUM_MD5));
        Date startDate = new Date();
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(testfile), 
                new URL(FILE_ADDRESS));
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                auditTrail, FILE_ID, FILE_SIZE, FROM, clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        identifyRequest.getCorrelationID(),
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Create and send the actual Put message to the pillar.", 
                "Should be received and handled by the pillar.");
        
        PutFileRequest putRequest = msgFactory.createPutFileRequest(auditTrail, checksumDataForFile, 
                csSHA1, receivedIdentifyResponse.getCorrelationID(), FILE_ADDRESS, FILE_ID, FILE_SIZE, 
                FROM, pillarId, clientDestinationId, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(putRequest);
        
        addStep("Retrieve the ProgressResponse for the put request", "The put response should be sent by the pillar.");
        PutFileProgressResponse progressResponse = clientTopic.waitForMessage(PutFileProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createPutFileProgressResponse(
                        identifyRequest.getCorrelationID(), 
                        progressResponse.getFileAddress(), 
                        progressResponse.getFileID(), 
                        pillarId, 
                        progressResponse.getPillarChecksumSpec(), 
                        progressResponse.getReplyTo(), 
                        progressResponse.getResponseInfo(), 
                        progressResponse.getTo()));
        
        addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
        PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.
                OPERATION_COMPLETED);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createPutFileFinalResponse(
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
        Assert.assertEquals(receivedChecksumData.getChecksumSpec(), putRequest.getChecksumRequestForNewFile(), 
                "Should return the same type of checksum as requested.");
        Assert.assertEquals(new String(receivedChecksumData.getChecksumValue()), 
                FILE_CHECKSUM_SHA1);
        Assert.assertTrue(receivedChecksumData.getCalculationTimestamp().toGregorianCalendar().getTimeInMillis() > startDate.getTime(), 
                "The received timestamp should be after the start of this test '" + startDate + "', but was "
                        + receivedChecksumData.getCalculationTimestamp().toGregorianCalendar().getTime() + "'");
        
        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 0, "Should not have send any alarms.");
        Assert.assertEquals(audits.getCallsForAuditEvent(), 3, "Should deliver 3 audits: One for the operation, and "
                + "two for calculating checksums (one for validation and one for the request)");
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarPutTestFailedFileSizeIdentification() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject a put operation for too large files "
                + "in the identification process.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = null;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        ChecksumSpecTYPE csSpecPillar = null;
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                auditTrail, FILE_ID, Long.MAX_VALUE, FROM, clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        identifyRequest.getCorrelationID(),
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Validate that the identification has failed.", 
                "The response info should give 'FAILURE'");
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.FAILURE);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarPutTestFailedFileSizeOperation() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject a put operation for too large files "
                + "in the operation process. Also tests that neither file id or file size is required by the identify.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = null;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        Long FILE_SIZE = Long.MAX_VALUE;
        ChecksumSpecTYPE csSpecPillar = null;
        
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");

        ChecksumSpecTYPE csMD5 = new ChecksumSpecTYPE();
        csMD5.setChecksumType(ChecksumType.MD5);
        String FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(testfile, csMD5);

        ChecksumDataForFileTYPE checksumDataForFile = new ChecksumDataForFileTYPE();
        checksumDataForFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        checksumDataForFile.setChecksumSpec(csMD5);
        checksumDataForFile.setChecksumValue(Base16Utils.encodeBase16(FILE_CHECKSUM_MD5));
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                auditTrail, null, null, FROM, clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        identifyRequest.getCorrelationID(),
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Validate that the identification has succeded.","The response info should be positive.");
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Create and send the operation message to the pillar.", "Should be received and handled by the pillar.");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(auditTrail, checksumDataForFile, 
                null, UUID.randomUUID().toString(), FILE_ADDRESS, FILE_ID, FILE_SIZE, FROM, 
                pillarId, clientDestinationId, pillarDestinationId);
        messageBus.sendMessage(putRequest);
        
        addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
        PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createPutFileFinalResponse(
                        finalResponse.getChecksumDataForNewFile(),
                        finalResponse.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarPutTestFileAlreadyExistsCase() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject put requests on a file, which it already have.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String auditTrail = null;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        ChecksumSpecTYPE csSpecPillar = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
        Long FILE_SIZE = testfile.length();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(testfile, new File(dir, FILE_ID));
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                auditTrail, FILE_ID, FILE_SIZE, FROM, clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class, 10, TimeUnit.SECONDS);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        identifyRequest.getCorrelationID(),
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Validate that the identification has failed.", 
                "The response info should give 'FILE_FOUND'");
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.DUPLICATE_FILE_FAILURE);
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarPutTestBadChecksumCase() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject put requests, "
                + "when it gives a wrong checksum for validation.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String auditTrail = null;
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        ChecksumSpecTYPE csSpecPillar = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
        Long FILE_SIZE = testfile.length();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        ChecksumSpecTYPE csMD5 = new ChecksumSpecTYPE();
        csMD5.setChecksumType(ChecksumType.MD5);
        ChecksumSpecTYPE csSHA1 = new ChecksumSpecTYPE();
        csSHA1.setChecksumType(ChecksumType.SHA1);

        String CORRECT_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(testfile, csMD5);
        byte[] WRONG_FILE_CHECKSUM_MD5 = Base16Utils.encodeBase16("erroneous-checksum");
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(testfile), 
                new URL(FILE_ADDRESS));
        ChecksumDataForFileTYPE checksumDataForFile = new ChecksumDataForFileTYPE();
        checksumDataForFile.setCalculationTimestamp(CalendarUtils.getEpoch());
        checksumDataForFile.setChecksumSpec(csMD5);
        checksumDataForFile.setChecksumValue(WRONG_FILE_CHECKSUM_MD5);

        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                auditTrail, FILE_ID, FILE_SIZE, FROM, clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        identifyRequest.getCorrelationID(),
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Create and send the actual Put message to the pillar.", 
                "Should be received and handled by the pillar.");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(auditTrail, checksumDataForFile, 
                csSHA1, receivedIdentifyResponse.getCorrelationID(), FILE_ADDRESS, FILE_ID, FILE_SIZE, 
                FROM, pillarId, clientDestinationId, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(putRequest);
        
        addStep("Retrieve the ProgressResponse for the put request", "The put response should be sent by the pillar.");
        PutFileProgressResponse progressResponse = clientTopic.waitForMessage(PutFileProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createPutFileProgressResponse(
                        identifyRequest.getCorrelationID(), 
                        progressResponse.getFileAddress(), 
                        FILE_ID, 
                        progressResponse.getPillarID(), 
                        progressResponse.getPillarChecksumSpec(), 
                        progressResponse.getReplyTo(), 
                        progressResponse.getResponseInfo(), 
                        progressResponse.getTo()));
        
        addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
        PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createPutFileFinalResponse(
                        finalResponse.getChecksumDataForNewFile(),
                        identifyRequest.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
        
        addStep("Validate that the response contains the correct information.", 
                "Should deliver a OPERATION_FAILED with both the correct and the wrong checksum.");
        ResponseInfo ir = finalResponse.getResponseInfo();
        Assert.assertEquals(ir.getResponseCode(), ResponseCode.NEW_FILE_CHECKSUM_FAILURE);
        
        Assert.assertTrue(ir.getResponseText().contains(CORRECT_FILE_CHECKSUM_MD5), 
                "The response should contain the actual checksum '" + CORRECT_FILE_CHECKSUM_MD5 + "', but was: '"
                + ir.getResponseText());
        Assert.assertTrue(ir.getResponseText().contains(Base16Utils.decodeBase16(WRONG_FILE_CHECKSUM_MD5)), 
                "The response should contain the wrong checksum '" + Base16Utils.decodeBase16(WRONG_FILE_CHECKSUM_MD5) 
                + "', but was: '" + ir.getResponseText());
    }
    

    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarPutTestFileAlreadyExistsCaseDuringOperation() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject put requests on a file, which it already have.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        ChecksumSpecTYPE csSpecPillar = null;
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String auditTrail = null;
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
        Long FILE_SIZE = null;
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(testfile, new File(dir, FILE_ID));
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                auditTrail, null, FILE_SIZE, FROM, clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        identifyRequest.getCorrelationID(),
                        csSpecPillar,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        
        addStep("Validate that the identification has succeded.","The response info should be positive.");
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Create and send the operation message to the pillar.", "Should be received and handled by the pillar.");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(auditTrail, null, 
                null, UUID.randomUUID().toString(), FILE_ADDRESS, FILE_ID, FILE_SIZE, FROM, 
                pillarId, clientDestinationId, pillarDestinationId);
        messageBus.sendMessage(putRequest);
        
        addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
        PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.DUPLICATE_FILE_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createPutFileFinalResponse(
                        finalResponse.getChecksumDataForNewFile(),
                        finalResponse.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getTo()));
    }
}
