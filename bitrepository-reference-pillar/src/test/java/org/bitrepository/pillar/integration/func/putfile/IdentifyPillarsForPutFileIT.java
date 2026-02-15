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
package org.bitrepository.pillar.integration.func.putfile;

import org.bitrepository.bitrepositoryelements.ResponseCode;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileRequest;
import org.bitrepository.bitrepositorymessages.IdentifyPillarsForPutFileResponse;
import org.bitrepository.bitrepositorymessages.MessageRequest;
import org.bitrepository.bitrepositorymessages.MessageResponse;
import org.bitrepository.pillar.PillarTestGroups;
import org.bitrepository.pillar.integration.func.DefaultPillarIdentificationTest;
import org.bitrepository.pillar.messagefactories.PutFileMessageFactory;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.bitrepository.protocol.utils.AllureTestUtils.addDescription;
import static org.bitrepository.protocol.utils.AllureTestUtils.addStep;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class IdentifyPillarsForPutFileIT extends DefaultPillarIdentificationTest {
    protected PutFileMessageFactory msgFactory;

    @BeforeEach
    public void initialiseReferenceTest() throws Exception {
        msgFactory = new PutFileMessageFactory(collectionID, settingsForTestClient, getPillarID(), null);
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    public void normalIdentificationTest() {
        addDescription("Verifies the normal behaviour for putFile identification");
        addStep("Sending a putFile identification request.",
                "The pillar under test should make a response with the following elements: <ol>" +
                        "<li>'CollectionID' element corresponding to the supplied value</li>" +
                        "<li>'CorrelationID' element corresponding to the supplied value</li>" +
                        "<li>'From' element corresponding to the pillars component ID</li>" +
                        "<li>'To' element should be set to the value of the 'From' elements in the request</li>" +
                        "<li>'Destination' element should be set to the value of 'ReplyTo' from the request</li>" +
                        "<li>'ChecksumDataForExistingFile' element should be null</li>" +
                        "<li>'PillarChecksumSpec' element should be null</li>" +
                        "<li>'PillarID' element corresponding to the pillars component ID</li>" +
                        "<li>'ResponseInfo.ResponseCode' element should be IDENTIFICATION_POSITIVE</li>" +
                        "</ol>");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                nonDefaultFileId, 0L);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assertions.assertEquals(identifyRequest.getCollectionID(), receivedIdentifyResponse.getCollectionID(),
                "Received unexpected CollectionID");
        Assertions.assertEquals(identifyRequest.getCorrelationID(), receivedIdentifyResponse.getCorrelationID(),
                "Received unexpected CorrelationID");
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getFrom(),
                "Received unexpected PillarID");
        Assertions.assertEquals(identifyRequest.getFrom(), receivedIdentifyResponse.getTo(),
                "Received unexpected 'To' element.");
        Assertions.assertNull(receivedIdentifyResponse.getChecksumDataForExistingFile(),
                "Received unexpected ChecksumDataForExistingFile");
        Assertions.assertNull(receivedIdentifyResponse.getPillarChecksumSpec(),
                "Received unexpected PillarChecksumSpec");
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getPillarID(),
                "Unexpected 'From' element in the " +
                        "received response:\n" + receivedIdentifyResponse + "\n");
        Assertions.assertEquals(ResponseCode.IDENTIFICATION_POSITIVE,
                receivedIdentifyResponse.getResponseInfo().getResponseCode(),
                "Received unexpected ResponseCode");
        Assertions.assertEquals(identifyRequest.getReplyTo(), receivedIdentifyResponse.getDestination(),
                "Received unexpected ReplyTo");
    }

    @Test
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    public void identificationTestForChecksumPillar() {
        addDescription("Verifies the normal behaviour for putFile identification for a checksum pillar");
        addStep("Sending a putFile identification.",
                "The pillar under test should make a response with the correct elements. The only different from a " +
                        "full pillar is that the checksum pillar will respond with the default checksum spec.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                nonDefaultFileId, 0L);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assertions.assertEquals(identifyRequest.getCollectionID(), receivedIdentifyResponse.getCollectionID());
        Assertions.assertEquals(identifyRequest.getCorrelationID(), receivedIdentifyResponse.getCorrelationID());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getFrom());
        Assertions.assertNull(receivedIdentifyResponse.getChecksumDataForExistingFile());
        Assertions.assertEquals(getPillarID(), receivedIdentifyResponse.getPillarID());
        Assertions.assertEquals(ResponseCode.IDENTIFICATION_POSITIVE,
                receivedIdentifyResponse.getResponseInfo().getResponseCode());
        Assertions.assertEquals(identifyRequest.getReplyTo(), receivedIdentifyResponse.getDestination());
    }

    @Test
    @Tag(PillarTestGroups.FULL_PILLAR_TEST)
    @Tag(PillarTestGroups.CHECKSUM_PILLAR_TEST)
    public void fileExistsTest() {
        addDescription("Verifies the exists of a file with the same ID is handled correctly. " +
                "This means that a checksum for the existing file is returned, enabling the client to continue with " +
                "the put operation for the pillars not yet containing the file. The client can easily " +
                "implement idempotent behaviour based on this response.");
        addStep("Sending a putFile identification for a file already in the pillar.",
                "The pillar under test should send a DUPLICATE_FILE_FAILURE response with the (default type) checksum " +
                        "of the existing file.");
        IdentifyPillarsForPutFileRequest identifyRequest = msgFactory.createIdentifyPillarsForPutFileRequest(
                defaultFileId, 0L);
        messageBus.sendMessage(identifyRequest);

        IdentifyPillarsForPutFileResponse receivedIdentifyResponse = clientReceiver.waitForMessage(
                IdentifyPillarsForPutFileResponse.class);
        Assertions.assertEquals(ResponseCode.DUPLICATE_FILE_FAILURE, receivedIdentifyResponse.getResponseInfo().getResponseCode());
    }

    @Override
    protected MessageRequest createRequest() {
        return msgFactory.createIdentifyPillarsForPutFileRequest(
                nonDefaultFileId, 0L);
    }

    @Override
    protected MessageResponse receiveResponse() {
        return clientReceiver.waitForMessage(IdentifyPillarsForPutFileResponse.class);
    }

    @Override
    protected void assertNoResponseIsReceived() {
        clientReceiver.checkNoMessageIsReceived(IdentifyPillarsForPutFileResponse.class);
    }
}
