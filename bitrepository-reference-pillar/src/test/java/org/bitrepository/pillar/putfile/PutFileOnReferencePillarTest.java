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
package org.bitrepository.pillar.putfile;

import java.io.File;
import java.io.FileInputStream;
import java.net.URL;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.ChecksumDataForFileTYPE;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositoryelements.ResponseInfo;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.PutFileFinalResponse;
import org.bitrepository.bitrepositorymessages.PutFileProgressResponse;
import org.bitrepository.bitrepositorymessages.PutFileRequest;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.PillarComponentFactory;
import org.bitrepository.pillar.referencepillar.ReferencePillar;
import org.bitrepository.protocol.ProtocolComponentFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class PutFileOnReferencePillarTest extends DefaultFixturePillarTest {
    PillarPutFileMessageFactory msgFactory;
    private static ReferencePillar pillar = null;
    
    @BeforeMethod (alwaysRun=true)
    public void initialisePutFileTests() throws Exception {
        msgFactory = new PillarPutFileMessageFactory(settings);
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        if(pillar == null) {
            pillar = PillarComponentFactory.getInstance().getReferencePillar(messageBus, settings);
        } else {
            FileUtils.retrieveDirectory(dir.getAbsolutePath());
            FileUtils.retrieveSubDirectory(dir, "fileDir");
            FileUtils.retrieveSubDirectory(dir, "tmpDir");
            FileUtils.retrieveSubDirectory(dir, "retainDir");
        }
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"pillartest"})
    public void pillarPutTestSuccessCase() throws Exception {
        addDescription("Tests the put functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
        Long FILE_SIZE = testfile.length();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(testfile, "md5", (byte[]) null);
        String FILE_CHECKSUM_SHA1 = ChecksumUtils.generateChecksum(testfile, "sha1", (byte[]) null);
        Date startDate = new Date();
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(testfile), 
                new URL(FILE_ADDRESS));
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest 
                = msgFactory.createIdentifyPillarsForPutFileRequest(clientDestinationId, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        identifyRequest.getCorrelationID(),
                        receivedIdentifyResponse.getReplyTo(),
                        pillarId,
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo(),
                        receivedIdentifyResponse.getResponseInfo()));
        
        addStep("Create and send the actual Put message to the pillar.", 
                "Should be received and handled by the pillar.");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(
                receivedIdentifyResponse.getCorrelationID(), FILE_ADDRESS, FILE_ID, FILE_SIZE,
                FILE_CHECKSUM_MD5, pillarId, clientDestinationId, receivedIdentifyResponse.getReplyTo());
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
                        progressResponse.getResponseInfo(), 
                        progressResponse.getReplyTo(), 
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
                        finalResponse.getResponseInfo(), 
                        pillarId, 
                        finalResponse.getPillarChecksumSpec(), 
                        finalResponse.getReplyTo(), 
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
    }
    
    @Test( groups = {"pillartest"})
    public void pillarPutTestFailedFileSize() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject put requests for too large files.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                clientDestinationId, FILE_ID, Long.MAX_VALUE);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        identifyRequest.getCorrelationID(),
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getPillarID(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo(),
                        receivedIdentifyResponse.getResponseInfo()));
        
        addStep("Validate that the identification has failed.", 
                "The response info should give 'FAILURE'");
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), ResponseCode.FAILURE);
    }
    
    @Test( groups = {"pillartest"})
    public void pillarPutTestFileAlreadyExistsCase() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject put requests on a file, which it already have.");
        addStep("Set up constants and variables.", "Should not fail here!");
        
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
        IdentifyPillarsForPutFileRequest identifyRequest 
                = msgFactory.createIdentifyPillarsForPutFileRequest(clientDestinationId, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        identifyRequest.getCorrelationID(),
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getPillarID(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo(),
                        receivedIdentifyResponse.getResponseInfo()));
        
        addStep("Validate that the identification has failed.", 
                "The response info should give 'FILE_FOUND'");
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.DUPLICATE_FILE_FAILURE);
    }
    
    @SuppressWarnings("deprecation")
    @Test( groups = {"pillartest"})
    public void pillarPutTestBadChecksumCase() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject put requests, "
                + "when it gives a wrong checksum for validation.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        
        addStep("Upload the test-file and calculate the checksum.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
        Long FILE_SIZE = testfile.length();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String CORRECT_FILE_CHECKSUM_MD5 = ChecksumUtils.generateChecksum(testfile, "md5", (byte[]) null);
        String WRONG_FILE_CHECKSUM_MD5 = "erroneous-checksum";
        ProtocolComponentFactory.getInstance().getFileExchange().uploadToServer(new FileInputStream(testfile), 
                new URL(FILE_ADDRESS));
        
        addStep("Create and send a identify message to the pillar.", "Should be received and handled by the pillar.");
        IdentifyPillarsForPutFileRequest identifyRequest 
                = msgFactory.createIdentifyPillarsForPutFileRequest(clientDestinationId, FILE_ID, FILE_SIZE);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", "The pillar should make a response.");
        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForPutFileResponse(
                        identifyRequest.getCorrelationID(),
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getPillarID(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo(),
                        receivedIdentifyResponse.getResponseInfo()));
        
        addStep("Create and send the actual Put message to the pillar.", 
                "Should be received and handled by the pillar.");
        PutFileRequest putRequest = msgFactory.createPutFileRequest(
                receivedIdentifyResponse.getCorrelationID(), FILE_ADDRESS, FILE_ID, FILE_SIZE,
                WRONG_FILE_CHECKSUM_MD5,  receivedIdentifyResponse.getPillarID(), clientDestinationId, 
                receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(putRequest);
        
        addStep("Retrieve the ProgressResponse for the put request", "The put response should be sent by the pillar.");
        PutFileProgressResponse progressResponse = clientTopic.waitForMessage(PutFileProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createPutFileProgressResponse(
                        identifyRequest.getCorrelationID(), 
                        progressResponse.getFileAddress(), 
                        progressResponse.getFileID(), 
                        progressResponse.getPillarID(), 
                        progressResponse.getPillarChecksumSpec(), 
                        progressResponse.getResponseInfo(), 
                        progressResponse.getReplyTo(), 
                        progressResponse.getTo()));
        
        addStep("Retrieve the FinalResponse for the put request", "The put response should be sent by the pillar.");
        PutFileFinalResponse finalResponse = clientTopic.waitForMessage(PutFileFinalResponse.class);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createPutFileFinalResponse(
                        finalResponse.getChecksumDataForNewFile(),
                        identifyRequest.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getPillarID(), 
                        finalResponse.getPillarChecksumSpec(), 
                        finalResponse.getReplyTo(), 
                        finalResponse.getTo()));
        
        addStep("Validate that the response contains the correct information.", 
                "Should deliver a OPERATION_FAILED with both the correct and the wrong checksum.");
        ResponseInfo ir = finalResponse.getResponseInfo();
        Assert.assertEquals(ir.getResponseCode(), ResponseCode.FAILURE);
        
        Assert.assertTrue(ir.getResponseText().contains(CORRECT_FILE_CHECKSUM_MD5), 
                "The response should contain the actual checksum '" + CORRECT_FILE_CHECKSUM_MD5 + "', but was: '"
                + ir.getResponseText());
        Assert.assertTrue(ir.getResponseText().contains(WRONG_FILE_CHECKSUM_MD5), 
                "The response should contain the wrong checksum '" + WRONG_FILE_CHECKSUM_MD5 + "', but was: '"
                + ir.getResponseText());
    }
    
}
