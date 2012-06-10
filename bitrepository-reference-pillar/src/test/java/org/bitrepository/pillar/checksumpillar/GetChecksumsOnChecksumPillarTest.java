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

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.pillar.messagefactories.GetChecksumsMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.Date;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class GetChecksumsOnChecksumPillarTest extends ChecksumPillarTest {
    private GetChecksumsMessageFactory msgFactory;

    @BeforeMethod (alwaysRun=true)
    public void initialiseDeleteFileTests() throws Exception {
        msgFactory = new GetChecksumsMessageFactory(componentSettings);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestSuccessCase() throws Exception {
        addDescription("Tests the GetChecksums functionality of the checksum pillar for the successful scenario.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "GET-CHECKSUMS-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String CS_DELIVERY_ADDRESS = null;
        String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        componentSettings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        FileIDs fileids = new FileIDs();
        fileids.setFileID(FILE_ID);

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the checksum pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(
                auditTrail, csSpec, fileids, getPillarID(), clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response getPillarID() the checksum pillar.", 
                "The checksum pillar should make a response.");
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
        
        addStep("Create and send the actual GetChecksums message to the checksum pillar.", 
                "Should be received and handled by the checksum pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(auditTrail,
                csSpec, receivedIdentifyResponse.getCorrelationID(), fileids, getPillarID(), pillarId, 
                clientDestinationId, CS_DELIVERY_ADDRESS, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(getChecksumsRequest);
        
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
                "The GetChecksums response should be sent by the checksum pillar.");
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
        Assert.assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 1);
        Assert.assertEquals(Base16Utils.decodeBase16(finalResponse.getResultingChecksums().getChecksumDataItems().get(0).getChecksumValue()), 
                CHECKSUM);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestFailedNoSuchFile() throws Exception {
        addDescription("Tests that the ChecksumPillar is able to reject a GetChecksums requests for a file, which it does not have.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "GET-CHECKSUMS-TEST";
        String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        componentSettings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        FileIDs fileids = new FileIDs();
        fileids.setFileID(FILE_ID);

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the checksum pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(
                auditTrail, csSpec, fileids, getPillarID(), clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response getPillarID() the checksum pillar.", 
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
        addDescription("Tests that the ChecksumPillar is able to reject a GetChecksums requests for a file, " +
                "which it does not have. But this time at the GetChecksums message.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "GET-CHECKSUMS-TEST";
        String CS_DELIVERY_ADDRESS = "http://sandkasse-01.kb.dk/dav/checksum-delivery-test.xml" + getTopicPostfix();
        componentSettings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        FileIDs fileids = new FileIDs();
        fileids.setFileID(FILE_ID);

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        addStep("Create and send the actual GetChecksums message to the checksum pillar.", 
                "Should be received and handled by the checksum pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                auditTrail, csSpec, msgFactory.getNewCorrelationID(), fileids, getPillarID(), getPillarID(),
                clientDestinationId, CS_DELIVERY_ADDRESS, pillarDestinationId);
        messageBus.sendMessage(getChecksumsRequest);
        
        addStep("Retrieve the FinalResponse for the GetChecksums request", 
                "The GetChecksums response should be sent by the checksum pillar.");
        GetChecksumsFinalResponse finalResponse = clientTopic.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createGetChecksumsFinalResponse(
                        csSpec,
                        getChecksumsRequest.getCorrelationID(),
                        getPillarID(),
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getResultingChecksums(),
                        finalResponse.getTo()));
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestFailedBadChecksumType() throws Exception {
        addDescription("Tests that the ChecksumPillar is able to reject a GetChecksums identification for a file, "
                + "where it does not support the requested checksum.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + new Date().getTime();
        String auditTrail = "GET-CHECKSUMS-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String CS_DELIVERY_ADDRESS = "http://sandkasse-01.kb.dk/dav/checksum-delivery-test.xml" + getTopicPostfix();
        String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        componentSettings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        FileIDs fileids = new FileIDs();
        fileids.setFileID(FILE_ID);

        ChecksumSpecTYPE badCsSpec = new ChecksumSpecTYPE();
        badCsSpec.setChecksumSalt(null);
        badCsSpec.setChecksumType(ChecksumType.OTHER);
        badCsSpec.setOtherChecksumType("UNSUPPORTED ALGORITHM");
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        addStep("Create and send the actual GetChecksums message to the checksum pillar.", 
                "Should be received and handled by the checksum pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                auditTrail, badCsSpec, msgFactory.getNewCorrelationID(), fileids, getPillarID(), pillarId, 
                clientDestinationId, CS_DELIVERY_ADDRESS, pillarDestinationId);
        messageBus.sendMessage(getChecksumsRequest);
        
        addStep("Retrieve the FinalResponse for the GetChecksums request", 
                "The GetChecksums response should be sent by the checksum pillar.");
        GetChecksumsFinalResponse finalResponse = clientTopic.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.REQUEST_NOT_UNDERSTOOD_FAILURE);
        
        Assert.assertEquals(finalResponse,
                msgFactory.createGetChecksumsFinalResponse(
                        badCsSpec,
                        getChecksumsRequest.getCorrelationID(), 
                        pillarId,
                        finalResponse.getReplyTo(), 
                        finalResponse.getResponseInfo(), 
                        finalResponse.getResultingChecksums(),
                        finalResponse.getTo()));
    }
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestSuccessCaseWithAddress() throws Exception {
        addDescription("Tests the GetChecksums functionality of the checksum pillar for the successful scenario, "
                + "where the all the files are requested and the result should be delivered to and URL.");
        addStep("Setting up the variables for the test.", "Should be instantiated.");
        String FILE_ID = DEFAULT_FILE_ID + System.currentTimeMillis();
        String auditTrail = "GET-CHECKSUMS-TEST";
        String CHECKSUM = "1234cccccccc4321";
        String CS_DELIVERY_ADDRESS = "http://sandkasse-01.kb.dk/dav/checksum-delivery-test.xml" 
                + System.currentTimeMillis() + getTopicPostfix();
        String pillarId = componentSettings.getReferenceSettings().getPillarSettings().getPillarID();
        componentSettings.getReferenceSettings().getPillarSettings().setChecksumPillarChecksumSpecificationType(
                ChecksumType.MD5.toString());
        FileIDs fileids = new FileIDs();
        fileids.setAllFileIDs("true");

        ChecksumSpecTYPE csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
        
        addStep("Move the test file into the file directory.", "Should be all-right");
        cache.putEntry(FILE_ID, CHECKSUM);
        
        addStep("Create and send the identify request message.", 
                "Should be received and handled by the checksum pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(
                auditTrail, csSpec, fileids, getPillarID(), clientDestinationId);
        messageBus.sendMessage(identifyRequest);
        
        addStep("Retrieve and validate the response getPillarID() the checksum pillar.", 
                "The checksum pillar should make a response.");
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
        
        addStep("Create and send the actual GetChecksums message to the checksum pillar.", 
                "Should be received and handled by the checksum pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(auditTrail,
                csSpec, receivedIdentifyResponse.getCorrelationID(), fileids, getPillarID(), pillarId, 
                clientDestinationId, CS_DELIVERY_ADDRESS, receivedIdentifyResponse.getReplyTo());
        messageBus.sendMessage(getChecksumsRequest);
        
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
                "The GetChecksums response should be sent by the checksum pillar.");
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
        Assert.assertEquals(finalResponse.getResultingChecksums().getResultAddress(), CS_DELIVERY_ADDRESS);
        Assert.assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 0, 
                "Should contain no checksum data");
    }
}
