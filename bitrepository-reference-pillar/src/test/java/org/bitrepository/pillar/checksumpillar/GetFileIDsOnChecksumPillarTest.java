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
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.common.utils.TestFileHelper;
import org.bitrepository.pillar.messagefactories.GetFileIDsMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class GetFileIDsOnChecksumPillarTest extends ChecksumPillarTest {
    private GetFileIDsMessageFactory msgFactory;
    static final FileIDs allFileIDs = new FileIDs();
    static {
        allFileIDs.setAllFileIDs("true");        
    }
    private static String DELIVERY_ADDRESS;

    @BeforeMethod (alwaysRun=true)
    public void initialiseGetFileIDsOnReferencePillarTest() throws Exception {
        msgFactory = new GetFileIDsMessageFactory(componentSettings);
        DELIVERY_ADDRESS =  httpServer.getURL("test.txt").toExternalForm();
    }    

    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetFileIDsTestSuccessCase() throws Exception {
        addDescription("Tests the GetFileIDs functionality of the checksum pillar for the successful scenario.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String auditTrail = "GET-FILE-IDS-TEST";
        String CHECKSUM = "1234cccccccc4321";
        FileIDs fileids = FileIDsUtils.createFileIDs(TestFileHelper.DEFAULT_FILE_ID);
        String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        componentSettings.getCollectionSettings().getProtocolSettings().setDefaultChecksumType(ChecksumType.MD5.toString());
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        cache.insertChecksumCalculation(TestFileHelper.DEFAULT_FILE_ID, CHECKSUM, new Date());
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the checksum pillar.");
        IdentifyPillarsForGetFileIDsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileIDsRequest(
                auditTrail, fileids, getPillarID(), clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response getPillarID() the checksum pillar.", 
                "The checksum pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForGetFileIDsResponse(
                        identifyRequest.getCorrelationID(),
                        fileids,
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Create and send the actual GetFileIDs message to the checksum pillar.", 
                "Should be received and handled by the checksum pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(
                auditTrail, receivedIdentifyResponse.getCorrelationID(), fileids, getPillarID(), pillarId, 
                clientDestinationId, DELIVERY_ADDRESS, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(getFileIDsRequest);
        
        addStep("Retrieve the ProgressResponse for the GetFileIDs request", 
                "The GetFileIDs progress response should be sent by the checksum pillar.");
        GetFileIDsProgressResponse progressResponse = clientTopic.waitForMessage(GetFileIDsProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createGetFileIDsProgressResponse(
                        identifyRequest.getCorrelationID(), 
                        fileids, 
                        pillarId, 
                        progressResponse.getReplyTo(), 
                        progressResponse.getResponseInfo(),
                        DELIVERY_ADDRESS,
                        progressResponse.getTo()));
        
        addStep("Retrieve the FinalResponse for the GetFileIDs request", 
                "The GetFileIDs response should be sent by the checksum pillar.");
        GetFileIDsFinalResponse finalResponse = clientTopic.waitForMessage(GetFileIDsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createGetFileIDsFinalResponse(
                        identifyRequest.getCorrelationID(), 
                        fileids,
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getResultingFileIDs(),
                        finalResponse.getTo()));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetFileIDsTestSuccessCaseAllFilesAndURL() throws Exception {
        addDescription("Tests the GetFileIDs functionality of the checksum pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        FileIDs fileids = FileIDsUtils.getAllFileIDs();
        String auditTrail = null;
        String CHECKSUM = "1234cccccccc4321";
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        cache.insertChecksumCalculation(DEFAULT_FILE_ID, CHECKSUM, new Date());
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileIDsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileIDsRequest(
                auditTrail, fileids, getPillarID(), clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response getPillarID() the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForGetFileIDsResponse(
                        identifyRequest.getCorrelationID(),
                        fileids, 
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Create and send the actual GetFileIDs message to the pillar.", 
                "Should be received and handled by the pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(
                auditTrail, receivedIdentifyResponse.getCorrelationID(), fileids, getPillarID(), pillarId, 
                clientDestinationId, DELIVERY_ADDRESS, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(getFileIDsRequest);
        
        addStep("Retrieve the ProgressResponse for the GetFileIDs request", 
                "The GetFileIDs progress response should be sent by the pillar.");
        GetFileIDsProgressResponse progressResponse = clientTopic.waitForMessage(GetFileIDsProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createGetFileIDsProgressResponse(
                        identifyRequest.getCorrelationID(), 
                        fileids, 
                        pillarId, 
                        progressResponse.getReplyTo(), 
                        progressResponse.getResponseInfo(), 
                        DELIVERY_ADDRESS,
                        progressResponse.getTo()));
        
        addStep("Retrieve the FinalResponse for the GetFileIDs request", 
                "The GetFileIDs response should be sent by the pillar.");
        GetFileIDsFinalResponse finalResponse = clientTopic.waitForMessage(GetFileIDsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createGetFileIDsFinalResponse(
                        identifyRequest.getCorrelationID(), 
                        fileids,
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getResultingFileIDs(),
                        finalResponse.getTo()));
        
        Assert.assertEquals(finalResponse.getResultingFileIDs().getResultAddress(), DELIVERY_ADDRESS);
        Assert.assertNull(finalResponse.getResultingFileIDs().getFileIDsData(), "Results should be delivered through URL");
        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assert.assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver any audit.");
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetFileIDsTestFailedNoSuchFile() throws Exception {
        addDescription("Tests that the ChecksumPillar is able to reject a GetFileIDs requests for a file, which it " +
                "does not have.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String auditTrail = "GET-FILE-IDS-TEST";
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        componentSettings.getCollectionSettings().getProtocolSettings().setDefaultChecksumType(ChecksumType.MD5.toString());
        FileIDs fileids = new FileIDs();
        fileids.setFileID(FILE_ID);
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the checksum pillar.");
        IdentifyPillarsForGetFileIDsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileIDsRequest(
                auditTrail, fileids, getPillarID(), clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response getPillarID() the checksum pillar.", 
                "The checksum pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForGetFileIDsResponse(
                        identifyRequest.getCorrelationID(),
                        fileids, 
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_NOT_FOUND_FAILURE);        
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetFileIDsTestFailedNoSuchFileInOperation() throws Exception {
        addDescription("Tests that the ChecksumPillar is able to reject a GetFileIDs requests for a file, " +
                "which it does not have. But this time at the GetFileIDs message.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String auditTrail = "GET-FILE-IDS-TEST";
        String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        componentSettings.getCollectionSettings().getProtocolSettings().setDefaultChecksumType(ChecksumType.MD5.toString());
        FileIDs fileids = FileIDsUtils.createFileIDs(DEFAULT_FILE_ID);
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the checksum pillar.");
        IdentifyPillarsForGetFileIDsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetFileIDsRequest(
                auditTrail, null, getPillarID(), clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response getPillarID() the checksum pillar.", 
                "The checksum pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForGetFileIDsResponse(
                        identifyRequest.getCorrelationID(),
                        null, 
                        pillarId,
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(),
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);

        addStep("Create and send the GetFileIDs request message.", 
                "Should be caught and handled by the checksum pillar.");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(
                auditTrail, msgFactory.getNewCorrelationID(), fileids, getPillarID(), pillarId, 
                clientDestinationId, DELIVERY_ADDRESS, pillarDestinationId);
        messageBus.sendMessage(getFileIDsRequest);
        
        addStep("Retrieve the FinalResponse for the GetFileIDs request", 
                "The GetFileIDs response should be sent by the checksum pillar.");
        GetFileIDsFinalResponse finalResponse = clientTopic.waitForMessage(GetFileIDsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createGetFileIDsFinalResponse(
                        getFileIDsRequest.getCorrelationID(), 
                        fileids,
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getResultingFileIDs(),
                        finalResponse.getTo()));      
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetFileIDsTestBadDeliveryURL() throws Exception {
        addDescription("Test the case when the delivery URL is unaccessible.");
        String badURL = "http://localhost:61616/¾";
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(
                null, msgFactory.getNewCorrelationID(), allFileIDs, getPillarID(), getPillarID(), 
                clientDestinationId, badURL, pillarDestinationId);
        messageBus.sendMessage(getFileIDsRequest);
        
        GetFileIDsFinalResponse finalResponse = clientTopic.waitForMessage(GetFileIDsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_TRANSFER_FAILURE);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetFileIDsTestDeliveryThroughMessage() throws Exception {
        addDescription("Test the case when the results should be delivered through the message .");
        GetFileIDsRequest getFileIDsRequest = msgFactory.createGetFileIDsRequest(
                null, msgFactory.getNewCorrelationID(), allFileIDs, getPillarID(), getPillarID(), 
                clientDestinationId, null, pillarDestinationId);
        messageBus.sendMessage(getFileIDsRequest);
        
        GetFileIDsFinalResponse finalResponse = clientTopic.waitForMessage(GetFileIDsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.OPERATION_COMPLETED);
        Assert.assertNull(finalResponse.getResultingFileIDs().getResultAddress());
        Assert.assertNotNull(finalResponse.getResultingFileIDs().getFileIDsData());
    }

    @Override
    protected String getComponentID() {
        return "ChecksumPillarUnderTest";
    }
}
