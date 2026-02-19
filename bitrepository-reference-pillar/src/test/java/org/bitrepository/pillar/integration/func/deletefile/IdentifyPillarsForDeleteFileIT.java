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
package org.bitrepository.pillar.integration.func.deletefile;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForDeleteFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarIdentificationTest;
import org.bitrepository.pillar.messagefactories.DeleteFileMessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;


public class IdentifyPillarsForDeleteFileIT extends DefaultPillarIdentificationTest {
    protected DeleteFileMessageFactory msgFactory;

    @BeforeEach
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new DeleteFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);

    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    public void normalIdentificationTest() {
        addDescription("Verifies the normal behaviour for deleteFile identification");
        addStep("Sending a deleteFile identification.",
                "The pillar under test should make a response with the correct elements.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = (IdentifyPillarsForDeleteFileRequest) createRequest();
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
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
        addDescription("Verifies the normal behaviour for deleteFile identification for a checksum pillar");
        addStep("Sending a deleteFile identification.",
                "The pillar under test should make a response with the correct elements. The only different from a " +
                        "full pillar is that the checksum pillar will respond with the default checksum spec.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = (IdentifyPillarsForDeleteFileRequest) createRequest();
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
        Assertions.assertEquals(identifyRequest.getCollectionID(), receivedIdentifyResponse.getCollectionID());
        Assertions.assertEquals(identifyRequest.getCorrelationID(), receivedIdentifyResponse.getCorrelationID());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getFrom());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getPillarID());
        Assertions.assertEquals(ResponseCode.IDENTIFICATION_POSITIVE,
                receivedIdentifyResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(identifyRequest.getReplyTo(), receivedIdentifyResponse.getDestination());
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    public void fileDoesNotExistsTest() {
        addDescription("Verifies that a request for a non-existing file is handled correctly");
        addStep("Sending a deleteFile identification for a file not in the pillar.",
                "The pillar under test should send a FILE_NOT_FOUND_FAILURE response.");
        IdentifyPillarsForDeleteFileRequest identifyRequest = msgFactory.createIdentifyPillarsForDeleteFileRequest(
                nonDefaultFileId);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForDeleteFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForDeleteFileResponse.class);
        Assertions.assertEquals(ResponseCode.FILE_NOT_FOUND_FAILURE,
                receivedIdentifyResponse.getResponseInfo().getResponseCode());
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createIdentifyPillarsForDeleteFileRequest(defaultFileId);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(IdentifyPillarsForDeleteFileResponse.class);
    }

    @Override
    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(IdentifyPillarsForDeleteFileResponse.class);
    }
}
