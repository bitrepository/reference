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

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.ChecksumType;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.AlarmMessage;
import org.bitrepository.bitrepositorymessages.GetChecksumsFinalResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsProgressResponse;
import org.bitrepository.bitrepositorymessages.GetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.common.utils.Base16Utils;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.common.utils.FileUtils;
import org.bitrepository.pillar.messagefactories.GetChecksumsMessageFactory;
import org.testng.Assert;
import org.testng.annotations.Test;

/**
 * Tests the PutFile functionality on the ReferencePillar.
 */
public class GetChecksumsOnReferencePillarTest extends ReferencePillarTest {
    private GetChecksumsMessageFactory msgFactory;
    private ChecksumSpecTYPE csSpec;

    @Override
    public void initializeCUT() {
        super.initializeCUT();
        msgFactory = new GetChecksumsMessageFactory(settingsForTestClient, getPillarID(), pillarDestinationId);

        csSpec = new ChecksumSpecTYPE();
        csSpec.setChecksumSalt(null);
        csSpec.setChecksumType(ChecksumType.MD5);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestSuccessCase() throws Exception {
        addDescription("Tests the GetChecksums functionality of the reference pillar for the successful scenario.");

        addStep("Set up constants and variables.", "Should not fail here!");
        String FILE_ID = DEFAULT_FILE_ID;
        FileIDs fileids = new FileIDs();
        fileids.setFileID(FILE_ID);

        addStep("Create and send the identify request message.", 
        "Should be received and handled by the pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(
                csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", 
        "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
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
        GetChecksumsProgressResponse progressResponse = clientReceiver.waitForMessage(GetChecksumsProgressResponse.class);
        Assert.assertEquals(progressResponse.getChecksumRequestForExistingFile(), csSpec);
        Assert.assertEquals(progressResponse.getFileIDs(), fileids);
        Assert.assertEquals(progressResponse.getPillarID(), getPillarID());
        Assert.assertNull(progressResponse.getResultAddress());

        addStep("Retrieve the FinalResponse for the GetChecksums request", 
        "The GetChecksums response should be sent by the pillar.");
        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getChecksumRequestForExistingFile(), csSpec);
        Assert.assertEquals(finalResponse.getPillarID(), getPillarID());
        Assert.assertNotNull(finalResponse.getResultingChecksums().getChecksumDataItems());
        Assert.assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 1);
        Assert.assertEquals(Base16Utils.decodeBase16(
                finalResponse.getResultingChecksums().getChecksumDataItems().get(0).getChecksumValue()), 
                EMPTY_FILE_CHECKSUM);

        alarmReceiver.checkNoMessageIsReceived(AlarmMessage.class);
        Assert.assertEquals(audits.getCallsForAuditEvent(), 0, "Should not deliver any audit for calculating the checksum");
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestWithAllFilesInIdentification() throws Exception {
        addDescription("Tests the GetChecksums functionality of the reference pillar for the successful scenario, "
                + "when calculating all files.");
        FileIDs fileids = new FileIDs();
        fileids.setAllFileIDs("true");

        IdentifyPillarsForGetChecksumsRequest identifyRequest = msgFactory.createIdentifyPillarsForGetChecksumsRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForGetChecksumsResponse identifyResponse = clientReceiver.waitForMessage(IdentifyPillarsForGetChecksumsResponse.class);
        Assert.assertEquals(identifyResponse.getResponseInfo().getResponseCode(), ResponseCode.IDENTIFICATION_POSITIVE);
        Assert.assertEquals(identifyResponse.getFileIDs(), fileids);
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestWithAllFilesInOperation() throws Exception {
        addDescription("Tests the GetChecksums functionality of the reference pillar for the successful scenario, "
                + "when calculating all files.");
        FileIDs fileids = new FileIDs();
        fileids.setAllFileIDs("true");

        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                csSpec, fileids, null);
        messageBus.sendMessage(getChecksumsRequest);

        GetChecksumsProgressResponse progressResponse = clientReceiver.waitForMessage(GetChecksumsProgressResponse.class);
        Assert.assertTrue(progressResponse.getFileIDs().isSetAllFileIDs());

        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 
                archives.getAllFileIds(settingsForCUT.getCollectionID()).size());
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestWithDeliveryAtURL() throws Exception {
        addDescription("Tests the GetChecksums functionality of the reference pillar when delivery at an URL.");
        String DELIVERY_ADDRESS =  httpServer.getURL("CS_TEST").toExternalForm();
        FileIDs fileids = new FileIDs();
        fileids.setAllFileIDs(DEFAULT_FILE_ID);

        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                csSpec, fileids, DELIVERY_ADDRESS);
        messageBus.sendMessage(getChecksumsRequest);

        GetChecksumsProgressResponse progressResponse = clientReceiver.waitForMessage(GetChecksumsProgressResponse.class);
        Assert.assertTrue(progressResponse.getFileIDs().isSetAllFileIDs());

        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 0);
        Assert.assertEquals(finalResponse.getResultingChecksums().getResultAddress(), DELIVERY_ADDRESS);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestWithDeliveryAtBadURL() throws Exception {
        addDescription("Tests the reference pillar handling of a bad URL in the GetChecksumRequest.");
        String DELIVERY_ADDRESS = "https:localhost:1/?";

        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                csSpec, FileIDsUtils.getAllFileIDs(), DELIVERY_ADDRESS);
        messageBus.sendMessage(getChecksumsRequest);

