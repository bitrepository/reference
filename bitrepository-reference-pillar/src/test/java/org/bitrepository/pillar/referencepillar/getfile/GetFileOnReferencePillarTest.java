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
package org.bitrepository.pillar.referencepillar.getfile;

import java.io.File;
import java.util.Date;

import org.bitrepository.bitrepositoryelements.AlarmCode;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.GetFileFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileResponse;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.PillarComponentFactory;
import org.bitrepository.pillar.referencepillar.ReferencePillar;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class GetFileOnReferencePillarTest extends DefaultFixturePillarTest {
    PillarGetFileMessageFactory msgFactory;
    ReferencePillar pillar = null;
    
    @BeforeMethod (alwaysRun=true)
    public void initialisePutFileTests() throws Exception {
        msgFactory = new PillarGetFileMessageFactory(settings);
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        if(dir.exists()) {
            FileUtils.delete(dir);
            FileUtils.retrieveDirectory(dir.getAbsolutePath());
            FileUtils.retrieveSubDirectory(dir, "fileDir");
            FileUtils.retrieveSubDirectory(dir, "tmpDir");
            FileUtils.retrieveSubDirectory(dir, "retainDir");
        }
        
        if(pillar == null) {
            pillar = PillarComponentFactory.getInstance().getReferencePillar(messageBus, settings);
        }
    }
    
    @Test( groups = {"pillartest"})
    public void pillarGetFileTestSuccessCase() throws Exception {
        addDescription("Tests the get functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
        Long FILE_SIZE = testfile.length();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(testfile, new File(dir, FILE_ID));
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                clientDestinationId, FILE_ID);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForGetFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForGetFileResponse(
                        identifyRequest.getCorrelationID(),
                        FILE_ID, 
                        receivedIdentifyResponse.getReplyTo(),
                        pillarId,
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo(),
                        receivedIdentifyResponse.getResponseInfo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Create and send the actual GetFile message to the pillar.", 
                "Should be received and handled by the pillar.");
        GetFileRequest getRequest = msgFactory.createGetFileRequest(
                receivedIdentifyResponse.getCorrelationID(), FILE_ADDRESS, FILE_ID, pillarId, 
                clientDestinationId, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(getRequest);
        
        addStep("Retrieve the ProgressResponse for the GetFile request", 
                "The GetFile progress response should be sent by the pillar.");
        GetFileProgressResponse progressResponse = clientTopic.waitForMessage(GetFileProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createGetFileProgressResponse(
                        identifyRequest.getCorrelationID(), 
                        progressResponse.getFileAddress(), 
                        progressResponse.getFileID(), 
                        pillarId, 
                        FILE_SIZE,
                        progressResponse.getResponseInfo(), 
                        progressResponse.getReplyTo(), 
                        progressResponse.getTo()));
        
        addStep("Retrieve the FinalResponse for the GetFile request", 
                "The GetFile response should be sent by the pillar.");
        GetFileFinalResponse finalResponse = clientTopic.waitForMessage(GetFileFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createGetFileFinalResponse(
                        identifyRequest.getCorrelationID(), 
                        finalResponse.getFileAddress(), 
                        finalResponse.getFileID(), 
                        finalResponse.getResponseInfo(), 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getTo()));
    }
    
    @Test( groups = {"pillartest"})
    public void pillarGetFileTestFailedNoSuchFile() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject a GetFile requests for a file, which it does not have.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                clientDestinationId, FILE_ID);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForGetFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForGetFileResponse(
                        identifyRequest.getCorrelationID(),
                        FILE_ID, 
                        receivedIdentifyResponse.getReplyTo(),
                        pillarId,
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo(),
                        receivedIdentifyResponse.getResponseInfo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_NOT_FOUND_FAILURE);        
    }
    
    @Test( groups = {"pillartest"})
    public void pillarGeneralTestWrongCollectionID() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject a GetFile requests with a wrong CollectionID.");
        addStep("Set up constants and variables.", "Should not fail here!");
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        
        addStep("Create and send the identify request message.", 
                "Should be received by the pillar, which should issue an alarm.");
        IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                clientDestinationId, FILE_ID);
        identifyRequest.setCollectionID(settings.getCollectionID() + "ERROR");
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve the alarm sent by the pillar.", 
                "The pillar should make a response.");
        AlarmMessage alarm = alarmDestination.waitForMessage(AlarmMessage.class);
        Assert.assertNotNull(alarm, "An alarm message should have been received.");
        Assert.assertEquals(alarm.getAlarm().getAlarmCode(), AlarmCode.FAILED_OPERATION, 
                "The operation should fail.");
        // TODO validate content of the alarm.
    }
    
    @Test( groups = {"pillartest"})
    public void pillarGeneralTestWrongPillarID() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject a GetFile requests with a wrong pillarID.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ADDRESS = "http://sandkasse-01.kb.dk/dav/test.txt";
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(testfile, new File(dir, FILE_ID));
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileRequest(
                clientDestinationId, FILE_ID);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForGetFileResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForGetFileResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForGetFileResponse(
                        identifyRequest.getCorrelationID(),
                        FILE_ID, 
                        receivedIdentifyResponse.getReplyTo(),
                        pillarId,
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo(),
                        receivedIdentifyResponse.getResponseInfo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Create and send the actual GetFile message to the pillar.", 
                "Should be received and handled by the pillar.");
        GetFileRequest getRequest = msgFactory.createGetFileRequest(
                receivedIdentifyResponse.getCorrelationID(), FILE_ADDRESS, FILE_ID, pillarId, 
                clientDestinationId, receivedIdentifyResponse.getReplyTo());
        getRequest.setPillarID(pillarId + "-ERROR");
        messageBus.sendMessage(getRequest);
        
        addStep("Retrieve the alarm sent by the pillar.", 
                "The pillar should make a response.");
        AlarmMessage alarm = alarmDestination.waitForMessage(AlarmMessage.class);
        Assert.assertNotNull(alarm, "An alarm message should have been received.");
        // TODO validate content of the alarm.
    }
}
