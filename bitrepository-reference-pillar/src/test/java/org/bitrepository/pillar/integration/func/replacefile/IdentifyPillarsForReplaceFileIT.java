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
package org.bitrepository.pillar.integration.func.replacefile;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForReplaceFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarIdentificationTest;
import org.bitrepository.pillar.messagefactories.ReplaceFileMessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.bitrepository.common.utils.AllureTestUtils.addDescription;
import static org.bitrepository.common.utils.AllureTestUtils.addStep;

public class IdentifyPillarsForReplaceFileIT extends DefaultPillarIdentificationTest {
    protected ReplaceFileMessageFactory msgFactory;

    @BeforeEach
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new ReplaceFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);

        msgFactory = new ReplaceFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);

    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    public void normalIdentificationTest() {
        addDescription("Verifies the normal behaviour for replaceFile identification");
        addStep("Sending a replaceFile identification.",
                "The pillar under test should make a response with the correct elements.");
        IdentifyPillarsForReplaceFileRequest identifyRequest = msgFactory.createIdentifyPillarsForReplaceFileRequest(
                defaultFileId, DEFAULT_FILE_SIZE);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForReplaceFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileResponse.class);
        Assertions.assertEquals(identifyRequest.getCollectionID(), receivedIdentifyResponse.getCollectionID());
        Assertions.assertEquals(identifyRequest.getCorrelationID(), receivedIdentifyResponse.getCorrelationID());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getFrom());
        Assertions.assertEquals(defaultFileId, receivedIdentifyResponse.getFileID());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getPillarID());
        Assertions.assertNull(receivedIdentifyResponse.getPillarChecksumSpec());
        Assertions.assertEquals(ResponseCode.IDENTIFICATION_POSITIVE, receivedIdentifyResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(identifyRequest.getReplyTo(), receivedIdentifyResponse.getDestination());
    }

    @Test
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    public void identificationTestForChecksumPillar() {
        addDescription("Verifies the normal behaviour for replaceFile identification for a checksum pillar");
        addStep("Sending a replaceFile identification.",
                "The pillar under test should make a response with the correct elements. The only different from a " +
                        "full pillar is that the checksum pillar will respond with the default checksum spec.");
        IdentifyPillarsForReplaceFileRequest identifyRequest = msgFactory.createIdentifyPillarsForReplaceFileRequest(
                defaultFileId, DEFAULT_FILE_SIZE);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForReplaceFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileResponse.class);
        Assertions.assertEquals(identifyRequest.getCollectionID(), receivedIdentifyResponse.getCollectionID());
        Assertions.assertEquals(identifyRequest.getCorrelationID(), receivedIdentifyResponse.getCorrelationID());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getFrom());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getPillarID());
        Assertions.assertEquals(ResponseCode.IDENTIFICATION_POSITIVE, receivedIdentifyResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(identifyRequest.getReplyTo(), receivedIdentifyResponse.getDestination());
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    public void fileDoesNotExistsTest() {
        addDescription("Verifies that a request for a non-existing file is handled correctly");
        addStep("Sending a replaceFile identification for a file not in the pillar.",
                "The pillar under test should send a FILE_NOT_FOUND_FAILURE response.");
        IdentifyPillarsForReplaceFileRequest identifyRequest = msgFactory.createIdentifyPillarsForReplaceFileRequest(
                nonDefaultFileId, 0L);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForReplaceFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForReplaceFileResponse.class);
        Assertions.assertEquals(ResponseCode.FILE_NOT_FOUND_FAILURE, receivedIdentifyResponse.getResponseInfo().getResponseCode());
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createIdentifyPillarsForReplaceFileRequest(
                defaultFileId, DEFAULT_FILE_SIZE);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(IdentifyPillarsForReplaceFileResponse.class);
    }

    @Override
    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(IdentifyPillarsForReplaceFileResponse.class);
    }
}
