/*
 * #%L
 * Bitrepository Reference Pillar
 * %%
 * Copyright (C) 2010 - 2012 The State and University Library, The Royal Library and The State Archives, Denmark
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
import org.bitrepository.pillar.checksumpillar.messagehandler.ChecksumPillarMediator;
import org.bitrepository.pillar.common.PillarContext;
import org.bitrepository.pillar.messagefactories.DeleteFileMessageFactory;
import org.bitrepository.protocol.utils.Base16Utils;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class DeleteFileOnChecksumPillarTest extends DefaultFixturePillarTest {
    DeleteFileMessageFactory msgFactory;
    
    MemoryCache cache;
    ChecksumPillarMediator mediator;
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
        cache = new MemoryCache();
        audits = new MockAuditManager();
        alarmDispatcher = new MockAlarmDispatcher(settings, messageBus);
        PillarContext context = new PillarContext(settings, messageBus, alarmDispatcher, audits);
        mediator = new ChecksumPillarMediator(context, cache);
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
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarDeleteFileTestSuccessCase() throws Exception {
        addDescription("Testing the delete operation for the checksum pillar.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        ChecksumDataForFileTYPE csData = new ChecksumDataForFileTYPE();
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setChecksumSpec(csSpec);
        csData.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));
        
        addStep("Send message for identification of the pillar.", 
                "The checksum pillar receive and handle the message.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = msgFactory.createIdentifyPillarsForDeleteFileRequest(auditTrail, 
                FILE_ID, FROM, clientDestinationId);
        if(useEmbeddedPillar()) {
            mediator.onMessage(identifyRequest);
        } else {
            messageBus.sendMessage(identifyRequest);
        }
        
        addStep("Receive and validate response from the checksum pillar.",
                "The pillar should make a positive response.");
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
        
        addStep("Create and send the actual DeleteFile message to the checksum pillar.", 
                "Should be received and handled by the checksum pillar.");
        DeleteFileRequest deleteFileRequest = msgFactory.createDeleteFileRequest(auditTrail, 
                csData, csSpec, receivedIdentifyResponse.getCollectionID(), FILE_ID, FROM, pillarId, 
                receivedIdentifyResponse.getTo(), receivedIdentifyResponse.getReplyTo());
        if(useEmbeddedPillar()) {
            mediator.onMessage(deleteFileRequest);
        } else {
            messageBus.sendMessage(deleteFileRequest);
        }
        
        addStep("Retrieve the ProgressResponse for the DeleteFile request", 
                "The DeleteFile progress response should be sent by the checksum pillar.");
        DeleteFileProgressResponse progressResponse = clientTopic.waitForMessage(DeleteFileProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createDeleteFileProgressResponse(progressResponse.getCorrelationID(), FILE_ID, 
                        pillarId, progressResponse.getReplyTo(), progressResponse.getResponseInfo(), 
                        progressResponse.getTo()));
        
        addStep("Retrieve the FinalResponse for the DeleteFile request", 
                "The DeleteFile response should be sent by the checksum pillar.");
        DeleteFileFinalResponse finalResponse = clientTopic.waitForMessage(DeleteFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createDeleteFileFinalResponse(finalResponse.getChecksumDataForExistingFile(), 
                        finalResponse.getCorrelationID(), FILE_ID, pillarId, finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), finalResponse.getTo()));

        addStep("Validate the content of the cache", "Should no longer contain the checksum of the file");
        Assert.assertNull(cache.getChecksum(FILE_ID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarDeleteFileTestFailedNoSuchFile() throws Exception {
        addDescription("Tests the DeleteFile functionality of the reference pillar for the scenario when the file does not exist.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        addStep("Cleanup the memory cache so it does not contain any files.", "Should be possible.");
        cache.cleanUp();
        
        ChecksumDataForFileTYPE csData = new ChecksumDataForFileTYPE();
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setChecksumSpec(csSpec);
        csData.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the pillar.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = msgFactory.createIdentifyPillarsForDeleteFileRequest(
                auditTrail, FILE_ID, FROM, clientDestinationId);
        if(useEmbeddedPillar()) {
            mediator.onMessage(identifyRequest);
        } else {
            messageBus.sendMessage(identifyRequest);
        }
        
        addStep("Retrieve and validate the response from the checksum pillar.", 
                "The checksum pillar should make a response for 'FILE_NOT_FOUND'.");
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
                ResponseCode.FILE_NOT_FOUND_FAILURE);  
        
        addStep("Validate the content of the cache", "Should not contain the checksum of the file");
        Assert.assertNull(cache.getChecksum(FILE_ID));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarDeleteFileTestFailedWrongChecksum() throws Exception {
        addDescription("Tests the DeleteFile functionality of the reference pillar for scenario when a wrong "
                + "checksum is given as argument.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String GOOD_CHECKSUM = "1234cccccccc4321";
        String BAD_CHECKSUM = "cccc12344321cccc";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, GOOD_CHECKSUM);
        
        ChecksumDataForFileTYPE csData = new ChecksumDataForFileTYPE();
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setChecksumSpec(csSpec);
        csData.setChecksumValue(Base16Utils.encodeBase16(BAD_CHECKSUM));
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the pillar.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = msgFactory.createIdentifyPillarsForDeleteFileRequest(
                auditTrail, FILE_ID, FROM, clientDestinationId);
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
                csData, csSpec, receivedIdentifyResponse.getCollectionID(), 
                FILE_ID, FROM, pillarId, receivedIdentifyResponse.getTo(), receivedIdentifyResponse.getReplyTo());
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
        
        addStep("Check the alarm dispatcher", "An alarm should have been sent.");
        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 1);
        
        addStep("Validate the content of the cache", "Should still contain the good checksum of the file");
        Assert.assertEquals(cache.getChecksum(FILE_ID), GOOD_CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarDeleteFileTestBadChecksumSpec() throws Exception {
        addDescription("Testing the delete operation for the checksum pillar.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "DELETE-FILE-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        settings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.OTHER);
        csSpec.setOtherChecksumType("UNSUPPORTED ALGORITHM");
        
        addStep("Populate the memory cache with the file to delete.", "Should be possible.");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        ChecksumDataForFileTYPE csData = new ChecksumDataForFileTYPE();
        csData.setCalculationTimestamp(CalendarUtils.getEpoch());
        csData.setChecksumSpec(csSpec);
        csData.setChecksumValue(Base16Utils.encodeBase16(CHECKSUM));
        
        addStep("Send message for identification of the pillar.", 
                "The checksum pillar receive and handle the message.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = msgFactory.createIdentifyPillarsForDeleteFileRequest(auditTrail, 
                FILE_ID, FROM, clientDestinationId);
        if(useEmbeddedPillar()) {
            mediator.onMessage(identifyRequest);
        } else {
            messageBus.sendMessage(identifyRequest);
        }
        
        addStep("Receive and validate response from the checksum pillar.",
                "The pillar should make a positive response.");
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
        
        addStep("Create and send the actual DeleteFile message to the checksum pillar.", 
                "Should be received and handled by the checksum pillar.");
        DeleteFileRequest deleteFileRequest = msgFactory.createDeleteFileRequest(auditTrail, 
                csData, csSpec, receivedIdentifyResponse.getCollectionID(), FILE_ID, FROM, pillarId, 
                receivedIdentifyResponse.getTo(), receivedIdentifyResponse.getReplyTo());
        if(useEmbeddedPillar()) {
            mediator.onMessage(deleteFileRequest);
        } else {
            messageBus.sendMessage(deleteFileRequest);
        }
        
        addStep("Retrieve the FinalResponse for the DeleteFile request", 
                "The DeleteFile response should be sent by the checksum pillar.");
        DeleteFileFinalResponse finalResponse = clientTopic.waitForMessage(DeleteFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createDeleteFileFinalResponse(finalResponse.getChecksumDataForExistingFile(), 
                        finalResponse.getCorrelationID(), FILE_ID, pillarId, finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), finalResponse.getTo()));
        
        addStep("Validate the content of the cache", "Should still contain the checksum of the file");
        Assert.assertEquals(cache.getChecksum(FILE_ID), CHECKSUM);
    }
}
