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

import java.io.File;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.DeleteFileFinalResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileProgressResponse;
import org.bitrepository.bitrepositorymessages.DeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.common.utils.CalendarUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.MockAlarmDispatcher;
import org.bitrepository.pillar.MockAuditManager;
import org.bitrepository.pillar.messagefactories.DeleteFileMessageFactory;
import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
import org.bitrepository.protocol.utils.Base16Utils;
import org.bitrepository.protocol.utils.ChecksumUtils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class DeleteFileOnReferencePillarTest extends DefaultFixturePillarTest {
    DeleteFileMessageFactory msgFactory;
    
    ReferenceArchive archive;
    ReferencePillarMediator mediator;
    MockAlarmDispatcher alarmDispatcher;
    MockAuditManager audits;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseDeleteFileTests() throws Exception {
        msgFactory = new DeleteFileMessageFactory(settings);
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        addStep("Initialize the pillar.", "Should not be a problem.");
        archive = new ReferenceArchive(settings.getReferenceSettings().getPillarSettings().getFileDir());
        audits = new MockAuditManager();
        alarmDispatcher = new MockAlarmDispatcher(settings, messageBus);
        mediator = new ReferencePillarMediator(messageBus, settings, archive, audits, alarmDispatcher);
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
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarDeleteFileTestSuccessCase() throws Exception {
        addDescription("Tests the DeleteFile functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        
        ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
        csSpecExisting.setChecksumSalt(null);
        csSpecExisting.setChecksumType(ChecksumType.MD5);

        ChecksumSpecTYPE csSpecRequest = new ChecksumSpecTYPE();
        csSpecRequest.setChecksumSalt(null);
        csSpecRequest.setChecksumType(ChecksumType.SHA384);

        addStep("Move the test file into the file directory.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");

        ChecksumDataForFileTYPE csData = new ChecksumDataForFileTYPE();
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setChecksumSpec(csSpecExisting);
        csData.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(testfile, csSpecExisting)));
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(testfile, new File(dir, FILE_ID));
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the pillar.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = msgFactory.createIdentifyPillarsForDeleteFileRequest(
                auditTrail, FILE_ID, clientDestinationId);
        if(useEmbeddedPillar()) {
            mediator.onMessage(identifyRequest);
        } else {
            messageBus.sendMessage(identifyRequest);
        }
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForDeleteFileResponse(
                        receivedIdentifyResponse.getCorrelationID(), 
                        FILE_ID, 
                        receivedIdentifyResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        receivedIdentifyResponse.getReplyTo(), receivedIdentifyResponse.getResponseInfo(), 
                        receivedIdentifyResponse.getTimeToDeliver(), receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertEquals(audits.getCallsForAuditEvent(), 0);
        
        addStep("Create and send the actual DeleteFile message to the pillar.", 
                "Should be received and handled by the pillar.");
        DeleteFileRequest deleteFileRequest = msgFactory.createDeleteFileRequest(auditTrail, 
                csData, csSpecRequest, receivedIdentifyResponse.getCollectionID(), 
                FILE_ID, pillarId, receivedIdentifyResponse.getTo(), receivedIdentifyResponse.getReplyTo());
        if(useEmbeddedPillar()) {
            mediator.onMessage(deleteFileRequest);
        } else {
            messageBus.sendMessage(deleteFileRequest);
        }
        
        addStep("Retrieve the ProgressResponse for the DeleteFile request", 
                "The DeleteFile progress response should be sent by the pillar.");
        DeleteFileProgressResponse progressResponse = clientTopic.waitForMessage(DeleteFileProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createDeleteFileProgressResponse(progressResponse.getCorrelationID(), FILE_ID, 
                        pillarId, progressResponse.getReplyTo(), progressResponse.getResponseInfo(), 
                        progressResponse.getTo()));
        
        addStep("Retrieve the FinalResponse for the DeleteFile request", 
                "The DeleteFile response should be sent by the pillar.");
        DeleteFileFinalResponse finalResponse = clientTopic.waitForMessage(DeleteFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createDeleteFileFinalResponse(finalResponse.getChecksumDataForExistingFile(), 
                        finalResponse.getCorrelationID(), FILE_ID, pillarId, finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), finalResponse.getTo()));
        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 0, "Should not have send any alarms.");
        Assert.assertEquals(audits.getCallsForAuditEvent(), 3, "Should deliver 3 audits. One for delete and two for "
                + "calculate checksums (both the validation and the requested).");
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarDeleteFileTestFailedNoSuchFile() throws Exception {
        addDescription("Tests the DeleteFile functionality of the reference pillar for the scenario when the file does not exist.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String wrongID = "error-file-id";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        
        ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
        csSpecExisting.setChecksumSalt(null);
        csSpecExisting.setChecksumType(ChecksumType.MD5);

        ChecksumSpecTYPE csSpecRequest = new ChecksumSpecTYPE();
        csSpecRequest.setChecksumSalt(null);
        csSpecRequest.setChecksumType(ChecksumType.SHA384);

        addStep("Move the test file into the file directory.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");

        ChecksumDataForFileTYPE csData = new ChecksumDataForFileTYPE();
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setChecksumSpec(csSpecExisting);
        csData.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(testfile, csSpecExisting)));
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(testfile, new File(dir, FILE_ID));
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the pillar.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = msgFactory.createIdentifyPillarsForDeleteFileRequest(
                auditTrail, wrongID, clientDestinationId);
        if(useEmbeddedPillar()) {
            mediator.onMessage(identifyRequest);
        } else {
            messageBus.sendMessage(identifyRequest);
        }
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForDeleteFileResponse(
                        receivedIdentifyResponse.getCorrelationID(), 
                        wrongID, 
                        receivedIdentifyResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        receivedIdentifyResponse.getReplyTo(), receivedIdentifyResponse.getResponseInfo(), 
                        receivedIdentifyResponse.getTimeToDeliver(), receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_NOT_FOUND_FAILURE);     
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarDeleteFileTestFailedWrongChecksum() throws Exception {
        addDescription("Tests the DeleteFile functionality of the reference pillar for scenario when a wrong "
                + "checksum is given as argument.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String auditTrail = null;
        
        ChecksumSpecTYPE csSpecExisting = new ChecksumSpecTYPE();
        csSpecExisting.setChecksumSalt(null);
        csSpecExisting.setChecksumType(ChecksumType.MD5);

        ChecksumSpecTYPE csSpecRequest = new ChecksumSpecTYPE();
        csSpecRequest.setChecksumSalt(null);
        csSpecRequest.setChecksumType(ChecksumType.SHA1);

        addStep("Move the test file into the file directory.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");

        addStep("Put one checksum spec in the data and calculate the checksum with the other spec.", 
                "The wrong checksum is calculated.");
        ChecksumDataForFileTYPE csData = new ChecksumDataForFileTYPE();
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setChecksumSpec(csSpecExisting);
        csData.setChecksumValue(Base16Utils.encodeBase16(ChecksumUtils.generateChecksum(testfile, csSpecRequest)));
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(testfile, new File(dir, FILE_ID));
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the pillar.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = msgFactory.createIdentifyPillarsForDeleteFileRequest(
                auditTrail, FILE_ID, clientDestinationId);
        if(useEmbeddedPillar()) {
            mediator.onMessage(identifyRequest);
        } else {
            messageBus.sendMessage(identifyRequest);
        }
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForDeleteFileResponse(
                        receivedIdentifyResponse.getCorrelationID(), 
                        FILE_ID, 
                        receivedIdentifyResponse.getPillarChecksumSpec(), 
                        pillarId, 
                        receivedIdentifyResponse.getReplyTo(), receivedIdentifyResponse.getResponseInfo(), 
                        receivedIdentifyResponse.getTimeToDeliver(), receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Create and send the actual DeleteFile message to the pillar.", 
                "Should be received and handled by the pillar.");
        DeleteFileRequest deleteFileRequest = msgFactory.createDeleteFileRequest(auditTrail, 
                csData, csSpecRequest, receivedIdentifyResponse.getCollectionID(), 
                FILE_ID, pillarId, receivedIdentifyResponse.getTo(), receivedIdentifyResponse.getReplyTo());
        if(useEmbeddedPillar()) {
            mediator.onMessage(deleteFileRequest);
        } else {
            messageBus.sendMessage(deleteFileRequest);
        }
        
        addStep("Retrieve the FinalResponse for the DeleteFile request", 
                "The DeleteFile response should be sent by the pillar.");
        DeleteFileFinalResponse finalResponse = clientTopic.waitForMessage(DeleteFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.EXISTING_FILE_CHECKSUM_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createDeleteFileFinalResponse(finalResponse.getChecksumDataForExistingFile(), 
                        finalResponse.getCorrelationID(), FILE_ID, pillarId, finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), finalResponse.getTo()));
        
        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 1, "Should have tried to send an alarm.");
    }
}
