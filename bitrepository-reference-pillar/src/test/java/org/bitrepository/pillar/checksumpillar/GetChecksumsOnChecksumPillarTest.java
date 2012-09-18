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
import org.bitrepository.common.utils.FileIDsUtils;
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
    private ChecksumSpecTYPE csSpec;

    @BeforeMethod (alwaysRun=true)
    public void initialiseGetChecksumsTests() throws Exception {
        msgFactory = new GetChecksumsMessageFactory(clientSettings, getPillarID(), pillarDestinationId);

        csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarGetChecksumsTestSuccessCase() throws Exception {
        addDescription("Tests the GetChecksums functionality of the reference pillar for the successful scenario.");
        initializeCacheWithMD5ChecksummedFile();

        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs(FILE_ID);

        addStep("Create and send the identify request message.", 
        "Should be received and handled by the pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(
                csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", 
        "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getChecksumRequestForExistingFile(), csSpec);
        Assert.assertEquals(receivedIdentifyResponse.getFileIDs(), fileids);

        addStep("Create and send the actual GetChecksums message to the pillar.", 
        "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                csSpec, fileids, null);
        messageBus.sendMessage(getChecksumsRequest);

        addStep("Retrieve the ProgressResponse for the GetChecksums request", 
        "The GetChecksums progress response should be sent by the pillar.");
        GetChecksumsProgressResponse progressResponse = clientTopic.waitForMessage(GetChecksumsProgressResponse.class);
        Assert.assertEquals(progressResponse.getChecksumRequestForExistingFile(), csSpec);
        Assert.assertEquals(progressResponse.getFileIDs(), fileids);
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetChecksums request", 
        "The GetChecksums response should be sent by the pillar.");
        GetChecksumsFinalResponse finalResponse = clientTopic.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getChecksumRequestForExistingFile(), csSpec);
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
        Assert.assertNotNull(finalResponse.getResultingChecksums().getChecksumDataItems());
        Assert.assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 1);
        Assert.assertEquals(Base16Utils.decodeBase16(
                finalResponse.getResultingChecksums().getChecksumDataItems().get(0).getChecksumValue()), 
                DEFAULT_MD5_CHECKSUM);

        Assert.assertEquals(alarmDispatcher.getCallsForSendAlarm(), 0, "Should not have send any alarms.");
        Assert.assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver audits");
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarGetChecksumsTestWithAllFilesInIdentification() throws Exception {
        addDescription("Tests the GetChecksums functionality of the reference pillar for the successful scenario, "
                + "when calculating all files.");
        initializeCacheWithMD5ChecksummedFile();
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForGetChecksumsResponse identifyResponse = clientTopic.waitForMessage(IdentifyPillarsForGetChecksumsResponse.class);
        Assert.assertEquals(identifyResponse.getResponseInfo().getResponseCode(), ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertEquals(identifyResponse.getFileIDs(), fileids);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarGetChecksumsTestWithAllFilesInOperation() throws Exception {
        addDescription("Tests the GetChecksums functionality of the reference pillar for the successful scenario, "
                + "when calculating all files.");
        initializeCacheWithMD5ChecksummedFile();
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                csSpec, fileids, null);
        messageBus.sendMessage(getChecksumsRequest);

        GetChecksumsProgressResponse progressResponse = clientTopic.waitForMessage(GetChecksumsProgressResponse.class);
        Assert.assertTrue(progressResponse.getFileIDs().isSetAllFileIDs());

        GetChecksumsFinalResponse finalResponse = clientTopic.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 
                1);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarGetChecksumsTestWithDeliveryAtURL() throws Exception {
        addDescription("Tests the GetChecksums functionality of the reference pillar when delivery at an URL.");
        String DELIVERY_ADDRESS = "http://sandkasse-01.kb.dk/dav/CS_TEST_" + new Date().getTime() + getPillarID();
        initializeCacheWithMD5ChecksummedFile();
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                csSpec, fileids, DELIVERY_ADDRESS);
        messageBus.sendMessage(getChecksumsRequest);

        GetChecksumsProgressResponse progressResponse = clientTopic.waitForMessage(GetChecksumsProgressResponse.class);
        Assert.assertTrue(progressResponse.getFileIDs().isSetAllFileIDs());

        GetChecksumsFinalResponse finalResponse = clientTopic.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 0);
        Assert.assertEquals(finalResponse.getResultingChecksums().getResultAddress(), DELIVERY_ADDRESS);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarGetChecksumsTestWithDeliveryAtBadURL() throws Exception {
        addDescription("Tests the reference pillar handling of a bad URL in the GetChecksumRequest.");
        String DELIVERY_ADDRESS = "https:localhost:1/?";
        initializeCacheWithMD5ChecksummedFile();
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                csSpec, fileids, DELIVERY_ADDRESS);
        messageBus.sendMessage(getChecksumsRequest);

        GetChecksumsProgressResponse progressResponse = clientTopic.waitForMessage(GetChecksumsProgressResponse.class);
        Assert.assertTrue(progressResponse.getFileIDs().isSetAllFileIDs());

        GetChecksumsFinalResponse finalResponse = clientTopic.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_TRANSFER_FAILURE);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarGetChecksumsTestFailedNoSuchFile() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject a GetChecksums requests for a file, which it does not have.");
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs("A-NON-EXISTING-FILE");

        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", 
              "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientTopic.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_NOT_FOUND_FAILURE);        
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarGetChecksumsTestFailedNoSuchFileInOperation() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject a GetChecksums requests for a file, " +
        "which it does not have. But this time at the GetChecksums message.");
        FileIDs fileids = FileIDsUtils.getSpecificFileIDs("A-NON-EXISTING-FILE");

        addStep("Create and send the actual GetChecksums message to the pillar.", 
        "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                csSpec, fileids, null);
        messageBus.sendMessage(getChecksumsRequest);

        addStep("Retrieve the FinalResponse for the GetChecksums request", 
        "The GetChecksums response should be sent by the pillar.");
        GetChecksumsFinalResponse finalResponse = clientTopic.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarGetChecksumsTestIdentifyWithNoChecksumSpec() throws Exception {
        addDescription("Tests that the ReferencePillar is accepts a GetChecksums requests when there is no checksum "
                +"type specified.");
        FileIDs fileids = FileIDsUtils.getAllFileIDs();

        addStep("Create and send the identify message", "Should be received and handled by the pillar.");
        messageBus.sendMessage(msgFactory.createIdentifyPillarsForGetChecksumsRequest(null, fileids));

        addStep("Retrieve the IdentifyResponse for the GetChecksums request and validate it.",
                "The pillar is positively identified.");
        IdentifyPillarsForGetChecksumsResponse response = clientTopic.waitForMessage(IdentifyPillarsForGetChecksumsResponse.class);
        Assert.assertEquals(response.getResponseInfo().getResponseCode(), ResponseCode.IDENTIFICATION_POSITIVE);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void checksumPillarGetChecksumsTestIdentifyWithBadChecksumSpec() throws Exception {
        addDescription("Tests that the ReferencePillar is rejcts a GetChecksums requests when a bad checksum "
                +"type specified. But it should just be returned as a negative identification, not a 'REQUEST_NOT_UNDERSTOOD_FAILURE'.");
        FileIDs fileids = FileIDsUtils.getAllFileIDs();
        ChecksumSpecTYPE badCsType = new ChecksumSpecTYPE();
        badCsType.setChecksumSalt(new byte[]{1,0,1,0});
        badCsType.setChecksumType(ChecksumType.OTHER);
        badCsType.setOtherChecksumType("AlgorithmDoesNotExist");
        
        addStep("Create and send the identify message", "Should be received and handled by the pillar.");
        messageBus.sendMessage(msgFactory.createIdentifyPillarsForGetChecksumsRequest(badCsType, fileids));

        addStep("Retrieve the IdentifyResponse for the GetChecksums request and validate it.",
                "The pillar gives a negative identification.");
        IdentifyPillarsForGetChecksumsResponse response = clientTopic.waitForMessage(IdentifyPillarsForGetChecksumsResponse.class);
        Assert.assertEquals(response.getResponseInfo().getResponseCode(), ResponseCode.IDENTIFICATION_NEGATIVE);
    }
}
