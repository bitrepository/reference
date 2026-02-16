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
package org.bitrepository.pillar.integration.func.getchecksums;

import org.bitrepository.bitrepositoryelements.ChecksumSpecTYPE;
import org.bitrepository.bitrepositoryelements.FileIDs;
import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForGetChecksumsResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.common.utils.ChecksumUtils;
import org.bitrepository.common.utils.FileIDsUtils;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarIdentificationTest;
import org.bitrepository.pillar.messagefactories.GetChecksumsMessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


public class IdentifyPillarsForGetChecksumsIT extends DefaultPillarIdentificationTest {
    protected GetChecksumsMessageFactory msgFactory;

    @BeforeEach
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new GetChecksumsMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
        clearReceivers();
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    public void normalIdentificationTest() {
        addDescription("Verifies the normal behaviour for getChecksums identification");
        addStep("Setup for test", "2 files on the pillar");
        pillarFileManager.ensureNumberOfFilesOnPillar(2, testMethodName);

        addStep("Sending a identify request.",
                "The pillar under test should make a response with the correct elements.");
        FileIDs fileids = FileIDsUtils.createFileIDs(defaultFileId);
        ChecksumSpecTYPE csSpec = ChecksumUtils.getDefault(settingsForCUT);

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest =
                msgFactory.createIdentifyPillarsForGetChecksumsRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
        Assertions.assertNotNull(receivedIdentifyResponse);
        Assertions.assertEquals(identifyRequest.getCollectionID(), receivedIdentifyResponse.getCollectionID(),
                "Received unexpected 'CollectionID' in response.");
        Assertions.assertEquals(identifyRequest.getCorrelationID(), receivedIdentifyResponse.getCorrelationID(),
                "Received unexpected 'CorrelationID' in response.");
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getFrom(),
                "Received unexpected 'From' in response.");
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getPillarID(),
                "Received unexpected 'PillarID' in response.");
        Assertions.assertNotNull(receivedIdentifyResponse.getReplyTo());
        Assertions.assertEquals(ResponseCode.IDENTIFICATION_POSITIVE, receivedIdentifyResponse.getResponseInfo().getResponseCode(), "Received" +
                " unexpected 'Response' in response.");
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    public void nonExistingFileTest() {
        addDescription("Tests that the pillar is able to reject a GetChecksums requests for a file, which it " +
                "does not have during the identification phase.");
        addStep("Setup for test", "2 files on the pillar");

        FileIDs fileids = FileIDsUtils.createFileIDs(nonDefaultFileId);
        ChecksumSpecTYPE csSpec = ChecksumUtils.getDefault(settingsForCUT);

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest =
                msgFactory.createIdentifyPillarsForGetChecksumsRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
        Assertions.assertNotNull(receivedIdentifyResponse.getFileIDs().getFileID());
        Assertions.assertEquals(ResponseCode.FILE_NOT_FOUND_FAILURE,
                receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                "Received unexpected 'ResponseCode' in response.");
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    public void allFilesTest() {
        addDescription("Tests that the pillar accepts a GetChecksums requests for all files, even though it does not " +
                "have any files.");
        FileIDs fileids = FileIDsUtils.getAllFileIDs();
        ChecksumSpecTYPE csSpec = ChecksumUtils.getDefault(settingsForCUT);

        addStep("Create and send the identify request message.",
                "Should be received and handled by the pillar.");
        IdentifyPillarsForGetChecksumsRequest identifyRequest =
                msgFactory.createIdentifyPillarsForGetChecksumsRequest(csSpec, fileids);
        messageBus.sendMessage(identifyRequest);

        addStep("Retrieve and validate the response getPillarID() the pillar.",
                "The pillar should make a response.");
        IdentifyPillarsForGetChecksumsResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForGetChecksumsResponse.class);
        Assertions.assertEquals(ResponseCode.IDENTIFICATION_POSITIVE,
                receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                "Received unexpected 'ResponseCode' in response.");
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createIdentifyPillarsForGetChecksumsRequest(ChecksumUtils.getDefault(settingsForCUT),
                FileIDsUtils.getAllFileIDs());
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(IdentifyPillarsForGetChecksumsResponse.class);
    }

    @Override
    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(IdentifyPillarsForGetChecksumsResponse.class);
    }
}
