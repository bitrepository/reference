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
package org.bitrepository.pillar.integration.func.getfileids;

import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetFileIDsResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarIdentificationTest;
import org.bitrepository.pillar.messagefactories.GetFileIDsMessageFactory;
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

public class IdentifyPillarsForGetFileIDsIT extends DefaultPillarIdentificationTest {
    protected GetFileIDsMessageFactory msgFactory;

    @BeforeMethod(alwaysRun=true)
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new GetFileIDsMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);
        clearReceivers();
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void normalIdentificationTest() {
        addDescription("Verifies the normal behaviour for getFileIDs identification");
        addStep("Sending a identify request.",
            "The pillar under test should make a response with the correct elements.");
        FileIDs fileids = FileIDsUtils.createFileIDs(DEFAULT_FILE_ID);

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileIDsRequest identifyRequest =
                msgFactory.createIdentifyPillarsForGetFileIDsRequest(fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        Assert.assertNotNull(receivedIdentifyResponse);
        Assert.assertEquals(receivedIdentifyResponse.getCorrelationID(), identifyRequest.getCorrelationID());
        Assert.assertEquals(receivedIdentifyResponse.getFrom(), getPillarID());
        Assert.assertEquals(receivedIdentifyResponse.getPillarID(), getPillarID());
        Assert.assertNotNull(receivedIdentifyResponse.getReplyTo());
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.IDENTIFICATION_POSITIVE);
    }

    @Test( groups = {PillarTestGroups.FULL_PILLAR_TEST, PillarTestGroups.CHECKSUM_PILLAR_TEST})
    public void nonExistingFileTest() {
        addDescription("Tests that the ChecksumPillar is able to reject a GetFileIDs requests for a file, which it " +
                       "does not have during the identification phase.");
        FileIDs fileids = FileIDsUtils.createFileIDs(NON_DEFAULT_FILE_ID);

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetFileIDsRequest identifyRequest =
                msgFactory.createIdentifyPillarsForGetFileIDsRequest(fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetFileIDsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetFileIDsResponse.class);
        Assert.assertEquals(receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                ResponseCode.FILE_NOT_FOUND_FAILURE);
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createIdentifyPillarsForGetFileIDsRequest(null);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(IdentifyPillarsForGetFileIDsResponse.class);
    }

    @Override
    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(IdentifyPillarsForGetFileIDsResponse.class);
    }
}
