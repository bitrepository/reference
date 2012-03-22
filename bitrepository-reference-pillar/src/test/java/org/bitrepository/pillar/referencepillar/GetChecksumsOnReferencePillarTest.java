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

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.DefaultFixturePillarTest;
import org.bitrepository.pillar.messagefactories.GetChecksumsMessageFactory;
import org.bitrepository.pillar.referencepillar.messagehandler.ReferencePillarMediator;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class GetChecksumsOnReferencePillarTest extends DefaultFixturePillarTest {
    GetChecksumsMessageFactory msgFactory;
    
    ReferenceArchive archive;
    ReferencePillarMediator mediator;
    
    @BeforeMethod (alwaysRun=true)
    public void initialiseGetChecksumsTests() throws Exception {
        msgFactory = new GetChecksumsMessageFactory(settings);
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir());
        if(dir.exists()) {
            FileUtils.delete(dir);
        }
        
        addStep("Initialize the pillar.", "Should not be a problem.");
        archive = new ReferenceArchive(settings.getReferenceSettings().getPillarSettings().getFileDir());
        mediator = new ReferencePillarMediator(messageBus, settings, archive);
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
    public void pillarGetChecksumsTestSuccessCase() throws Exception {
        addDescription("Tests the GetChecksums functionality of the reference pillar for the successful scenario.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String CS_DELIVERY_ADDRESS = "http://sandkasse-01.kb.dk/dav/checksum-delivery-test.xml" + getTopicPostfix();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        FileIDs fileids = new FileIDs();
        fileids.setFileID(FILE_ID);
        String auditTrail = null;
        
        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        File testfile = new File("src/test/resources/" + DEFAULT_FILE_ID);
        Assert.assertTrue(testfile.isFile(), "The test file does not exist at '" + testfile.getAbsolutePath() + "'.");
        
        File dir = new File(settings.getReferenceSettings().getPillarSettings().getFileDir() + "/fileDir");
        Assert.assertTrue(dir.isDirectory(), "The file directory for the reference pillar should be instantiated at '"
                + dir.getAbsolutePath() + "'");
        FileUtils.copyFile(testfile, new File(dir, FILE_ID));
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(
                auditTrail, csSpec, fileids, clientDestinationId);
        mediator.onMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForGetChecksumsResponse(
                      csSpec,
                      identifyRequest.getCorrelationID(),
                      fileids, 
                      receivedIdentifyResponse.getPillarChecksumSpec(),
                      pillarId, 
                      receivedIdentifyResponse.getReplyTo(),
                      receivedIdentifyResponse.getResponseInfo(), 
                      receivedIdentifyResponse.getTimeToDeliver(),
                      receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        
        addStep("Create and send the actual GetChecksums message to the pillar.", 
                "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(auditTrail,
                csSpec, receivedIdentifyResponse.getCorrelationID(), fileids, pillarId, 
                clientDestinationId, CS_DELIVERY_ADDRESS, receivedIdentifyResponse.getReplyTo());
        mediator.onMessage(getChecksumsRequest);
        
        addStep("Retrieve the ProgressResponse for the GetChecksums request", 
                "The GetChecksums progress response should be sent by the pillar.");
        GetChecksumsProgressResponse progressResponse = clientTopic.waitForMessage(GetChecksumsProgressResponse.class);
        Assert.assertEquals(progressResponse,
                msgFactory.createGetChecksumsProgressResponse(
                        csSpec, 
                        identifyRequest.getCorrelationID(), 
                        fileids, 
                        pillarId, 
                        progressResponse.getReplyTo(), 
                        progressResponse.getResponseInfo(), 
                        CS_DELIVERY_ADDRESS,
                        progressResponse.getTo()));
        
        addStep("Retrieve the FinalResponse for the GetChecksums request", 
                "The GetChecksums response should be sent by the pillar.");
        GetChecksumsFinalResponse finalResponse = clientTopic.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createGetChecksumsFinalResponse(
                        csSpec,
                        identifyRequest.getCorrelationID(), 
                        pillarId, 
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getResultingChecksums(),
                        finalResponse.getTo()));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestFailedNoSuchFile() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject a GetChecksums requests for a file, which it does not have.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        FileIDs fileids = new FileIDs();
        fileids.setFileID(FILE_ID);
        String auditTrail = null;
        
        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(
                auditTrail, csSpec, fileids, clientDestinationId);
        mediator.onMessage(identifyRequest);
        
        addStep("Retrieve and validate the response from the pillar.", 
                "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse, 
                msgFactory.createIdentifyPillarsForGetChecksumsResponse(
                        csSpec,
                        identifyRequest.getCorrelationID(),
                        fileids, 
                        receivedIdentifyResponse.getPillarChecksumSpec(),
                        pillarId, 
                        receivedIdentifyResponse.getReplyTo(),
                        receivedIdentifyResponse.getResponseInfo(), 
                        receivedIdentifyResponse.getTimeToDeliver(),
                        receivedIdentifyResponse.getTo()));
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_NOT_FOUND_FAILURE);        
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestFailedNoSuchFileInOperation() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject a GetChecksums requests for a file, " +
                "which it does not have. But this time at the GetChecksums message.");
        addStep("Set up constants and variables.", "Should not fail here!");
        String CS_DELIVERY_ADDRESS = "http://sandkasse-01.kb.dk/dav/checksum-delivery-test.xml" + getTopicPostfix();
        String pillarId = settings.getReferenceSettings().getPillarSettings().getPillarID();
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        FileIDs fileids = new FileIDs();
        fileids.setFileID(FILE_ID);
        String auditTrail = null;
        
        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        addStep("Create and send the actual GetChecksums message to the pillar.", 
                "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                auditTrail, csSpec, msgFactory.getNewCorrelationID(), fileids, pillarId, 
                clientDestinationId, CS_DELIVERY_ADDRESS, pillarDestinationId);
        mediator.onMessage(getChecksumsRequest);
        
        addStep("Retrieve the FinalResponse for the GetChecksums request", 
                "The GetChecksums response should be sent by the pillar.");
        GetChecksumsFinalResponse finalResponse = clientTopic.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createGetChecksumsFinalResponse(
                        csSpec,
                        getChecksumsRequest.getCorrelationID(), 
                        pillarId,
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getResultingChecksums(),
                        finalResponse.getTo()));
    }
}