        clientReceiver.waitForMessage(GetChecksumsProgressResponse.class);

        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_TRANSFER_FAILURE);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestFailedNoSuchFile() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject a GetChecksums requests for a file, which it does not have.");
        FileIDs fileids = new FileIDs();
        fileids.setFileID("A-NON-EXISTING-FILE");

        IdentifyPillarsForGetChecksumsRequest identifyRequest =
                msgFactory.createIdentifyPillarsForGetChecksumsRequest(csSpec, FileIDsUtils.getSpecificFileIDs(NON_DEFAULT_FILE_ID));
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.", 
              "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(), 
                ResponseCode.FILE_NOT_FOUND_FAILURE);        
    }

    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsTestFailedNoSuchFileInOperation() throws Exception {
        addDescription("Tests that the ReferencePillar is able to reject a GetChecksums requests for a file, " +
        "which it does not have. But this time at the GetChecksums message.");
        FileIDs fileids = new FileIDs();
        fileids.setFileID("A-NON-EXISTING-FILE");

        addStep("Create and send the actual GetChecksums message to the pillar.", 
        "Should be received and handled by the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                csSpec, fileids, null);
        messageBus.sendMessage(getChecksumsRequest);

        addStep("Retrieve the FinalResponse for the GetChecksums request", 
        "The GetChecksums response should be sent by the pillar.");
        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.FILE_NOT_FOUND_FAILURE);
    }
    
    @Test( groups = {"regressiontest", "pillartest"})
    public void pillarGetChecksumsOfRemovedFile() throws Exception {
        addDescription("Tests how the reference pillar acts, when the file it is supposed to retrieve the checksum "
                + "of is removed between two GetChecksum operations.");
        addStep("Request the checksum all files", "Message is sent to the pillar.");
        GetChecksumsRequest getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                csSpec, FileIDsUtils.getSpecificFileIDs(DEFAULT_FILE_ID), null);
        messageBus.sendMessage(getChecksumsRequest);
        
        addStep("Retrieve the FinalResponse for the GetChecksums request", 
                "Contains the checksum of the default file.");
        GetChecksumsFinalResponse finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 1);
        
        addStep("Remove the file from beneath the pillar", 
                "The file is not longer in the archive, but still in the database.");
        FileUtils.delete(archives.getFile(DEFAULT_FILE_ID, settingsForCUT.getCollectionID()));
        Assert.assertFalse(archives.hasFile(DEFAULT_FILE_ID, settingsForCUT.getCollectionID()));
        Assert.assertTrue(csCache.hasFile(DEFAULT_FILE_ID, settingsForCUT.getCollectionID()));
        
        addStep("Request the checksum of the default file again", "Message is sent to the pillar.");
        getChecksumsRequest = msgFactory.createGetChecksumsRequest(
                csSpec, FileIDsUtils.getAllFileIDs(), null);
        messageBus.sendMessage(getChecksumsRequest);
        
        addStep("Retrieve the FinalResponse for the GetChecksums request", 
                "No checksums returned, the file has been removed from the cache, and an alarm was dispatched.");
        finalResponse = clientReceiver.waitForMessage(GetChecksumsFinalResponse.class);
        Assert.assertEquals(finalResponse.getResponseInfo().getResponseCode(), ResponseCode.OPERATION_COMPLETED);
        Assert.assertEquals(finalResponse.getResultingChecksums().getChecksumDataItems().size(), 0);
        Assert.assertFalse(csCache.hasFile(DEFAULT_FILE_ID, settingsForCUT.getCollectionID()));
        AlarmMessage alarm = alarmReceiver.waitForMessage(AlarmMessage.class);
        Assert.assertNotNull(alarm);
    }
}
