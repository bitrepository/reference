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

import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.GetFileIDsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.pillar.messagefactories.GetFileIDsMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class GetFileIDsOnChecksumPillarTest extends ChecksumPillarTest {
    private GetFileIDsMessageFactory msgFactory;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseGetFileIDsOnChecksum() throws Exception {
        msgFactory = new GetFileIDsMessageFactory(componentSettings);
    }    

    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetFileIDsTestSuccessCase() throws Exception {
        addDescription("Tests the GetFileIDs functionality of the checksum pillar for the successful scenario.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_IDS_DELIVERY_ADDRESS = "http://sandkasse-01.kb.dk/dav/fileids-delivery-test.xml" + getTopicPostfix();        
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "GET-FILE-IDS-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        componentSettings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        FileIDs fileids = new FileIDs();
        fileids.setFileID(FILE_ID);
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        cache.putEntry(FILE_ID, CHECKSUM);
        
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
                clientDestinationId, FILE_IDS_DELIVERY_ADDRESS, receivedIdentifyResponse.getReplyTo());
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
                        FILE_IDS_DELIVERY_ADDRESS,
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
    public void pillarGetFileIDsTestFailedNoSuchFile() throws Exception {
        addDescription("Tests that the ChecksumPillar is able to reject a GetFileIDs requests for a file, which it does not have.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String auditTrail = "GET-FILE-IDS-TEST";
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        componentSettings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
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
        String FILE_IDS_DELIVERY_ADDRESS = "http://sandkasse-01.kb.dk/dav/checksum-delivery-test.xml" + getTopicPostfix();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        componentSettings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        FileIDs fileids = new FileIDs();
        fileids.setFileID(FILE_ID);
        
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
                clientDestinationId, FILE_IDS_DELIVERY_ADDRESS, pillarDestinationId);
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

    @Override
    protected String getComponentID() {
        return "ChecksumPillarUnderTest";
    }
}
